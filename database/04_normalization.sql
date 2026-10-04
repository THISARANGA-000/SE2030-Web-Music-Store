-- ============================================================================
-- Script: 04_normalization.sql
-- Project: MelodyMart Web-Based Music Store
-- DBMS: Microsoft SQL Server
-- Description: Comprehensive Normalization Analysis & Verification Script.
--              Documents UNF -> 1NF -> 2NF -> 3NF transformations, functional
--              dependencies, candidate keys, partial/transitive dependency proofs,
--              LIBRARY_ITEMS provenance resolution, complete 21-table normalization
--              summary matrix, and practical SQL queries demonstrating normalized JOINs.
-- ============================================================================

USE MelodyMartDB;
GO

/*
==============================================================================
1. NORMALIZATION PROCESS OVERVIEW
==============================================================================

1.1 UNNORMALIZED FORM (UNF) -> FIRST NORMAL FORM (1NF)
------------------------------------------------------------------------------
- Definition of UNF: Raw data structured with multi-valued attributes, repeating
  groups, non-atomic attributes, or unformatted nested structures.
- Transformation Steps to 1NF:
  1. Remove Repeating Groups: Multi-item records (such as multiple items in an
     order or cart, or multiple track tracks in an album) were extracted into
     dedicated separate relations (e.g., ORDER_ITEM, CART_ITEM, TRACK).
  2. Decompose Composite Attributes: User full names were split into atomic
     FirstName and LastName attributes.
  3. Establish Primary Keys: Every relation was assigned a unique Primary Key
     (surrogate IDENTITY integer or natural primary/composite key).
  4. Ensure Scalar Domain Values: All columns store strictly atomic, single-valued
     data types (e.g., INT, DATETIME2, DECIMAL, NVARCHAR).

1.2 FIRST NORMAL FORM (1NF) -> SECOND NORMAL FORM (2NF)
------------------------------------------------------------------------------
- Definition of 2NF: The relation must be in 1NF and every non-prime attribute
  must be fully functionally dependent on the ENTIRE primary key (no partial
  dependencies).
- Transformation Steps to 2NF:
  1. Single-Attribute Primary Keys: Relations with single-column primary keys
     (USER, ARTIST, GENRE, Catalog, ALBUM, CART, ORDERS, PAYMENT, PROMOTION,
     FAQ, COMPLAINT, REVIEW, DIGITAL_LIBRARY, LIBRARY_ITEMS, DOWNLOAD) automatically
     satisfy 2NF because proper subsets of a single-attribute key do not exist.
  2. Composite Primary Keys: For relations with composite primary keys:
     - CART_ITEM (CartID, ItemNo): Attributes (AlbumID, CatalogID, Quantity,
       UnitPrice, AddedDate) depend on both CartID AND ItemNo. ItemNo alone does
       not uniquely identify an item across different carts.
     - ORDER_ITEM (OrderID, ItemNo): Attributes (AlbumID, CatalogID, Quantity,
       UnitPrice) depend on both OrderID AND ItemNo.
     - ALBUM_PROMOTION (AlbumID, PromotionID): Pure junction table with no non-key
       attributes; trivially in 2NF.
     - TRACK (Candidate key: AlbumID, TrackNumber): Non-key attributes (TrackTitle,
       Duration, AudioFileURL) depend on the combination of AlbumID AND TrackNumber.

1.3 SECOND NORMAL FORM (2NF) -> THIRD NORMAL FORM (3NF)
------------------------------------------------------------------------------
- Definition of 3NF: The relation must be in 2NF and NO non-prime attribute may be
  transitively dependent on the primary key (no non-key attribute determines
  another non-key attribute: X -> Y implies X is a superkey).
- Transformation Steps to 3NF:
  1. Point-in-Time Price Snapshots: In CART_ITEM and ORDER_ITEM, UnitPrice stores
     the historical transaction price at purchase time. This eliminates a transitive
     dependency on current ALBUM.Price or Catalog.Price.
  2. Candidate Keys: Unique constraints on candidate keys (USER.Email, GENRE.GenreName,
     PAYMENT.TransactionReference, PAYMENT.OrderID, DIGITAL_LIBRARY.ListenerID,
     CART.ListenerID) maintain functional dependencies on superkeys.
  3. Class-Table Inheritance: USER attributes (FirstName, Email, etc.) are kept in
     USER superclass. Subclasses ADMINISTRATOR and LISTENER reference UserID as PK/FK,
     preventing NULL pollution and transitive dependencies.

==============================================================================
2. FUNCTIONAL DEPENDENCIES (FDs) FOR IMPORTANT RELATIONS
==============================================================================

- dbo.[USER]:
    FD1: UserID -> {FirstName, LastName, Email, PasswordHash, PhoneNumber, RegistrationDate, AccountStatus}
    FD2: Email -> {UserID, FirstName, LastName, PasswordHash, PhoneNumber, RegistrationDate, AccountStatus} (Candidate Key)

- dbo.[ADMINISTRATOR]:
    FD1: AdminID -> {AdminRole}

- dbo.[LISTENER]:
    FD1: ListenerID -> {} (inherits USER attributes)

- dbo.[ARTIST]:
    FD1: ArtistID -> {ArtistName, Biography, Country}

- dbo.[GENRE]:
    FD1: GenreID -> {GenreName, Description}
    FD2: GenreName -> {GenreID, Description} (Candidate Key)

- dbo.[Catalog]:
    FD1: CatalogID -> {CatalogName, Description, Price, CreatedDate}

- dbo.[ALBUM]:
    FD1: AlbumID -> {AlbumTitle, ReleaseDate, AlbumStatus, Price, Description, CoverImageURL, ArtistID, GenreID, CatalogID}

- dbo.[TRACK]:
    FD1: TrackID -> {AlbumID, TrackTitle, TrackNumber, Duration, AudioFileURL}
    FD2: (AlbumID, TrackNumber) -> {TrackID, TrackTitle, Duration, AudioFileURL} (Candidate Key)

- dbo.[CART]:
    FD1: CartID -> {ListenerID, CreatedDate, LastUpdatedDate, CartStatus}
    FD2: ListenerID -> {CartID, CreatedDate, LastUpdatedDate, CartStatus} (Candidate Key)

- dbo.[CART_ITEM]:
    FD1: (CartID, ItemNo) -> {AlbumID, CatalogID, Quantity, UnitPrice, AddedDate}

- dbo.[ORDERS]:
    FD1: OrderID -> {ListenerID, OrderDate, OrderStatus, TotalAmount}

- dbo.[ORDER_ITEM]:
    FD1: (OrderID, ItemNo) -> {AlbumID, CatalogID, Quantity, UnitPrice}

- dbo.[PAYMENT]:
    FD1: PaymentID -> {OrderID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus}
    FD2: OrderID -> {PaymentID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus} (Candidate Key)
    FD3: TransactionReference -> {PaymentID, OrderID, PaymentDate, Amount, PaymentMethod, PaymentStatus} (Candidate Key)

- dbo.[PROMOTION]:
    FD1: PromotionID -> {PromotionName, Description, DiscountType, DiscountValue, StartDate, EndDate, PromotionStatus}

- dbo.[ALBUM_PROMOTION]:
    FD1: (AlbumID, PromotionID) -> {}

- dbo.[FAQ]:
    FD1: FAQID -> {Question, Answer, FAQStatus, CreatedDate, UpdatedDate}

- dbo.[COMPLAINT]:
    FD1: ComplaintID -> {ListenerID, Subject, Description, ComplaintDate, Status, Response, ResolvedDate}

- dbo.[REVIEW]:
    FD1: ReviewID -> {ListenerID, AlbumID, Rating, Comment, ReviewDate, ReviewStatus}

- dbo.[DIGITAL_LIBRARY]:
    FD1: LibraryID -> {ListenerID, CreatedDate, LastUpdatedDate}
    FD2: ListenerID -> {LibraryID, CreatedDate, LastUpdatedDate} (Candidate Key)

- dbo.[LIBRARY_ITEMS]:
    FD1: LibraryItemID -> {LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus}
    FD2: (LibraryID, AlbumID) -> {LibraryItemID, CatalogID, AddedDate, PurchaseType, AccessStatus} (Candidate Key)

- dbo.[DOWNLOAD]:
    FD1: DownloadID -> {LibraryItemID, DownloadDateTime, FileFormat, FileSize, DownloadStatus}

==============================================================================
3. PRIMARY KEYS, CANDIDATE KEYS & DEPENDENCY ANALYSIS
==============================================================================

3.1 CANDIDATE KEYS AND PRIMARY KEYS SUMMARY
------------------------------------------------------------------------------
Table              Primary Key               Alternate Candidate Key(s)
------------------------------------------------------------------------------
USER               UserID                    Email
ADMINISTRATOR      AdminID                   -
LISTENER           ListenerID                -
ARTIST             ArtistID                  -
GENRE              GenreID                   GenreName
Catalog            CatalogID                 -
ALBUM              AlbumID                   -
TRACK              TrackID                   (AlbumID, TrackNumber)
CART               CartID                    ListenerID
CART_ITEM          (CartID, ItemNo)          -
ORDERS             OrderID                   -
ORDER_ITEM         (OrderID, ItemNo)         -
PAYMENT            PaymentID                 OrderID, TransactionReference
PROMOTION          PromotionID               -
ALBUM_PROMOTION    (AlbumID, PromotionID)    -
FAQ                FAQID                     -
COMPLAINT          ComplaintID               -
REVIEW             ReviewID                  -
DIGITAL_LIBRARY    LibraryID                 ListenerID
LIBRARY_ITEMS      LibraryItemID             (LibraryID, AlbumID)
DOWNLOAD           DownloadID                -

3.2 PARTIAL DEPENDENCY ANALYSIS
------------------------------------------------------------------------------
- Definition: A partial dependency occurs when a non-prime attribute depends on
  only a part of a composite primary key.
- Analysis:
  - In CART_ITEM(CartID, ItemNo), Quantity, UnitPrice, AddedDate, AlbumID, and
    CatalogID depend on the COMBINATION of CartID and ItemNo. CartID alone only
    identifies the cart container, not the item. ItemNo alone is a relative sequence
    number. Therefore, NO partial dependency exists.
  - In ORDER_ITEM(OrderID, ItemNo), Quantity, UnitPrice, AlbumID, and CatalogID
    depend on the COMBINATION of OrderID and ItemNo. Therefore, NO partial dependency exists.
  - In ALBUM_PROMOTION(AlbumID, PromotionID), there are no non-prime attributes.
    Therefore, partial dependencies are impossible.
  - All other 18 tables have single-attribute primary keys, rendering partial
    dependencies mathematically impossible.
- Conclusion: 0 Partial Dependencies found. All relations satisfy 2NF.

3.3 TRANSITIVE DEPENDENCY ANALYSIS
------------------------------------------------------------------------------
- Definition: A transitive dependency occurs when X -> Y and Y -> Z, where X is a
  primary key and Y & Z are non-prime attributes (a non-key attribute determining
  another non-key attribute).
- Analysis:
  - In USER: FirstName, LastName, Email, PasswordHash, PhoneNumber, RegistrationDate,
    and AccountStatus depend directly on UserID. Email is a candidate key, not a
    non-prime attribute.
  - In ALBUM: Price, Title, ReleaseDate, ArtistID, GenreID, CatalogID depend directly
    on AlbumID. GenreID and ArtistID are foreign keys pointing to independent relations;
    genre details are stored in GENRE, not ALBUM.
  - In ORDER_ITEM: UnitPrice captures historical price at order time. It does NOT
    depend on ALBUM.Price (which can change independently over time).
  - In PAYMENT: OrderID and TransactionReference are candidate keys, so functional
    dependencies on them do not violate 3NF.
- Conclusion: 0 Transitive Dependencies found. All relations satisfy 3NF.

3.4 PROOF FOR COMPOSITE-KEY TABLES (CART_ITEM, ORDER_ITEM, ALBUM_PROMOTION)
------------------------------------------------------------------------------
- CART_ITEM:
  - 1NF: Composite PK (CartID, ItemNo), all fields scalar.
  - 2NF: No partial dependency. Item details depend on (CartID, ItemNo).
  - 3NF: No transitive dependency. UnitPrice is historical snapshot data.
- ORDER_ITEM:
  - 1NF: Composite PK (OrderID, ItemNo), all fields scalar.
  - 2NF: No partial dependency. Line item details depend on (OrderID, ItemNo).
  - 3NF: No transitive dependency. UnitPrice is historical snapshot data.
- ALBUM_PROMOTION:
  - 1NF: Composite PK (AlbumID, PromotionID).
  - 2NF: Satisfied trivially (no non-prime attributes).
  - 3NF: Satisfied trivially (no non-prime attributes).

3.5 PROOF FOR SINGLE-KEY ENTITIES (USER, ALBUM, ORDERS, PAYMENT, REVIEW, ETC.)
------------------------------------------------------------------------------
- USER: Single PK UserID. Candidate key Email. All non-key fields depend on key. 3NF satisfied.
- ALBUM: Single PK AlbumID. Foreign keys ArtistID, GenreID, CatalogID decouple entity dependencies. 3NF satisfied.
- ORDERS: Single PK OrderID. ListenerID FK links to listener. TotalAmount is operational summary. 3NF satisfied.
- PAYMENT: Single PK PaymentID. Candidate keys OrderID and TransactionReference. 3NF satisfied.
- REVIEW: Single PK ReviewID. Rating, Comment, ReviewDate depend on ReviewID. Foreign keys ListenerID, AlbumID link entities. 3NF satisfied.

3.6 PROVENANCE CORRECTION: LIBRARY_ITEMS WITH CatalogID
------------------------------------------------------------------------------
- Original Issue: LIBRARY_ITEMS recorded PurchaseType = 'Catalog' when a listener
  purchased a catalog bundle, but omitted a CatalogID foreign key attribute.
- Problem: Discarded foreign key reference to Catalog, causing loss of purchase provenance.
- Solution Implemented:
  1. Added CatalogID INT NULL attribute referencing dbo.Catalog(CatalogID).
  2. Added CHECK constraint CK_LIBRARY_ITEMS_CatalogRef:
     - PurchaseType = 'Album'   -> CatalogID IS NULL
     - PurchaseType = 'Catalog' -> CatalogID IS NOT NULL
- Normalization Proof: FD: LibraryItemID -> {LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus}.
  CatalogID is fully functionally dependent on the key. 3NF is preserved while referential integrity is fully restored.

==============================================================================
4. COMPLETE 21-TABLE NORMALIZATION SUMMARY MATRIX
==============================================================================

----------------------------------------------------------------------------------------------
Table Name         Primary Key              1NF  2NF  3NF  Normalization Status & Notes
----------------------------------------------------------------------------------------------
USER               UserID                   Yes  Yes  Yes  3NF (Candidate Key: Email)
ADMINISTRATOR      AdminID                  Yes  Yes  Yes  3NF (Class-Table Inheritance FK->USER)
LISTENER           ListenerID               Yes  Yes  Yes  3NF (Class-Table Inheritance FK->USER)
ARTIST             ArtistID                 Yes  Yes  Yes  3NF (Independent Entity)
GENRE              GenreID                  Yes  Yes  Yes  3NF (Candidate Key: GenreName)
Catalog            CatalogID                Yes  Yes  Yes  3NF (Bundle Entity)
ALBUM              AlbumID                  Yes  Yes  Yes  3NF (FKs: ArtistID, GenreID, CatalogID)
TRACK              TrackID                  Yes  Yes  Yes  3NF (Candidate Key: AlbumID, TrackNumber)
CART               CartID                   Yes  Yes  Yes  3NF (Candidate Key: ListenerID)
CART_ITEM          (CartID, ItemNo)         Yes  Yes  Yes  3NF (Weak Entity; Full FD on Composite PK)
ORDERS             OrderID                  Yes  Yes  Yes  3NF (Header Table; Financial Total Snapshot)
ORDER_ITEM         (OrderID, ItemNo)        Yes  Yes  Yes  3NF (Weak Entity; Point-in-Time UnitPrice)
PAYMENT            PaymentID                Yes  Yes  Yes  3NF (Candidate Keys: OrderID, TransactionRef)
PROMOTION          PromotionID              Yes  Yes  Yes  3NF (Independent Promotion Entity)
ALBUM_PROMOTION    (AlbumID, PromotionID)   Yes  Yes  Yes  3NF (Pure N:M Junction Table)
FAQ                FAQID                    Yes  Yes  Yes  3NF (Support Content Entity)
COMPLAINT          ComplaintID              Yes  Yes  Yes  3NF (Customer Support Entity)
REVIEW             ReviewID                 Yes  Yes  Yes  3NF (Album Review Entity)
DIGITAL_LIBRARY    LibraryID                Yes  Yes  Yes  3NF (Candidate Key: ListenerID)
LIBRARY_ITEMS      LibraryItemID            Yes  Yes  Yes  3NF (Candidate Key: LibraryID, AlbumID; CatalogID FK restored)
DOWNLOAD           DownloadID               Yes  Yes  Yes  3NF (Download History Log Entity)
----------------------------------------------------------------------------------------------

*/
-- ============================================================================
-- 5. PRACTICAL DEMONSTRATION QUERIES USING NORMALIZED JOINs
-- ============================================================================

