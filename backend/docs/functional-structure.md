# MelodyMart Functional File Structure

The project has been reorganized physically around its main functional capabilities while strictly maintaining the core Java Spring Boot MVC / Data JPA architecture (`Controller` -> `Service` -> `Repository` -> `Model/Entity` -> `Database`). The existing database configuration, specifically the disabled DDL auto-generation (`spring.jpa.hibernate.ddl-auto=none`) and the full 21-table schema have been perfectly preserved without alteration. 

No underlying business logic, session/authentication handling, URL structures, or backend entity data structures have been modified.

### 1. Album & Catalog Management
*Package/Folder: `com.melodymart.albumcatalog` / `templates/album-catalog/`*
Contains all components related to albums, catalogs, displaying browse listings, and full details for both modules.
* **Models**: `Album`, `Catalog`
* **Repositories**: `AlbumRepository`, `CatalogRepository`
* **Services**: `AlbumService`
* **Controllers**: `AlbumController`
* **Templates**: `albums.html`, `album-detail.html`, `catalogs.html`, `catalog-detail.html`

### 2. Artist, Genre & Track Management
*Package/Folder: `com.melodymart.artistgenretrack` / `templates/artist-genre-track/`*
Stores underlying entities corresponding directly to internal media data representation decoupled from higher level albums and catalogs.
* **Models**: `Artist`, `Genre`, `Track`
* **Repositories**: `ArtistRepository`, `GenreRepository`, `TrackRepository`

### 3. Order & Sales
*Package/Folder: `com.melodymart.ordersales` / `templates/order-sales/`*
Holds complete checkout funnel mechanics, tracking payments, order invoices, items mappings and active cart state.
* **Models**: `Cart`, `CartItem`, `CartItemId`, `Order`, `OrderItem`, `Payment`
* **Repositories**: `CartRepository`, `CartItemRepository`, `OrderRepository`, `OrderItemRepository`, `PaymentRepository`
* **Services**: `CartService`
* **Controllers**: `CartController`
* **Templates**: `cart.html`, `checkout.html`, `checkout-success.html`, `orders.html`, `order-detail.html`

### 4. Digital Library & Playlist
*Package/Folder: `com.melodymart.libraryplaylist` / `templates/digital-library-playlist/`*
Dictates listener access logic tied to digital items, handling active file downloads and complex playlist structures (where playlist functionalities specifically remain restricted to verified owners).
* **Models**: `DigitalLibrary`, `LibraryItem`, `Playlist`, `PlaylistItem`, `Download`
* **Repositories**: `DigitalLibraryRepository`, `LibraryItemRepository`, `PlaylistRepository`, `PlaylistItemRepository`, `DownloadRepository`
* **Services**: `PlaylistService`
* **Controllers**: `PlaylistController`
* **Templates**: `my-library.html`, `playlists.html`, `playlist-form.html`, `playlist-edit.html`, `playlist-detail.html`

### 5. FAQ & Promotion
*Package/Folder: `com.melodymart.faqpromotion` / `templates/faq-promotion/`*
Manages content marketing displays (promotional hero areas) and informational FAQs for user support.
* **Models**: `Faq`, `Promotion`
* **Repositories**: `FaqRepository`, `PromotionRepository`
* **Services**: `FaqService`, `PromotionService`
* **Templates**: `faqs.html`, `promotions.html`

### 6. Complaint & Review
*Package/Folder: `com.melodymart.complaintreview` / `templates/complaint-review/`*
Isolates user sentiment features, allowing complaint submission against staff processes and user reviews logged per album.
* **Models**: `Complaint`, `Review`
* **Repositories**: `ComplaintRepository`, `ReviewRepository`
* **Services**: `ComplaintService`, `ReviewService`
* **Controllers**: `ComplaintController`
* **Templates**: `complaints.html`, `complaint-form.html`, `complaint-detail.html`

### 7. Common & Shared Infrastructure
*Package/Folder: `com.melodymart.common` / `templates/common/` (plus `templates/admin/`, `templates/fragments/`)*
Houses shared architecture that cuts across standard functional partitions, serving as universal utilities, core security features, cross-platform UI blocks and master dashboard controllers.
* **Models**: `User`, `Administrator`, `Listener`
* **Repositories**: `UserRepository`, `AdministratorRepository`, `ListenerRepository`
* **Services**: `AdminService`, `ListenerService`, `AuthService`
* **Controllers**: `AdminController`, `ListenerController`, `AuthController`, `ProfileController`
* **Templates**: `login.html`, `register.html`, `profile.html`, `index.html` (and all `admin/` / `fragments/` subfolders).
