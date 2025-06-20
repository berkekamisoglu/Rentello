-- ================================================
-- VIEWS CREATION SCRIPT
-- 10+ Views for abstraction, security, and reporting
-- ================================================

USE CarRentalDB;
GO

-- 1. User Information View (Security - hide sensitive data)
CREATE VIEW vw_UserInfo AS
SELECT 
    u.UserID,
    u.Username,
    u.Email,
    u.FirstName,
    u.LastName,
    u.PhoneNumber,
    u.DateOfBirth,
    ur.RoleName,
    c.CityName,
    co.CountryName,
    u.IsActive,
    u.LastLoginDate,
    u.CreatedDate
FROM Users u
INNER JOIN UserRoles ur ON u.RoleID = ur.RoleID
LEFT JOIN Cities c ON u.CityID = c.CityID
LEFT JOIN Countries co ON c.CountryID = co.CountryID
WHERE u.IsActive = 1;
GO

-- 2. Available Vehicles View
CREATE VIEW vw_AvailableVehicles AS
SELECT 
    v.VehicleID,
    v.VehicleRegistration,
    vb.BrandName,
    vm.ModelName,
    vc.CategoryName,
    v.Color,
    v.Mileage,
    v.DailyRentalRate,
    vs.StatusName,
    vs.IsAvailableForRent,
    l.LocationName,
    c.CityName,
    vm.ManufactureYear,
    vm.FuelType,
    vm.TransmissionType,
    vm.SeatingCapacity,
    v.VehicleDescription,
    v.ImageUrls,
    vm.ManufactureYear AS Year,
    vs.StatusID,
    vb.BrandID,
    vm.ModelID,
    vc.CategoryID,
    l.LocationID,
    c.CityID
FROM Vehicles v
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
INNER JOIN Cities c ON l.CityID = c.CityID
WHERE vs.IsAvailableForRent = 1;
GO

-- 3. Active Rentals View
CREATE VIEW vw_ActiveRentals AS
SELECT 
    r.RentalID,
    CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
    u.Email AS CustomerEmail,
    u.PhoneNumber AS CustomerPhone,
    v.VehicleRegistration,
    CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
    pl.LocationName AS PickupLocation,
    rl.LocationName AS ReturnLocation,
    r.PlannedPickupDate,
    r.PlannedReturnDate,
    r.ActualPickupDate,
    rs.StatusName AS RentalStatus,
    r.TotalAmount,
    DATEDIFF(DAY, r.PlannedPickupDate, r.PlannedReturnDate) AS RentalDays,
    CASE 
        WHEN r.PlannedReturnDate < GETDATE() AND r.ActualReturnDate IS NULL 
        THEN DATEDIFF(DAY, r.PlannedReturnDate, GETDATE())
        ELSE 0
    END AS OverdueDays
FROM Rentals r
INNER JOIN Users u ON r.CustomerID = u.UserID
INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
INNER JOIN Locations pl ON r.PickupLocationID = pl.LocationID
INNER JOIN Locations rl ON r.ReturnLocationID = rl.LocationID
INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
WHERE rs.StatusName IN ('Reserved', 'Active', 'Overdue');
GO

-- 4. Revenue Summary View
CREATE VIEW vw_RevenueSummary AS
SELECT 
    YEAR(r.CreatedDate) AS Year,
    MONTH(r.CreatedDate) AS Month,
    DATENAME(MONTH, r.CreatedDate) AS MonthName,
    COUNT(r.RentalID) AS TotalRentals,
    SUM(r.BaseAmount) AS TotalBaseAmount,
    SUM(r.TaxAmount) AS TotalTaxAmount,
    SUM(r.TotalAmount) AS TotalRevenue,
    AVG(r.TotalAmount) AS AverageRentalValue,
    COUNT(DISTINCT r.CustomerID) AS UniqueCustomers,
    COUNT(DISTINCT r.VehicleID) AS VehiclesRented
FROM Rentals r
INNER JOIN RentalStatus rs ON r.RentalStatusID = rs.RentalStatusID
WHERE rs.StatusName != 'Cancelled'
GROUP BY YEAR(r.CreatedDate), MONTH(r.CreatedDate), DATENAME(MONTH, r.CreatedDate);
GO

