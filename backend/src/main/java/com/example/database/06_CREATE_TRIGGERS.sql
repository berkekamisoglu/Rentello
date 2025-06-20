-- ================================================
-- TRIGGERS CREATION SCRIPT
-- 10+ Triggers for automatic rule enforcement and logging
-- ================================================

USE CarRentalDB;
GO

-- 1. User Audit Trigger
CREATE TRIGGER tr_Users_Audit
ON Users
AFTER INSERT, UPDATE, DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Handle INSERT
    IF EXISTS (SELECT * FROM inserted) AND NOT EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, NewValues, UserID)
        SELECT 
            'Users', 
            'INSERT', 
            i.UserID,
            CONCAT('Username: ', i.Username, ', Email: ', i.Email, ', Role: ', ur.RoleName),
            i.CreatedBy
        FROM inserted i
        LEFT JOIN UserRoles ur ON i.RoleID = ur.RoleID;
    END
    
    -- Handle UPDATE
    IF EXISTS (SELECT * FROM inserted) AND EXISTS (SELECT * FROM deleted)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues, NewValues, UserID)
        SELECT 
            'Users',
            'UPDATE',
            i.UserID,
            CONCAT('Username: ', d.Username, ', Email: ', d.Email, ', IsActive: ', d.IsActive),
            CONCAT('Username: ', i.Username, ', Email: ', i.Email, ', IsActive: ', i.IsActive),
            i.UpdatedBy
        FROM inserted i
        INNER JOIN deleted d ON i.UserID = d.UserID;
    END
    
    -- Handle DELETE
    IF EXISTS (SELECT * FROM deleted) AND NOT EXISTS (SELECT * FROM inserted)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues)
        SELECT 
            'Users',
            'DELETE',
            d.UserID,
            CONCAT('Username: ', d.Username, ', Email: ', d.Email)
        FROM deleted d;
    END
END;
GO

-- 2. Rental Status Change Trigger
CREATE TRIGGER tr_Rentals_StatusChange
ON Rentals
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check for status changes and update vehicle status accordingly
    IF UPDATE(RentalStatusID)
    BEGIN        -- When rental becomes active, update vehicle to rented
        UPDATE v
        SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Kiralandi'),
            UpdatedDate = GETDATE()
        FROM Vehicles v
        INNER JOIN inserted i ON v.VehicleID = i.VehicleID
        INNER JOIN RentalStatus rs ON i.RentalStatusID = rs.RentalStatusID
        WHERE rs.StatusName = 'Aktif';
        
        -- When rental is completed, update vehicle to available
        UPDATE v
        SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Musait'),
            UpdatedDate = GETDATE()
        FROM Vehicles v
        INNER JOIN inserted i ON v.VehicleID = i.VehicleID
        INNER JOIN RentalStatus rs ON i.RentalStatusID = rs.RentalStatusID
        WHERE rs.StatusName = 'Tamamlandi';
        
        -- When rental is cancelled, update vehicle to available
        UPDATE v
        SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Musait'),
            UpdatedDate = GETDATE()
        FROM Vehicles v
        INNER JOIN inserted i ON v.VehicleID = i.VehicleID
        INNER JOIN RentalStatus rs ON i.RentalStatusID = rs.RentalStatusID
        WHERE rs.StatusName = 'Iptal Edildi';
    END
END;
GO

-- 3. Vehicle Maintenance Alert Trigger
CREATE TRIGGER tr_Vehicles_MaintenanceAlert
ON Vehicles
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if vehicle mileage requires maintenance
    INSERT INTO VehicleMaintenance (VehicleID, MaintenanceTypeID, ScheduledDate, Description, CreatedBy)
    SELECT 
        i.VehicleID,
        (SELECT MaintenanceTypeID FROM MaintenanceTypes WHERE TypeName = 'General Service'),
        DATEADD(DAY, 30, GETDATE()),
        'Automatic maintenance scheduled - high mileage detected',
        1
    FROM inserted i
    INNER JOIN deleted d ON i.VehicleID = d.VehicleID
    WHERE i.Mileage > d.Mileage 
    AND i.Mileage % 10000 = 0 -- Every 10,000 miles
    AND i.Mileage > 0;
END;
GO