PRINT '==============================================================================';
PRINT 'DEMONSTRATION QUERY 1: Normalized Sales Breakdown & Line-Item Price Verification';
PRINT '==============================================================================';
GO

SELECT 
    o.OrderID,
    u.FirstName + ' ' + u.LastName AS ListenerName,
    u.Email,
    o.OrderDate,
    oi.ItemNo,
    COALESCE(a.AlbumTitle, c.CatalogName) AS PurchasedItemName,
    CASE 
        WHEN oi.AlbumID IS NOT NULL THEN 'Album'
        ELSE 'Catalog Bundle'
    END AS ItemType,
    oi.Quantity,
    oi.UnitPrice AS OrderUnitPrice,
    (oi.Quantity * oi.UnitPrice) AS LineTotal,
    p.PaymentMethod,
    p.TransactionReference,
    p.PaymentStatus
FROM dbo.[ORDERS] o
INNER JOIN dbo.[LISTENER] l ON o.ListenerID = l.ListenerID
INNER JOIN dbo.[USER] u ON l.ListenerID = u.UserID
INNER JOIN dbo.[ORDER_ITEM] oi ON o.OrderID = oi.OrderID
LEFT JOIN dbo.[ALBUM] a ON oi.AlbumID = a.AlbumID
LEFT JOIN dbo.[Catalog] c ON oi.CatalogID = c.CatalogID
INNER JOIN dbo.[PAYMENT] p ON o.OrderID = p.OrderID
ORDER BY o.OrderID, oi.ItemNo;
GO

