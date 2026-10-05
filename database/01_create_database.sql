-- ============================================================================
-- Script: 01_create_database.sql
-- Project: MelodyMart Web-Based Music Store
-- DBMS: Microsoft SQL Server
-- Description: Creates the MelodyMartDB database.
-- ============================================================================

USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'MelodyMartDB')
BEGIN
    ALTER DATABASE MelodyMartDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE MelodyMartDB;
END
GO

CREATE DATABASE MelodyMartDB;
GO

USE MelodyMartDB;
GO
