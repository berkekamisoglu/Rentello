-- ================================================
-- SQL SERVER AGENT AUTOMATION JOBS
-- Scheduled tasks for backup, archiving, notifications
-- ================================================

USE CarRentalDB;
GO

-- ================================================
-- 1. BACKUP AUTOMATION PROCEDURES
-- ================================================

-- Procedure for automated database backup
CREATE PROCEDURE sp_AutomatedBackup
    @BackupType NVARCHAR(10) = 'FULL', -- FULL, DIFF, LOG
    @BackupPath NVARCHAR(500) = 'C:\Database\Backups\'
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @FileName NVARCHAR(500);
    DECLARE @SQL NVARCHAR(MAX);
    DECLARE @DatabaseName NVARCHAR(100) = 'CarRentalDB';
    
    -- Generate filename with timestamp
    SET @FileName = @BackupPath + @DatabaseName + '_' + @BackupType + '_' 
                   + FORMAT(GETDATE(), 'yyyyMMdd_HHmmss') + '.bak';
    
    IF @BackupType = 'FULL'
    BEGIN
        SET @SQL = 'BACKUP DATABASE [' + @DatabaseName + '] TO DISK = ''' + @FileName + ''' 
                   WITH FORMAT, COMPRESSION, CHECKSUM, 
                   DESCRIPTION = ''CarRental DB Full Backup - ' + CAST(GETDATE() AS NVARCHAR(50)) + '''';
    END
    ELSE IF @BackupType = 'DIFF'
    BEGIN
        SET @SQL = 'BACKUP DATABASE [' + @DatabaseName + '] TO DISK = ''' + @FileName + ''' 
                   WITH DIFFERENTIAL, COMPRESSION, CHECKSUM,
                   DESCRIPTION = ''CarRental DB Differential Backup - ' + CAST(GETDATE() AS NVARCHAR(50)) + '''';
    END
    ELSE IF @BackupType = 'LOG'
    BEGIN
        SET @FileName = REPLACE(@FileName, '.bak', '.trn');
        SET @SQL = 'BACKUP LOG [' + @DatabaseName + '] TO DISK = ''' + @FileName + ''' 
                   WITH COMPRESSION, CHECKSUM,
                   DESCRIPTION = ''CarRental DB Log Backup - ' + CAST(GETDATE() AS NVARCHAR(50)) + '''';
    END
    
    -- Execute backup
    EXEC sp_executesql @SQL;
    
    -- Log backup completion
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    VALUES ('Backup', 'AUTOMATED_BACKUP', 
           CONCAT('Backup Type: ', @BackupType, ', File: ', @FileName), 1);
    
    PRINT 'Backup completed: ' + @FileName;
END;
GO

-- ================================================
-- 2. DATA ARCHIVING PROCEDURES
-- ================================================

-- Procedure to archive old rental data
CREATE PROCEDURE sp_ArchiveOldRentals
    @ArchiveDays INT = 365 -- Archive rentals older than this many days
