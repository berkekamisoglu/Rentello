-- ================================================
-- STORED PROCEDURES CREATION SCRIPT
-- 10+ Stored Procedures for complex operations and business logic
-- ================================================

USE CarRentalDB;
GO

-- 1. User Authentication Procedure
CREATE PROCEDURE sp_AuthenticateUser
    @Username NVARCHAR(50),
    @Password NVARCHAR(255),
    @UserID INT OUTPUT,
    @RoleName NVARCHAR(50) OUTPUT,
    @IsSuccess BIT OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @StoredPasswordHash NVARCHAR(255);
    
    -- Get user information
    SELECT 
        @UserID = u.UserID,
        @StoredPasswordHash = u.PasswordHash,
        @RoleName = ur.RoleName
    FROM Users u
    INNER JOIN UserRoles ur ON u.RoleID = ur.RoleID
    WHERE u.Username = @Username AND u.IsActive = 1;
    
    -- Verify password (in real implementation, use proper hash verification)
    IF @UserID IS NOT NULL AND @StoredPasswordHash = @Password
    BEGIN
        SET @IsSuccess = 1;
        
        -- Update last login date
        UPDATE Users 
        SET LastLoginDate = GETDATE()
        WHERE UserID = @UserID;
        
        -- Log successful login
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, NewValues, UserID)
        VALUES ('Users', 'LOGIN', @UserID, 'Successful login', @UserID);
    END
    ELSE
    BEGIN
        SET @IsSuccess = 0;
        SET @UserID = NULL;
        SET @RoleName = NULL;
        
        -- Log failed login attempt
        INSERT INTO SystemLogs (TableName, OperationType, NewValues)
        VALUES ('Users', 'LOGIN_FAILED', CONCAT('Failed login attempt for username: ', @Username));
    END
END;
GO

