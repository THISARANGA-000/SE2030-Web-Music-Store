-- ============================================================================
-- Script: 03_insert_sample_data.sql
-- Project: MelodyMart Web-Based Music Store
-- DBMS: Microsoft SQL Server
-- Description: Inserts realistic sample data for all 21 relational tables in
--              strict foreign key dependency order. Populates multiple records
--              for listeners, admins, artists, genres, catalogs, albums,
--              tracks, carts, orders, payments, promotions, FAQs, complaints,
--              reviews, digital libraries, library items, and download logs.
-- ============================================================================

USE MelodyMartDB;
GO

-- ============================================================================
-- 1. USER & AUTHENTICATION MODULE
-- ============================================================================

-- 1.1 USER (10 Users: 3 Admins, 7 Listeners)
SET IDENTITY_INSERT dbo.[USER] ON;
INSERT INTO dbo.[USER] (UserID, FirstName, LastName, Email, PasswordHash, PhoneNumber, RegistrationDate, AccountStatus)
VALUES
    (1, N'Emily',   N'Watson',    N'emily.watson@melodymart.com',   N'$2a$12$e8Y7z9.kF3jLq6P9x1M2ReAbCdEfGhIjKlMnOpQrStUvWxYz01234', N'+1-555-0101', '2025-01-10 08:30:00', N'Active'),
    (2, N'Marcus',  N'Vance',     N'marcus.vance@melodymart.com',   N'$2a$12$v9X8w7.mN2kPq5R8y2O1SdEfGhIjKlMnOpQrStUvWxYzAbCdEf56789', N'+1-555-0102', '2025-01-15 09:15:00', N'Active'),
    (3, N'Sarah',   N'Connor',    N'sarah.connor@melodymart.com',   N'$2a$12$p4Q3r2.sT1uVw4X7z3L0TaGhIjKlMnOpQrStUvWxYzAbCdEfGh90123', N'+1-555-0103', '2025-02-01 10:00:00', N'Active'),
    (4, N'David',   N'Kim',       N'david.kim@gmail.com',           N'$2a$12$k1L2m3.nO4pQr5S6t7U8VbIjKlMnOpQrStUvWxYzAbCdEfGhIj34567', N'+1-555-0201', '2025-02-10 11:20:00', N'Active'),
    (5, N'Sophia',  N'Martinez',  N'sophia.m@outlook.com',          N'$2a$12$a9B8c7.dE6fGh5I4j3K2LcMnOpQrStUvWxYzAbCdEfGhIjKlMn78901', N'+1-555-0202', '2025-02-14 14:45:00', N'Active'),
    (6, N'Liam',    N'Johnson',   N'liam.j@yahoo.com',              N'$2a$12$z3Y4x5.wV6uTs7R8q9P0MdOpQrStUvWxYzAbCdEfGhIjKlMnOp12345', N'+1-555-0203', '2025-03-01 16:10:00', N'Active'),
    (7, N'Aria',    N'Chen',      N'aria.chen@icloud.com',          N'$2a$12$t8S7r6.qP5oNn4M3l2K1NeQrStUvWxYzAbCdEfGhIjKlMnOpQr67890', N'+1-555-0204', '2025-03-15 13:00:00', N'Active'),
    (8, N'Noah',    N'Williams',  N'noah.w@gmail.com',              N'$2a$12$h5G6f7.eD8cBa9Z0y1X2OfStUvWxYzAbCdEfGhIjKlMnOpQrSt23456', N'+1-555-0205', '2025-04-01 09:40:00', N'Active'),
    (9, N'Elena',   N'Rostova',   N'elena.rostova@mail.com',        N'$2a$12$m2N3o4.pQ5rSt6U7v8W9PgUvWxYzAbCdEfGhIjKlMnOpQrStUv78901', N'+1-555-0206', '2025-04-20 17:25:00', N'Active'),
    (10, N'Lucas',  N'Silva',     N'lucas.silva@gmail.com',         N'$2a$12$b1C2d3.eF4gHi5J6k7L8QhWxYzAbCdEfGhIjKlMnOpQrStUvWx34567', N'+1-555-0207', '2025-05-01 12:05:00', N'Active');
SET IDENTITY_INSERT dbo.[USER] OFF;
GO

