-- ============================================================
-- MelodyMart Playlist Tables Migration
-- File: 05_playlist.sql
-- Run ONLY if PLAYLIST and PLAYLIST_ITEM do not yet exist.
-- DO NOT DROP or TRUNCATE any existing tables.
-- ============================================================

-- ─── PLAYLIST table ────────────────────────────────────────
IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'PLAYLIST' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE dbo.[PLAYLIST] (
        PlaylistID       INT IDENTITY(1,1) NOT NULL,
        ListenerID       INT               NOT NULL,
        PlaylistName     NVARCHAR(100)     NOT NULL,
        CreatedDate      DATETIME2         NOT NULL DEFAULT GETDATE(),
        LastUpdatedDate  DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_PLAYLIST PRIMARY KEY (PlaylistID),
        CONSTRAINT FK_PLAYLIST_LISTENER FOREIGN KEY (ListenerID)
            REFERENCES dbo.[LISTENER](ListenerID)
            ON DELETE CASCADE
            ON UPDATE CASCADE
    );
    PRINT 'PLAYLIST table created successfully.';
END
ELSE
BEGIN
    PRINT 'PLAYLIST table already exists - skipped creation.';
END;

-- ─── PLAYLIST_ITEM table ─────────────────────────────────────
IF NOT EXISTS (
    SELECT 1 FROM sys.tables WHERE name = 'PLAYLIST_ITEM' AND schema_id = SCHEMA_ID('dbo')
)
BEGIN
    CREATE TABLE dbo.[PLAYLIST_ITEM] (
        PlaylistItemID  INT IDENTITY(1,1) NOT NULL,
        PlaylistID      INT               NOT NULL,
        TrackID         INT               NULL,
        AlbumID         INT               NULL,
        AddedDate       DATETIME2         NOT NULL DEFAULT GETDATE(),
        CONSTRAINT PK_PLAYLIST_ITEM PRIMARY KEY (PlaylistItemID),
        CONSTRAINT FK_PLAYLIST_ITEM_PLAYLIST FOREIGN KEY (PlaylistID)
            REFERENCES dbo.[PLAYLIST](PlaylistID)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
        CONSTRAINT FK_PLAYLIST_ITEM_TRACK FOREIGN KEY (TrackID)
            REFERENCES dbo.[TRACK](TrackID)
            ON DELETE CASCADE
            ON UPDATE NO ACTION,
        -- NO ACTION for Album to avoid multiple cascade paths
        CONSTRAINT FK_PLAYLIST_ITEM_ALBUM FOREIGN KEY (AlbumID)
            REFERENCES dbo.[ALBUM](AlbumID)
            ON DELETE NO ACTION
            ON UPDATE NO ACTION,
        -- Exactly ONE of TrackID or AlbumID must be set
        CONSTRAINT CHK_PLAYLIST_ITEM_ONE_TARGET CHECK (
            (TrackID IS NOT NULL AND AlbumID IS NULL)
            OR
            (TrackID IS NULL AND AlbumID IS NOT NULL)
        )
    );
    PRINT 'PLAYLIST_ITEM table created successfully.';
END
ELSE
BEGIN
    PRINT 'PLAYLIST_ITEM table already exists - skipped creation.';
END;

PRINT 'Playlist migration complete.';
