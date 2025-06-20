-- ================================================
-- DATABASE EXECUTION MASTER SCRIPT
-- Run this script to create the complete database
-- ================================================

-- Step 1: Create Database
PRINT 'Step 1: Creating Database...';
:r "01_CREATE_DATABASE.sql"

-- Step 2: Create Tables
PRINT 'Step 2: Creating Tables...';
:r "02_CREATE_TABLES.sql"

-- Step 3: Create Indexes
PRINT 'Step 3: Creating Indexes...';
:r "03_CREATE_INDEXES.sql"

-- Step 4: Create Views
PRINT 'Step 4: Creating Views...';
:r "04_CREATE_VIEWS.sql"

-- Step 5: Create Stored Procedures
PRINT 'Step 5: Creating Stored Procedures...';
:r "05_CREATE_PROCEDURES.sql"

-- Step 6: Create Triggers
PRINT 'Step 6: Creating Triggers...';
:r "06_CREATE_TRIGGERS.sql"

-- Step 7: Create Functions
PRINT 'Step 7: Creating User-Defined Functions...';
:r "07_CREATE_FUNCTIONS.sql"

-- Step 8: Setup Security
PRINT 'Step 8: Setting up Security and Encryption...';
:r "08_SECURITY_SETUP.sql"

-- Step 9: Insert Sample Data
PRINT 'Step 9: Inserting Sample Data...';
:r "09_INSERT_SAMPLE_DATA.sql"

-- Step 10: Setup Automation Jobs
PRINT 'Step 10: Setting up Automation Jobs...';
:r "10_AUTOMATION_JOBS.sql"

-- Step 11: Setup Full-Text Search
PRINT 'Step 11: Setting up Full-Text Search...';
:r "11_FULLTEXT_SEARCH.sql"

PRINT '';
PRINT '================================================';
PRINT 'DATABASE SETUP COMPLETED SUCCESSFULLY!';
PRINT '================================================';
PRINT '';
PRINT 'Database Features Summary:';
PRINT '✓ 20 normalized tables with proper relationships';
PRINT '✓ 25+ indexes (clustered, non-clustered, composite, unique)';
PRINT '✓ 12+ views for abstraction and reporting';
PRINT '✓ 10+ stored procedures for business logic';
PRINT '✓ 12+ triggers for automatic rule enforcement';
PRINT '✓ 14+ user-defined functions (scalar and table-valued)';
PRINT '✓ Row-level security implementation';
PRINT '✓ Column-level encryption for sensitive data';
PRINT '✓ Role-based access control (RBAC)';
PRINT '✓ Full-text search capabilities';
PRINT '✓ Automated backup and maintenance jobs';
PRINT '✓ Comprehensive audit logging';
PRINT '✓ Sample data for testing';
PRINT '';
PRINT 'Ready for JavaFX application integration!';
