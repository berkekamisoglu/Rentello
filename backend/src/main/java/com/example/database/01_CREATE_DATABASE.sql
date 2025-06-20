-- ================================================
-- Car Rental Management System Database
-- Advanced Database Systems Project
-- ================================================

USE master;
GO

-- Drop database if exists
IF EXISTS (SELECT name FROM sys.databases WHERE name = 'CarRentalDB')
BEGIN
    ALTER DATABASE CarRentalDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE CarRentalDB;
END
GO

-- Create database
CREATE DATABASE CarRentalDB
ON 
( NAME = 'CarRentalDB_Data',
  FILENAME = 'C:\Database\CarRentalDB_Data.mdf',
  SIZE = 500MB,
  MAXSIZE = 10GB,
  FILEGROWTH = 50MB )
LOG ON 
( NAME = 'CarRentalDB_Log',
  FILENAME = 'C:\Database\CarRentalDB_Log.ldf',
  SIZE = 100MB,
  MAXSIZE = 2GB,
  FILEGROWTH = 10MB );
GO

USE CarRentalDB;
GO

-- Enable Full-Text Search
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'CarRentalCatalog')
BEGIN
    CREATE FULLTEXT CATALOG CarRentalCatalog AS DEFAULT;
END
GO

PRINT 'Database CarRentalDB created successfully!';