AS
BEGIN
    SET NOCOUNT ON;
    BEGIN TRANSACTION;
    
    BEGIN TRY
        DECLARE @ArchiveDate DATE = DATEADD(DAY, -@ArchiveDays, GETDATE());
        DECLARE @ArchivedCount INT = 0;
        
        -- Create archive table if not exists
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ArchivedRentals')
        BEGIN
            SELECT * 
            INTO ArchivedRentals 
            FROM Rentals 
            WHERE 1 = 0; -- Create structure only
            
            -- Add archive-specific columns
            ALTER TABLE ArchivedRentals ADD ArchivedDate DATETIME2 DEFAULT GETDATE();
            ALTER TABLE ArchivedRentals ADD ArchivedBy INT DEFAULT 1;
        END
        
        -- Move old completed rentals to archive
        INSERT INTO ArchivedRentals (
            RentalID, CustomerID, VehicleID, PickupLocationID, ReturnLocationID,
            PlannedPickupDate, PlannedReturnDate, ActualPickupDate, ActualReturnDate,
            RentalStatusID, BaseAmount, TaxAmount, DiscountAmount, TotalAmount,
            SecurityDeposit, Notes, CreatedDate, UpdatedDate, CreatedBy, UpdatedBy
        )
        SELECT 
            RentalID, CustomerID, VehicleID, PickupLocationID, ReturnLocationID,
            PlannedPickupDate, PlannedReturnDate, ActualPickupDate, ActualReturnDate,
            RentalStatusID, BaseAmount, TaxAmount, DiscountAmount, TotalAmount,
            SecurityDeposit, Notes, CreatedDate, UpdatedDate, CreatedBy, UpdatedBy
        FROM Rentals r
        INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
        WHERE rs.StatusName IN ('Completed', 'Cancelled')
        AND r.ActualReturnDate < @ArchiveDate;
        
        SET @ArchivedCount = @@ROWCOUNT;
        
        -- Archive related payments
        IF NOT EXISTS (SELECT * FROM sys.tables WHERE name = 'ArchivedPayments')
        BEGIN
            SELECT * 
            INTO ArchivedPayments 
            FROM Payments 
            WHERE 1 = 0;
            
            ALTER TABLE ArchivedPayments ADD ArchivedDate DATETIME2 DEFAULT GETDATE();
        END
        
        INSERT INTO ArchivedPayments (
            PaymentID, RentalID, PaymentMethodID, PaymentAmount, PaymentDate,
            TransactionReference, PaymentStatus, ProcessingFee, Notes, CreatedBy
        )
        SELECT 
            p.PaymentID, p.RentalID, p.PaymentMethodID, p.PaymentAmount, p.PaymentDate,
            p.TransactionReference, p.PaymentStatus, p.ProcessingFee, p.Notes, p.CreatedBy
        FROM Payments p
        WHERE p.RentalID IN (SELECT RentalID FROM ArchivedRentals);
        
        -- Delete archived data from main tables
        DELETE p FROM Payments p 
        WHERE p.RentalID IN (SELECT RentalID FROM ArchivedRentals WHERE ArchivedDate = CAST(GETDATE() AS DATE));
        
        DELETE r FROM Rentals r
        INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
        WHERE rs.StatusName IN ('Completed', 'Cancelled')
        AND r.ActualReturnDate < @ArchiveDate;
        
        -- Log archiving activity
        INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
        VALUES ('Rentals', 'AUTOMATED_ARCHIVE', 
               CONCAT('Archived ', @ArchivedCount, ' rentals older than ', @ArchiveDays, ' days'), 1);
        
        COMMIT TRANSACTION;
        PRINT CONCAT('Successfully archived ', @ArchivedCount, ' rental records');
    END TRY
    BEGIN CATCH
        ROLLBACK TRANSACTION;
        THROW;
    END CATCH
END;
GO

-- ================================================
-- 3. EMAIL NOTIFICATION PROCEDURES
-- ================================================

-- Procedure for overdue rental notifications
CREATE PROCEDURE sp_SendOverdueNotifications
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Get overdue rentals
    DECLARE @OverdueRentals TABLE (
        RentalID INT,
        CustomerName NVARCHAR(101),
        CustomerEmail NVARCHAR(100),
        VehicleName NVARCHAR(151),
        DaysOverdue INT,
        LateFee DECIMAL(12,2)
    );
    
    INSERT INTO @OverdueRentals
    SELECT 
        r.RentalID,
        CONCAT(u.FirstName, ' ', u.LastName),
        u.Email,
        CONCAT(vb.BrandName, ' ', vm.ModelName),
        DATEDIFF(DAY, r.PlannedReturnDate, GETDATE()),
        dbo.fn_CalculateLateFee(r.PlannedReturnDate, GETDATE(), v.DailyRentalRate)
    FROM Rentals r
    INNER JOIN Users u ON r.CustomerID = u.UserID
    INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    WHERE rs.StatusName IN ('Active', 'Overdue')
    AND r.PlannedReturnDate < GETDATE()
    AND r.ActualReturnDate IS NULL;
    
    -- In a real implementation, this would send actual emails
    -- For demo purposes, we'll log the notifications
    DECLARE @NotificationCount INT = (SELECT COUNT(*) FROM @OverdueRentals);
    
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    SELECT 
        'Notifications',
        'OVERDUE_EMAIL',
        CONCAT('Overdue notification sent to ', CustomerEmail, ' for rental ', RentalID, 
               ' - ', DaysOverdue, ' days overdue, Late fee: $', LateFee),
        1
    FROM @OverdueRentals;
    
    PRINT CONCAT('Sent ', @NotificationCount, ' overdue rental notifications');
