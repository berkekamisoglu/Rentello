-- ================================================
-- FULL-TEXT SEARCH IMPLEMENTATION
-- Search capabilities for vehicle descriptions and customer feedback
-- ================================================

USE CarRentalDB;
GO

-- ================================================
-- 1. FULL-TEXT CATALOG AND INDEXES SETUP
-- ================================================

-- Enable full-text search on the database (if not already enabled)
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'CarRentalCatalog')
BEGIN
    CREATE FULLTEXT CATALOG CarRentalCatalog AS DEFAULT;
    PRINT 'Full-text catalog created successfully';
END
ELSE
BEGIN
    PRINT 'Full-text catalog already exists';
END
GO

-- Create full-text index on Vehicles table for VehicleDescription
IF NOT EXISTS (SELECT * FROM sys.fulltext_indexes WHERE object_id = OBJECT_ID('Vehicles'))
BEGIN
    CREATE FULLTEXT INDEX ON Vehicles(VehicleDescription)
    KEY INDEX PK__Vehicles__476B54B2C2E4E7FB
    ON CarRentalCatalog;
    PRINT 'Full-text index created on Vehicles.VehicleDescription';
END
ELSE
BEGIN
    PRINT 'Full-text index on Vehicles already exists';
END
GO

-- Create full-text index on CustomerFeedback table
IF NOT EXISTS (SELECT * FROM sys.fulltext_indexes WHERE object_id = OBJECT_ID('CustomerFeedback'))
BEGIN
    CREATE FULLTEXT INDEX ON CustomerFeedback(FeedbackText, ResponseText)
    KEY INDEX PK__Customer__6A4BEDF670B9C2E8
    ON CarRentalCatalog;
    PRINT 'Full-text index created on CustomerFeedback';
END
ELSE
BEGIN
    PRINT 'Full-text index on CustomerFeedback already exists';
END
GO

-- ================================================
-- 2. FULL-TEXT SEARCH PROCEDURES
-- ================================================

-- Procedure to search vehicles by description
CREATE PROCEDURE sp_SearchVehiclesByDescription
    @SearchTerms NVARCHAR(255),
    @MaxResults INT = 20
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT TOP (@MaxResults)
        v.VehicleID,
        v.VehicleRegistration,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        vc.CategoryName,
        v.Color,
        v.DailyRentalRate,
        l.LocationName,
        c.CityName,
        v.VehicleDescription,
        vs.StatusName,
        KEY_TBL.RANK AS SearchRank
    FROM CONTAINSTABLE(Vehicles, VehicleDescription, @SearchTerms) AS KEY_TBL
    INNER JOIN Vehicles v ON v.VehicleID = KEY_TBL.[KEY]
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
    INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
    INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
    INNER JOIN Cities c ON l.CityID = c.CityID
    ORDER BY KEY_TBL.RANK DESC;
END;
GO

-- Procedure to search customer feedback
CREATE PROCEDURE sp_SearchCustomerFeedback
    @SearchTerms NVARCHAR(255),
    @MinRating INT = 1,
    @MaxRating INT = 5,
    @MaxResults INT = 50
AS
BEGIN
    SET NOCOUNT ON;
    
    SELECT TOP (@MaxResults)
        cf.FeedbackID,
        cf.RentalID,
        CONCAT(u.FirstName, ' ', u.LastName) AS CustomerName,
        cf.Rating,
        cf.FeedbackText,
        cf.ResponseText,
        cf.FeedbackDate,
        cf.ResponseDate,
        CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
        KEY_TBL.RANK AS SearchRank
    FROM CONTAINSTABLE(CustomerFeedback, (FeedbackText, ResponseText), @SearchTerms) AS KEY_TBL
    INNER JOIN CustomerFeedback cf ON cf.FeedbackID = KEY_TBL.[KEY]
    INNER JOIN Users u ON cf.CustomerID = u.UserID
    INNER JOIN Rentals r ON cf.RentalID = r.RentalID
    INNER JOIN Vehicles v ON r.VehicleID = v.VehicleID
    INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
    INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
    WHERE cf.Rating BETWEEN @MinRating AND @MaxRating
    ORDER BY KEY_TBL.RANK DESC, cf.FeedbackDate DESC;
END;
GO

-- ================================================
-- 3. ADVANCED SEARCH PROCEDURES
-- ================================================

