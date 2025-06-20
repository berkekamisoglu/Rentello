-- ================================================
-- USER-DEFINED FUNCTIONS CREATION SCRIPT
-- 10+ UDFs (Scalar and Table-valued functions)
-- ================================================

USE CarRentalDB;
GO

-- 1. Scalar Function - Calculate Age
CREATE FUNCTION fn_CalculateAge(@DateOfBirth DATE)
RETURNS INT
AS
BEGIN
    RETURN DATEDIFF(YEAR, @DateOfBirth, GETDATE()) - 
           CASE WHEN MONTH(@DateOfBirth) > MONTH(GETDATE()) 
                OR (MONTH(@DateOfBirth) = MONTH(GETDATE()) AND DAY(@DateOfBirth) > DAY(GETDATE()))
           THEN 1 ELSE 0 END;
END;
GO

-- 2. Scalar Function - Calculate Rental Duration
CREATE FUNCTION fn_CalculateRentalDuration(@StartDate DATETIME2, @EndDate DATETIME2)
RETURNS INT
AS
BEGIN
    DECLARE @Duration INT;
    SET @Duration = DATEDIFF(DAY, @StartDate, @EndDate);
    RETURN CASE WHEN @Duration < 1 THEN 1 ELSE @Duration END; -- Minimum 1 day
END;
GO

-- 3. Scalar Function - Calculate Late Fee
CREATE FUNCTION fn_CalculateLateFee(@PlannedReturnDate DATETIME2, @ActualReturnDate DATETIME2, @DailyRate DECIMAL(10,2))
RETURNS DECIMAL(12,2)
AS
BEGIN
    DECLARE @LateDays INT;
    DECLARE @LateFee DECIMAL(12,2);
    
    SET @LateDays = CASE 
        WHEN @ActualReturnDate > @PlannedReturnDate 
        THEN DATEDIFF(DAY, @PlannedReturnDate, @ActualReturnDate)
        ELSE 0
    END;
    
    SET @LateFee = @LateDays * @DailyRate * 1.5; -- 150% of daily rate
    
    RETURN @LateFee;
END;
GO

-- 4. Scalar Function - Get Customer Loyalty Tier
CREATE FUNCTION fn_GetCustomerLoyaltyTier(@CustomerID INT)
RETURNS NVARCHAR(20)
AS
BEGIN
    DECLARE @TotalSpent DECIMAL(12,2);
    DECLARE @RentalCount INT;
    DECLARE @LoyaltyTier NVARCHAR(20);
    
    SELECT 
        @TotalSpent = ISNULL(SUM(TotalAmount), 0),
        @RentalCount = COUNT(RentalID)
    FROM Rentals 
    WHERE CustomerID = @CustomerID;
    
    SET @LoyaltyTier = CASE 
        WHEN @TotalSpent >= 10000 OR @RentalCount >= 20 THEN 'Platinum'
        WHEN @TotalSpent >= 5000 OR @RentalCount >= 10 THEN 'Gold'
        WHEN @TotalSpent >= 1000 OR @RentalCount >= 3 THEN 'Silver'
        ELSE 'Bronze'
    END;
    
    RETURN @LoyaltyTier;
END;
GO

-- 5. Scalar Function - Calculate Discount Percentage
CREATE FUNCTION fn_CalculateDiscountPercentage(@CustomerID INT, @RentalDays INT)
RETURNS DECIMAL(5,2)
AS
BEGIN
    DECLARE @LoyaltyTier NVARCHAR(20);
    DECLARE @DiscountPercentage DECIMAL(5,2);
    
    SET @LoyaltyTier = dbo.fn_GetCustomerLoyaltyTier(@CustomerID);
    
    -- Base discount based on loyalty tier
    SET @DiscountPercentage = CASE @LoyaltyTier
        WHEN 'Platinum' THEN 15.00
        WHEN 'Gold' THEN 10.00
        WHEN 'Silver' THEN 5.00
        ELSE 0.00
    END;
    
    -- Additional discount for long-term rentals
    IF @RentalDays >= 30
        SET @DiscountPercentage = @DiscountPercentage + 10.00;
    ELSE IF @RentalDays >= 7
        SET @DiscountPercentage = @DiscountPercentage + 5.00;
    
    -- Maximum discount is 25%
    IF @DiscountPercentage > 25.00
        SET @DiscountPercentage = 25.00;
    
    RETURN @DiscountPercentage;