END;
GO

-- ================================================
-- 4. MAINTENANCE REMINDER PROCEDURE
-- ================================================

-- Procedure for maintenance reminders
CREATE PROCEDURE sp_SendMaintenanceReminders
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Get vehicles due for maintenance in next 7 days
    DECLARE @MaintenanceDue TABLE (
        VehicleID INT,
        VehicleRegistration NVARCHAR(20),
        MaintenanceType NVARCHAR(50),
        DueDate DATE,
        DaysUntilDue INT
    );
    
    INSERT INTO @MaintenanceDue
    SELECT 
        v.VehicleID,
        v.VehicleRegistration,
        mt.TypeName,
        vm.ScheduledDate,
        DATEDIFF(DAY, GETDATE(), vm.ScheduledDate)
    FROM VehicleMaintenance vm
    INNER JOIN Vehicles v ON vm.VehicleID = v.VehicleID
    INNER JOIN MaintenanceTypes mt ON vm.MaintenanceTypeID = mt.MaintenanceTypeID
    WHERE vm.CompletedDate IS NULL
    AND vm.ScheduledDate BETWEEN GETDATE() AND DATEADD(DAY, 7, GETDATE());
    
    -- Log maintenance reminders
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    SELECT 
        'Maintenance',
        'REMINDER_SENT',
        CONCAT('Maintenance reminder for vehicle ', VehicleRegistration, 
               ' - ', MaintenanceType, ' due in ', DaysUntilDue, ' days'),
        1
    FROM @MaintenanceDue;
    
    DECLARE @ReminderCount INT = (SELECT COUNT(*) FROM @MaintenanceDue);
    PRINT CONCAT('Sent ', @ReminderCount, ' maintenance reminders');
END;
GO

-- ================================================
-- 5. PERFORMANCE MONITORING PROCEDURE
-- ================================================

-- Procedure to monitor database performance
CREATE PROCEDURE sp_MonitorPerformance
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check for slow queries, blocking, etc.
    DECLARE @PerformanceIssues TABLE (
        IssueType NVARCHAR(50),
        Description NVARCHAR(255),
        Severity NVARCHAR(20)
    );
    
    -- Check database size
    DECLARE @DBSizeMB DECIMAL(10,2);
    SELECT @DBSizeMB = SUM(CAST(size AS DECIMAL(10,2)) * 8 / 1024)
    FROM sys.database_files;
    
    IF @DBSizeMB > 5000 -- 5GB threshold
    BEGIN
        INSERT INTO @PerformanceIssues VALUES 
        ('Database Size', CONCAT('Database size is ', @DBSizeMB, ' MB'), 'WARNING');
    END
    
    -- Check for missing indexes (simplified)
    IF EXISTS (
        SELECT * FROM sys.dm_db_missing_index_details
        WHERE database_id = DB_ID()
    )
    BEGIN
        INSERT INTO @PerformanceIssues VALUES 
        ('Missing Indexes', 'Potential missing indexes detected', 'INFO');
    END
    
    -- Log performance monitoring results
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    SELECT 
        'Performance',
        'MONITORING',
        CONCAT(IssueType, ': ', Description, ' (', Severity, ')'),
        1
    FROM @PerformanceIssues;
    
    IF NOT EXISTS (SELECT * FROM @PerformanceIssues)
    BEGIN
        INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
        VALUES ('Performance', 'MONITORING', 'No performance issues detected', 1);
    END
    
    PRINT 'Performance monitoring completed';
END;
GO

-- ================================================
-- 6. CLEANUP PROCEDURE
-- ================================================

-- Procedure to clean up old logs and temporary data
CREATE PROCEDURE sp_CleanupOldData
    @LogRetentionDays INT = 90