-- Advanced vehicle search with multiple criteria
CREATE PROCEDURE sp_AdvancedVehicleSearch
    @SearchText NVARCHAR(255) = NULL,
    @CategoryID INT = NULL,
    @BrandID INT = NULL,
    @MinDailyRate DECIMAL(10,2) = NULL,
    @MaxDailyRate DECIMAL(10,2) = NULL,
    @FuelType NVARCHAR(30) = NULL,
    @TransmissionType NVARCHAR(30) = NULL,
    @MinSeatingCapacity INT = NULL,
    @LocationID INT = NULL,
    @AvailableOnly BIT = 1,
    @MaxResults INT = 50
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Use CTE for better performance
    WITH SearchResults AS (
        SELECT 
            v.VehicleID,
            v.VehicleRegistration,
            CONCAT(vb.BrandName, ' ', vm.ModelName) AS VehicleName,
            vc.CategoryName,
            v.Color,
            v.DailyRentalRate,
            l.LocationName,
            c.CityName,
            v.VehicleDescription,
            vs.StatusName,
            vm.FuelType,
            vm.TransmissionType,
            vm.SeatingCapacity,
            CASE 
                WHEN @SearchText IS NOT NULL 
                THEN ISNULL(KEY_TBL.RANK, 0)
                ELSE 100 -- Default rank when no text search
            END AS SearchRank
        FROM Vehicles v
        INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
        INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
        INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
        INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
        INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
        INNER JOIN Cities c ON l.CityID = c.CityID
        LEFT JOIN CONTAINSTABLE(Vehicles, VehicleDescription, @SearchText) AS KEY_TBL 
            ON v.VehicleID = KEY_TBL.[KEY]
        WHERE 
            (@SearchText IS NULL OR KEY_TBL.[KEY] IS NOT NULL)
            AND (@CategoryID IS NULL OR vm.CategoryID = @CategoryID)
            AND (@BrandID IS NULL OR vm.BrandID = @BrandID)
            AND (@MinDailyRate IS NULL OR v.DailyRentalRate >= @MinDailyRate)
            AND (@MaxDailyRate IS NULL OR v.DailyRentalRate <= @MaxDailyRate)
            AND (@FuelType IS NULL OR vm.FuelType = @FuelType)
            AND (@TransmissionType IS NULL OR vm.TransmissionType = @TransmissionType)
            AND (@MinSeatingCapacity IS NULL OR vm.SeatingCapacity >= @MinSeatingCapacity)
            AND (@LocationID IS NULL OR v.CurrentLocationID = @LocationID)
            AND (@AvailableOnly = 0 OR vs.IsAvailableForRent = 1)
    )
    SELECT TOP (@MaxResults) *
    FROM SearchResults
    ORDER BY SearchRank DESC, DailyRentalRate ASC;
END;
GO

-- ================================================
-- 4. SEARCH ANALYTICS PROCEDURES
-- ================================================

-- Procedure to track popular search terms
CREATE PROCEDURE sp_LogSearchActivity
    @SearchType NVARCHAR(50),
    @SearchTerms NVARCHAR(255),
    @ResultCount INT,
    @UserID INT = NULL
AS
BEGIN
    SET NOCOUNT ON;
    
    INSERT INTO SystemLogs (TableName, OperationType, NewValues, UserID)
    VALUES (
        'SearchActivity',
        @SearchType,
        CONCAT('Search: "', @SearchTerms, '" returned ', @ResultCount, ' results'),
        @UserID
    );
END;
GO