PRINT '==============================================================================';
PRINT 'DEMONSTRATION QUERY 2: Digital Library Provenance & CatalogID Linkage Verification';
PRINT '==============================================================================';
GO

SELECT 
    dl.LibraryID,
    u.FirstName + ' ' + u.LastName AS ListenerName,
    li.LibraryItemID,
    a.AlbumTitle,
    art.ArtistName,
    li.PurchaseType,
    li.CatalogID,
    c.CatalogName AS OriginatingCatalog,
    li.AddedDate,
    li.AccessStatus
FROM dbo.[DIGITAL_LIBRARY] dl
INNER JOIN dbo.[USER] u ON dl.ListenerID = u.UserID
INNER JOIN dbo.[LIBRARY_ITEMS] li ON dl.LibraryID = li.LibraryID
INNER JOIN dbo.[ALBUM] a ON li.AlbumID = a.AlbumID
INNER JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID
LEFT JOIN dbo.[Catalog] c ON li.CatalogID = c.CatalogID
ORDER BY dl.LibraryID, li.LibraryItemID;
GO

PRINT '==============================================================================';
PRINT 'DEMONSTRATION QUERY 3: N:M Junction Table Query (ALBUM_PROMOTION)';
PRINT '==============================================================================';
GO

SELECT 
    p.PromotionID,
    p.PromotionName,
    p.DiscountType,
    p.DiscountValue,
    a.AlbumID,
    a.AlbumTitle,
    art.ArtistName,
    a.Price AS StandardPrice,
    CASE 
        WHEN p.DiscountType = 'Percentage' THEN ROUND(a.Price * (1.00 - (p.DiscountValue / 100.00)), 2)
        WHEN p.DiscountType = 'FixedAmount' THEN CASE WHEN (a.Price - p.DiscountValue) < 0 THEN 0.00 ELSE (a.Price - p.DiscountValue) END
    END AS PromotionalPrice