END;
GO

-- 6. Table-valued Function - Get Available Vehicles by Date Range
CREATE FUNCTION fn_GetAvailableVehiclesByDateRange(@StartDate DATETIME2, @EndDate DATETIME2)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        v.VehicleID,
        v.VehicleRegistration,
        vb.BrandName,
        vm.ModelName,
        vc.CategoryName,
        v.Color,
        v.DailyRentalRate,
        l.LocationName,
        c.CityName
    FROM Vehicles v
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
    INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
    INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
    INNER JOIN Cities c ON l.CityID = c.CityID
    WHERE vs.IsAvailableForRent = 1
    AND NOT EXISTS (
        SELECT 1 FROM Rentals r
        INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
        WHERE r.VehicleID = v.VehicleID 
        AND rs.StatusName IN ('Reserved', 'Active')
        AND (
            (@StartDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
            OR (@EndDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
            OR (r.PlannedPickupDate BETWEEN @StartDate AND @EndDate)
        )
    )
);
GO

-- 7. Table-valued Function - Get Customer Rental History
CREATE FUNCTION fn_GetCustomerRentalHistory(@CustomerID INT, @TopN INT = 10)
RETURNS TABLE
AS
RETURN
(
    SELECT TOP (@TopN)
        r.RentalID,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        v.VehicleRegistration,
        r.PlannedPickupDate,
        r.PlannedReturnDate,
        r.ActualReturnDate,
        rs.StatusName,
        r.TotalAmount,
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
    ORDER BY r.CreatedDate DESC
);
GO

-- 8. Table-valued Function - Get Vehicle Maintenance History
CREATE FUNCTION fn_GetVehicleMaintenanceHistory(@VehicleID INT)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        vm.MaintenanceID,
        mt.TypeName AS MaintenanceType,
        vm.ScheduledDate,
        vm.CompletedDate,
        vm.Cost,
        vm.ServiceProvider,
        vm.Description,
        CASE 
            WHEN vm.CompletedDate IS NULL AND vm.ScheduledDate < GETDATE() 
            THEN 'Overdue'
            WHEN vm.CompletedDate IS NULL 
            THEN 'Scheduled'
            ELSE 'Completed'
        END AS Status
    FROM VehicleMaintenance vm
    INNER JOIN MaintenanceTypes mt ON vm.MaintenanceTypeID = mt.MaintenanceTypeID
    WHERE vm.VehicleID = @VehicleID
);
GO