-- Procedure to get search analytics
CREATE PROCEDURE sp_GetSearchAnalytics
    @StartDate DATE = NULL,
    @EndDate DATE = NULL,
    @TopN INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    
    IF @StartDate IS NULL SET @StartDate = DATEADD(DAY, -30, GETDATE());
    IF @EndDate IS NULL SET @EndDate = GETDATE();
    
    -- Most popular search terms
    SELECT TOP (@TopN)
        SUBSTRING(NewValues, CHARINDEX('"', NewValues) + 1, 
                 CHARINDEX('" returned', NewValues) - CHARINDEX('"', NewValues) - 1) AS SearchTerm,
        COUNT(*) AS SearchCount,
        AVG(CAST(SUBSTRING(NewValues, 
                          CHARINDEX('returned ', NewValues) + 9,
                          CHARINDEX(' results', NewValues) - CHARINDEX('returned ', NewValues) - 9) 
                 AS INT)) AS AvgResults
    FROM SystemLogs
    WHERE TableName = 'SearchActivity'
    AND LogDate BETWEEN @StartDate AND @EndDate
    AND NewValues LIKE 'Search: "%'
    GROUP BY SUBSTRING(NewValues, CHARINDEX('"', NewValues) + 1, 
                      CHARINDEX('" returned', NewValues) - CHARINDEX('"', NewValues) - 1)
    ORDER BY COUNT(*) DESC;
    
    -- Search activity by type
    SELECT 
        OperationType AS SearchType,
        COUNT(*) AS SearchCount,
        COUNT(DISTINCT UserID) AS UniqueUsers
    FROM SystemLogs
    WHERE TableName = 'SearchActivity'
    AND LogDate BETWEEN @StartDate AND @EndDate
    GROUP BY OperationType
    ORDER BY COUNT(*) DESC;
END;
GO

-- ================================================
-- 5. SAMPLE SEARCH QUERIES AND TESTING
-- ================================================

-- Test full-text search functionality
PRINT 'Testing full-text search functionality...';

-- Search for hybrid vehicles
EXEC sp_SearchVehiclesByDescription @SearchTerms = 'hybrid OR fuel-efficient';

-- Search for luxury features
EXEC sp_SearchVehiclesByDescription @SearchTerms = 'luxury OR premium OR advanced';

-- Search customer feedback for service quality
EXEC sp_SearchCustomerFeedback @SearchTerms = 'excellent OR outstanding OR professional';

-- Advanced search example
EXEC sp_AdvancedVehicleSearch 
    @SearchText = 'hybrid fuel-efficient',
    @CategoryID = NULL,
    @MinDailyRate = 20.00,
    @MaxDailyRate = 50.00,
    @AvailableOnly = 1;

PRINT 'Full-text search testing completed';

-- ================================================
-- 6. SEARCH OPTIMIZATION VIEWS
-- ================================================

-- View for search-optimized vehicle listings
CREATE VIEW vw_SearchableVehicles AS
SELECT 
    v.VehicleID,
    v.VehicleRegistration,
    vb.BrandName,
    vm.ModelName,
    CONCAT(vb.BrandName, ' ', vm.ModelName) AS FullVehicleName,
    vc.CategoryName,
    v.Color,
    v.DailyRentalRate,
    l.LocationName,
    c.CityName,
    v.VehicleDescription,
    vm.FuelType,
    vm.TransmissionType,
    vm.SeatingCapacity,
    vm.ManufactureYear,
    vs.StatusName,
    vs.IsAvailableForRent,
    -- Create searchable text field combining multiple attributes
    CONCAT(
        vb.BrandName, ' ',
        vm.ModelName, ' ',
        vc.CategoryName, ' ',
        v.Color, ' ',
        vm.FuelType, ' ',
        vm.TransmissionType, ' ',
        CAST(vm.SeatingCapacity AS NVARCHAR), ' seater ',
        CAST(vm.ManufactureYear AS NVARCHAR), ' ',
        l.LocationName, ' ',
        c.CityName, ' ',
        ISNULL(v.VehicleDescription, '')
    ) AS SearchableText
FROM Vehicles v
INNER JOIN VehicleModels vm ON v.ModelID = vm.ModelID
INNER JOIN VehicleBrands vb ON vm.BrandID = vb.BrandID
INNER JOIN VehicleCategories vc ON vm.CategoryID = vc.CategoryID
INNER JOIN VehicleStatus vs ON v.CurrentStatusID = vs.StatusID
INNER JOIN Locations l ON v.CurrentLocationID = l.LocationID
INNER JOIN Cities c ON l.CityID = c.CityID;
GO

-- ================================================
-- 7. AUTO-COMPLETE SEARCH SUGGESTIONS
-- ================================================