-- 4. Payment Completion Trigger
CREATE TRIGGER tr_Payments_Completion
ON Payments
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check if rental is fully paid
    DECLARE @RentalID INT, @TotalAmount DECIMAL(12,2), @PaidAmount DECIMAL(12,2);
    
    SELECT DISTINCT @RentalID = i.RentalID
    FROM inserted i;
    
    SELECT @TotalAmount = TotalAmount FROM Rentals WHERE RentalID = @RentalID;
    
    SELECT @PaidAmount = SUM(PaymentAmount) 
    FROM Payments 
    WHERE RentalID = @RentalID AND PaymentStatus = 'Completed';
    
    -- If fully paid, update rental status
    IF @PaidAmount >= @TotalAmount
    BEGIN
        UPDATE Rentals 
        SET RentalStatusID = (SELECT RentalStatusID FROM RentalStatus WHERE StatusName = 'Odendi')
        WHERE RentalID = @RentalID;
    END
END;
GO

-- 5. Customer Feedback Auto-Response Trigger
CREATE TRIGGER tr_CustomerFeedback_AutoResponse
ON CustomerFeedback
AFTER INSERT
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Auto-respond to low ratings
    UPDATE cf
    SET ResponseText = 'Thank you for your feedback. We apologize for any inconvenience. Our customer service team will contact you within 24 hours to address your concerns.',
        ResponseDate = GETDATE(),
        RespondedBy = 1 -- System user
    FROM CustomerFeedback cf
    INNER JOIN inserted i ON cf.FeedbackID = i.FeedbackID
    WHERE i.Rating <= 2;
    
    -- Auto-respond to high ratings
    UPDATE cf
    SET ResponseText = 'Thank you for your excellent feedback! We''re delighted that you had a great experience with our service.',
        ResponseDate = GETDATE(),
        RespondedBy = 1 -- System user
    FROM CustomerFeedback cf
    INNER JOIN inserted i ON cf.FeedbackID = i.FeedbackID
    WHERE i.Rating >= 4;
END;
GO

-- 6. Vehicle Damage Cost Tracking Trigger
CREATE TRIGGER tr_VehicleDamages_CostTracking
ON VehicleDamages
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Update vehicle status when damage is reported
    UPDATE v
    SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Hasarli'),
        UpdatedDate = GETDATE()
    FROM Vehicles v
    INNER JOIN inserted i ON v.VehicleID = i.VehicleID
    WHERE i.IsRepaired = 0;
      -- Update vehicle status when damage is repaired
    UPDATE v
    SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Musait'),
        UpdatedDate = GETDATE()
    FROM Vehicles v
    INNER JOIN inserted i ON v.VehicleID = i.VehicleID
    WHERE i.IsRepaired = 1
    AND NOT EXISTS (
        SELECT 1 FROM VehicleDamages vd 
        WHERE vd.VehicleID = i.VehicleID AND vd.IsRepaired = 0
    );
END;
GO

-- 7. Rental Overdue Detection Trigger
CREATE TRIGGER tr_Rentals_OverdueDetection
ON Rentals
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Update rental status to overdue if return date has passed
    UPDATE r    SET RentalStatusID = (SELECT RentalStatusID FROM RentalStatus WHERE StatusName = 'Gecikmis')
    FROM Rentals r
    INNER JOIN inserted i ON r.RentalID = i.RentalID
    INNER JOIN RentalStatus rs ON i.RentalStatusID = rs.RentalStatusID
    WHERE rs.StatusName = 'Aktif'
    AND i.PlannedReturnDate < GETDATE()
    AND i.ActualReturnDate IS NULL;
END;
GO

-- 8. User Role Change Notification Trigger
CREATE TRIGGER tr_Users_RoleChange
ON Users
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Log role changes for security audit
    IF UPDATE(RoleID)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues, NewValues, UserID)
        SELECT 
            'Users',
            'ROLE_CHANGE',
            i.UserID,
            CONCAT('Old Role: ', old_role.RoleName),
            CONCAT('New Role: ', new_role.RoleName),
            i.UpdatedBy
        FROM inserted i
        INNER JOIN deleted d ON i.UserID = d.UserID
        INNER JOIN UserRoles old_role ON d.RoleID = old_role.RoleID
        INNER JOIN UserRoles new_role ON i.RoleID = new_role.RoleID
        WHERE i.RoleID != d.RoleID;
    END
