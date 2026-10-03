-- ============================================================================
-- Script: 06_srilanka_sample_data.sql
-- Project: MelodyMart Web-Based Music Store
-- Description: Replaces ALL existing demo/sample data with realistic
--              Sri Lankan-themed fictional data for university presentation.
-- IMPORTANT:
--   - DO NOT drop or alter any tables, constraints, columns, or schema.
--   - spring.jpa.hibernate.ddl-auto=none is preserved.
--   - All phone numbers are EXACTLY 10 digits in 07XXXXXXXX format.
--   - Passwords: the app accepts "password123" for all demo accounts.
--   - Delete child records before parents (FK dependency order).
--   - Insert parents before children.
-- ============================================================================

USE MelodyMartDB;
GO

-- ============================================================================
-- SAFETY CHECK: Confirm we are on MelodyMartDB
-- ============================================================================
IF DB_NAME() <> 'MelodyMartDB'
BEGIN
    RAISERROR('Wrong database! Script aborted. Expected MelodyMartDB.', 16, 1);
    RETURN;
END
GO

PRINT '=== MelodyMart Sri Lankan Data Replacement Starting ===';
PRINT 'Database confirmed: ' + DB_NAME();
GO

BEGIN TRY
    BEGIN TRANSACTION;

    -- ========================================================================
    -- STEP 1: DELETE ALL EXISTING DATA (Child to Parent FK order)
    -- ========================================================================

    PRINT 'Step 1: Deleting existing data...';

    DELETE FROM dbo.[DOWNLOAD];
    DELETE FROM dbo.[PLAYLIST_ITEM];
    DELETE FROM dbo.[PLAYLIST];
    DELETE FROM dbo.[LIBRARY_ITEMS];
    DELETE FROM dbo.[DIGITAL_LIBRARY];
    DELETE FROM dbo.[REVIEW];
    DELETE FROM dbo.[COMPLAINT];
    DELETE FROM dbo.[ALBUM_PROMOTION];
    DELETE FROM dbo.[PROMOTION];
    DELETE FROM dbo.[FAQ];
    DELETE FROM dbo.[PAYMENT];
    DELETE FROM dbo.[ORDER_ITEM];
    DELETE FROM dbo.[ORDERS];
    DELETE FROM dbo.[CART_ITEM];
    DELETE FROM dbo.[CART];
    DELETE FROM dbo.[TRACK];
    DELETE FROM dbo.[ALBUM];
    DELETE FROM dbo.[Catalog];
    DELETE FROM dbo.[GENRE];
    DELETE FROM dbo.[ARTIST];
    DELETE FROM dbo.[LISTENER];
    DELETE FROM dbo.[ADMINISTRATOR];
    DELETE FROM dbo.[USER];

    PRINT 'Step 1 complete: All existing data deleted.';

    -- ========================================================================
    -- STEP 2: RESET IDENTITY SEEDS
    -- ========================================================================

    DBCC CHECKIDENT('[USER]', RESEED, 0);
    DBCC CHECKIDENT('[ARTIST]', RESEED, 0);
    DBCC CHECKIDENT('[GENRE]', RESEED, 0);
    DBCC CHECKIDENT('[Catalog]', RESEED, 0);
    DBCC CHECKIDENT('[ALBUM]', RESEED, 0);
    DBCC CHECKIDENT('[TRACK]', RESEED, 0);
    DBCC CHECKIDENT('[CART]', RESEED, 0);
    DBCC CHECKIDENT('[ORDERS]', RESEED, 0);
    DBCC CHECKIDENT('[PAYMENT]', RESEED, 0);
    DBCC CHECKIDENT('[PROMOTION]', RESEED, 0);
    DBCC CHECKIDENT('[FAQ]', RESEED, 0);
    DBCC CHECKIDENT('[COMPLAINT]', RESEED, 0);
    DBCC CHECKIDENT('[REVIEW]', RESEED, 0);
    DBCC CHECKIDENT('[DIGITAL_LIBRARY]', RESEED, 0);
    DBCC CHECKIDENT('[LIBRARY_ITEMS]', RESEED, 0);
    DBCC CHECKIDENT('[DOWNLOAD]', RESEED, 0);
    DBCC CHECKIDENT('[PLAYLIST]', RESEED, 0);
    DBCC CHECKIDENT('[PLAYLIST_ITEM]', RESEED, 0);

    PRINT 'Step 2 complete: Identity seeds reset.';

    -- ========================================================================
    -- STEP 3: INSERT USERS (4 admins + 10 listeners = 14 total)
    --   Phone numbers: EXACTLY 10 digits, 07XXXXXXXX format.
    --   The app accepts "password123" for all demo accounts (AuthService.java).
    -- ========================================================================

    PRINT 'Step 3: Inserting users...';

    SET IDENTITY_INSERT dbo.[USER] ON;
    INSERT INTO dbo.[USER] (UserID, FirstName, LastName, Email, PasswordHash, PhoneNumber, RegistrationDate, AccountStatus)
    VALUES
        (1,  N'Nimal',    N'Perera',         N'nimal.perera@melodymart.lk',      N'password123', N'0712345678', '2025-01-05 08:00:00', N'Active'),
        (2,  N'Kasun',    N'Wijeratne',      N'kasun.wijeratne@melodymart.lk',   N'password123', N'0773456789', '2025-01-06 08:30:00', N'Active'),
        (3,  N'Sachini',  N'Fernando',       N'sachini.fernando@melodymart.lk',  N'password123', N'0754567890', '2025-01-07 09:00:00', N'Active'),
        (4,  N'Ruwan',    N'Jayawardena',    N'ruwan.jayawardena@melodymart.lk', N'password123', N'0765678901', '2025-01-08 09:30:00', N'Active'),
        (5,  N'Kavindu',  N'Perera',         N'kavindu.perera@gmail.com',        N'password123', N'0786789012', '2025-01-15 10:00:00', N'Active'),
        (6,  N'Tharushi', N'Fernando',       N'tharushi.fernando@gmail.com',     N'password123', N'0707890123', '2025-01-20 11:30:00', N'Active'),
        (7,  N'Dineth',   N'Jayasinghe',     N'dineth.jayasinghe@gmail.com',     N'password123', N'0711234567', '2025-02-01 12:00:00', N'Active'),
        (8,  N'Sanduni',  N'Silva',          N'sanduni.silva@gmail.com',         N'password123', N'0772345678', '2025-02-10 13:15:00', N'Active'),
        (9,  N'Chamod',   N'Wijesinghe',     N'chamod.wijesinghe@gmail.com',     N'password123', N'0753456789', '2025-02-15 14:00:00', N'Active'),
        (10, N'Hiruni',   N'Gunawardena',    N'hiruni.gunawardena@gmail.com',    N'password123', N'0764567890', '2025-03-01 09:45:00', N'Active'),
        (11, N'Pasindu',  N'Bandara',        N'pasindu.bandara@gmail.com',       N'password123', N'0785678901', '2025-03-10 10:30:00', N'Active'),
        (12, N'Nethmi',   N'Senanayake',     N'nethmi.senanayake@yahoo.com',     N'password123', N'0706789012', '2025-03-20 11:00:00', N'Active'),
        (13, N'Dilan',    N'Karunaratne',    N'dilan.karunaratne@gmail.com',     N'password123', N'0717890123', '2025-04-01 12:30:00', N'Active'),
        (14, N'Isuru',    N'Wickramasinghe', N'isuru.wickramasinghe@gmail.com',  N'password123', N'0778901234', '2025-04-10 14:00:00', N'Active');
    SET IDENTITY_INSERT dbo.[USER] OFF;

    PRINT 'Step 3 complete: 14 users inserted.';

    -- ========================================================================
    -- STEP 4: ADMINISTRATOR
    -- ========================================================================

    PRINT 'Step 4: Inserting administrators...';

    INSERT INTO dbo.[ADMINISTRATOR] (AdminID, AdminRole)
    VALUES
        (1, N'SuperAdmin'),
        (2, N'StoreManager'),
        (3, N'SalesManager'),
        (4, N'CustomerSupport');

    PRINT 'Step 4 complete: 4 administrators inserted.';

    -- ========================================================================
    -- STEP 5: LISTENER
    -- ========================================================================

    PRINT 'Step 5: Inserting listeners...';

    INSERT INTO dbo.[LISTENER] (ListenerID)
    VALUES (5), (6), (7), (8), (9), (10), (11), (12), (13), (14);

    PRINT 'Step 5 complete: 10 listeners inserted.';

    -- ========================================================================
    -- STEP 6: ARTIST (8 Sri Lankan artists)
    -- ========================================================================

    PRINT 'Step 6: Inserting artists...';

    SET IDENTITY_INSERT dbo.[ARTIST] ON;
    INSERT INTO dbo.[ARTIST] (ArtistID, ArtistName, Biography, Country)
    VALUES
        (1, N'Kavindu Jayasuriya',   N'A celebrated Sinhala pop and acoustic singer-songwriter from Colombo, known for his soulful ballads that blend traditional Sri Lankan melodies with modern guitar arrangements. His debut album captivated audiences across the island.', N'Sri Lanka'),
        (2, N'Dinuka Fernando',      N'One of Sri Lanka''s leading Sinhala rock artists, Dinuka Fernando rose to fame with powerful anthems and energetic live performances. His dynamic stage presence and emotionally charged lyrics have won him a devoted fan base.', N'Sri Lanka'),
        (3, N'Sahan Perera',         N'A versatile multi-instrumentalist and music producer from Kandy, Sahan Perera crafts intricate instrumental compositions that blend Sri Lankan classical music with contemporary jazz and fusion influences.', N'Sri Lanka'),
        (4, N'Nethara Silva',        N'A rising star in Sri Lankan indie pop, Nethara Silva is known for her ethereal vocals, dreamy production style, and deeply personal lyrics. Her atmospheric sound draws inspiration from the island''s coastal landscapes.', N'Sri Lanka'),
        (5, N'Ashen Wijesinghe',     N'A pioneer of Sri Lankan Baila music, Ashen Wijesinghe infuses traditional Baila rhythms with contemporary production to create a unique, festive sound. His albums are beloved at cultural celebrations across Sri Lanka.', N'Sri Lanka'),
        (6, N'Tharindu Bandara',     N'A classical virtuoso trained at the Colombo Conservatory of Music, Tharindu Bandara composes orchestral suites and instrumental pieces that celebrate the rich musical heritage of Sri Lanka with modern sensibility.', N'Sri Lanka'),
        (7, N'Mihiranga Senanayake', N'Known as the voice of the south, Mihiranga Senanayake is a beloved Sinhala folk and acoustic artist from Galle. His music reflects the beauty of rural Sri Lanka through heartfelt storytelling and warm acoustic arrangements.', N'Sri Lanka'),
        (8, N'Sachini Jayawardena',  N'A celebrated Tamil pop and classical crossover artist from Jaffna, Sachini Jayawardena bridges cultural divides through music that honours Tamil classical traditions while embracing contemporary Tamil pop production.', N'Sri Lanka');
    SET IDENTITY_INSERT dbo.[ARTIST] OFF;

    PRINT 'Step 6 complete: 8 artists inserted.';

    -- ========================================================================
    -- STEP 7: GENRE (10 Sri Lankan genres)
    -- UQ_GENRE_GenreName enforced — no duplicates allowed.
    -- ========================================================================

    PRINT 'Step 7: Inserting genres...';

    SET IDENTITY_INSERT dbo.[GENRE] ON;
    INSERT INTO dbo.[GENRE] (GenreID, GenreName, Description)
    VALUES
        (1,  N'Sinhala Pop',          N'Contemporary Sinhala-language pop music featuring melodic vocals and modern production.'),
        (2,  N'Sinhala Rock',         N'Energetic Sinhala rock and alternative music with electric guitars and powerful rhythms.'),
        (3,  N'Sinhala Acoustic',     N'Warm fingerstyle acoustic guitar music with heartfelt Sinhala lyrics and folk influences.'),
        (4,  N'Sri Lankan Classical', N'Formal classical compositions drawing on Sri Lankan musical heritage and Western orchestration.'),
        (5,  N'Baila',                N'Traditional Sri Lankan Baila music blending Portuguese-influenced rhythms with local festive culture.'),
        (6,  N'Fusion',               N'Creative genre blending Sri Lankan traditional sounds with jazz, blues, or world music elements.'),
        (7,  N'Tamil Pop',            N'Vibrant Tamil-language pop music with modern beats and melodic vocals rooted in South Asian traditions.'),
        (8,  N'Tamil Classical',      N'Carnatic-influenced classical music from Sri Lanka''s Tamil tradition, featuring violin and veena.'),
        (9,  N'Instrumental',         N'Purely instrumental compositions spanning acoustic, jazz, and orchestral Sri Lankan styles.'),
        (10, N'Lo-fi and Chill',      N'Relaxed lo-fi and ambient music with Sri Lankan influences, perfect for studying and unwinding.');
    SET IDENTITY_INSERT dbo.[GENRE] OFF;

    PRINT 'Step 7 complete: 10 genres inserted.';

    -- ========================================================================
    -- STEP 8: CATALOG (5 Sri Lankan catalogs)
    -- ========================================================================

    PRINT 'Step 8: Inserting catalogs...';

    SET IDENTITY_INSERT dbo.[Catalog] ON;
    INSERT INTO dbo.[Catalog] (CatalogID, CatalogName, Description, Price, CreatedDate)
    VALUES
        (1, N'Sinhala Classics Collection', N'A handpicked bundle of timeless Sinhala pop and acoustic albums celebrating the golden era of Sri Lankan music. Perfect for long drives and lazy evenings.', 24.99, '2025-01-15 09:00:00'),
        (2, N'New Sri Lankan Hits 2025',    N'The freshest Sri Lankan pop and rock releases of 2025, curated for listeners who want to stay on top of the local music scene.', 19.99, '2025-02-01 10:00:00'),
        (3, N'Island Acoustic Collection',  N'A soothing selection of acoustic and folk albums that capture the natural beauty and tranquillity of Sri Lanka''s countryside and coast.', 22.99, '2025-02-15 11:00:00'),
        (4, N'Baila and Fusion Essentials', N'Everything you need for a Sri Lankan celebration. The finest Baila, fusion, and world music albums guaranteed to get you dancing.', 17.99, '2025-03-01 09:30:00'),
        (5, N'Tamil Melodies Collection',   N'A curated anthology of the finest Tamil pop and classical albums from Sri Lanka, honouring the island''s rich Tamil musical heritage.', 21.99, '2025-03-15 10:00:00');
    SET IDENTITY_INSERT dbo.[Catalog] OFF;

    PRINT 'Step 8 complete: 5 catalogs inserted.';

    -- ========================================================================
    -- STEP 9: ALBUM (12 albums)
    -- CatalogID assignments used for library item mapping:
    --   Catalog 1: Albums 1, 2
    --   Catalog 2: Albums 3, 4, 8
    --   Catalog 3: Albums 5, 6, 11
    --   Catalog 4: Album 9
    --   Catalog 5: Album 12
    --   No catalog: Albums 7, 10
    -- ========================================================================

    PRINT 'Step 9: Inserting albums...';

    SET IDENTITY_INSERT dbo.[ALBUM] ON;
    INSERT INTO dbo.[ALBUM] (AlbumID, AlbumTitle, ReleaseDate, AlbumStatus, Price, Description, CoverImageURL, ArtistID, GenreID, CatalogID)
    VALUES
        (1,  N'Sanda Eliya',           '2024-03-10', N'Available',  9.99,
             N'A deeply personal debut album from Kavindu Jayasuriya. Blending acoustic guitar melodies with intimate Sinhala lyrics, Sanda Eliya captures the quiet beauty of moonlit nights in Sri Lanka.',
             N'/assets/covers/sanda_eliya.jpg',       1, 1, 1),
        (2,  N'Hithata Ahimi',         '2024-09-15', N'Available', 11.99,
             N'Kavindu''s sophomore release explores themes of longing and hope. Rich acoustic arrangements and warm vocal harmonies make this an essential Sinhala pop collection.',
             N'/assets/covers/hithata_ahimi.jpg',     1, 1, 1),
        (3,  N'Colombo After Rain',    '2024-05-20', N'Available', 12.99,
             N'Dinuka Fernando''s powerful Sinhala rock album inspired by the moody atmosphere of Colombo after monsoon rain. Crunchy guitar riffs and anthemic choruses define this electric collection.',
             N'/assets/covers/colombo_after_rain.jpg',2, 2, 2),
        (4,  N'Raata Pahana',          '2024-11-08', N'Available', 13.99,
             N'A bold rock journey through the Sri Lankan night. Raata Pahana features Dinuka Fernando''s most ambitious guitar work and introspective lyrics about identity and belonging.',
             N'/assets/covers/raata_pahana.jpg',      2, 2, 2),
        (5,  N'Kandyan Skies',         '2024-07-12', N'Available',  8.99,
             N'A meditative instrumental album inspired by the misty hill country of Kandy. Sahan Perera weaves together classical strings, flute, and subtle percussion in a beautifully layered soundscape.',
             N'/assets/covers/kandyan_skies.jpg',     3, 9, 3),
        (6,  N'Island Echoes',         '2024-12-01', N'Available', 10.99,
             N'Sahan Perera''s fusion masterpiece that bridges Sri Lankan classical tradition with jazz improvisation. A cinematic and meditative listening experience.',
             N'/assets/covers/island_echoes.jpg',     3, 6, 3),
        (7,  N'Ocean Road',            '2025-01-18', N'Available', 11.99,
             N'Nethara Silva''s evocative debut featuring dreamy lo-fi production and ethereal vocals. Each track feels like a gentle ocean breeze drifting through the palm trees of Sri Lanka''s southern coast.',
             N'/assets/covers/ocean_road.jpg',        4, 10, NULL),
        (8,  N'Tropical Memories',     '2025-03-05', N'Available',  9.99,
             N'A warm and hazy collection of indie pop tracks that celebrate summer afternoons and island nostalgia. Nethara Silva''s most radio-friendly and accessible album to date.',
             N'/assets/covers/tropical_memories.jpg', 4, 1, 2),
        (9,  N'Baila Night in Colombo','2024-06-21', N'Available', 10.99,
             N'The ultimate Baila party album by Ashen Wijesinghe. Infectious rhythms, vibrant brass, and joyful lyrics make this the go-to album for every Sri Lankan celebration.',
             N'/assets/covers/baila_night.jpg',       5, 5, 4),
        (10, N'City Lights',           '2024-08-30', N'Available', 14.99,
             N'Tharindu Bandara''s orchestral tribute to the bright lights and busy streets of Colombo. Sweeping string arrangements and grand piano compositions paint a vivid musical portrait of the city.',
             N'/assets/covers/city_lights.jpg',       6, 4, NULL),
        (11, N'Southern Breeze',       '2024-10-15', N'Available',  8.99,
             N'A tender acoustic collection from Mihiranga Senanayake, drawing on the folk traditions of Sri Lanka''s southern province. Simple, beautiful, and deeply authentic.',
             N'/assets/covers/southern_breeze.jpg',   7, 3, 3),
        (12, N'Yarl Isai',             '2025-02-14', N'Available', 12.99,
             N'Sachini Jayawardena''s magnificent Tamil classical crossover album. Rooted in Carnatic tradition and enriched with contemporary Tamil pop production, Yarl Isai is a landmark Sri Lankan album.',
             N'/assets/covers/yarl_isai.jpg',         8, 7, 5);
    SET IDENTITY_INSERT dbo.[ALBUM] OFF;

    PRINT 'Step 9 complete: 12 albums inserted.';

    -- ========================================================================
    -- STEP 10: TRACK (4 tracks per album = 48 total)
    -- Duration in seconds. UQ_TRACK_AlbumTrackNumber enforced.
    -- ========================================================================

    PRINT 'Step 10: Inserting tracks...';

    SET IDENTITY_INSERT dbo.[TRACK] ON;
    INSERT INTO dbo.[TRACK] (TrackID, AlbumID, TrackTitle, TrackNumber, Duration, AudioFileURL)
    VALUES
        -- Album 1: Sanda Eliya
        (1,  1, N'Sanda Eliya',            1, 222, N'/audio/album1/01_sanda_eliya.mp3'),
        (2,  1, N'Nuba Nisa',              2, 255, N'/audio/album1/02_nuba_nisa.mp3'),
        (3,  1, N'Oba Wage Dawasak',       3, 241, N'/audio/album1/03_oba_wage_dawasak.mp3'),
        (4,  1, N'Aadaraya Namak',         4, 198, N'/audio/album1/04_aadaraya_namak.mp3'),
        -- Album 2: Hithata Ahimi
        (5,  2, N'Hithata Ahimi',          1, 247, N'/audio/album2/01_hithata_ahimi.mp3'),
        (6,  2, N'Pem Sewana',             2, 219, N'/audio/album2/02_pem_sewana.mp3'),
        (7,  2, N'Diya Danuna',            3, 263, N'/audio/album2/03_diya_danuna.mp3'),
        (8,  2, N'Mal Pohoye',             4, 235, N'/audio/album2/04_mal_pohoye.mp3'),
        -- Album 3: Colombo After Rain
        (9,  3, N'Colombo After Rain',     1, 251, N'/audio/album3/01_colombo_after_rain.mp3'),
        (10, 3, N'Ratu Mal Piduru',        2, 278, N'/audio/album3/02_ratu_mal_piduru.mp3'),
        (11, 3, N'Kinissa Mama',           3, 313, N'/audio/album3/03_kinissa_mama.mp3'),
        (12, 3, N'Midnight Colombo',       4, 232, N'/audio/album3/04_midnight_colombo.mp3'),
        -- Album 4: Raata Pahana
        (13, 4, N'Raata Pahana',           1, 268, N'/audio/album4/01_raata_pahana.mp3'),
        (14, 4, N'Eka Dasa Diwiya',        2, 295, N'/audio/album4/02_eka_dasa_diwiya.mp3'),
        (15, 4, N'Seethalaya Andura',      3, 241, N'/audio/album4/03_seethalaya_andura.mp3'),
        (16, 4, N'Sulanga Wela',           4, 257, N'/audio/album4/04_sulanga_wela.mp3'),
        -- Album 5: Kandyan Skies
        (17, 5, N'Mahanuwara Uyana',       1, 301, N'/audio/album5/01_mahanuwara_uyana.mp3'),
        (18, 5, N'Kirala Nada',            2, 275, N'/audio/album5/02_kirala_nada.mp3'),
        (19, 5, N'Hill Country Dawn',      3, 338, N'/audio/album5/03_hill_country_dawn.mp3'),
        (20, 5, N'Temple Bells',           4, 289, N'/audio/album5/04_temple_bells.mp3'),
        -- Album 6: Island Echoes
        (21, 6, N'Island Echoes',          1, 312, N'/audio/album6/01_island_echoes.mp3'),
        (22, 6, N'Colombo Jazz Walk',      2, 267, N'/audio/album6/02_colombo_jazz_walk.mp3'),
        (23, 6, N'Galle Fort Sunset',      3, 343, N'/audio/album6/03_galle_fort_sunset.mp3'),
        (24, 6, N'Tropical Storm',         4, 281, N'/audio/album6/04_tropical_storm.mp3'),
        -- Album 7: Ocean Road
        (25, 7, N'Ocean Road',             1, 214, N'/audio/album7/01_ocean_road.mp3'),
        (26, 7, N'Palm Trees and Rain',    2, 239, N'/audio/album7/02_palm_trees_rain.mp3'),
        (27, 7, N'Dusk at Mirissa',        3, 256, N'/audio/album7/03_dusk_at_mirissa.mp3'),
        (28, 7, N'Blue Water Blues',       4, 228, N'/audio/album7/04_blue_water_blues.mp3'),
        -- Album 8: Tropical Memories
        (29, 8, N'Tropical Memories',      1, 203, N'/audio/album8/01_tropical_memories.mp3'),
        (30, 8, N'Summer in Weligama',     2, 231, N'/audio/album8/02_summer_in_weligama.mp3'),
        (31, 8, N'Firefly Evenings',       3, 247, N'/audio/album8/03_firefly_evenings.mp3'),
        (32, 8, N'Island Sunshine',        4, 219, N'/audio/album8/04_island_sunshine.mp3'),
        -- Album 9: Baila Night in Colombo
        (33, 9, N'Baila Night in Colombo', 1, 193, N'/audio/album9/01_baila_night.mp3'),
        (34, 9, N'Hora Party',             2, 218, N'/audio/album9/02_hora_party.mp3'),
        (35, 9, N'Avurudu Gee',            3, 205, N'/audio/album9/03_avurudu_gee.mp3'),
        (36, 9, N'Narthana Maduwaka',      4, 197, N'/audio/album9/04_narthana_maduwaka.mp3'),
        -- Album 10: City Lights
        (37, 10, N'City Lights',           1, 376, N'/audio/album10/01_city_lights.mp3'),
        (38, 10, N'Pettah Street',         2, 421, N'/audio/album10/02_pettah_street.mp3'),
        (39, 10, N'Galle Road Nocturne',   3, 358, N'/audio/album10/03_galle_road_nocturne.mp3'),
        (40, 10, N'Dawn Over Colombo',     4, 342, N'/audio/album10/04_dawn_over_colombo.mp3'),
        -- Album 11: Southern Breeze
        (41, 11, N'Southern Breeze',       1, 211, N'/audio/album11/01_southern_breeze.mp3'),
        (42, 11, N'Galu Korale',           2, 234, N'/audio/album11/02_galu_korale.mp3'),
        (43, 11, N'Koha Pata',             3, 248, N'/audio/album11/03_koha_pata.mp3'),
        (44, 11, N'Thambapanni Gee',       4, 226, N'/audio/album11/04_thambapanni_gee.mp3'),
        -- Album 12: Yarl Isai
        (45, 12, N'Yarl Isai',             1, 271, N'/audio/album12/01_yarl_isai.mp3'),
        (46, 12, N'Nila Kuyil',            2, 304, N'/audio/album12/02_nila_kuyil.mp3'),
        (47, 12, N'Vaanam Paarthen',       3, 289, N'/audio/album12/03_vaanam_paarthen.mp3'),
        (48, 12, N'Kadal Alai',            4, 261, N'/audio/album12/04_kadal_alai.mp3');
    SET IDENTITY_INSERT dbo.[TRACK] OFF;

    PRINT 'Step 10 complete: 48 tracks inserted.';

    -- ========================================================================
    -- STEP 11: CART (1 per listener, UNIQUE ListenerID)
    -- ========================================================================

    PRINT 'Step 11: Inserting carts...';

    SET IDENTITY_INSERT dbo.[CART] ON;
    INSERT INTO dbo.[CART] (CartID, ListenerID, CreatedDate, LastUpdatedDate, CartStatus)
    VALUES
        (1,  5,  '2025-01-15 10:10:00', '2025-06-10 09:00:00', N'Active'),
        (2,  6,  '2025-01-20 11:40:00', '2025-06-12 14:30:00', N'Active'),
        (3,  7,  '2025-02-01 12:15:00', '2025-06-15 11:20:00', N'Active'),
        (4,  8,  '2025-02-10 13:30:00', '2025-06-18 16:45:00', N'Active'),
        (5,  9,  '2025-02-15 14:10:00', '2025-04-01 18:00:00', N'Converted'),
        (6,  10, '2025-03-01 09:50:00', '2025-06-20 10:15:00', N'Active'),
        (7,  11, '2025-03-10 10:40:00', '2025-06-22 17:10:00', N'Active'),
        (8,  12, '2025-03-20 11:05:00', '2025-06-25 12:00:00', N'Active'),
        (9,  13, '2025-04-01 12:40:00', '2025-06-01 09:30:00', N'Converted'),
        (10, 14, '2025-04-10 14:10:00', '2025-06-28 15:45:00', N'Active');
    SET IDENTITY_INSERT dbo.[CART] OFF;

    PRINT 'Step 11 complete: 10 carts inserted.';

    -- ========================================================================
    -- STEP 12: CART_ITEM (XOR: AlbumID XOR CatalogID)
    -- ========================================================================

    PRINT 'Step 12: Inserting cart items...';

    INSERT INTO dbo.[CART_ITEM] (CartID, ItemNo, AlbumID, CatalogID, Quantity, UnitPrice, AddedDate)
    VALUES
        (1,  1, 7,    NULL, 1, 11.99, '2025-06-10 09:00:00'),
        (1,  2, NULL, 2,    1, 19.99, '2025-06-10 09:05:00'),
        (2,  1, 3,    NULL, 1, 12.99, '2025-06-12 14:30:00'),
        (3,  1, NULL, 3,    1, 22.99, '2025-06-15 11:20:00'),
        (4,  1, 10,   NULL, 1, 14.99, '2025-06-18 16:45:00'),
        (6,  1, 4,    NULL, 1, 13.99, '2025-06-20 10:15:00'),
        (7,  1, NULL, 4,    1, 17.99, '2025-06-22 17:10:00'),
        (8,  1, 12,   NULL, 1, 12.99, '2025-06-25 12:00:00'),
        (10, 1, 8,    NULL, 1,  9.99, '2025-06-28 15:45:00');

    PRINT 'Step 12 complete: 9 cart items inserted.';

    -- ========================================================================
    -- STEP 13: ORDERS (18 orders, varied listeners and statuses)
    -- OrderStatus: Pending | Processing | Completed | Cancelled | Refunded
    -- Only Completed orders get library access.
    -- ========================================================================

    PRINT 'Step 13: Inserting orders...';

    SET IDENTITY_INSERT dbo.[ORDERS] ON;
    INSERT INTO dbo.[ORDERS] (OrderID, ListenerID, OrderDate, OrderStatus, TotalAmount)
    VALUES
        (1,  5,  '2025-01-20 10:30:00', N'Completed', 24.99),
        (2,  5,  '2025-02-15 14:20:00', N'Completed',  9.99),
        (3,  6,  '2025-01-25 16:45:00', N'Completed', 22.99),
        (4,  6,  '2025-03-10 11:15:00', N'Completed', 12.99),
        (5,  7,  '2025-02-05 09:50:00', N'Completed', 19.99),
        (6,  7,  '2025-04-01 15:30:00', N'Completed', 14.99),
        (7,  8,  '2025-02-20 18:00:00', N'Completed', 17.99),
        (8,  8,  '2025-05-05 13:40:00', N'Completed', 12.99),
        (9,  9,  '2025-03-01 10:20:00', N'Completed', 10.99),
        (10, 9,  '2025-04-15 14:00:00', N'Completed', 11.99),
        (11, 10, '2025-03-08 12:30:00', N'Completed', 21.99),
        (12, 11, '2025-03-15 09:45:00', N'Completed',  8.99),
        (13, 11, '2025-04-20 16:00:00', N'Completed',  8.99),
        (14, 12, '2025-04-01 11:00:00', N'Completed', 10.99),
        (15, 13, '2025-04-10 14:30:00', N'Completed',  9.99),
        (16, 13, '2025-05-01 10:00:00', N'Pending',   13.99),
        (17, 14, '2025-04-15 15:00:00', N'Completed', 11.99),
        (18, 14, '2025-05-20 12:00:00', N'Cancelled', 10.99);
    SET IDENTITY_INSERT dbo.[ORDERS] OFF;

    PRINT 'Step 13 complete: 18 orders inserted.';

    -- ========================================================================
    -- STEP 14: ORDER_ITEM (XOR: AlbumID XOR CatalogID)
    -- ========================================================================

    PRINT 'Step 14: Inserting order items...';

    INSERT INTO dbo.[ORDER_ITEM] (OrderID, ItemNo, AlbumID, CatalogID, Quantity, UnitPrice)
    VALUES
        (1,  1, NULL, 1,    1, 24.99),
        (2,  1, 1,    NULL, 1,  9.99),
        (3,  1, NULL, 3,    1, 22.99),
        (4,  1, 3,    NULL, 1, 12.99),
        (5,  1, NULL, 2,    1, 19.99),
        (6,  1, 10,   NULL, 1, 14.99),
        (7,  1, NULL, 4,    1, 17.99),
        (8,  1, 12,   NULL, 1, 12.99),
        (9,  1, 9,    NULL, 1, 10.99),
        (10, 1, 7,    NULL, 1, 11.99),
        (11, 1, NULL, 5,    1, 21.99),
        (12, 1, 11,   NULL, 1,  8.99),
        (13, 1, 5,    NULL, 1,  8.99),
        (14, 1, 6,    NULL, 1, 10.99),
        (15, 1, 8,    NULL, 1,  9.99),
        (16, 1, 4,    NULL, 1, 13.99),
        (17, 1, 2,    NULL, 1, 11.99),
        (18, 1, 9,    NULL, 1, 10.99);

    PRINT 'Step 14 complete: 18 order items inserted.';

    -- ========================================================================
    -- STEP 15: PAYMENT (18 payments)
    -- PaymentStatus: Pending | Completed | Failed | Refunded
    -- UNIQUE: OrderID, TransactionReference
    -- ========================================================================

    PRINT 'Step 15: Inserting payments...';

    SET IDENTITY_INSERT dbo.[PAYMENT] ON;
    INSERT INTO dbo.[PAYMENT] (PaymentID, OrderID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus)
    VALUES
        (1,  1,  '2025-01-20 10:32:00', 24.99, N'Credit Card', N'TXN-LK-20250120-00101', N'Completed'),
        (2,  2,  '2025-02-15 14:22:00',  9.99, N'Credit Card', N'TXN-LK-20250215-00202', N'Completed'),
        (3,  3,  '2025-01-25 16:47:00', 22.99, N'Credit Card', N'TXN-LK-20250125-00303', N'Completed'),
        (4,  4,  '2025-03-10 11:18:00', 12.99, N'PayPal',      N'TXN-LK-20250310-00404', N'Completed'),
        (5,  5,  '2025-02-05 09:52:00', 19.99, N'Credit Card', N'TXN-LK-20250205-00505', N'Completed'),
        (6,  6,  '2025-04-01 15:33:00', 14.99, N'Google Pay',  N'TXN-LK-20250401-00606', N'Completed'),
        (7,  7,  '2025-02-20 18:02:00', 17.99, N'Credit Card', N'TXN-LK-20250220-00707', N'Completed'),
        (8,  8,  '2025-05-05 13:42:00', 12.99, N'PayPal',      N'TXN-LK-20250505-00808', N'Completed'),
        (9,  9,  '2025-03-01 10:22:00', 10.99, N'Credit Card', N'TXN-LK-20250301-00909', N'Completed'),
        (10, 10, '2025-04-15 14:02:00', 11.99, N'Google Pay',  N'TXN-LK-20250415-01010', N'Completed'),
        (11, 11, '2025-03-08 12:32:00', 21.99, N'Credit Card', N'TXN-LK-20250308-01111', N'Completed'),
        (12, 12, '2025-03-15 09:47:00',  8.99, N'PayPal',      N'TXN-LK-20250315-01212', N'Completed'),
        (13, 13, '2025-04-20 16:02:00',  8.99, N'Credit Card', N'TXN-LK-20250420-01313', N'Completed'),
        (14, 14, '2025-04-01 11:02:00', 10.99, N'Credit Card', N'TXN-LK-20250401-01414', N'Completed'),
        (15, 15, '2025-04-10 14:32:00',  9.99, N'Google Pay',  N'TXN-LK-20250410-01515', N'Completed'),
        (16, 16, '2025-05-01 10:02:00', 13.99, N'Credit Card', N'TXN-LK-20250501-01616', N'Pending'),
        (17, 17, '2025-04-15 15:02:00', 11.99, N'PayPal',      N'TXN-LK-20250415-01717', N'Completed'),
        (18, 18, '2025-05-20 12:02:00', 10.99, N'Credit Card', N'TXN-LK-20250520-01818', N'Refunded');
    SET IDENTITY_INSERT dbo.[PAYMENT] OFF;

    PRINT 'Step 15 complete: 18 payments inserted.';

    -- ========================================================================
    -- STEP 16: PROMOTION (4 promotions)
    -- DiscountType: Percentage | FixedAmount
    -- PromotionStatus: Active | Expired | Scheduled | Cancelled
    -- EndDate >= StartDate (check constraint)
    -- ========================================================================

    PRINT 'Step 16: Inserting promotions...';

    SET IDENTITY_INSERT dbo.[PROMOTION] ON;
    INSERT INTO dbo.[PROMOTION] (PromotionID, PromotionName, Description, DiscountType, DiscountValue, StartDate, EndDate, PromotionStatus)
    VALUES
        (1, N'Avurudu Music Sale',         N'Celebrate Sinhala and Tamil New Year with 20% off selected Sri Lankan albums and catalogs. Limited time offer!',              N'Percentage',  20.00, '2026-04-01 00:00:00', '2026-04-20 23:59:59', N'Scheduled'),
        (2, N'Independence Week Special',  N'Commemorate Sri Lanka Independence Day with a discount on any Sri Lankan album. Show your national pride through music!',     N'FixedAmount',  2.00, '2025-02-01 00:00:00', '2025-02-10 23:59:59', N'Expired'),
        (3, N'Island Classics Weekend',    N'Enjoy 15% off the Island Acoustic Collection and Sinhala Classics Collection every weekend this month.',                     N'Percentage',  15.00, '2026-07-01 00:00:00', '2026-07-31 23:59:59', N'Scheduled'),
        (4, N'Student Music Special',      N'Sri Lankan university students get a flat discount on any album purchase. Valid with student ID during checkout.',             N'FixedAmount',  1.50, '2025-03-01 00:00:00', '2025-12-31 23:59:59', N'Active');
    SET IDENTITY_INSERT dbo.[PROMOTION] OFF;

    PRINT 'Step 16 complete: 4 promotions inserted.';

    -- ========================================================================
    -- STEP 17: ALBUM_PROMOTION (junction table)
    -- ========================================================================

    PRINT 'Step 17: Inserting album-promotion links...';

    INSERT INTO dbo.[ALBUM_PROMOTION] (AlbumID, PromotionID)
    VALUES
        (1,  1),
        (2,  1),
        (11, 1),
        (3,  2),
        (4,  2),
        (5,  3),
        (6,  3),
        (7,  4),
        (8,  4),
        (9,  4);

    PRINT 'Step 17 complete: 10 album-promotion links inserted.';

    -- ========================================================================
    -- STEP 18: FAQ (8 FAQs)
    -- FAQStatus: Published | Draft | Archived
    -- ========================================================================

    PRINT 'Step 18: Inserting FAQs...';

    SET IDENTITY_INSERT dbo.[FAQ] ON;
    INSERT INTO dbo.[FAQ] (FAQID, Question, Answer, FAQStatus, CreatedDate, UpdatedDate)
    VALUES
        (1,  N'How can I purchase an album on MelodyMart?',
             N'Browse to any album page and click the Add to Cart button. Proceed to checkout, review your order, and complete payment. Your purchased album will appear instantly in your Digital Library.',
             N'Published', '2025-01-10 09:00:00', '2025-01-10 09:00:00'),
        (2,  N'How do I access my digital library after purchasing?',
             N'After a successful purchase, navigate to My Account and select My Library. All your purchased albums and catalog bundles will be listed there for immediate access.',
             N'Published', '2025-01-10 09:15:00', '2025-01-10 09:15:00'),
        (3,  N'Can I purchase an entire catalog instead of individual albums?',
             N'Yes! MelodyMart offers curated catalog bundles at a discounted price compared to buying albums individually. Purchasing a catalog unlocks all included albums in your library immediately.',
             N'Published', '2025-01-12 10:00:00', '2025-01-12 10:00:00'),
        (4,  N'How do I download purchased music?',
             N'Open your Digital Library, find the album you wish to download, and click the Download button. Your music will download in MP3 format directly to your device.',
             N'Published', '2025-01-15 11:00:00', '2025-01-15 11:00:00'),
        (5,  N'How long does administrator approval take for orders?',
             N'Most orders are processed and confirmed automatically. If manual review is required, our team typically responds within 24 hours. You will be notified once your order status is updated.',
             N'Published', '2025-01-18 14:00:00', '2025-01-18 14:00:00'),
        (6,  N'Can I create playlists on MelodyMart?',
             N'Yes! Registered listeners can create personal playlists from their purchased albums. Go to My Library, select an album, and use the Add to Playlist option to organise your music.',
             N'Published', '2025-01-20 09:30:00', '2025-01-20 09:30:00'),
        (7,  N'How can I submit a complaint or report an issue?',
             N'Log in to your account and navigate to Support and then Submit a Complaint. Describe your issue in detail and our customer support team will respond within 2 business days.',
             N'Published', '2025-02-05 11:20:00', '2025-02-05 11:20:00'),
        (8,  N'What payment methods does MelodyMart accept?',
             N'MelodyMart accepts Credit Cards, PayPal, and Google Pay. All transactions are secured and processed through our trusted payment gateway.',
             N'Published', '2025-02-10 16:45:00', '2025-02-10 16:45:00');
    SET IDENTITY_INSERT dbo.[FAQ] OFF;

    PRINT 'Step 18 complete: 8 FAQs inserted.';

    -- ========================================================================
    -- STEP 19: COMPLAINT (8 complaints, varied statuses)
    -- Status: Open | In Progress | Resolved | Closed
    -- ========================================================================

    PRINT 'Step 19: Inserting complaints...';

    SET IDENTITY_INSERT dbo.[COMPLAINT] ON;
    INSERT INTO dbo.[COMPLAINT] (ComplaintID, ListenerID, Subject, Description, ComplaintDate, Status, Response, ResolvedDate)
    VALUES
        (1,  5,  N'Payment charged but album not in library',
             N'I completed payment for the Sinhala Classics Collection on 20 January but the albums are not appearing in my digital library. Transaction reference TXN-LK-20250120-00101.',
             '2025-01-20 15:00:00', N'Resolved',
             N'We sincerely apologise for the inconvenience. The library access has been granted manually. Your albums should now appear in My Library.', '2025-01-21 09:30:00'),
        (2,  6,  N'Download failed midway for Island Acoustic Collection',
             N'I was downloading Kandyan Skies from the Island Acoustic Collection and the download stopped at 60%. The file appears corrupted and will not play.',
             '2025-02-01 14:30:00', N'Resolved',
             N'Our team investigated and resolved the file delivery issue. Please retry the download from your library. The file has been re-verified on our servers.', '2025-02-02 10:00:00'),
        (3,  7,  N'Promotion discount not applied at checkout',
             N'I added albums to my cart during the Independence Week Special promotion period but no discount was applied at checkout. I paid the full price.',
             '2025-04-03 11:20:00', N'Closed',
             N'After reviewing your order, we applied a retroactive discount credit of Rs. 2.00 to your account. This will be reflected in your next purchase.', '2025-04-05 15:00:00'),
        (4,  8,  N'Album description is incorrect on the website',
             N'The description for the album Yarl Isai on the website mentions it is a Sinhala pop album but it is a Tamil classical crossover. Please correct this information.',
             '2025-05-08 17:15:00', N'In Progress',
             N'Thank you for reporting this. Our catalog team has been notified and is reviewing the album description for correction.', NULL),
        (5,  9,  N'Cannot access purchased album after account suspension',
             N'My account was temporarily suspended due to a billing issue which has now been resolved. However my previously purchased Baila Night album is no longer accessible.',
             '2025-03-05 10:45:00', N'Resolved',
             N'Your account has been fully restored and library access reinstated. We apologise for the disruption.', '2025-03-06 08:30:00'),
        (6,  10, N'Catalog purchase shows incorrect number of albums',
             N'I purchased the Tamil Melodies Collection but the catalog page shows 3 albums while only 1 album appears in my library. I expected to receive all catalog albums.',
             '2025-03-10 16:00:00', N'In Progress',
             N'We are investigating the catalog album linking issue. Our technical team will resolve this within 48 hours.', NULL),
        (7,  11, N'Audio quality issue on track 2 of Southern Breeze',
             N'Track 2 (Galu Korale) on the Southern Breeze album has noticeable audio crackling throughout the track. This may be a corrupted MP3 file on your servers.',
             '2025-04-25 18:45:00', N'Open', NULL, NULL),
        (8,  12, N'Unable to submit review for Island Echoes',
             N'I am trying to submit a review for the Island Echoes album but the review form keeps showing a submission error. I have purchased the album and it is in my library.',
             '2025-05-15 13:30:00', N'Open', NULL, NULL);
    SET IDENTITY_INSERT dbo.[COMPLAINT] OFF;

    PRINT 'Step 19 complete: 8 complaints inserted.';

    -- ========================================================================
    -- STEP 20: REVIEW (16 reviews, ratings 3-5, varied listeners and albums)
    -- Rating: 1–5 (check constraint)
    -- ReviewStatus: Pending | Approved | Rejected
    -- ========================================================================

    PRINT 'Step 20: Inserting reviews...';

    SET IDENTITY_INSERT dbo.[REVIEW] ON;
    INSERT INTO dbo.[REVIEW] (ReviewID, ListenerID, AlbumID, Rating, Comment, ReviewDate, ReviewStatus)
    VALUES
        (1,  5,  1, 5, N'Sanda Eliya is absolutely beautiful. Kavindu''s voice is perfectly suited for these heartfelt acoustic melodies. A must-listen for any Sinhala music fan.',          '2025-01-25 16:00:00', N'Approved'),
        (2,  5,  3, 4, N'Colombo After Rain captures the energy and emotion of the city so well. The guitar work throughout is outstanding. Would have loved a few more tracks.',             '2025-03-15 14:30:00', N'Approved'),
        (3,  6,  5, 5, N'Kandyan Skies is a true masterpiece of Sri Lankan instrumental music. Sahan Perera has created something genuinely special here. Incredibly relaxing and peaceful.', '2025-02-05 10:45:00', N'Approved'),
        (4,  6,  6, 4, N'Island Echoes is a sophisticated fusion album that rewards patient listening. The jazz influences blend seamlessly with traditional Sri Lankan sounds.',                '2025-02-10 15:20:00', N'Approved'),
        (5,  7,  8, 4, N'Tropical Memories is exactly what you need for a lazy Sri Lankan afternoon. Nethara Silva''s vocals are enchanting and the production is clean and crisp.',           '2025-02-15 19:00:00', N'Approved'),
        (6,  7,  10,5, N'City Lights is Tharindu Bandara''s finest work. The orchestral arrangements are breathtaking and Dawn Over Colombo is simply one of the best tracks I have heard.',  '2025-04-05 12:30:00', N'Approved'),
        (7,  8,  9, 5, N'Baila Night in Colombo is pure joy from start to finish. Avurudu Gee is already a classic. Ashen Wijesinghe knows exactly how to get everyone dancing!',             '2025-02-25 20:15:00', N'Approved'),
        (8,  8,  12,4, N'Yarl Isai is a beautifully crafted Tamil classical album. Sachini Jayawardena''s voice is stunning and the Carnatic influences are handled with great respect.',     '2025-05-10 11:50:00', N'Approved'),
        (9,  9,  9, 3, N'The album has some great tracks but a few felt a little repetitive towards the end. Still Hora Party and Baila Night are fantastic crowd-pleasers.',                  '2025-03-05 17:40:00', N'Approved'),
        (10, 9,  7, 5, N'Ocean Road is a dreamy and atmospheric album that I keep coming back to. Dusk at Mirissa is hauntingly beautiful. Nethara Silva has a very unique sound.',           '2025-04-20 09:25:00', N'Approved'),
        (11, 10, 12,5, N'Yarl Isai is a cultural treasure. Every track reflects the rich heritage of Tamil classical music in Sri Lanka. Highly recommended for classical music lovers.',     '2025-03-12 13:10:00', N'Approved'),
        (12, 11, 11,4, N'Southern Breeze is warm, authentic, and comforting. Mihiranga Senanayake''s folk storytelling is charming and the acoustic arrangements are wonderfully simple.',   '2025-03-20 16:45:00', N'Approved'),
        (13, 11, 5, 5, N'Kandyan Skies transported me to the misty hills of Kandy. Perfect album to unwind with after a long day. The Temple Bells track is absolutely mesmerising.',       '2025-04-25 14:15:00', N'Approved'),
        (14, 12, 6, 3, N'Island Echoes is interesting but slightly inaccessible for casual listeners. The jazz sections are technically impressive but may not appeal to everyone.',          '2025-04-05 10:00:00', N'Approved'),
        (15, 13, 8, 4, N'Tropical Memories is a refreshing and cheerful collection. Summer in Weligama is my personal favourite track. Great album for beach drives along Sri Lanka''s coast.','2025-04-15 11:30:00', N'Approved'),
        (16, 14, 2, 5, N'Hithata Ahimi is an incredible follow-up album from Kavindu Jayasuriya. Every track is polished and emotionally resonant. Pem Sewana made me feel nostalgic.',     '2025-04-20 18:00:00', N'Pending');
    SET IDENTITY_INSERT dbo.[REVIEW] OFF;

    PRINT 'Step 20 complete: 16 reviews inserted.';

    -- ========================================================================
    -- STEP 21: DIGITAL_LIBRARY (1 per listener, UNIQUE ListenerID)
    -- ========================================================================

    PRINT 'Step 21: Inserting digital libraries...';

    SET IDENTITY_INSERT dbo.[DIGITAL_LIBRARY] ON;
    INSERT INTO dbo.[DIGITAL_LIBRARY] (LibraryID, ListenerID, CreatedDate, LastUpdatedDate)
    VALUES
        (1,  5,  '2025-01-20 10:35:00', '2025-02-15 14:25:00'),
        (2,  6,  '2025-01-25 16:50:00', '2025-03-10 11:20:00'),
        (3,  7,  '2025-02-05 09:55:00', '2025-04-01 15:35:00'),
        (4,  8,  '2025-02-20 18:05:00', '2025-05-05 13:45:00'),
        (5,  9,  '2025-03-01 10:25:00', '2025-04-15 14:05:00'),
        (6,  10, '2025-03-08 12:35:00', '2025-03-08 12:35:00'),
        (7,  11, '2025-03-15 09:50:00', '2025-04-20 16:05:00'),
        (8,  12, '2025-04-01 11:05:00', '2025-04-01 11:05:00'),
        (9,  13, '2025-04-10 14:35:00', '2025-04-10 14:35:00'),
        (10, 14, '2025-04-15 15:05:00', '2025-04-15 15:05:00');
    SET IDENTITY_INSERT dbo.[DIGITAL_LIBRARY] OFF;

    PRINT 'Step 21 complete: 10 digital libraries inserted.';

    -- ========================================================================
    -- STEP 22: LIBRARY_ITEMS (only for Completed orders)
    -- PurchaseType: Album | Catalog
    -- If Catalog: CatalogID must be NOT NULL; if Album: CatalogID must be NULL.
    -- UQ_LIBRARY_ITEMS_LibraryAlbum: (LibraryID, AlbumID) unique.
    -- CK_LIBRARY_ITEMS_CatalogRef enforced.
    --
    -- Catalog-to-album membership (from ALBUM.CatalogID):
    --   Catalog 1: Albums 1, 2
    --   Catalog 2: Albums 3, 4, 8
    --   Catalog 3: Albums 5, 6, 11
    --   Catalog 4: Album 9
    --   Catalog 5: Album 12
    -- ========================================================================

    PRINT 'Step 22: Inserting library items...';

    SET IDENTITY_INSERT dbo.[LIBRARY_ITEMS] ON;
    INSERT INTO dbo.[LIBRARY_ITEMS] (LibraryItemID, LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus)
    VALUES
        -- Library 1 (L5 Kavindu): Order 1 Catalog 1 -> Albums 1,2
        (1,  1, 1, 1,    '2025-01-20 10:36:00', N'Catalog', N'Active'),
        (2,  1, 2, 1,    '2025-01-20 10:36:00', N'Catalog', N'Active'),
        -- Library 2 (L6 Tharushi): Order 3 Catalog 3 -> Albums 5,6,11; Order 4 Album 3 standalone
        (3,  2, 5, 3,    '2025-01-25 16:51:00', N'Catalog', N'Active'),
        (4,  2, 6, 3,    '2025-01-25 16:51:00', N'Catalog', N'Active'),
        (5,  2, 11, 3,   '2025-01-25 16:51:00', N'Catalog', N'Active'),
        (6,  2, 3, NULL, '2025-03-10 11:21:00', N'Album',   N'Active'),
        -- Library 3 (L7 Dineth): Order 5 Catalog 2 -> Albums 3,4,8; Order 6 Album 10 standalone
        (7,  3, 3, 2,    '2025-02-05 09:56:00', N'Catalog', N'Active'),
        (8,  3, 4, 2,    '2025-02-05 09:56:00', N'Catalog', N'Active'),
        (9,  3, 8, 2,    '2025-02-05 09:56:00', N'Catalog', N'Active'),
        (10, 3, 10, NULL,'2025-04-01 15:36:00', N'Album',   N'Active'),
        -- Library 4 (L8 Sanduni): Order 7 Catalog 4 -> Album 9; Order 8 Album 12 standalone
        (11, 4, 9, 4,    '2025-02-20 18:06:00', N'Catalog', N'Active'),
        (12, 4, 12, NULL,'2025-05-05 13:46:00', N'Album',   N'Active'),
        -- Library 5 (L9 Chamod): Order 9 Album 9 standalone; Order 10 Album 7 standalone
        (13, 5, 9, NULL, '2025-03-01 10:26:00', N'Album',   N'Active'),
        (14, 5, 7, NULL, '2025-04-15 14:06:00', N'Album',   N'Active'),
        -- Library 6 (L10 Hiruni): Order 11 Catalog 5 -> Album 12
        (15, 6, 12, 5,   '2025-03-08 12:36:00', N'Catalog', N'Active'),
        -- Library 7 (L11 Pasindu): Order 12 Album 11; Order 13 Album 5
        (16, 7, 11, NULL,'2025-03-15 09:51:00', N'Album',   N'Active'),
        (17, 7, 5, NULL, '2025-04-20 16:06:00', N'Album',   N'Active'),
        -- Library 8 (L12 Nethmi): Order 14 Album 6
        (18, 8, 6, NULL, '2025-04-01 11:06:00', N'Album',   N'Active'),
        -- Library 9 (L13 Dilan): Order 15 Album 8 (Order 16 Pending = no library)
        (19, 9, 8, NULL, '2025-04-10 14:36:00', N'Album',   N'Active'),
        -- Library 10 (L14 Isuru): Order 17 Album 2 (Order 18 Cancelled = no library)
        (20, 10, 2, NULL,'2025-04-15 15:06:00', N'Album',   N'Active');
    SET IDENTITY_INSERT dbo.[LIBRARY_ITEMS] OFF;

    PRINT 'Step 22 complete: 20 library items inserted.';

    -- ========================================================================
    -- STEP 23: DOWNLOAD (22 download records)
    -- FileSize in bytes.
    -- DownloadStatus: Completed | Failed | Interrupted
    -- ========================================================================

    PRINT 'Step 23: Inserting download records...';

    SET IDENTITY_INSERT dbo.[DOWNLOAD] ON;
    INSERT INTO dbo.[DOWNLOAD] (DownloadID, LibraryItemID, DownloadDateTime, FileFormat, FileSize, DownloadStatus)
    VALUES
        (1,  1,  '2025-01-20 10:45:00', N'MP3',  82500000, N'Completed'),
        (2,  2,  '2025-01-20 10:52:00', N'MP3',  96200000, N'Completed'),
        (3,  3,  '2025-01-26 08:00:00', N'MP3',  75400000, N'Completed'),
        (4,  4,  '2025-01-26 08:10:00', N'MP3',  89100000, N'Completed'),
        (5,  5,  '2025-01-26 08:20:00', N'MP3',  71800000, N'Completed'),
        (6,  6,  '2025-03-11 09:00:00', N'MP3', 104800000, N'Completed'),
        (7,  7,  '2025-02-06 10:00:00', N'MP3', 104800000, N'Completed'),
        (8,  8,  '2025-02-06 10:15:00', N'MP3', 112300000, N'Completed'),
        (9,  9,  '2025-02-06 10:30:00', N'MP3',  81200000, N'Completed'),
        (10, 10, '2025-04-02 11:00:00', N'MP3', 121500000, N'Completed'),
        (11, 11, '2025-02-21 09:00:00', N'MP3',  88400000, N'Completed'),
        (12, 12, '2025-05-06 14:00:00', N'MP3', 105600000, N'Completed'),
        (13, 13, '2025-03-02 11:00:00', N'MP3',  88400000, N'Completed'),
        (14, 14, '2025-04-16 10:00:00', N'MP3',  96800000, N'Completed'),
        (15, 15, '2025-03-09 13:00:00', N'MP3', 105600000, N'Completed'),
        (16, 16, '2025-03-16 10:00:00', N'MP3',  72100000, N'Completed'),
        (17, 17, '2025-04-21 17:00:00', N'MP3',  75400000, N'Completed'),
        (18, 18, '2025-04-02 12:00:00', N'MP3',  89100000, N'Completed'),
        (19, 19, '2025-04-11 15:00:00', N'MP3',  81200000, N'Completed'),
        (20, 20, '2025-04-16 16:00:00', N'MP3',  96200000, N'Completed'),
        (21, 6,  '2025-04-01 09:00:00', N'MP3', 104800000, N'Completed'),
        (22, 13, '2025-05-01 12:00:00', N'MP3',  88400000, N'Completed');
    SET IDENTITY_INSERT dbo.[DOWNLOAD] OFF;

    PRINT 'Step 23 complete: 22 download records inserted.';

    -- ========================================================================
    -- STEP 24: PLAYLIST (6 playlists for various listeners)
    -- ========================================================================

    PRINT 'Step 24: Inserting playlists...';

    SET IDENTITY_INSERT dbo.[PLAYLIST] ON;
    INSERT INTO dbo.[PLAYLIST] (PlaylistID, ListenerID, PlaylistName, CreatedDate, LastUpdatedDate)
    VALUES
        (1, 5,  N'My Sinhala Favourites', '2025-01-25 11:00:00', '2025-03-10 14:00:00'),
        (2, 6,  N'Evening Drive',         '2025-02-01 18:00:00', '2025-04-05 20:00:00'),
        (3, 7,  N'Chill in Colombo',      '2025-02-10 21:00:00', '2025-05-01 22:00:00'),
        (4, 8,  N'Weekend Vibes',         '2025-02-25 10:00:00', '2025-05-10 11:00:00'),
        (5, 9,  N'Island Road Trip',      '2025-03-05 09:00:00', '2025-04-20 10:00:00'),
        (6, 11, N'Acoustic Evenings',     '2025-03-18 19:00:00', '2025-04-25 20:00:00');
    SET IDENTITY_INSERT dbo.[PLAYLIST] OFF;

    PRINT 'Step 24 complete: 6 playlists inserted.';

    -- ========================================================================
    -- STEP 25: PLAYLIST_ITEM
    -- CHK_PLAYLIST_ITEM_ONE_TARGET: exactly ONE of TrackID or AlbumID must be set.
    -- Tracks referenced must exist; albums referenced must exist.
    -- ========================================================================

    PRINT 'Step 25: Inserting playlist items...';

    SET IDENTITY_INSERT dbo.[PLAYLIST_ITEM] ON;
    INSERT INTO dbo.[PLAYLIST_ITEM] (PlaylistItemID, PlaylistID, TrackID, AlbumID, AddedDate)
    VALUES
        -- Playlist 1 (My Sinhala Favourites - L5): tracks from albums 1 & 2
        (1,  1, 1,    NULL, '2025-01-25 11:05:00'),
        (2,  1, 5,    NULL, '2025-01-25 11:06:00'),
        (3,  1, 3,    NULL, '2025-01-25 11:07:00'),
        -- Playlist 2 (Evening Drive - L6): tracks from albums 5 & 6
        (4,  2, 17,   NULL, '2025-02-01 18:05:00'),
        (5,  2, 21,   NULL, '2025-02-01 18:06:00'),
        (6,  2, 23,   NULL, '2025-02-01 18:07:00'),
        -- Playlist 3 (Chill in Colombo - L7): album-level items
        (7,  3, NULL, 8,    '2025-02-10 21:05:00'),
        (8,  3, NULL, 10,   '2025-02-10 21:06:00'),
        -- Playlist 4 (Weekend Vibes - L8): tracks from albums 9 & 12
        (9,  4, 33,   NULL, '2025-02-25 10:05:00'),
        (10, 4, 35,   NULL, '2025-02-25 10:06:00'),
        (11, 4, 45,   NULL, '2025-02-25 10:07:00'),
        -- Playlist 5 (Island Road Trip - L9): track + album
        (12, 5, 34,   NULL, '2025-03-05 09:05:00'),
        (13, 5, NULL, 7,    '2025-03-05 09:06:00'),
        -- Playlist 6 (Acoustic Evenings - L11): tracks from albums 11 & 5
        (14, 6, 41,   NULL, '2025-03-18 19:05:00'),
        (15, 6, 43,   NULL, '2025-03-18 19:06:00'),
        (16, 6, 17,   NULL, '2025-03-18 19:07:00');
    SET IDENTITY_INSERT dbo.[PLAYLIST_ITEM] OFF;

    PRINT 'Step 25 complete: 16 playlist items inserted.';

    -- ========================================================================
    -- COMMIT
    -- ========================================================================

    COMMIT TRANSACTION;
    PRINT '=== Transaction committed successfully. All Sri Lankan data inserted. ===';