-- 1.2 ADMINISTRATOR (Subclass of USER: UserIDs 1, 2, 3)
INSERT INTO dbo.[ADMINISTRATOR] (AdminID, AdminRole)
VALUES
    (1, N'SuperAdmin'),
    (2, N'StoreManager'),
    (3, N'CustomerSupport');
GO

-- 1.3 LISTENER (Subclass of USER: UserIDs 4 through 10)
INSERT INTO dbo.[LISTENER] (ListenerID)
VALUES
    (4),
    (5),
    (6),
    (7),
    (8),
    (9),
    (10);
GO

-- ============================================================================
-- 2. ARTIST, GENRE & MUSIC CATALOG MODULE
-- ============================================================================

-- 2.1 ARTIST (6 Artists)
SET IDENTITY_INSERT dbo.[ARTIST] ON;
INSERT INTO dbo.[ARTIST] (ArtistID, ArtistName, Biography, Country)
VALUES
    (1, N'Luna Eclipse',       N'Electronic and ambient music producer based in London, renowned for ethereal synthesizers and chillwave soundscapes.', N'United Kingdom'),
    (2, N'The Midnight Echoes', N'Indie alternative rock band formed in Seattle with chart-topping guitar riffs and energetic drum beats.', N'United States'),
    (3, N'Acoustic Solitude',   N'Fingerstyle acoustic guitarist and indie-folk singer-songwriter crafting intimate, soulful ballads.', N'Australia'),
    (4, N'Velvet Groove',       N'Neo-soul and smooth jazz quintet combining vintage brass, Rhodes piano, and groove basslines.', N'Canada'),
    (5, N'Aurora Beats',        N'Progressive house and synthwave DJ duo producing festival anthems and retro-futuristic club tracks.', N'Sweden'),
    (6, N'Classical Horizons',  N'Modern classical chamber orchestra creating cinematic soundtrack suites and emotional string concertos.', N'Germany');
SET IDENTITY_INSERT dbo.[ARTIST] OFF;
GO

-- 2.2 GENRE (6 Genres)
SET IDENTITY_INSERT dbo.[GENRE] ON;
INSERT INTO dbo.[GENRE] (GenreID, GenreName, Description)
VALUES
    (1, N'Electronic & Ambient', N'Synthesizer-driven ambient, downtempo, chillwave, and electronic compositions.'),
    (2, N'Indie Rock',           N'Alternative rock, indie garage beats, and electric guitar-driven anthems.'),
    (3, N'Acoustic & Folk',      N'Warm fingerstyle acoustic guitar, folk melodies, and heartfelt acoustic arrangements.'),
    (4, N'Jazz & Soul',          N'Smooth jazz, neo-soul vocals, vintage brass, and mellow groove progressions.'),
    (5, N'Synthwave & EDM',      N'High-energy EDM, progressive house, synthpop, and retro-futuristic rhythms.'),
    (6, N'Classical & Cinematic',N'Full orchestral scores, grand piano themes, and evocative cinematic string arrangements.');
SET IDENTITY_INSERT dbo.[GENRE] OFF;
GO

-- 2.3 Catalog (3 Curated Catalog Bundles)
SET IDENTITY_INSERT dbo.[Catalog] ON;
INSERT INTO dbo.[Catalog] (CatalogID, CatalogName, Description, Price, CreatedDate)
VALUES
    (1, N'Indie & Electronic Essentials 2025', N'Curated bundle featuring premier ambient electronic and indie alternative albums at a value package discount.', 24.99, '2025-01-20 10:00:00'),
    (2, N'Acoustic & Soul Master Anthology',  N'Relaxing compilation bringing together acoustic folk guitars and velvet neo-soul studio master recordings.', 19.99, '2025-02-01 11:30:00'),
    (3, N'Cinematic Soundscapes Collection',   N'Grand collection of modern classical orchestral suites and emotive cinematic pieces for composers and listeners.', 29.99, '2025-03-01 09:00:00');
SET IDENTITY_INSERT dbo.[Catalog] OFF;
GO

