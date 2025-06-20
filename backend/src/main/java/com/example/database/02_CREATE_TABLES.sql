-- ================================================
-- TABLES CREATION SCRIPT
-- 15+ Normalized Tables with Relationships
-- ================================================

USE CarRentalDB;
GO

-- 1. Countries Table
CREATE TABLE Countries (
    CountryID INT IDENTITY(1,1) PRIMARY KEY,
    CountryName NVARCHAR(100) NOT NULL UNIQUE,
    CountryCode NVARCHAR(10) NOT NULL UNIQUE,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE()
);

-- 2. Cities Table
CREATE TABLE Cities (
    CityID INT IDENTITY(1,1) PRIMARY KEY,
    CityName NVARCHAR(100) NOT NULL,
    CountryID INT NOT NULL,
    PostalCode NVARCHAR(20),
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (CountryID) REFERENCES Countries(CountryID)
);

-- 3. User Roles Table
CREATE TABLE UserRoles (
    RoleID INT IDENTITY(1,1) PRIMARY KEY,
    RoleName NVARCHAR(50) NOT NULL UNIQUE,
    RoleDescription NVARCHAR(255),
    Permissions NVARCHAR(MAX), -- JSON format permissions
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE()
);

-- 4. Users Table (with encryption for sensitive data)
CREATE TABLE Users (
    UserID INT IDENTITY(1,1) PRIMARY KEY,
    Username NVARCHAR(50) NOT NULL UNIQUE,
    Email NVARCHAR(100) NOT NULL UNIQUE,
    PasswordHash NVARCHAR(255) NOT NULL,
    FirstName NVARCHAR(50) NOT NULL,
    LastName NVARCHAR(50) NOT NULL,
    PhoneNumber NVARCHAR(20),
    DateOfBirth DATE,
    NationalID VARBINARY(256), -- Encrypted
    RoleID INT NOT NULL,
    CityID INT,
    Address NVARCHAR(255),
    IsActive BIT DEFAULT 1,
    LastLoginDate DATETIME2,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    CreatedBy INT,
    UpdatedBy INT,
    FOREIGN KEY (RoleID) REFERENCES UserRoles(RoleID),
    FOREIGN KEY (CityID) REFERENCES Cities(CityID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    FOREIGN KEY (UpdatedBy) REFERENCES Users(UserID)
);

-- 5. Vehicle Categories Table
CREATE TABLE VehicleCategories (
    CategoryID INT IDENTITY(1,1) PRIMARY KEY,
    CategoryName NVARCHAR(50) NOT NULL UNIQUE,
    CategoryDescription NVARCHAR(255),
    BasePrice DECIMAL(10,2) NOT NULL,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE()
);

-- 6. Vehicle Brands Table
CREATE TABLE VehicleBrands (
    BrandID INT IDENTITY(1,1) PRIMARY KEY,
    BrandName NVARCHAR(50) NOT NULL UNIQUE,
    BrandCountry NVARCHAR(50),
    Website NVARCHAR(255),
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE()
);

-- 7. Vehicle Models Table
CREATE TABLE VehicleModels (
    ModelID INT IDENTITY(1,1) PRIMARY KEY,
    ModelName NVARCHAR(100) NOT NULL,
    BrandID INT NOT NULL,
    CategoryID INT NOT NULL,
    ManufactureYear INT,
    EngineType NVARCHAR(50),
    FuelType NVARCHAR(30),
    TransmissionType NVARCHAR(30),
    SeatingCapacity INT,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (BrandID) REFERENCES VehicleBrands(BrandID),
    FOREIGN KEY (CategoryID) REFERENCES VehicleCategories(CategoryID)
);

-- 8. Vehicle Status Table
CREATE TABLE VehicleStatus (
    StatusID INT IDENTITY(1,1) PRIMARY KEY,
    StatusName NVARCHAR(50) NOT NULL UNIQUE,
    StatusDescription NVARCHAR(255),
    IsAvailableForRent BIT DEFAULT 0,
    CreatedDate DATETIME2 DEFAULT GETDATE()
);

-- 9. Locations Table (Rental Offices)
CREATE TABLE Locations (
    LocationID INT IDENTITY(1,1) PRIMARY KEY,
    LocationName NVARCHAR(100) NOT NULL,
    CityID INT NOT NULL,
    Address NVARCHAR(255) NOT NULL,
    PhoneNumber NVARCHAR(20),
    Email NVARCHAR(100),
    IsActive BIT DEFAULT 1,
    ManagerID INT,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    FOREIGN KEY (CityID) REFERENCES Cities(CityID),
    FOREIGN KEY (ManagerID) REFERENCES Users(UserID)
);

-- 10. Vehicles Table
CREATE TABLE Vehicles (
    VehicleID INT IDENTITY(1,1) PRIMARY KEY,
    VehicleRegistration NVARCHAR(20) NOT NULL UNIQUE,
    ModelID INT NOT NULL,
    Color NVARCHAR(30),
    Mileage INT DEFAULT 0,
    PurchaseDate DATE,
    PurchasePrice DECIMAL(12,2),
    CurrentStatusID INT NOT NULL,
    CurrentLocationID INT NOT NULL,
    DailyRentalRate DECIMAL(10,2) NOT NULL,
    InsurancePolicyNumber NVARCHAR(50),    NextMaintenanceDate DATE,
    VehicleDescription NVARCHAR(MAX), -- For full-text search
    ImageUrls NVARCHAR(MAX), -- JSON format: ["url1", "url2", "url3"]
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    CreatedBy INT,
    UpdatedBy INT,
    FOREIGN KEY (ModelID) REFERENCES VehicleModels(ModelID),
    FOREIGN KEY (CurrentStatusID) REFERENCES VehicleStatus(StatusID),
    FOREIGN KEY (CurrentLocationID) REFERENCES Locations(LocationID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    FOREIGN KEY (UpdatedBy) REFERENCES Users(UserID)
);

-- 11. Rental Status Table
CREATE TABLE RentalStatus (
    RentalStatusID INT IDENTITY(1,1) PRIMARY KEY,
    StatusName NVARCHAR(50) NOT NULL UNIQUE,
    StatusDescription NVARCHAR(255),
    CreatedDate DATETIME2 DEFAULT GETDATE()
);

-- 12. Rentals Table
CREATE TABLE Rentals (
    RentalID INT IDENTITY(1,1) PRIMARY KEY,
    CustomerID INT NOT NULL,
    VehicleID INT NOT NULL,
    PickupLocationID INT NOT NULL,
    ReturnLocationID INT NOT NULL,
    PlannedPickupDate DATETIME2 NOT NULL,
    PlannedReturnDate DATETIME2 NOT NULL,
    ActualPickupDate DATETIME2,
    ActualReturnDate DATETIME2,
    RentalStatusID INT NOT NULL,
    BaseAmount DECIMAL(12,2) NOT NULL,
    TaxAmount DECIMAL(12,2) DEFAULT 0,
    DiscountAmount DECIMAL(12,2) DEFAULT 0,
    TotalAmount DECIMAL(12,2) NOT NULL,
    SecurityDeposit DECIMAL(12,2) DEFAULT 0,
    Notes NVARCHAR(MAX),
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    UpdatedDate DATETIME2 DEFAULT GETDATE(),
    CreatedBy INT,
    UpdatedBy INT,
    FOREIGN KEY (CustomerID) REFERENCES Users(UserID),
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (PickupLocationID) REFERENCES Locations(LocationID),
    FOREIGN KEY (ReturnLocationID) REFERENCES Locations(LocationID),
    FOREIGN KEY (RentalStatusID) REFERENCES RentalStatus(RentalStatusID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID),
    FOREIGN KEY (UpdatedBy) REFERENCES Users(UserID)
);

-- 13. Payment Methods Table
CREATE TABLE PaymentMethods (
    PaymentMethodID INT IDENTITY(1,1) PRIMARY KEY,
    MethodName NVARCHAR(50) NOT NULL UNIQUE,
    MethodDescription NVARCHAR(255),
    IsActive BIT DEFAULT 1,
    ProcessingFeePercentage DECIMAL(5,4) DEFAULT 0,
    CreatedDate DATETIME2 DEFAULT GETDATE()
);

-- 14. Payments Table
CREATE TABLE Payments (
    PaymentID INT IDENTITY(1,1) PRIMARY KEY,
    RentalID INT NOT NULL,
    PaymentMethodID INT NOT NULL,
    PaymentAmount DECIMAL(12,2) NOT NULL,
    PaymentDate DATETIME2 DEFAULT GETDATE(),
    TransactionReference NVARCHAR(100),
    PaymentStatus NVARCHAR(30) DEFAULT 'Pending',
    ProcessingFee DECIMAL(10,2) DEFAULT 0,
    Notes NVARCHAR(500),
    CreatedBy INT,
    FOREIGN KEY (RentalID) REFERENCES Rentals(RentalID),
    FOREIGN KEY (PaymentMethodID) REFERENCES PaymentMethods(PaymentMethodID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);

-- 15. Maintenance Types Table
CREATE TABLE MaintenanceTypes (
    MaintenanceTypeID INT IDENTITY(1,1) PRIMARY KEY,
    TypeName NVARCHAR(50) NOT NULL UNIQUE,
    TypeDescription NVARCHAR(255),
    EstimatedDuration INT, -- in hours
    AverageCost DECIMAL(10,2),
    CreatedDate DATETIME2 DEFAULT GETDATE()
);

-- 16. Vehicle Maintenance Table
CREATE TABLE VehicleMaintenance (
    MaintenanceID INT IDENTITY(1,1) PRIMARY KEY,
    VehicleID INT NOT NULL,
    MaintenanceTypeID INT NOT NULL,
    ScheduledDate DATE NOT NULL,
    CompletedDate DATE,
    Cost DECIMAL(10,2),
    ServiceProvider NVARCHAR(100),
    Description NVARCHAR(MAX),
    NextServiceMileage INT,
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    CreatedBy INT,
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (MaintenanceTypeID) REFERENCES MaintenanceTypes(MaintenanceTypeID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);

-- 17. Damage Types Table
CREATE TABLE DamageTypes (
    DamageTypeID INT IDENTITY(1,1) PRIMARY KEY,
    DamageTypeName NVARCHAR(50) NOT NULL UNIQUE,
    DamageDescription NVARCHAR(255),
    AverageRepairCost DECIMAL(10,2),
    CreatedDate DATETIME2 DEFAULT GETDATE()
);

-- 18. Vehicle Damages Table
CREATE TABLE VehicleDamages (
    DamageID INT IDENTITY(1,1) PRIMARY KEY,
    VehicleID INT NOT NULL,
    RentalID INT, -- NULL if damage not related to rental
    DamageTypeID INT NOT NULL,
    DamageDate DATE NOT NULL,
    DamageDescription NVARCHAR(MAX),
    RepairCost DECIMAL(10,2),
    RepairDate DATE,
    IsRepaired BIT DEFAULT 0,
    ResponsibleParty NVARCHAR(100),
    CreatedDate DATETIME2 DEFAULT GETDATE(),
    CreatedBy INT,
    FOREIGN KEY (VehicleID) REFERENCES Vehicles(VehicleID),
    FOREIGN KEY (RentalID) REFERENCES Rentals(RentalID),
    FOREIGN KEY (DamageTypeID) REFERENCES DamageTypes(DamageTypeID),
    FOREIGN KEY (CreatedBy) REFERENCES Users(UserID)
);

-- 19. Customer Feedback Table
CREATE TABLE CustomerFeedback (
    FeedbackID INT IDENTITY(1,1) PRIMARY KEY,
    RentalID INT NOT NULL,
    CustomerID INT NOT NULL,
    Rating INT CHECK (Rating BETWEEN 1 AND 5),
    FeedbackText NVARCHAR(MAX),
    ResponseText NVARCHAR(MAX),
    FeedbackDate DATETIME2 DEFAULT GETDATE(),
    ResponseDate DATETIME2,
    RespondedBy INT,
    IsPublic BIT DEFAULT 0,
    FOREIGN KEY (RentalID) REFERENCES Rentals(RentalID),
    FOREIGN KEY (CustomerID) REFERENCES Users(UserID),
    FOREIGN KEY (RespondedBy) REFERENCES Users(UserID)
);

-- 20. System Logs Table (for audit trail)
CREATE TABLE SystemLogs (
    LogID BIGINT IDENTITY(1,1) PRIMARY KEY,
    TableName NVARCHAR(100) NOT NULL,
    OperationType NVARCHAR(20) NOT NULL, -- INSERT, UPDATE, DELETE
    RecordID INT,
    OldValues NVARCHAR(MAX), -- JSON format
    NewValues NVARCHAR(MAX), -- JSON format
    UserID INT,
    LogDate DATETIME2 DEFAULT GETDATE(),
    IPAddress NVARCHAR(45),
    UserAgent NVARCHAR(500),
    FOREIGN KEY (UserID) REFERENCES Users(UserID)
);

PRINT 'All tables created successfully!';