-- 5. Vehicle Utilization View
CREATE VIEW vw_VehicleUtilization AS
SELECT 
    v.VehicleID,
    v.VehicleRegistration,
    CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
    vc.CategoryName,
    COUNT(r.RentalID) AS TotalRentals,
    SUM(DATEDIFF(DAY, r.PlannedPickupDate, r.PlannedReturnDate)) AS TotalRentalDays,
    SUM(r.TotalAmount) AS TotalRevenue,
    AVG(r.TotalAmount) AS AverageRentalValue,
    MAX(r.PlannedReturnDate) AS LastRentalDate,
    DATEDIFF(DAY, MAX(r.PlannedReturnDate), GETDATE()) AS DaysSinceLastRental
FROM Vehicles v
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
LEFT JOIN Rentals r ON v.VehicleID = r.VehicleID
GROUP BY v.VehicleID, v.VehicleRegistration, vb.BrandName, vm.ModelName, vc.CategoryName;
GO

-- 6. Customer History View
CREATE VIEW vw_CustomerHistory AS
SELECT 
    u.UserID,
    CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
    u.Email,
    COUNT(r.RentalID) AS TotalRentals,
    SUM(r.TotalAmount) AS TotalSpent,
    AVG(r.TotalAmount) AS AverageRentalValue,
    MAX(r.CreatedDate) AS LastRentalDate,
    MIN(r.CreatedDate) AS FirstRentalDate,
    AVG(CAST(cf.Rating AS FLOAT)) AS AverageRating,
    COUNT(cf.FeedbackID) AS FeedbackCount
FROM Users u
INNER JOIN Rentals r ON u.UserID = r.CustomerID
LEFT JOIN CustomerFeedback cf ON r.RentalID = cf.RentalID
WHERE u.RoleID = (SELECT RoleID FROM UserRoles WHERE RoleName = 'Customer')
GROUP BY u.UserID, u.FirstName, u.LastName, u.Email;
GO

-- 7. Maintenance Schedule View
CREATE VIEW vw_MaintenanceSchedule AS
SELECT 
    vm.MaintenanceID,
    v.VehicleRegistration,
    CONCAT(vb.BrandName, ' ', vmod.ModelName) AS VehicleName,
    mt.TypeName AS MaintenanceType,
    vm.ScheduledDate,
    vm.CompletedDate,
    vm.Cost,
    vm.ServiceProvider,
    CASE 
        WHEN vm.CompletedDate IS NULL AND vm.ScheduledDate < GETDATE() 
        THEN 'Overdue'
        WHEN vm.CompletedDate IS NULL 
        THEN 'Scheduled'
        ELSE 'Completed'
    END AS MaintenanceStatus,
    DATEDIFF(DAY, GETDATE(), vm.ScheduledDate) AS DaysUntilDue
FROM VehicleMaintenance vm
INNER JOIN Vehicles v ON vm.VehicleID = v.VehicleID
INNER JOIN VehicleModels vmod ON v.ModelID = vmod.ModelID
INNER JOIN VehicleBrands vb ON vmod.BrandID = vb.BrandID
INNER JOIN MaintenanceTypes mt ON vm.MaintenanceTypeID = mt.MaintenanceTypeID;
GO

-- 8. Payment Summary View
CREATE VIEW vw_PaymentSummary AS
SELECT 
    p.PaymentID,
    r.RentalID,
    CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
    pm.MethodName AS PaymentMethod,
    p.PaymentAmount,
    p.PaymentDate,
    p.PaymentStatus,
    p.TransactionReference,
    r.TotalAmount AS RentalAmount,
    SUM(p.PaymentAmount) OVER (PARTITION BY r.RentalID) AS TotalPaid,
    r.TotalAmount - SUM(p.PaymentAmount) OVER (PARTITION BY r.RentalID) AS BalanceRemaining