-- 2.4 ALBUM (8 Albums across Artists, Genres, and Catalogs)
SET IDENTITY_INSERT dbo.[ALBUM] ON;
INSERT INTO dbo.[ALBUM] (AlbumID, AlbumTitle, ReleaseDate, AlbumStatus, Price, Description, CoverImageURL, ArtistID, GenreID, CatalogID)
VALUES
    (1, N'Celestial Drift',          '2024-05-12', N'Available',  9.99, N'Deep atmospheric ambient soundscapes and lush chillwave textures.',           N'/assets/covers/celestial_drift.jpg',    1, 1, 1),
    (2, N'Neon Horizon',             '2024-08-20', N'Available', 11.99, N'Retro-synth electronic rhythms infused with pulsating analog synthesizers.',    N'/assets/covers/neon_horizon.jpg',       1, 1, 1),
    (3, N'Shadows in the Rain',      '2024-03-15', N'Available', 12.99, N'Moody indie rock anthems featuring crunchy guitar chords and driving rhythms.', N'/assets/covers/shadows_rain.jpg',        2, 2, 1),
    (4, N'Echoes of the Canyon',     '2024-07-04', N'Available',  8.99, N'Intimate acoustic guitar solos and rustic folk storytelling.',                   N'/assets/covers/echoes_canyon.jpg',      3, 3, 2),
    (5, N'Midnight Velvet',          '2024-09-18', N'Available', 10.99, N'Sensual neo-soul with brass arrangements, Rhodes electric piano, and jazz drums.',N'/assets/covers/midnight_velvet.jpg',     4, 4, 2),
    (6, N'Solar Flare Anthem',       '2024-11-01', N'Available', 13.99, N'Energetic progressive house and festival EDM bangers with soaring lead synths.', N'/assets/covers/solar_flare.jpg',         5, 5, NULL),
    (7, N'Symphony of Time',         '2024-10-10', N'Available', 14.99, N'Sweeping symphonic compositions featuring grand piano and full orchestra.',       N'/assets/covers/symphony_time.jpg',       6, 6, 3),
    (8, N'Whispers in Starlight',    '2025-01-05', N'Available', 11.99, N'Emotive cinematic string suites and serene ambient piano movements.',             N'/assets/covers/whispers_starlight.jpg',  6, 6, 3);
SET IDENTITY_INSERT dbo.[ALBUM] OFF;
GO

-- 2.5 TRACK (24 Tracks: 3 Tracks per Album)
SET IDENTITY_INSERT dbo.[TRACK] ON;
INSERT INTO dbo.[TRACK] (TrackID, AlbumID, TrackTitle, TrackNumber, Duration, AudioFileURL)
VALUES
    -- Album 1: Celestial Drift
    (1,  1, N'Starlight Awakening',  1, 245, N'/audio/album1/01_starlight_awakening.mp3'),
    (2,  1, N'Orbiting Void',        2, 310, N'/audio/album1/02_orbiting_void.mp3'),
    (3,  1, N'Lunar Serenade',       3, 275, N'/audio/album1/03_lunar_serenade.mp3'),
    -- Album 2: Neon Horizon
    (4,  2, N'Grid Runner',          1, 215, N'/audio/album2/01_grid_runner.mp3'),
    (5,  2, N'Cybernetic Glow',      2, 260, N'/audio/album2/02_cybernetic_glow.mp3'),
    (6,  2, N'Retrograde Dreams',    3, 230, N'/audio/album2/03_retrograde_dreams.mp3'),
    -- Album 3: Shadows in the Rain
    (7,  3, N'Rain on Glass',        1, 250, N'/audio/album3/01_rain_on_glass.mp3'),
    (8,  3, N'Electric Fog',         2, 280, N'/audio/album3/02_electric_fog.mp3'),
    (9,  3, N'Last Train Home',      3, 315, N'/audio/album3/03_last_train_home.mp3'),
    -- Album 4: Echoes of the Canyon
    (10, 4, N'Morning Mist',         1, 195, N'/audio/album4/01_morning_mist.mp3'),
    (11, 4, N'Dusty Trail',          2, 220, N'/audio/album4/02_dusty_trail.mp3'),
    (12, 4, N'Campfire Warmth',      3, 240, N'/audio/album4/03_campfire_warmth.mp3'),
    -- Album 5: Midnight Velvet
    (13, 5, N'Silk & Brass',         1, 265, N'/audio/album5/01_silk_brass.mp3'),
    (14, 5, N'Late Night Espresso',  2, 290, N'/audio/album5/02_late_night_espresso.mp3'),
    (15, 5, N'Golden Hour Groove',   3, 255, N'/audio/album5/03_golden_hour_groove.mp3'),
    -- Album 6: Solar Flare Anthem
    (16, 6, N'Ignition Pulse',       1, 230, N'/audio/album6/01_ignition_pulse.mp3'),
    (17, 6, N'Peak Velocity',        2, 285, N'/audio/album6/02_peak_velocity.mp3'),
    (18, 6, N'Afterglow Symphony',   3, 300, N'/audio/album6/03_afterglow_symphony.mp3'),
    -- Album 7: Symphony of Time
    (19, 7, N'Overture of Ancients', 1, 360, N'/audio/album7/01_overture_ancients.mp3'),
    (20, 7, N'Cello Requiem',        2, 420, N'/audio/album7/02_cello_requiem.mp3'),
    (21, 7, N'Triumph of Dawn',      3, 340, N'/audio/album7/03_triumph_dawn.mp3'),
    -- Album 8: Whispers in Starlight
    (22, 8, N'Stardust Reflection',  1, 270, N'/audio/album8/01_stardust_reflection.mp3'),
    (23, 8, N'Cosmic Lullaby',       2, 310, N'/audio/album8/02_cosmic_lullaby.mp3'),
    (24, 8, N'Infinite Horizon',     3, 390, N'/audio/album8/03_infinite_horizon.mp3');
