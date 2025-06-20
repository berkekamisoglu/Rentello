-- ================================================
-- SECURITY AND ENCRYPTION SETUP SCRIPT
-- Row-Level Security, Roles, Encryption
-- ================================================

USE CarRentalDB;
GO

-- ================================================
-- 1. CREATE ROLES AND USERS
-- ================================================

-- Create Database Roles
IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'db_admin_role')
    CREATE ROLE db_admin_role;

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'db_manager_role')
    CREATE ROLE db_manager_role;

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'db_employee_role')
    CREATE ROLE db_employee_role;

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'db_customer_role')
    CREATE ROLE db_customer_role;

-- Create Login and Users (examples)
IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'CarRentalAdmin')
    CREATE LOGIN CarRentalAdmin WITH PASSWORD = 'AdminPass123!';

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalAdmin')
    CREATE USER CarRentalAdmin FOR LOGIN CarRentalAdmin;

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'CarRentalManager')
    CREATE LOGIN CarRentalManager WITH PASSWORD = 'ManagerPass123!';

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalManager')
    CREATE USER CarRentalManager FOR LOGIN CarRentalManager;

IF NOT EXISTS (SELECT * FROM sys.server_principals WHERE name = 'CarRentalEmployee')
    CREATE LOGIN CarRentalEmployee WITH PASSWORD = 'EmployeePass123!';

IF NOT EXISTS (SELECT * FROM sys.database_principals WHERE name = 'CarRentalEmployee')
    CREATE USER CarRentalEmployee FOR LOGIN CarRentalEmployee;

-- Assign users to roles
ALTER ROLE db_admin_role ADD MEMBER CarRentalAdmin;
ALTER ROLE db_manager_role ADD MEMBER CarRentalManager;
ALTER ROLE db_employee_role ADD MEMBER CarRentalEmployee;

-- ================================================
-- 2. GRANT PERMISSIONS TO ROLES
-- ================================================

-- Admin Role - Full access
GRANT SELECT, INSERT, UPDATE, DELETE ON SCHEMA::dbo TO db_admin_role;
GRANT EXECUTE ON SCHEMA::dbo TO db_admin_role;
GRANT CREATE TABLE, CREATE VIEW, CREATE PROCEDURE, CREATE FUNCTION TO db_admin_role;

-- Manager Role - Business operations access
GRANT SELECT, INSERT, UPDATE ON Users TO db_manager_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON Rentals TO db_manager_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON Vehicles TO db_manager_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON VehicleMaintenance TO db_manager_role;
GRANT SELECT, INSERT, UPDATE, DELETE ON Payments TO db_manager_role;
GRANT SELECT ON ALL VIEWS TO db_manager_role;
GRANT EXECUTE ON ALL STORED_PROCEDURES TO db_manager_role;

-- Employee Role - Limited operational access
GRANT SELECT, INSERT, UPDATE ON Rentals TO db_employee_role;
GRANT SELECT, UPDATE ON Vehicles TO db_employee_role;
GRANT SELECT, INSERT ON Payments TO db_employee_role;
GRANT SELECT ON vw_AvailableVehicles TO db_employee_role;
GRANT SELECT ON vw_ActiveRentals TO db_employee_role;
GRANT SELECT ON vw_CustomerHistory TO db_employee_role;
GRANT EXECUTE ON sp_CreateRental TO db_employee_role;
GRANT EXECUTE ON sp_ProcessVehicleReturn TO db_employee_role;
GRANT EXECUTE ON sp_SearchAvailableVehicles TO db_employee_role;
GRANT EXECUTE ON sp_ProcessPayment TO db_employee_role;

-- Customer Role - Very limited access (for self-service portals)
GRANT SELECT ON vw_AvailableVehicles TO db_customer_role;
GRANT EXECUTE ON sp_SearchAvailableVehicles TO db_customer_role;

-- ================================================
-- 3. ENCRYPTION SETUP
-- ================================================

-- Create Database Master Key
IF NOT EXISTS (SELECT * FROM sys.symmetric_keys WHERE name = '##MS_DatabaseMasterKey##')
    CREATE MASTER KEY ENCRYPTION BY PASSWORD = 'CarRental_MasterKey_2024!';