FROM dbo.[PROMOTION] p
INNER JOIN dbo.[ALBUM_PROMOTION] ap ON p.PromotionID = ap.PromotionID
INNER JOIN dbo.[ALBUM] a ON ap.AlbumID = a.AlbumID
INNER JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID
ORDER BY p.PromotionID, a.AlbumID;
GO

PRINT '==============================================================================';
PRINT 'DEMONSTRATION QUERY 4: Weak Entity Candidate Key Query (TRACK)';
PRINT '==============================================================================';
GO

SELECT 
    a.AlbumID,
    a.AlbumTitle,
    art.ArtistName,
    g.GenreName,
    t.TrackNumber,
    t.TrackTitle,
    t.Duration AS DurationSeconds,
    t.AudioFileURL
FROM dbo.[ALBUM] a
INNER JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID
INNER JOIN dbo.[GENRE] g ON a.GenreID = g.GenreID
INNER JOIN dbo.[TRACK] t ON a.AlbumID = t.AlbumID
ORDER BY a.AlbumID, t.TrackNumber;
GO

PRINT '==============================================================================';
PRINT 'DEMONSTRATION QUERY 5: Class-Table Inheritance & Support Query (USER -> COMPLAINT)';
PRINT '==============================================================================';
GO

SELECT 
    c.ComplaintID,
    u.FirstName + ' ' + u.LastName AS ListenerName,
    u.Email,
    c.Subject,
    c.Description,
    c.ComplaintDate,
    c.Status,
    c.Response,
    c.ResolvedDate
FROM dbo.[COMPLAINT] c
INNER JOIN dbo.[LISTENER] l ON c.ListenerID = l.ListenerID
INNER JOIN dbo.[USER] u ON l.ListenerID = u.UserID
ORDER BY c.ComplaintID;
GO