SET IDENTITY_INSERT dbo.[TRACK] OFF;
GO

-- ============================================================================
-- 3. SHOPPING CART & ORDER / SALES MANAGEMENT MODULE
-- ============================================================================

-- 3.1 CART (7 Carts: 1 per Listener, Unique ListenerID)
SET IDENTITY_INSERT dbo.[CART] ON;
INSERT INTO dbo.[CART] (CartID, ListenerID, CreatedDate, LastUpdatedDate, CartStatus)
VALUES
    (1, 4,  '2025-02-10 11:30:00', '2025-05-10 09:00:00', N'Active'),
    (2, 5,  '2025-02-14 15:00:00', '2025-05-12 14:30:00', N'Active'),
    (3, 6,  '2025-03-01 16:30:00', '2025-05-15 11:20:00', N'Active'),
    (4, 7,  '2025-03-15 13:30:00', '2025-05-18 16:45:00', N'Active'),
    (5, 8,  '2025-04-01 10:00:00', '2025-05-20 10:15:00', N'Active'),
    (6, 9,  '2025-04-20 17:30:00', '2025-04-25 18:00:00', N'Converted'),
    (7, 10, '2025-05-01 12:15:00', '2025-05-22 17:10:00', N'Active');
SET IDENTITY_INSERT dbo.[CART] OFF;
GO

-- 3.2 CART_ITEM (Active items in Listener carts; album vs catalog items)
INSERT INTO dbo.[CART_ITEM] (CartID, ItemNo, AlbumID, CatalogID, Quantity, UnitPrice, AddedDate)
VALUES
    (1, 1, 6,    NULL, 1, 13.99, '2025-05-10 09:00:00'), -- Listener 4 has Album 6
    (2, 1, NULL, 2,    1, 19.99, '2025-05-12 14:30:00'), -- Listener 5 has Catalog 2
    (3, 1, 1,    NULL, 1,  9.99, '2025-05-15 11:00:00'), -- Listener 6 has Album 1
    (3, 2, 3,    NULL, 1, 12.99, '2025-05-15 11:20:00'), -- Listener 6 has Album 3
    (4, 1, NULL, 3,    1, 29.99, '2025-05-18 16:45:00'), -- Listener 7 has Catalog 3
    (5, 1, 2,    NULL, 1, 11.99, '2025-05-20 10:15:00'), -- Listener 8 has Album 2
    (7, 1, 5,    NULL, 1, 10.99, '2025-05-22 17:10:00'); -- Listener 10 has Album 5
GO