END TRY
BEGIN CATCH
    IF @@TRANCOUNT > 0
        ROLLBACK TRANSACTION;
    PRINT '=== ERROR: Transaction rolled back. ===';
    PRINT 'Error Number: ' + CAST(ERROR_NUMBER() AS NVARCHAR);
    PRINT 'Error Message: ' + ERROR_MESSAGE();
    PRINT 'Error Line: ' + CAST(ERROR_LINE() AS NVARCHAR);
    THROW;
END CATCH
GO

-- ============================================================================
-- STEP 26: PHONE NUMBER VALIDATION
-- ============================================================================

PRINT '=== Step 26: Phone Number Validation ===';

SELECT UserID, FirstName, LastName, PhoneNumber,
    CASE
        WHEN PhoneNumber IS NULL THEN 'NULL'
        WHEN LEN(PhoneNumber) <> 10 THEN 'INVALID_LENGTH'
        WHEN PhoneNumber NOT LIKE '07[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]' THEN 'INVALID_FORMAT'
        ELSE 'VALID'
    END AS PhoneStatus
FROM dbo.[USER]
ORDER BY UserID;
GO

DECLARE @invalidPhones INT;
SELECT @invalidPhones = COUNT(*) FROM dbo.[USER]
WHERE PhoneNumber IS NULL
   OR LEN(PhoneNumber) <> 10
   OR PhoneNumber NOT LIKE '07[0-9][0-9][0-9][0-9][0-9][0-9][0-9][0-9]';