-- Create Certificate for encryption
IF NOT EXISTS (SELECT * FROM sys.certificates WHERE name = 'CarRentalCert')
    CREATE CERTIFICATE CarRentalCert
    WITH SUBJECT = 'Car Rental System Certificate for Data Encryption';

-- Create Symmetric Key for sensitive data encryption
IF NOT EXISTS (SELECT * FROM sys.symmetric_keys WHERE name = 'CarRentalSymmetricKey')
    CREATE SYMMETRIC KEY CarRentalSymmetricKey
    WITH ALGORITHM = AES_256
    ENCRYPTION BY CERTIFICATE CarRentalCert;

-- ================================================
-- 4. ROW-LEVEL SECURITY SETUP
-- ================================================

-- Enable Row-Level Security on Users table
ALTER TABLE Users ENABLE ROW LEVEL SECURITY;

-- Create security policy for Users table
-- Users can only see their own data unless they are admins/managers
CREATE FUNCTION fn_UserSecurityPredicate(@UserID INT)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN
(
    SELECT 1 AS AccessResult 
    WHERE 
        @UserID = USER_ID() -- User can see their own data
        OR IS_MEMBER('db_admin_role') = 1 -- Admins can see all
        OR IS_MEMBER('db_manager_role') = 1 -- Managers can see all
        OR IS_MEMBER('db_employee_role') = 1 -- Employees can see all for business operations
);
GO

-- Apply the security policy
CREATE SECURITY POLICY UserSecurityPolicy
ADD FILTER PREDICATE fn_UserSecurityPredicate(UserID) ON Users
WITH (STATE = ON);
GO

-- Create security policy for Rentals table
-- Customers can only see their own rentals
CREATE FUNCTION fn_RentalSecurityPredicate(@CustomerID INT)
RETURNS TABLE
WITH SCHEMABINDING
AS
RETURN
(
    SELECT 1 AS AccessResult 
    WHERE 
        @CustomerID = USER_ID() -- Customer can see their own rentals
        OR IS_MEMBER('db_admin_role') = 1 -- Admins can see all
        OR IS_MEMBER('db_manager_role') = 1 -- Managers can see all
        OR IS_MEMBER('db_employee_role') = 1 -- Employees can see all
);
GO

-- Apply the security policy to Rentals
CREATE SECURITY POLICY RentalSecurityPolicy
ADD FILTER PREDICATE fn_RentalSecurityPredicate(CustomerID) ON Rentals
WITH (STATE = ON);
GO

-- ================================================
-- 5. COLUMN-LEVEL ENCRYPTION FUNCTIONS
-- ================================================

-- Function to encrypt National ID
CREATE FUNCTION fn_EncryptNationalID(@NationalID NVARCHAR(50))
RETURNS VARBINARY(256)
AS
BEGIN
    DECLARE @EncryptedValue VARBINARY(256);
    
    OPEN SYMMETRIC KEY CarRentalSymmetricKey
    DECRYPTION BY CERTIFICATE CarRentalCert;
    
    SET @EncryptedValue = EncryptByKey(Key_GUID('CarRentalSymmetricKey'), @NationalID);
    
    CLOSE SYMMETRIC KEY CarRentalSymmetricKey;
    
    RETURN @EncryptedValue;
END;
GO

-- Function to decrypt National ID
CREATE FUNCTION fn_DecryptNationalID(@EncryptedNationalID VARBINARY(256))
RETURNS NVARCHAR(50)
AS
BEGIN
    DECLARE @DecryptedValue NVARCHAR(50);
    
    OPEN SYMMETRIC KEY CarRentalSymmetricKey
    DECRYPTION BY CERTIFICATE CarRentalCert;
    
    SET @DecryptedValue = DecryptByKey(@EncryptedNationalID);
    
    CLOSE SYMMETRIC KEY CarRentalSymmetricKey;
    
    RETURN @DecryptedValue;
END;
GO

-- ================================================
-- 6. AUDIT SETUP
-- ================================================