-- 3.3 ORDERS (8 Orders across Listeners)
SET IDENTITY_INSERT dbo.[ORDERS] ON;
INSERT INTO dbo.[ORDERS] (OrderID, ListenerID, OrderDate, OrderStatus, TotalAmount)
VALUES
    (1, 4,  '2025-02-15 10:30:00', N'Completed',  24.99), -- Catalog 1 purchase
    (2, 4,  '2025-02-18 14:20:00', N'Completed',  13.99), -- Album 6 purchase
    (3, 5,  '2025-02-25 16:45:00', N'Completed',  19.99), -- Catalog 2 purchase
    (4, 6,  '2025-03-05 11:15:00', N'Completed',  22.98), -- Album 1 + Album 3 purchase
    (5, 7,  '2025-03-20 09:50:00', N'Completed',  29.99), -- Catalog 3 purchase
    (6, 8,  '2025-04-10 15:30:00', N'Completed',  10.99), -- Album 5 purchase
    (7, 9,  '2025-04-25 18:00:00', N'Completed',   9.99), -- Album 1 purchase
    (8, 10, '2025-05-02 13:40:00', N'Pending',    14.99); -- Album 7 pending order
SET IDENTITY_INSERT dbo.[ORDERS] OFF;
GO

-- 3.4 ORDER_ITEM (Order Line Items with XOR constraint on AlbumID vs CatalogID)
INSERT INTO dbo.[ORDER_ITEM] (OrderID, ItemNo, AlbumID, CatalogID, Quantity, UnitPrice)
VALUES
    (1, 1, NULL, 1,    1, 24.99), -- Order 1: Catalog 1
    (2, 1, 6,    NULL, 1, 13.99), -- Order 2: Album 6
    (3, 1, NULL, 2,    1, 19.99), -- Order 3: Catalog 2
    (4, 1, 1,    NULL, 1,  9.99), -- Order 4: Album 1
    (4, 2, 3,    NULL, 1, 12.99), -- Order 4: Album 3
    (5, 1, NULL, 3,    1, 29.99), -- Order 5: Catalog 3
    (6, 1, 5,    NULL, 1, 10.99), -- Order 6: Album 5
    (7, 1, 1,    NULL, 1,  9.99), -- Order 7: Album 1
    (8, 1, 7,    NULL, 1, 14.99); -- Order 8: Album 7
GO

-- 3.5 PAYMENT (8 Payments for Orders 1 through 8, Unique OrderID and TxRef)
SET IDENTITY_INSERT dbo.[PAYMENT] ON;
INSERT INTO dbo.[PAYMENT] (PaymentID, OrderID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus)
VALUES
    (1, 1, '2025-02-15 10:32:00', 24.99, N'Credit Card', N'TXN-MM-20250215-00101', N'Completed'),
    (2, 2, '2025-02-18 14:22:00', 13.99, N'PayPal',      N'TXN-MM-20250218-00202', N'Completed'),
    (3, 3, '2025-02-25 16:47:00', 19.99, N'Credit Card', N'TXN-MM-20250225-00303', N'Completed'),
    (4, 4, '2025-03-05 11:18:00', 22.98, N'Apple Pay',   N'TXN-MM-20250305-00404', N'Completed'),
    (5, 5, '2025-03-20 09:52:00', 29.99, N'Credit Card', N'TXN-MM-20250320-00505', N'Completed'),
    (6, 6, '2025-04-10 15:33:00', 10.99, N'Google Pay',  N'TXN-MM-20250410-00606', N'Completed'),
    (7, 7, '2025-04-25 18:02:00',  9.99, N'PayPal',      N'TXN-MM-20250425-00707', N'Completed'),
    (8, 8, '2025-05-02 13:40:00', 14.99, N'Credit Card', N'TXN-MM-20250502-00808', N'Pending');
SET IDENTITY_INSERT dbo.[PAYMENT] OFF;
GO

-- ============================================================================
-- 4. PROMOTION & FAQ MODULE
-- ============================================================================

-- 4.1 PROMOTION (4 Promotions)
SET IDENTITY_INSERT dbo.[PROMOTION] ON;
INSERT INTO dbo.[PROMOTION] (PromotionID, PromotionName, Description, DiscountType, DiscountValue, StartDate, EndDate, PromotionStatus)
VALUES
    (1, N'Spring Synthwave & Electronic Festival', N'Enjoy 20% off all electronic, ambient, and synthwave master albums.',      N'Percentage',  20.00, '2025-03-01 00:00:00', '2025-04-30 23:59:59', N'Active'),
    (2, N'Indie Band Spotlight Discount',         N'Flat $3.00 instant discount on top indie rock and acoustic folk albums.',   N'FixedAmount',  3.00, '2025-02-01 00:00:00', '2025-03-31 23:59:59', N'Expired'),
    (3, N'Cinematic Masterpieces Gala',            N'15% off grand orchestral suites and cinematic soundtrack collections.',     N'Percentage',  15.00, '2025-06-01 00:00:00', '2025-08-31 23:59:59', N'Scheduled'),
    (4, N'New Artist Showcase Promo',              N'Flat $2.00 savings on debut studio albums across all genres.',              N'FixedAmount',  2.00, '2025-01-01 00:00:00', '2025-12-31 23:59:59', N'Active');