-- Procedure to get search suggestions (auto-complete)
CREATE PROCEDURE sp_GetSearchSuggestions
    @Prefix NVARCHAR(50),
    @MaxSuggestions INT = 10
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Get brand suggestions
    SELECT TOP (@MaxSuggestions)
        'Brand' AS SuggestionType,
        BrandName AS Suggestion,
        COUNT(*) AS Frequency
    FROM VehicleBrands vb
    INNER JOIN VehicleModels vm ON vb.BrandID = vm.BrandID
    INNER JOIN Vehicles v ON vm.ModelID = v.ModelID
    WHERE vb.BrandName LIKE @Prefix + '%'
    GROUP BY BrandName
    ORDER BY COUNT(*) DESC, BrandName
    
    UNION ALL
    
    -- Get model suggestions
    SELECT TOP (@MaxSuggestions)
        'Model' AS SuggestionType,
        ModelName AS Suggestion,
        COUNT(*) AS Frequency
    FROM VehicleModels vm
    INNER JOIN Vehicles v ON vm.ModelID = v.ModelID
    WHERE vm.ModelName LIKE @Prefix + '%'
    GROUP BY ModelName
    ORDER BY COUNT(*) DESC, ModelName
    
    UNION ALL
    
    -- Get category suggestions
    SELECT TOP (@MaxSuggestions)
        'Category' AS SuggestionType,
        CategoryName AS Suggestion,
        COUNT(*) AS Frequency
    FROM VehicleCategories vc
    INNER JOIN VehicleModels vm ON vc.CategoryID = vm.CategoryID
    INNER JOIN Vehicles v ON vm.ModelID = v.ModelID
    WHERE vc.CategoryName LIKE @Prefix + '%'
    GROUP BY CategoryName
    ORDER BY COUNT(*) DESC, CategoryName
    
    ORDER BY Frequency DESC, Suggestion;
END;
GO

-- ================================================
-- 8. SEARCH PERFORMANCE MONITORING
-- ================================================

-- Procedure to monitor search performance
CREATE PROCEDURE sp_MonitorSearchPerformance
AS
BEGIN
    SET NOCOUNT ON;
    
    -- Check full-text catalog status
    SELECT 
        name AS CatalogName,
        is_default,
        is_accent_sensitivity_on,
        FULLTEXTCATALOGPROPERTY(name, 'PopulateStatus') AS PopulateStatus,
        FULLTEXTCATALOGPROPERTY(name, 'ItemCount') AS ItemCount,
        FULLTEXTCATALOGPROPERTY(name, 'IndexSize') AS IndexSizeMB
    FROM sys.fulltext_catalogs;
    
    -- Check full-text index status
    SELECT 
        OBJECT_NAME(object_id) AS TableName,
        FULLTEXTINDEXPROPERTY(object_id, 'PopulateStatus') AS PopulateStatus,
        FULLTEXTINDEXPROPERTY(object_id, 'DocCount') AS DocCount,
        FULLTEXTINDEXPROPERTY(object_id, 'ItemCount') AS ItemCount
    FROM sys.fulltext_indexes;
    
    -- Search performance statistics
    SELECT 
        'Search Performance' AS MetricType,
        COUNT(*) AS TotalSearches,
        COUNT(DISTINCT UserID) AS UniqueUsers,
        AVG(CAST(SUBSTRING(NewValues, 
                          CHARINDEX('returned ', NewValues) + 9,
                          CHARINDEX(' results', NewValues) - CHARINDEX('returned ', NewValues) - 9) 
                 AS INT)) AS AvgResultsPerSearch
    FROM SystemLogs
    WHERE TableName = 'SearchActivity'
    AND LogDate >= DATEADD(DAY, -7, GETDATE());
END;
GO

PRINT '================================================';
PRINT 'FULL-TEXT SEARCH IMPLEMENTATION COMPLETED';
PRINT '================================================';
PRINT '';
PRINT 'Features implemented:';
PRINT '✓ Full-text catalog and indexes';
PRINT '✓ Vehicle description search';
PRINT '✓ Customer feedback search';
PRINT '✓ Advanced multi-criteria search';
PRINT '✓ Search analytics and logging';
PRINT '✓ Auto-complete suggestions';
PRINT '✓ Performance monitoring';
PRINT '';
PRINT 'Usage examples:';
PRINT '- Search vehicles: EXEC sp_SearchVehiclesByDescription ''hybrid luxury''';
PRINT '- Search feedback: EXEC sp_SearchCustomerFeedback ''excellent service''';
PRINT '- Advanced search: EXEC sp_AdvancedVehicleSearch @SearchText=''SUV'', @MaxDailyRate=100';
PRINT '- Get suggestions: EXEC sp_GetSearchSuggestions ''Toy''';
PRINT '';
PRINT 'Full-text search is now ready for use!';