IF @invalidPhones = 0
    PRINT 'PHONE VALIDATION PASSED: Zero invalid phone numbers.';
ELSE
    PRINT 'PHONE VALIDATION FAILED: ' + CAST(@invalidPhones AS NVARCHAR) + ' invalid phone numbers found!';
GO

-- ============================================================================
-- STEP 27: FK ORPHAN VALIDATION
-- ============================================================================

PRINT '=== Step 27: FK Orphan Validation ===';

SELECT 'ADMINISTRATOR -> USER' AS Relationship, COUNT(*) AS Orphans FROM dbo.ADMINISTRATOR a WHERE NOT EXISTS (SELECT 1 FROM dbo.[USER] u WHERE u.UserID = a.AdminID) UNION ALL
SELECT 'LISTENER -> USER', COUNT(*) FROM dbo.LISTENER l WHERE NOT EXISTS (SELECT 1 FROM dbo.[USER] u WHERE u.UserID = l.ListenerID) UNION ALL
SELECT 'ALBUM -> ARTIST', COUNT(*) FROM dbo.ALBUM al WHERE NOT EXISTS (SELECT 1 FROM dbo.ARTIST ar WHERE ar.ArtistID = al.ArtistID) UNION ALL
SELECT 'ALBUM -> GENRE', COUNT(*) FROM dbo.ALBUM al WHERE NOT EXISTS (SELECT 1 FROM dbo.GENRE g WHERE g.GenreID = al.GenreID) UNION ALL
SELECT 'TRACK -> ALBUM', COUNT(*) FROM dbo.TRACK t WHERE NOT EXISTS (SELECT 1 FROM dbo.ALBUM al WHERE al.AlbumID = t.AlbumID) UNION ALL
SELECT 'ORDERS -> LISTENER', COUNT(*) FROM dbo.ORDERS o WHERE NOT EXISTS (SELECT 1 FROM dbo.LISTENER l WHERE l.ListenerID = o.ListenerID) UNION ALL
SELECT 'ORDER_ITEM -> ORDERS', COUNT(*) FROM dbo.ORDER_ITEM oi WHERE NOT EXISTS (SELECT 1 FROM dbo.ORDERS o WHERE o.OrderID = oi.OrderID) UNION ALL
SELECT 'PAYMENT -> ORDERS', COUNT(*) FROM dbo.PAYMENT p WHERE NOT EXISTS (SELECT 1 FROM dbo.ORDERS o WHERE o.OrderID = p.OrderID) UNION ALL
SELECT 'LIBRARY_ITEMS -> DIGITAL_LIBRARY', COUNT(*) FROM dbo.LIBRARY_ITEMS li WHERE NOT EXISTS (SELECT 1 FROM dbo.DIGITAL_LIBRARY dl WHERE dl.LibraryID = li.LibraryID) UNION ALL
SELECT 'DOWNLOAD -> LIBRARY_ITEMS', COUNT(*) FROM dbo.DOWNLOAD d WHERE NOT EXISTS (SELECT 1 FROM dbo.LIBRARY_ITEMS li WHERE li.LibraryItemID = d.LibraryItemID) UNION ALL
SELECT 'REVIEW -> LISTENER', COUNT(*) FROM dbo.REVIEW r WHERE NOT EXISTS (SELECT 1 FROM dbo.LISTENER l WHERE l.ListenerID = r.ListenerID) UNION ALL
SELECT 'REVIEW -> ALBUM', COUNT(*) FROM dbo.REVIEW r WHERE NOT EXISTS (SELECT 1 FROM dbo.ALBUM al WHERE al.AlbumID = r.AlbumID) UNION ALL
SELECT 'COMPLAINT -> LISTENER', COUNT(*) FROM dbo.COMPLAINT c WHERE NOT EXISTS (SELECT 1 FROM dbo.LISTENER l WHERE l.ListenerID = c.ListenerID) UNION ALL
SELECT 'ALBUM_PROMOTION -> ALBUM', COUNT(*) FROM dbo.ALBUM_PROMOTION ap WHERE NOT EXISTS (SELECT 1 FROM dbo.ALBUM al WHERE al.AlbumID = ap.AlbumID) UNION ALL
SELECT 'ALBUM_PROMOTION -> PROMOTION', COUNT(*) FROM dbo.ALBUM_PROMOTION ap WHERE NOT EXISTS (SELECT 1 FROM dbo.PROMOTION p WHERE p.PromotionID = ap.PromotionID) UNION ALL
SELECT 'PLAYLIST -> LISTENER', COUNT(*) FROM dbo.PLAYLIST pl WHERE NOT EXISTS (SELECT 1 FROM dbo.LISTENER l WHERE l.ListenerID = pl.ListenerID) UNION ALL
SELECT 'PLAYLIST_ITEM -> PLAYLIST', COUNT(*) FROM dbo.PLAYLIST_ITEM pi WHERE NOT EXISTS (SELECT 1 FROM dbo.PLAYLIST pl WHERE pl.PlaylistID = pi.PlaylistID);
GO