SET IDENTITY_INSERT dbo.[PROMOTION] OFF;
GO

-- 4.2 ALBUM_PROMOTION (Junction Table: M:N relationship between ALBUM and PROMOTION)
INSERT INTO dbo.[ALBUM_PROMOTION] (AlbumID, PromotionID)
VALUES
    (1, 1), -- Celestial Drift on Spring Synthwave Festival
    (2, 1), -- Neon Horizon on Spring Synthwave Festival
    (3, 2), -- Shadows in the Rain on Indie Band Spotlight
    (4, 2), -- Echoes of the Canyon on Indie Band Spotlight
    (7, 3), -- Symphony of Time on Cinematic Masterpieces Gala
    (8, 3), -- Whispers in Starlight on Cinematic Masterpieces Gala
    (6, 4); -- Solar Flare Anthem on New Artist Showcase Promo
GO

-- 4.3 FAQ (6 Frequently Asked Questions)
SET IDENTITY_INSERT dbo.[FAQ] ON;
INSERT INTO dbo.[FAQ] (FAQID, Question, Answer, FAQStatus, CreatedDate, UpdatedDate)
VALUES
    (1, N'How do I access and download my purchased music?',
        N'Once your payment is confirmed, navigate to My Account > Digital Library. Click the Download button next to any purchased album or song to download high-quality audio files directly to your device.',
        N'Published', '2025-01-15 10:00:00', '2025-01-15 10:00:00'),
    (2, N'What audio file formats are supported for downloads?',
        N'All music on MelodyMart is available in high-bitrate 320 kbps MP3 format. Select master edition releases also include studio-quality lossless FLAC downloads.',
        N'Published', '2025-01-15 10:05:00', '2025-01-15 10:05:00'),
    (3, N'Can I buy an entire catalog bundle instead of individual albums?',
        N'Yes! MelodyMart offers curated Catalog Bundles at substantial discounts compared to purchasing albums separately. A purchased catalog automatically unlocks all included albums in your digital library.',
        N'Published', '2025-01-18 14:00:00', '2025-01-18 14:00:00'),
    (4, N'What payment methods are supported on MelodyMart?',
        N'We securely accept Visa, MasterCard, American Express, PayPal, Apple Pay, and Google Pay through our PCI-DSS compliant payment processing gateway.',
        N'Published', '2025-01-20 09:30:00', '2025-01-20 09:30:00'),
    (5, N'How do I submit a review or rating for an album?',
        N'Go to the album detail page and scroll down to the Customer Reviews section. Select your star rating from 1 to 5 stars, type your thoughts, and click Submit Review.',
        N'Published', '2025-02-05 11:20:00', '2025-02-05 11:20:00'),
    (6, N'What should I do if my download is interrupted or fails?',
        N'Your purchased music remains permanently in your Digital Library. You can re-attempt the download at any time by clicking the Download button again, or submit a support ticket under Complaints.',
        N'Published', '2025-02-10 16:45:00', '2025-02-10 16:45:00');
SET IDENTITY_INSERT dbo.[FAQ] OFF;
GO

-- ============================================================================
-- 5. COMPLAINT & REVIEW MODULE
-- ============================================================================