END;
GO

-- 9. Location Vehicle Count Trigger
CREATE TRIGGER tr_Vehicles_LocationUpdate
ON Vehicles
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Log vehicle location changes
    IF UPDATE(CurrentLocationID)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues, NewValues, UserID)
        SELECT 
            'Vehicles',
            'LOCATION_CHANGE',
            i.VehicleID,
            CONCAT('Old Location: ', old_loc.LocationName),
            CONCAT('New Location: ', new_loc.LocationName),
            i.UpdatedBy
        FROM inserted i
        INNER JOIN deleted d ON i.VehicleID = d.VehicleID
        INNER JOIN Locations old_loc ON d.CurrentLocationID = old_loc.LocationID
        INNER JOIN Locations new_loc ON i.CurrentLocationID = new_loc.LocationID
        WHERE i.CurrentLocationID != d.CurrentLocationID;
    END
END;
GO

-- 10. Maintenance Completion Trigger
CREATE TRIGGER tr_VehicleMaintenance_Completion
ON VehicleMaintenance
AFTER UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- When maintenance is completed, update vehicle status
    IF UPDATE(CompletedDate)
    BEGIN        UPDATE v
        SET CurrentStatusID = (SELECT StatusID FROM VehicleStatus WHERE StatusName = 'Musait'),
            NextMaintenanceDate = CASE
                WHEN mt.TypeName = 'Oil Change' THEN DATEADD(MONTH, 3, GETDATE())
                WHEN mt.TypeName = 'General Service' THEN DATEADD(MONTH, 6, GETDATE())
                WHEN mt.TypeName = 'Annual Inspection' THEN DATEADD(YEAR, 1, GETDATE())
                ELSE DATEADD(MONTH, 6, GETDATE())
            END,
            UpdatedDate = GETDATE()
        FROM Vehicles v
        INNER JOIN inserted i ON v.VehicleID = i.VehicleID
        INNER JOIN MaintenanceTypes mt ON i.MaintenanceTypeID = mt.MaintenanceTypeID
        WHERE i.CompletedDate IS NOT NULL;
    END
END;
GO

-- 11. Security Trigger - Prevent Unauthorized Deletions
CREATE TRIGGER tr_Security_PreventDeletion
ON Users
INSTEAD OF DELETE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Log deletion attempt
    INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues)
    SELECT 
        'Users',
        'DELETE_ATTEMPT',
        d.UserID,
        CONCAT('Username: ', d.Username, ', Email: ', d.Email)
    FROM deleted d;
    
    -- Instead of deleting, deactivate the user
    UPDATE Users 
    SET IsActive = 0, UpdatedDate = GETDATE()
    WHERE UserID IN (SELECT UserID FROM deleted);
    
    PRINT 'Users cannot be deleted for security reasons. They have been deactivated instead.';
END;
GO

-- 12. Price Validation Trigger
CREATE TRIGGER tr_Vehicles_PriceValidation
ON Vehicles
AFTER INSERT, UPDATE
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Validate daily rental rate is reasonable
    IF EXISTS (
        SELECT 1 FROM inserted 
        WHERE DailyRentalRate < 10 OR DailyRentalRate > 1000
    )
    BEGIN
        RAISERROR('Daily rental rate must be between $10 and $1000', 16, 1);
        ROLLBACK TRANSACTION;
        RETURN;
    END
    
    -- Log significant price changes
    IF UPDATE(DailyRentalRate)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, RecordID, OldValues, NewValues, UserID)
        SELECT 
            'Vehicles',
            'PRICE_CHANGE',
            i.VehicleID,
            CONCAT('Old Rate: $', d.DailyRentalRate),
            CONCAT('New Rate: $', i.DailyRentalRate),
            i.UpdatedBy
        FROM inserted i
        INNER JOIN deleted d ON i.VehicleID = d.VehicleID
        WHERE ABS(i.DailyRentalRate - d.DailyRentalRate) > 50; -- Log changes > $50
    END
END;
GO

PRINT 'All triggers created successfully!';