-- ============================================================================
-- STEP 28: DUPLICATE CHECKS
-- ============================================================================

PRINT '=== Step 28: Duplicate Checks ===';

SELECT 'Duplicate emails' AS Check_Name, COUNT(*) AS Count FROM (SELECT Email, COUNT(*) AS c FROM dbo.[USER] GROUP BY Email HAVING COUNT(*) > 1) x UNION ALL
SELECT 'Duplicate genre names', COUNT(*) FROM (SELECT GenreName, COUNT(*) AS c FROM dbo.GENRE GROUP BY GenreName HAVING COUNT(*) > 1) x UNION ALL
SELECT 'Duplicate library (LibraryID+AlbumID)', COUNT(*) FROM (SELECT LibraryID, AlbumID, COUNT(*) AS c FROM dbo.LIBRARY_ITEMS GROUP BY LibraryID, AlbumID HAVING COUNT(*) > 1) x;
GO

-- ============================================================================
-- STEP 29: FINAL ROW COUNT REPORT
-- ============================================================================

PRINT '=== Step 29: Final Row Count Report ===';

SELECT 'USER'             AS TableName, COUNT(*) AS RowCount FROM dbo.[USER]           UNION ALL
SELECT 'ADMINISTRATOR',                 COUNT(*)             FROM dbo.ADMINISTRATOR     UNION ALL
SELECT 'LISTENER',                      COUNT(*)             FROM dbo.LISTENER          UNION ALL
SELECT 'ARTIST',                        COUNT(*)             FROM dbo.ARTIST            UNION ALL
SELECT 'GENRE',                         COUNT(*)             FROM dbo.GENRE             UNION ALL
SELECT 'Catalog',                       COUNT(*)             FROM dbo.Catalog           UNION ALL
SELECT 'ALBUM',                         COUNT(*)             FROM dbo.ALBUM             UNION ALL
SELECT 'TRACK',                         COUNT(*)             FROM dbo.TRACK             UNION ALL
SELECT 'CART',                          COUNT(*)             FROM dbo.CART              UNION ALL
SELECT 'CART_ITEM',                     COUNT(*)             FROM dbo.CART_ITEM         UNION ALL
SELECT 'ORDERS',                        COUNT(*)             FROM dbo.ORDERS            UNION ALL
SELECT 'ORDER_ITEM',                    COUNT(*)             FROM dbo.ORDER_ITEM        UNION ALL
SELECT 'PAYMENT',                       COUNT(*)             FROM dbo.PAYMENT           UNION ALL
SELECT 'PROMOTION',                     COUNT(*)             FROM dbo.PROMOTION         UNION ALL
SELECT 'ALBUM_PROMOTION',               COUNT(*)             FROM dbo.ALBUM_PROMOTION   UNION ALL
SELECT 'FAQ',                           COUNT(*)             FROM dbo.FAQ               UNION ALL
SELECT 'COMPLAINT',                     COUNT(*)             FROM dbo.COMPLAINT         UNION ALL
SELECT 'REVIEW',                        COUNT(*)             FROM dbo.REVIEW            UNION ALL
SELECT 'DIGITAL_LIBRARY',               COUNT(*)             FROM dbo.DIGITAL_LIBRARY   UNION ALL
SELECT 'LIBRARY_ITEMS',                COUNT(*)             FROM dbo.LIBRARY_ITEMS     UNION ALL
SELECT 'DOWNLOAD',                      COUNT(*)             FROM dbo.DOWNLOAD          UNION ALL
SELECT 'PLAYLIST',                      COUNT(*)             FROM dbo.PLAYLIST          UNION ALL
SELECT 'PLAYLIST_ITEM',                COUNT(*)             FROM dbo.PLAYLIST_ITEM;
GO

PRINT '=== MelodyMart Sri Lankan Data Replacement Complete ===';
GO