-- 5.1 COMPLAINT (5 Complaints across Listeners)
SET IDENTITY_INSERT dbo.[COMPLAINT] ON;
INSERT INTO dbo.[COMPLAINT] (ComplaintID, ListenerID, Subject, Description, ComplaintDate, Status, Response, ResolvedDate)
VALUES
    (1, 4, N'Download token expired during download',
        N'Track 2 on Celestial Drift stopped downloading midway due to a network glitch and says token expired.',
        '2025-02-20 14:30:00', N'Resolved', N'Download session token reissued. Download verified successfully.', '2025-02-21 09:15:00'),
    (2, 5, N'Duplicate charge on Order #3',
        N'My credit card statement shows two identical charges of $19.99 for Order #3 on Feb 25.',
        '2025-02-26 11:00:00', N'Resolved', N'Duplicate charge confirmed and refund of $19.99 processed via gateway.', '2025-02-27 10:30:00'),
    (3, 6, N'Missing digital booklet PDF in archive',
        N'The promotional artwork and liner notes PDF was not included in the Shadows in the Rain zip archive.',
        '2025-03-08 17:15:00', N'In Progress', N'Support team is updating the album bundle asset package on CDN.', NULL),
    (4, 7, N'Promo discount code was not applied',
        N'I entered the Spring Promo coupon during checkout but full price was charged on Order #5.',
        '2025-03-22 10:20:00', N'Closed', N'Applied retroactive 15% discount and credited $4.50 to customer account.', '2025-03-23 15:00:00'),
    (5, 8, N'Audio clipping on Track 14 Late Night Espresso',
        N'There appears to be digital clipping distortion at timestamp 02:15 on the MP3 file.',
        '2025-04-12 18:45:00', N'Open', NULL, NULL);
SET IDENTITY_INSERT dbo.[COMPLAINT] OFF;
GO

-- 5.2 REVIEW (8 Reviews across Listeners and Albums)
SET IDENTITY_INSERT dbo.[REVIEW] ON;
INSERT INTO dbo.[REVIEW] (ReviewID, ListenerID, AlbumID, Rating, Comment, ReviewDate, ReviewStatus)
VALUES
    (1, 4, 1, 5, N'A masterclass in atmospheric ambient sound design. Starlight Awakening is breathtaking!', '2025-02-18 16:00:00', N'Approved'),
    (2, 4, 6, 5, N'Pure energy! Peak Velocity has become the centerpiece of my morning workout playlist.',        '2025-02-22 19:30:00', N'Approved'),
    (3, 5, 4, 4, N'Warm, resonant fingerstyle acoustic work. Perfect for unwinding with a book on a rainy day.',  '2025-03-02 12:45:00', N'Approved'),
    (4, 5, 5, 5, N'Midnight Velvet delivers authentic neo-soul magic. The brass section and Rhodes keys shine!',  '2025-03-10 20:15:00', N'Approved'),
    (5, 6, 3, 4, N'Dynamic indie alternative record with memorable guitar hooks and great mixing.',               '2025-03-16 14:10:00', N'Approved'),
    (6, 7, 7, 5, N'Symphony of Time is monumental. Cello Requiem gave me chills from start to finish.',           '2025-03-28 11:30:00', N'Approved'),
    (7, 8, 5, 4, N'Smooth grooves, tight pocket drums, and expressive vocals. Very well produced.',                '2025-04-15 15:20:00', N'Approved'),
    (8, 9, 1, 5, N'Incredibly calming and beautifully mixed ambient music. Highly recommended.',                  '2025-04-28 09:50:00', N'Approved');
SET IDENTITY_INSERT dbo.[REVIEW] OFF;
GO

-- ============================================================================
-- 6. DIGITAL LIBRARY & DOWNLOAD MODULE
-- ============================================================================

-- 6.1 DIGITAL_LIBRARY (7 Libraries: 1 per Listener, Unique ListenerID)
SET IDENTITY_INSERT dbo.[DIGITAL_LIBRARY] ON;
INSERT INTO dbo.[DIGITAL_LIBRARY] (LibraryID, ListenerID, CreatedDate, LastUpdatedDate)
VALUES
    (1, 4,  '2025-02-10 11:20:00', '2025-02-18 14:25:00'),
    (2, 5,  '2025-02-14 14:45:00', '2025-02-25 16:50:00'),
    (3, 6,  '2025-03-01 16:10:00', '2025-03-05 11:20:00'),
    (4, 7,  '2025-03-15 13:00:00', '2025-03-20 09:55:00'),
    (5, 8,  '2025-04-01 09:40:00', '2025-04-10 15:35:00'),
    (6, 9,  '2025-04-20 17:25:00', '2025-04-25 18:05:00'),
    (7, 10, '2025-05-01 12:05:00', '2025-05-01 12:05:00');
SET IDENTITY_INSERT dbo.[DIGITAL_LIBRARY] OFF;
GO