FROM Payments p
INNER JOIN Rentals r ON p.RentalID = r.RentalID
INNER JOIN Users u ON r.CustomerID = u.UserID
INNER JOIN PaymentMethods pm ON p.PaymentMethodID = pm.PaymentMethodID;
GO

-- 9. Vehicle Damage Report View
CREATE VIEW vw_VehicleDamageReport AS
SELECT 
    vd.DamageID,
    v.VehicleRegistration,
    CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
    dt.DamageTypeName,
    vd.DamageDate,
    vd.RepairDate,
    vd.RepairCost,
    vd.IsRepaired,
    vd.ResponsibleParty,
    CASE 
        WHEN r.RentalID IS NOT NULL 
        THEN CONCAT(u.FirstName, ' ', u.LastName)
        ELSE 'N/A'
    END AS CustomerName
FROM VehicleDamages vd
INNER JOIN Vehicles v ON vd.VehicleID = v.VehicleID
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
INNER JOIN DamageTypes dt ON vd.DamageTypeID = dt.DamageTypeID
LEFT JOIN Rentals r ON vd.RentalID = r.RentalID
LEFT JOIN Users u ON r.CustomerID = u.UserID;
GO

-- 10. Fleet Overview View
CREATE VIEW vw_FleetOverview AS
SELECT 
    vc.CategoryName,
    COUNT(v.VehicleID) AS TotalVehicles,
    SUM(CASE WHEN vs.IsAvailableForRent = 1 THEN 1 ELSE 0 END) AS AvailableVehicles,
    SUM(CASE WHEN vs.StatusName = 'Rented' THEN 1 ELSE 0 END) AS RentedVehicles,
    SUM(CASE WHEN vs.StatusName = 'Maintenance' THEN 1 ELSE 0 END) AS InMaintenance,
    SUM(CASE WHEN vs.StatusName = 'Damaged' THEN 1 ELSE 0 END) AS DamagedVehicles,
    AVG(v.DailyRentalRate) AS AverageRate,
    AVG(v.Mileage) AS AverageMileage
FROM Vehicles v
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
GROUP BY vc.CategoryName;
GO

-- 11. Top Customers View
CREATE VIEW vw_TopCustomers AS
SELECT TOP 100
    u.UserID,
    CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
    u.Email,
    u.PhoneNumber,
    COUNT(r.RentalID) AS TotalRentals,
    SUM(r.TotalAmount) AS TotalSpent,
    AVG(r.TotalAmount) AS AverageRentalValue,
    MAX(r.CreatedDate) AS LastRentalDate,
    AVG(CAST(cf.Rating AS FLOAT)) AS AverageRating
FROM Users u
INNER JOIN Rentals r ON u.UserID = r.CustomerID
LEFT JOIN CustomerFeedback cf ON r.RentalID = cf.RentalID
WHERE u.RoleID = (SELECT RoleID FROM UserRoles WHERE RoleName = 'Customer')
GROUP BY u.UserID, u.FirstName, u.LastName, u.Email, u.PhoneNumber
ORDER BY SUM(r.TotalAmount) DESC;
GO

-- 12. Location Performance View
CREATE VIEW vw_LocationPerformance AS
SELECT 
    l.LocationID,
    l.LocationName,
    c.CityName,
    COUNT(DISTINCT v.VehicleID) AS TotalVehicles,
    COUNT(r.RentalID) AS TotalRentals,
    SUM(r.TotalAmount) AS TotalRevenue,
    AVG(r.TotalAmount) AS AverageRentalValue,
    COUNT(DISTINCT r.CustomerID) AS UniqueCustomers,
    AVG(CAST(cf.Rating AS FLOAT)) AS AverageRating
FROM Locations l
INNER JOIN Cities c ON l.CityID = c.CityID
LEFT JOIN Vehicles v ON l.LocationID = v.CurrentLocationID
LEFT JOIN Rentals r ON l.LocationID = r.PickupLocationID
LEFT JOIN CustomerFeedback cf ON r.RentalID = cf.RentalID
WHERE l.IsActive = 1
GROUP BY l.LocationID, l.LocationName, c.CityName;
GO

PRINT 'All views created successfully!';