-- 9. Scalar Function - Check Vehicle Availability
CREATE FUNCTION fn_IsVehicleAvailable(@VehicleID INT, @StartDate DATETIME2, @EndDate DATETIME2)
RETURNS BIT
AS
BEGIN
    DECLARE @IsAvailable BIT = 0;
    
    -- Check if vehicle exists and is available for rent
    IF EXISTS (
        SELECT 1 FROM Vehicles v
        INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
        WHERE v.VehicleID = @VehicleID AND vs.IsAvailableForRent = 1
    )
    BEGIN
        -- Check for conflicting rentals
        IF NOT EXISTS (
            SELECT 1 FROM Rentals r
            INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
            WHERE r.VehicleID = @VehicleID 
            AND rs.StatusName IN ('Reserved', 'Active')
            AND (
                (@StartDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
                OR (@EndDate BETWEEN r.PlannedPickupDate AND r.PlannedReturnDate)
                OR (r.PlannedPickupDate BETWEEN @StartDate AND @EndDate)
            )
        )
        BEGIN
            SET @IsAvailable = 1;
        END
    END
    
    RETURN @IsAvailable;
END;
GO

-- 10. Table-valued Function - Get Revenue Report by Period
CREATE FUNCTION fn_GetRevenueReportByPeriod(@StartDate DATE, @EndDate DATE)
RETURNS TABLE
AS
RETURN
(
    SELECT 
        l.LocationName,
        c.CityName,
        vc.CategoryName,
        COUNT(r.RentalID) AS TotalRentals,
        SUM(r.BaseAmount) AS TotalBaseAmount,
        SUM(r.TaxAmount) AS TotalTaxAmount,
        SUM(r.TotalAmount) AS TotalRevenue,
        AVG(r.TotalAmount) AS AverageRentalValue
    FROM Rentals r
    INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
    INNER JOIN Locations l ON r.PickupLocationID = l.LocationID
    INNER JOIN Cities c ON l.CityID = c.CityID
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    WHERE r.CreatedDate BETWEEN @StartDate AND @EndDate
    AND rs.StatusName != 'Cancelled'
    GROUP BY l.LocationName, c.CityName, vc.CategoryName
);
GO

-- 11. Scalar Function - Calculate Vehicle Utilization Rate
CREATE FUNCTION fn_CalculateVehicleUtilizationRate(@VehicleID INT, @Days INT = 30)
RETURNS DECIMAL(5,2)
AS
BEGIN
    DECLARE @RentedDays INT;
    DECLARE @UtilizationRate DECIMAL(5,2);
    DECLARE @StartDate DATE = DATEADD(DAY, -@Days, GETDATE());
    
    SELECT @RentedDays = ISNULL(SUM(
        DATEDIFF(DAY, 
            CASE WHEN r.PlannedPickupDate < @StartDate THEN @StartDate ELSE r.PlannedPickupDate END,
            CASE WHEN r.PlannedReturnDate > GETDATE() THEN GETDATE() ELSE r.PlannedReturnDate END
        ) + 1
    ), 0)
    FROM Rentals r
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    WHERE r.VehicleID = @VehicleID
    AND rs.StatusName IN ('Active', 'Completed')
    AND r.PlannedReturnDate >= @StartDate
    AND r.PlannedPickupDate <= GETDATE();
    
    SET @UtilizationRate = CASE 
        WHEN @Days > 0 THEN (CAST(@RentedDays AS DECIMAL(5,2)) / @Days) * 100
        ELSE 0
    END;
    
    RETURN CASE WHEN @UtilizationRate > 100 THEN 100 ELSE @UtilizationRate END;
END;
GO

-- 12. Table-valued Function - Get Overdue Rentals
CREATE FUNCTION fn_GetOverdueRentals()
RETURNS TABLE
AS
RETURN
(
    SELECT 
        r.RentalID,
        CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
        u.Email,
        u.PhoneNumber,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        v.VehicleRegistration,
        r.PlannedReturnDate,
        DATEDIFF(DAY, r.PlannedReturnDate, GETDATE()) AS OverdueDays,
        dbo.fn_CalculateLateFee(r.PlannedReturnDate, GETDATE(), v.DailyRentalRate) AS LateFee,
        l.LocationName AS PickupLocation
    FROM Rentals r
    INNER JOIN Users u ON r.CustomerID = u.UserID
    INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN Locations l ON r.PickupLocationID = l.LocationID
    INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
    WHERE rs.StatusName IN ('Active', 'Overdue')
    AND r.PlannedReturnDate < GETDATE()
    AND r.ActualReturnDate IS NULL
);
GO

-- 13. Scalar Function - Format Currency
CREATE FUNCTION fn_FormatCurrency(@Amount DECIMAL(12,2))
RETURNS NVARCHAR(20)
AS
BEGIN
    RETURN '$' + FORMAT(@Amount, 'N2');
END;
GO

-- 14. Table-valued Function - Get Popular Vehicles
CREATE FUNCTION fn_GetPopularVehicles(@TopN INT = 10)
RETURNS TABLE
AS
RETURN
(
    SELECT TOP (@TopN)
        v.VehicleID,
        v.VehicleRegistration,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        vc.CategoryName,
        COUNT(r.RentalID) AS RentalCount,
        SUM(r.TotalAmount) AS TotalRevenue,
        AVG(CAST(cf.Rating AS FLOAT)) AS AverageRating,
        dbo.fn_CalculateVehicleUtilizationRate(v.VehicleID, 90) AS UtilizationRate90Days
    FROM Vehicles v
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
    LEFT JOIN Rentals r ON v.VehicleID = r.VehicleID
    LEFT JOIN CustomerFeedback cf ON r.RentalID = cf.RentalID
    GROUP BY v.VehicleID, v.VehicleRegistration, vb.BrandName, vm.ModelName, vc.CategoryName
    ORDER BY COUNT(r.RentalID) DESC, AVG(CAST(cf.Rating AS FLOAT)) DESC
);
GO

PRINT 'All user-defined functions created successfully!';
