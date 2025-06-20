-- ================================================
-- INDEXES CREATION SCRIPT (Simplified)
-- Various types: Clustered, Non-Clustered, Composite, Unique
-- ================================================

USE CarRentalDB;
GO

-- Non-Clustered Indexes on Foreign Keys
CREATE NONCLUSTERED INDEX IX_Cities_CountryID ON Cities(CountryID);
CREATE NONCLUSTERED INDEX IX_Users_RoleID ON Users(RoleID);
CREATE NONCLUSTERED INDEX IX_Users_CityID ON Users(CityID);
CREATE NONCLUSTERED INDEX IX_VehicleModels_BrandID ON VehicleModels(BrandID);
CREATE NONCLUSTERED INDEX IX_VehicleModels_CategoryID ON VehicleModels(CategoryID);
CREATE NONCLUSTERED INDEX IX_Locations_CityID ON Locations(CityID);
CREATE NONCLUSTERED INDEX IX_Vehicles_ModelID ON Vehicles(ModelID);
CREATE NONCLUSTERED INDEX IX_Vehicles_StatusID ON Vehicles(CurrentStatusID);
CREATE NONCLUSTERED INDEX IX_Vehicles_LocationID ON Vehicles(CurrentLocationID);
CREATE NONCLUSTERED INDEX IX_Rentals_CustomerID ON Rentals(CustomerID);
CREATE NONCLUSTERED INDEX IX_Rentals_VehicleID ON Rentals(VehicleID);
CREATE NONCLUSTERED INDEX IX_Rentals_StatusID ON Rentals(RentalStatusID);
CREATE NONCLUSTERED INDEX IX_Payments_RentalID ON Payments(RentalID);
CREATE NONCLUSTERED INDEX IX_VehicleMaintenance_VehicleID ON VehicleMaintenance(VehicleID);
CREATE NONCLUSTERED INDEX IX_VehicleDamages_VehicleID ON VehicleDamages(VehicleID);
CREATE NONCLUSTERED INDEX IX_CustomerFeedback_RentalID ON CustomerFeedback(RentalID);

-- Unique Indexes
CREATE UNIQUE NONCLUSTERED INDEX IX_Users_Username ON Users(Username);
CREATE UNIQUE NONCLUSTERED INDEX IX_Users_Email ON Users(Email);
CREATE UNIQUE NONCLUSTERED INDEX IX_Vehicles_Registration ON Vehicles(VehicleRegistration);

-- Composite Indexes for common queries
CREATE NONCLUSTERED INDEX IX_Rentals_Dates_Status 
ON Rentals(PlannedPickupDate, PlannedReturnDate, RentalStatusID)
INCLUDE (CustomerID, VehicleID);

CREATE NONCLUSTERED INDEX IX_Vehicles_Available 
ON Vehicles(CurrentStatusID, CurrentLocationID)
INCLUDE (ModelID, DailyRentalRate);

CREATE NONCLUSTERED INDEX IX_Users_Active_Role 
ON Users(IsActive, RoleID)
INCLUDE (FirstName, LastName, Email);

CREATE NONCLUSTERED INDEX IX_Payments_Date_Status 
ON Payments(PaymentDate, PaymentStatus)
INCLUDE (PaymentAmount, RentalID);

CREATE NONCLUSTERED INDEX IX_VehicleMaintenance_Schedule 
ON VehicleMaintenance(ScheduledDate, CompletedDate)
INCLUDE (VehicleID, MaintenanceTypeID, Cost);

-- Filtered Indexes
CREATE NONCLUSTERED INDEX IX_Users_Active 
ON Users(LastLoginDate) 
WHERE IsActive = 1;

CREATE NONCLUSTERED INDEX IX_Vehicles_Available_For_Rent 
ON Vehicles(DailyRentalRate, ModelID) 
WHERE CurrentStatusID IN (1, 2); -- Available statuses

CREATE NONCLUSTERED INDEX IX_Rentals_Active 
ON Rentals(PlannedPickupDate, PlannedReturnDate) 
WHERE RentalStatusID IN (1, 2, 3); -- Active rental statuses

PRINT 'All basic indexes created successfully!';