-- Create audit specification for sensitive operations
-- This would typically be done at server level, but including for completeness

-- Enable database audit
-- CREATE DATABASE AUDIT SPECIFICATION CarRentalAudit
-- FOR SERVER AUDIT CarRentalServerAudit
-- ADD (INSERT, UPDATE, DELETE ON Users BY db_admin_role),
-- ADD (INSERT, UPDATE, DELETE ON Rentals BY db_manager_role),
-- ADD (SELECT ON Users BY db_employee_role)
-- WITH (STATE = ON);

-- ================================================
-- 7. DYNAMIC DATA MASKING
-- ================================================

-- Apply dynamic data masking to sensitive columns
ALTER TABLE Users 
ALTER COLUMN Email ADD MASKED WITH (FUNCTION = 'email()');

ALTER TABLE Users 
ALTER COLUMN PhoneNumber ADD MASKED WITH (FUNCTION = 'partial(1,"XXX-XXX-",4)');

-- ================================================
-- 8. SECURITY VIEWS (Data Anonymization)
-- ================================================

-- Secure view for customer data (masks sensitive information)
CREATE VIEW vw_SecureCustomerInfo AS
SELECT 
    UserID,
    Username,
    CASE 
        WHEN IS_MEMBER('db_admin_role') = 1 OR IS_MEMBER('db_manager_role') = 1 
        THEN Email 
        ELSE LEFT(Email, 2) + '***@' + RIGHT(Email, CHARINDEX('@', REVERSE(Email)) - 1)
    END AS Email,
    FirstName,
    LastName,
    CASE 
        WHEN IS_MEMBER('db_admin_role') = 1 OR IS_MEMBER('db_manager_role') = 1 
        THEN PhoneNumber 
        ELSE LEFT(PhoneNumber, 3) + '-XXX-XXXX'
    END AS PhoneNumber,
    DateOfBirth,
    IsActive,
    LastLoginDate,
    CreatedDate
FROM Users
WHERE RoleID = (SELECT RoleID FROM UserRoles WHERE RoleName = 'Customer');
GO

-- ================================================
-- 9. BACKUP ENCRYPTION
-- ================================================

-- Create backup encryption certificate
IF NOT EXISTS (SELECT * FROM sys.certificates WHERE name = 'CarRentalBackupCert')
    CREATE CERTIFICATE CarRentalBackupCert
    WITH SUBJECT = 'Car Rental Backup Encryption Certificate';
GO

-- ================================================
-- 10. CONNECTION SECURITY PROCEDURES
-- ================================================

-- Procedure to validate user session
CREATE PROCEDURE sp_ValidateUserSession
    @UserID INT,
    @SessionToken NVARCHAR(255),
    @IsValid BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- In a real implementation, this would validate against a sessions table
    -- For demo purposes, we'll do basic validation
    
    DECLARE @LastLoginDate DATETIME2;
    DECLARE @IsActive BIT;
    
    SELECT 
        @LastLoginDate = LastLoginDate,
        @IsActive = IsActive
    FROM Users 
    WHERE UserID = @UserID;
    
    IF @IsActive = 1 AND @LastLoginDate > DATEADD(HOUR, -24, GETDATE())
    BEGIN
        SET @IsValid = 1;
        
        -- Update last activity
        UPDATE Users 
        SET LastLoginDate = GETDATE()
        WHERE UserID = @UserID;
    END
    ELSE
    BEGIN
        SET @IsValid = 0;
    END
    
    -- Log the validation attempt
    INSERT INTO SystemLogs (TableName, OperationType, RecordID, NewValues, UserID)
    VALUES (
        'Users', 
        'SESSION_VALIDATION', 
        @UserID,
        CONCAT('Session validation result: ', CASE WHEN @IsValid = 1 THEN 'SUCCESS' ELSE 'FAILED' END),
        @UserID
    );
END;
GO

PRINT 'Security and encryption setup completed successfully!';
PRINT 'Row-Level Security enabled on Users and Rentals tables';
PRINT 'Column-level encryption functions created for sensitive data';
PRINT 'Database roles and permissions configured';
PRINT 'Dynamic data masking applied to email and phone fields';