-- 6.2 LIBRARY_ITEMS (12 Library Items: Albums unlocked via Catalog bundle vs standalone Album purchase)
-- Enforces: UQ_LIBRARY_ITEMS_LibraryAlbum (LibraryID, AlbumID), CK_LIBRARY_ITEMS_PurchaseType ('Album', 'Catalog'), and CK_LIBRARY_ITEMS_CatalogRef
SET IDENTITY_INSERT dbo.[LIBRARY_ITEMS] ON;
INSERT INTO dbo.[LIBRARY_ITEMS] (LibraryItemID, LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus)
VALUES
    -- Listener 4: Unlocked Albums 1, 2, 3 via Catalog 1, plus Album 6 as standalone Album
    (1,  1, 1, 1,    '2025-02-15 10:35:00', N'Catalog', N'Active'),
    (2,  1, 2, 1,    '2025-02-15 10:35:00', N'Catalog', N'Active'),
    (3,  1, 3, 1,    '2025-02-15 10:35:00', N'Catalog', N'Active'),
    (4,  1, 6, NULL, '2025-02-18 14:25:00', N'Album',   N'Active'),
    -- Listener 5: Unlocked Albums 4, 5 via Catalog 2
    (5,  2, 4, 2,    '2025-02-25 16:50:00', N'Catalog', N'Active'),
    (6,  2, 5, 2,    '2025-02-25 16:50:00', N'Catalog', N'Active'),
    -- Listener 6: Unlocked Albums 1 and 3 as standalone Albums
    (7,  3, 1, NULL, '2025-03-05 11:20:00', N'Album',   N'Active'),
    (8,  3, 3, NULL, '2025-03-05 11:20:00', N'Album',   N'Active'),
    -- Listener 7: Unlocked Albums 7 and 8 via Catalog 3
    (9,  4, 7, 3,    '2025-03-20 09:55:00', N'Catalog', N'Active'),
    (10, 4, 8, 3,    '2025-03-20 09:55:00', N'Catalog', N'Active'),
    -- Listener 8: Unlocked Album 5 as standalone Album
    (11, 5, 5, NULL, '2025-04-10 15:35:00', N'Album',   N'Active'),
    -- Listener 9: Unlocked Album 1 as standalone Album
    (12, 6, 1, NULL, '2025-04-25 18:05:00', N'Album',   N'Active');
SET IDENTITY_INSERT dbo.[LIBRARY_ITEMS] OFF;
GO

-- 6.3 DOWNLOAD (12 Download logs across Library Items)
SET IDENTITY_INSERT dbo.[DOWNLOAD] ON;
INSERT INTO dbo.[DOWNLOAD] (DownloadID, LibraryItemID, DownloadDateTime, FileFormat, FileSize, DownloadStatus)
VALUES
    (1,   1, '2025-02-15 10:40:00', N'MP3',   85400000, N'Completed'), -- ~85.4 MB
    (2,   2, '2025-02-15 10:48:00', N'MP3',   92100000, N'Completed'), -- ~92.1 MB
    (3,   3, '2025-02-15 11:05:00', N'MP3',  104800000, N'Completed'), -- ~104.8 MB
    (4,   4, '2025-02-18 14:30:00', N'FLAC', 285600000, N'Completed'), -- ~285.6 MB
    (5,   5, '2025-02-25 17:00:00', N'MP3',   78200000, N'Completed'), -- ~78.2 MB
    (6,   6, '2025-02-25 17:15:00', N'MP3',   88900000, N'Completed'), -- ~88.9 MB
    (7,   7, '2025-03-05 11:30:00', N'MP3',   85400000, N'Completed'), -- ~85.4 MB
    (8,   8, '2025-03-05 11:45:00', N'FLAC', 262400000, N'Completed'), -- ~262.4 MB
    (9,   9, '2025-03-20 10:05:00', N'MP3',  120500000, N'Completed'), -- ~120.5 MB
    (10, 10, '2025-03-20 10:25:00', N'MP3',  115200000, N'Completed'), -- ~115.2 MB
    (11, 11, '2025-04-10 15:45:00', N'MP3',   88900000, N'Completed'), -- ~88.9 MB
    (12, 12, '2025-04-25 18:15:00', N'MP3',   85400000, N'Completed'); -- ~85.4 MB
SET IDENTITY_INSERT dbo.[DOWNLOAD] OFF;
GO