-- 2. Create New Rental Procedure
CREATE PROCEDURE sp_CreateRental
    @CustomerID INT,
    @VehicleID INT,
    @PickupLocationID INT,
    @ReturnLocationID INT,
    @PlannedPickupDate DATETIME2,
    @PlannedReturnDate DATETIME2,
    @CreatedBy INT,
    @RentalID INT OUTPUT,
    @TotalAmount DECIMAL(12,2) OUTPUT,
    @IsSuccess BIT OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Validate vehicle availability
        IF NOT EXISTS (
            SELECT 1 FROM Vehicles v
            INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
            WHERE v.VehicleID = @VehicleID AND vs.IsAvailableForRent = 1
        )
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Vehicle is not available for rent';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Check for conflicting rentals
        IF EXISTS (
            SELECT 1 FROM Rentals r
            INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
            WHERE r.VehicleID = @VehicleID 
            AND rs.StatusName IN ('Reserved', 'Active')
            AND (
                (@PlannedPickupDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
                OR (@PlannedReturnDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
                OR (r.PlannedPickupDate BETWEEN @PlannedPickupDate AND @PlannedReturnDate)
            )
        )
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Vehicle is already booked for the selected dates';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Calculate rental amount
        DECLARE @DailyRate DECIMAL(10,2);
        DECLARE @RentalDays INT;
        DECLARE @BaseAmount DECIMAL(12,2);
        DECLARE @TaxAmount DECIMAL(12,2);
        
        SELECT @DailyRate = DailyRentalRate FROM Vehicles WHERE VehicleID = @VehicleID;
        SET @RentalDays = DATEDIFF(DAY, @PlannedPickupDate, @PlannedReturnDate);
        SET @BaseAmount = @DailyRate * @RentalDays;
        SET @TaxAmount = @BaseAmount * 0.18; -- 18% tax
        SET @TotalAmount = @BaseAmount + @TaxAmount;
        
        -- Create rental record
        INSERT INTO Rentals (
            CustomerID, VehicleID, PickupLocationID, ReturnLocationID,
            PlannedPickupDate, PlannedReturnDate, RentalStatusID,
            BaseAmount, TaxAmount, TotalAmount, SecurityDeposit, CreatedBy
        )
        VALUES (
            @CustomerID, @VehicleID, @PickupLocationID, @ReturnLocationID,
            @PlannedPickupDate, @PlannedReturnDate, 1, -- Reserved status
            @BaseAmount, @TaxAmount, @TotalAmount, @BaseAmount * 0.2, @CreatedBy
        );
        
        SET @RentalID = SCOPE_IDENTITY();
          -- Update vehicle status to reserved
        UPDATE Vehicles 
        SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Rezerve')
        WHERE VehicleID = @VehicleID;
        
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 3. Process Vehicle Return Procedure
CREATE PROCEDURE sp_ProcessVehicleReturn
    @RentalID INT,
    @ActualReturnDate DATETIME2,
    @Mileage INT,
    @DamageNotes NVARCHAR(MAX) = NULL,
    @ProcessedBy INT,
    @IsSuccess BIT OUTPUT,
    @LateFee DECIMAL(10,2) OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        DECLARE @VehicleID INT;
        DECLARE @PlannedReturnDate DATETIME2;
        DECLARE @DailyRate DECIMAL(10,2);
        DECLARE @OverdueDays INT;
        
        -- Get rental information
        SELECT 
            @VehicleID = r.VehicleID,
            @PlannedReturnDate = r.PlannedReturnDate,
            @DailyRate = v.DailyRentalRate
        FROM Rentals r
        INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
        WHERE r.RentalID = @RentalID;
        
        -- Calculate late fee if applicable
        SET @OverdueDays = CASE 
            WHEN @ActualReturnDate > @PlannedReturnDate 
            THEN DATEDIFF(DAY, @PlannedReturnDate, @ActualReturnDate)
            ELSE 0
        END;
        
        SET @LateFee = @OverdueDays * @DailyRate * 1.5; -- 150% daily rate for late returns
        
        -- Update rental record
        UPDATE Rentals 
        SET 
            ActualReturnDate = @ActualReturnDate,
            RentalStatusID = (SELECT RentalStatusID FROM RentalStatus WHERE StatusName = 'Tamamlandi'),
            TotalAmount = TotalAmount + @LateFee,
            UpdatedBy = @ProcessedBy,
            UpdatedDate = GETDATE()
        WHERE RentalID = @RentalID;
        
        -- Update vehicle mileage and status
        UPDATE Vehicles 
        SET 
            Mileage = @Mileage,
            CurrentStatusID = CASE 
                WHEN @DamageNotes IS NOT NULL                THEN (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Kontrol Gerekli')
                ELSE (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Musait')
            END,
            UpdatedBy = @ProcessedBy,
            UpdatedDate = GETDATE()
        WHERE VehicleID = @VehicleID;
        
        -- Record damage if any
        IF @DamageNotes IS NOT NULL
        BEGIN
            INSERT INTO VehicleDamages (
                VehicleID, RentalID, DamageTypeID, DamageDate, DamageDescription, CreatedBy
            )
            VALUES (
                @VehicleID, @RentalID, 1, -- General damage type
                @ActualReturnDate, @DamageNotes, @ProcessedBy
            );
        END
        
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 4. Search Available Vehicles Procedure
CREATE PROCEDURE sp_SearchAvailableVehicles
    @PickupDate DATETIME2,
    @ReturnDate DATETIME2,
    @LocationID INT = NULL,
    @CategoryID INT = NULL,
    @MaxDailyRate DECIMAL(10,2) = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        v.VehicleID,
        v.VehicleRegistration,
        vb.BrandName,
        vm.ModelName,
        vc.CategoryName,
        v.Color,
        v.DailyRentalRate,
        l.LocationName,
        c.CityName,
        vm.FuelType,
        vm.TransmissionType,
        vm.SeatingCapacity,
        v.Mileage
    FROM Vehicles v
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
    INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
    INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
    INNER JOIN Cities c ON l.CityID = c.CityID
    WHERE vs.IsAvailableForRent = 1
    AND (@LocationID IS NULL OR v.CurrentLocationID = @LocationID)
    AND (@CategoryID IS NULL OR vm.CategoryID = @CategoryID)
    AND (@MaxDailyRate IS NULL OR v.DailyRentalRate <= @MaxDailyRate)
    AND NOT EXISTS (
        SELECT 1 FROM Rentals r
        INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
        WHERE r.VehicleID = v.VehicleID 
        AND rs.StatusName IN ('Reserved', 'Active')
        AND (
            (@PickupDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
            OR (@ReturnDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
            OR (r.PlannedPickupDate BETWEEN @PickupDate AND @ReturnDate)
        )
    )
    ORDER BY v.DailyRentalRate;
END;
GO

-- 5. Generate Revenue Report Procedure
CREATE PROCEDURE sp_GenerateRevenueReport
    @StartDate DATE,
    @EndDate DATE,
    @LocationID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT 
        l.LocationName,
        c.CityName,
        COUNT(r.RentalID) AS TotalRentals,
        SUM(r.BaseAmount) AS TotalBaseAmount,
        SUM(r.TaxAmount) AS TotalTaxAmount,
        SUM(r.TotalAmount) AS TotalRevenue,
        AVG(r.TotalAmount) AS AverageRentalValue,
        COUNT(DISTINCT r.CustomerID) AS UniqueCustomers,
        COUNT(DISTINCT r.VehicleID) AS VehiclesRented
    FROM Rentals r
    INNER JOIN Locations l ON r.PickupLocationID = l.LocationID
    INNER JOIN Cities c ON l.CityID = c.CityID
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    WHERE r.CreatedDate BETWEEN @StartDate AND @EndDate
    AND rs.StatusName != 'Cancelled'
    AND (@LocationID IS NULL OR l.LocationID = @LocationID)
    GROUP BY l.LocationName, c.CityName
    ORDER BY TotalRevenue DESC;
END;
GO

-- 6. Customer Registration Procedure
CREATE PROCEDURE sp_RegisterCustomer
    @Username NVARCHAR(50),
    @Email NVARCHAR(100),
    @PasswordHash NVARCHAR(255),
    @FirstName NVARCHAR(50),
    @LastName NVARCHAR(50),
    @PhoneNumber NVARCHAR(20),
    @DateOfBirth DATE,
    @NationalID NVARCHAR(50),
    @CityID INT,
    @Address NVARCHAR(255),
    @UserID INT OUTPUT,
    @IsSuccess BIT OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Check if username or email already exists
        IF EXISTS (SELECT 1 FROM Users WHERE Username = @Username)
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Username already exists';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        IF EXISTS (SELECT 1 FROM Users WHERE Email = @Email)
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Email already exists';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Get Customer role ID
        DECLARE @CustomerRoleID INT;
        SELECT @CustomerRoleID = RoleID FROM UserRoles WHERE RoleName = 'Customer';
        
        -- Encrypt National ID (simplified - in real implementation use proper encryption)
        DECLARE @EncryptedNationalID VARBINARY(256);
        SET @EncryptedNationalID = CONVERT(VARBINARY(256), @NationalID);
        
        -- Insert new user
        INSERT INTO Users (
            Username, Email, PasswordHash, FirstName, LastName,
            PhoneNumber, DateOfBirth, NationalID, RoleID, CityID, Address
        )
        VALUES (
            @Username, @Email, @PasswordHash, @FirstName, @LastName,
            @PhoneNumber, @DateOfBirth, @EncryptedNationalID, @CustomerRoleID, @CityID, @Address
        );
        
        SET @UserID = SCOPE_IDENTITY();
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 7. Schedule Maintenance Procedure
CREATE PROCEDURE sp_ScheduleMaintenance
    @VehicleID INT,
    @MaintenanceTypeID INT,
    @ScheduledDate DATE,
    @ServiceProvider NVARCHAR(100),
    @Description NVARCHAR(MAX),
    @ScheduledBy INT,
    @MaintenanceID INT OUTPUT,
    @IsSuccess BIT OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Check if vehicle exists
        IF NOT EXISTS (SELECT 1 FROM Vehicles WHERE VehicleID = @VehicleID)
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Vehicle not found';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Insert maintenance record
        INSERT INTO VehicleMaintenance (
            VehicleID, MaintenanceTypeID, ScheduledDate, 
            ServiceProvider, Description, CreatedBy
        )
        VALUES (
            @VehicleID, @MaintenanceTypeID, @ScheduledDate,
            @ServiceProvider, @Description, @ScheduledBy
        );
        
        SET @MaintenanceID = SCOPE_IDENTITY();
        
        -- Update vehicle status if maintenance is due soon
        IF @ScheduledDate <= DATEADD(DAY, 7, GETDATE())
        BEGIN
            UPDATE Vehicles 
            SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Bakim Planlandi')
            WHERE VehicleID = @VehicleID;
        END
        
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 8. Process Payment Procedure
CREATE PROCEDURE sp_ProcessPayment
    @RentalID INT,
    @PaymentMethodID INT,
    @PaymentAmount DECIMAL(12,2),
    @TransactionReference NVARCHAR(100),
    @ProcessedBy INT,
    @PaymentID INT OUTPUT,
    @IsSuccess BIT OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        -- Validate rental exists
        IF NOT EXISTS (SELECT 1 FROM Rentals WHERE RentalID = @RentalID)
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Rental not found';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Calculate processing fee
        DECLARE @ProcessingFeePercentage DECIMAL(5,4);
        DECLARE @ProcessingFee DECIMAL(10,2);
        
        SELECT @ProcessingFeePercentage = ProcessingFeePercentage 
        FROM PaymentMethods 
        WHERE PaymentMethodID = @PaymentMethodID;
        
        SET @ProcessingFee = @PaymentAmount * @ProcessingFeePercentage;
        
        -- Insert payment record
        INSERT INTO Payments (
            RentalID, PaymentMethodID, PaymentAmount, TransactionReference,
            PaymentStatus, ProcessingFee, CreatedBy
        )
        VALUES (
            @RentalID, @PaymentMethodID, @PaymentAmount, @TransactionReference,
            'Completed', @ProcessingFee, @ProcessedBy
        );
        
        SET @PaymentID = SCOPE_IDENTITY();
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 9. Update Vehicle Status Procedure
CREATE PROCEDURE sp_UpdateVehicleStatus
    @VehicleID INT,
    @NewStatusID INT,
    @Notes NVARCHAR(MAX) = NULL,
    @UpdatedBy INT,
    @IsSuccess BIT OUTPUT,
    @ErrorMessage NVARCHAR(255) OUTPUT
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        DECLARE @OldStatusID INT;
        
        -- Get current status
        SELECT @OldStatusID = CurrentStatusID FROM Vehicles WHERE VehicleID = @VehicleID;
        
        IF @OldStatusID IS NULL
        BEGIN
            SET @IsSuccess = 0;
            SET @ErrorMessage = 'Vehicle not found';
            ROLLBACK TRANSACTION;
            RETURN;
        END
        
        -- Update vehicle status
        UPDATE Vehicles 
        SET 
            CurrentStatusID = @NewStatusID,
            UpdatedBy = @UpdatedBy,
            UpdatedDate = GETDATE()
        WHERE VehicleID = @VehicleID;
        
        -- Log the status change
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues, NewValues, UserID)
        VALUES (
            'Vehicles', 'UPDATE', @VehicleID,
            CONCAT('StatusID: ', @OldStatusID),
            CONCAT('StatusID: ', @NewStatusID, CASE WHEN @Notes IS NOT NULL THEN CONCAT(', Notes: ', @Notes) ELSE '' END),
            @UpdatedBy
        );
        
        SET @IsSuccess = 1;
        SET @ErrorMessage = NULL;
        
        COMMIT TRANSACTION;
    END TRY
    BEGIN CATCH
        SET @IsSuccess = 0;
        SET @ErrorMessage = ERROR_MESSAGE();
        ROLLBACK TRANSACTION;
    END CATCH
END;
GO

-- 10. Get Customer Dashboard Data Procedure
CREATE PROCEDURE sp_GetCustomerDashboard
    @CustomerID INT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Current Active Rentals
    SELECT 
        r.RentalID,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        v.VehicleRegistration,
        r.PlannedPickupDate,
        r.PlannedReturnDate,
        rs.StatusName,
        pl.LocationName AS PickupLocation,
        rl.LocationName AS ReturnLocation
    FROM Rentals r
    INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    INNER JOIN Locations pl ON r.PickupLocationID = pl.LocationID
    INNER JOIN Locations rl ON r.ReturnLocationID = rl.LocationID
    WHERE r.CustomerID = @CustomerID 
    AND rs.StatusName IN ('Reserved', 'Active')
    ORDER BY r.PlannedPickupDate;
    
    -- Rental History Summary
    SELECT 
        COUNT(r.RentalID) AS TotalRentals,
        SUM(r.TotalAmount) AS TotalSpent,
        AVG(r.TotalAmount) AS AverageRentalValue,
        MAX(r.CreatedDate) AS LastRentalDate
    FROM Rentals r
    WHERE r.CustomerID = @CustomerID;
    
    -- Unpaid Invoices
    SELECT 
        r.RentalID,
        r.TotalAmount,
        ISNULL(SUM(p.PaymentAmount), 0) AS PaidAmount,
        r.TotalAmount - ISNULL(SUM(p.PaymentAmount), 0) AS BalanceRemaining
    FROM Rentals r
    LEFT JOIN Payments p ON r.RentalID = p.RentalID AND p.PaymentStatus = 'Completed'
    WHERE r.CustomerID = @CustomerID
    GROUP BY r.RentalID, r.TotalAmount
    HAVING r.TotalAmount > ISNULL(SUM(p.PaymentAmount), 0);
END;
GO

PRINT 'All stored procedures created successfully!';