AS
BEGIN
    SET NOCOUNT ON;
    
    DECLARE @CleanupDate DATETIME2 = DATEADD(DAY, -@LogRetentionDays, GETDATE());
    DECLARE @DeletedLogs INT = 0;
    
    -- Clean up old system logs
    DELETE FROM SystemLogs 
    WHERE LogDate < @CleanupDate 
    AND OperationType NOT IN ('LOGIN_FAILED', 'SECURITY_VIOLATION'); -- Keep security logs longer
    
    SET @DeletedLogs = @@ROWCOUNT;
    
    -- Clean up old backup files (would need xp_cmdshell enabled)
    -- This is commented out for security reasons
    /*
    DECLARE @CleanupCommand NVARCHAR(500);
    SET @CleanupCommand = 'forfiles /p "C:\Database\Backups" /s /m *.bak /d -30 /c "cmd /c del @path"';
    EXEC xp_cmdshell @CleanupCommand;
    */
    
    -- Log cleanup activity
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    VALUES ('SystemLogs', 'AUTOMATED_CLEANUP', 
           CONCAT('Deleted ', @DeletedLogs, ' log entries older than ', @LogRetentionDays, ' days'), 1);
    
    PRINT CONCAT('Cleanup completed - removed ', @DeletedLogs, ' old log entries');
END;
GO

-- ================================================
-- 7. SQL AGENT JOB CREATION SCRIPTS
-- ================================================

-- Note: These would typically be created through SQL Server Management Studio
-- or using sp_add_job procedures. Here's the T-SQL equivalent:

PRINT '================================================';
PRINT 'SQL SERVER AGENT JOBS CREATION REFERENCE';
PRINT '================================================';
PRINT '';
PRINT '1. DAILY FULL BACKUP JOB:';
PRINT 'EXEC sp_AutomatedBackup @BackupType = ''FULL'';';
PRINT 'Schedule: Daily at 2:00 AM';
PRINT '';
PRINT '2. HOURLY LOG BACKUP JOB:';
PRINT 'EXEC sp_AutomatedBackup @BackupType = ''LOG'';';
PRINT 'Schedule: Every hour during business hours';
PRINT '';
PRINT '3. WEEKLY DIFFERENTIAL BACKUP JOB:';
PRINT 'EXEC sp_AutomatedBackup @BackupType = ''DIFF'';';
PRINT 'Schedule: Weekly on Sundays at 6:00 PM';
PRINT '';
PRINT '4. MONTHLY DATA ARCHIVING JOB:';
PRINT 'EXEC sp_ArchiveOldRentals @ArchiveDays = 365;';
PRINT 'Schedule: First day of each month at 3:00 AM';
PRINT '';
PRINT '5. DAILY OVERDUE NOTIFICATIONS JOB:';
PRINT 'EXEC sp_SendOverdueNotifications;';
PRINT 'Schedule: Daily at 9:00 AM';
PRINT '';
PRINT '6. WEEKLY MAINTENANCE REMINDERS JOB:';
PRINT 'EXEC sp_SendMaintenanceReminders;';
PRINT 'Schedule: Every Monday at 8:00 AM';
PRINT '';
PRINT '7. DAILY PERFORMANCE MONITORING JOB:';
PRINT 'EXEC sp_MonitorPerformance;';
PRINT 'Schedule: Daily at 6:00 AM';
PRINT '';
PRINT '8. WEEKLY CLEANUP JOB:';
PRINT 'EXEC sp_CleanupOldData @LogRetentionDays = 90;';
PRINT 'Schedule: Every Sunday at 4:00 AM';

-- ================================================
-- 8. SAMPLE JOB EXECUTION (FOR TESTING)
-- ================================================

-- Test the automation procedures
PRINT '';
PRINT 'Testing automation procedures...';

-- Test backup procedure
EXEC sp_AutomatedBackup @BackupType = 'FULL';

-- Test maintenance reminders
EXEC sp_SendMaintenanceReminders;

-- Test overdue notifications
EXEC sp_SendOverdueNotifications;

-- Test performance monitoring
EXEC sp_MonitorPerformance;

PRINT '';
PRINT 'Automation setup completed successfully!';
PRINT 'All procedures are ready for SQL Server Agent scheduling.';
