package com.melodymart.common.service;
import com.melodymart.artistgenretrack.repository.TrackRepository;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.common.model.Listener;
import com.melodymart.artistgenretrack.model.Track;
import com.melodymart.artistgenretrack.repository.GenreRepository;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.common.model.User;
import com.melodymart.common.repository.ListenerRepository;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.repository.ArtistRepository;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.artistgenretrack.repository.ArtistRepository;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.artistgenretrack.repository.GenreRepository;
import com.melodymart.common.repository.ListenerRepository;
import com.melodymart.artistgenretrack.repository.TrackRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class AdminService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final ListenerRepository listenerRepository;
    private final GenreRepository genreRepository;
    private final CatalogRepository catalogRepository;
    private final TrackRepository trackRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public AdminService(AlbumRepository albumRepository,
                        ArtistRepository artistRepository,
                        ListenerRepository listenerRepository,
                        GenreRepository genreRepository,
                        CatalogRepository catalogRepository,
                        TrackRepository trackRepository) {
        this.albumRepository  = albumRepository;
        this.artistRepository  = artistRepository;
        this.listenerRepository = listenerRepository;
        this.genreRepository   = genreRepository;
        this.catalogRepository = catalogRepository;
        this.trackRepository   = trackRepository;
    }

    // ── Utility ───────────────────────────────────────────────────────────────

    private long nativeCount(String sql) {
        try {
            List<?> r = em.createNativeQuery(sql).getResultList();
            if (!r.isEmpty()) return ((Number) r.get(0)).longValue();
        } catch (Exception ignored) {}
        return 0L;
    }

    @SuppressWarnings("unchecked")
    private List<Object[]> nativeList(String sql) {
        try { return em.createNativeQuery(sql).getResultList(); }
        catch (Exception e) { return List.of(); }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DASHBOARD STATS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public Map<String, Object> getDashboardStats() {
        Map<String, Object> s = new HashMap<>();
        s.put("albumCount",    albumRepository.count());
        s.put("artistCount",   artistRepository.count());
        s.put("listenerCount", listenerRepository.count());
        s.put("genreCount",    genreRepository.count());
        s.put("catalogCount",  catalogRepository.count());
        s.put("adminCount",    nativeCount("SELECT COUNT(*) FROM dbo.[ADMINISTRATOR]"));
        s.put("userCount",     nativeCount("SELECT COUNT(*) FROM dbo.[USER]"));
        s.put("orderCount",    nativeCount("SELECT COUNT(*) FROM dbo.[ORDERS]"));
        s.put("totalRevenue",  getRevenue());
        return s;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCatalogStats() {
        Map<String, Object> s = new HashMap<>();
        s.put("albumCount",   albumRepository.count());
        s.put("artistCount",  artistRepository.count());
        s.put("genreCount",   genreRepository.count());
        s.put("catalogCount", catalogRepository.count());
        return s;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getOrderStats() {
        Map<String, Object> s = new HashMap<>();
        s.put("totalOrders",     nativeCount("SELECT COUNT(*) FROM dbo.[ORDERS]"));
        s.put("pendingOrders",   nativeCount("SELECT COUNT(*) FROM dbo.[ORDERS] WHERE OrderStatus='Pending'"));
        s.put("completedOrders", nativeCount("SELECT COUNT(*) FROM dbo.[ORDERS] WHERE OrderStatus='Completed'"));
        s.put("paymentCount",    nativeCount("SELECT COUNT(*) FROM dbo.[PAYMENT]"));
        s.put("totalRevenue",    getRevenue());
        return s;
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getSupportStats() {
        Map<String, Object> s = new HashMap<>();
        s.put("reviewCount",       nativeCount("SELECT COUNT(*) FROM dbo.[REVIEW]"));
        s.put("complaintCount",    nativeCount("SELECT COUNT(*) FROM dbo.[COMPLAINT]"));
        s.put("pendingComplaints", nativeCount("SELECT COUNT(*) FROM dbo.[COMPLAINT] WHERE Status='Open'"));
        s.put("activePromotions",  nativeCount("SELECT COUNT(*) FROM dbo.[PROMOTION] WHERE PromotionStatus='Active'"));
        s.put("faqCount",          nativeCount("SELECT COUNT(*) FROM dbo.[FAQ]"));
        return s;
    }

    private BigDecimal getRevenue() {
        try {
            List<?> r = em.createNativeQuery(
                "SELECT ISNULL(SUM(Amount),0) FROM dbo.[PAYMENT] WHERE PaymentStatus='Completed'").getResultList();
            if (!r.isEmpty()) {
                Object v = r.get(0);
                if (v instanceof BigDecimal bd) {
                    return bd;
                } else if (v != null) {
                    return new BigDecimal(v.toString());
                }
            }
        } catch (Exception ignored) {}
        return BigDecimal.ZERO;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // LISTENER MANAGEMENT
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllListeners(String search) {
        String sql = "SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.PhoneNumber, " +
                     "u.RegistrationDate, u.AccountStatus " +
                     "FROM dbo.[USER] u " +
                     "JOIN dbo.[LISTENER] l ON u.UserID = l.ListenerID ";
        if (search != null && !search.isBlank()) {
            sql += "WHERE LOWER(u.FirstName + ' ' + u.LastName) LIKE '%" + search.trim().toLowerCase() + "%' " +
                   "OR LOWER(u.Email) LIKE '%" + search.trim().toLowerCase() + "%' ";
        }
        sql += "ORDER BY u.RegistrationDate DESC";
        return nativeList(sql);
    }

    @Transactional(readOnly = true)
    public Object[] getListenerDetail(Integer id) {
        List<Object[]> r = nativeList(
            "SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.PhoneNumber, " +
            "u.RegistrationDate, u.AccountStatus " +
            "FROM dbo.[USER] u JOIN dbo.[LISTENER] l ON u.UserID = l.ListenerID " +
            "WHERE u.UserID = " + id);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getOrdersByListener(Integer id) {
        return nativeList("SELECT OrderID, OrderDate, OrderStatus, TotalAmount " +
                          "FROM dbo.[ORDERS] WHERE ListenerID = " + id + " ORDER BY OrderDate DESC");
    }

    @Transactional(readOnly = true)
    public List<Object[]> getReviewsByListener(Integer id) {
        return nativeList(
            "SELECT r.ReviewID, a.AlbumTitle, r.Rating, r.Comment, r.ReviewDate, r.ReviewStatus " +
            "FROM dbo.[REVIEW] r JOIN dbo.[ALBUM] a ON r.AlbumID = a.AlbumID " +
            "WHERE r.ListenerID = " + id + " ORDER BY r.ReviewDate DESC");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // CATALOG CRUD
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true) public List<Album>   getAllAlbums()   { return albumRepository.findAll(); }
    @Transactional(readOnly = true) public Optional<Album>  getAlbumById(Integer id)  { return albumRepository.findById(id); }
    @Transactional                  public Album  saveAlbum(Album a)   { return albumRepository.save(a); }
    @Transactional
    public void deleteAlbum(Integer id) {
        if (id == null) return;

        // 1. Delete PLAYLIST_ITEM referencing this album directly or referencing tracks belonging to this album
        em.createNativeQuery(
            "DELETE FROM dbo.[PLAYLIST_ITEM] WHERE AlbumID = ? OR TrackID IN (SELECT TrackID FROM dbo.[TRACK] WHERE AlbumID = ?)")
          .setParameter(1, id)
          .setParameter(2, id)
          .executeUpdate();

        // 2. Delete CART_ITEM referencing this album
        em.createNativeQuery("DELETE FROM dbo.[CART_ITEM] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 3. Delete DOWNLOAD logs for library items referencing this album
        em.createNativeQuery(
            "DELETE FROM dbo.[DOWNLOAD] WHERE LibraryItemID IN (SELECT LibraryItemID FROM dbo.[LIBRARY_ITEMS] WHERE AlbumID = ?)")
          .setParameter(1, id)
          .executeUpdate();

        // 4. Delete LIBRARY_ITEMS referencing this album
        em.createNativeQuery("DELETE FROM dbo.[LIBRARY_ITEMS] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 5. Delete ORDER_ITEM referencing this album
        em.createNativeQuery("DELETE FROM dbo.[ORDER_ITEM] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 6. Delete ALBUM_PROMOTION junction records
        em.createNativeQuery("DELETE FROM dbo.[ALBUM_PROMOTION] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 7. Delete REVIEW records for this album
        em.createNativeQuery("DELETE FROM dbo.[REVIEW] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 8. Delete TRACK records for this album
        em.createNativeQuery("DELETE FROM dbo.[TRACK] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();

        // 9. Delete the ALBUM itself
        em.createNativeQuery("DELETE FROM dbo.[ALBUM] WHERE AlbumID = ?")
          .setParameter(1, id)
          .executeUpdate();
    }

    @Transactional(readOnly = true) public List<Artist>  getAllArtists()  { return artistRepository.findAll(); }
    @Transactional(readOnly = true) public Optional<Artist> getArtistById(Integer id) { return artistRepository.findById(id); }
    @Transactional                  public Artist saveArtist(Artist a)  { return artistRepository.save(a); }
    @Transactional                  public void   deleteArtist(Integer id) { artistRepository.deleteById(id); }

    @Transactional(readOnly = true) public List<Genre>   getAllGenres()   { return genreRepository.findAllByOrderByGenreNameAsc(); }
    @Transactional(readOnly = true) public Optional<Genre>  getGenreById(Integer id)  { return genreRepository.findById(id); }
    @Transactional                  public Genre  saveGenre(Genre g)   { return genreRepository.save(g); }
    @Transactional                  public void   deleteGenre(Integer id)  { genreRepository.deleteById(id); }

    @Transactional(readOnly = true) public List<Catalog> getAllCatalogs() { return catalogRepository.findAll(); }
    @Transactional(readOnly = true) public Optional<Catalog> getCatalogById(Integer id) { return catalogRepository.findById(id); }
    @Transactional                  public Catalog saveCatalog(Catalog c) {
        if (c.getCreatedDate() == null) c.setCreatedDate(LocalDateTime.now());
        return catalogRepository.save(c);
    }
    @Transactional
    public Catalog saveCatalogWithAlbums(Catalog catalog, List<Integer> albumIds) {
        if (catalog.getCreatedDate() == null) catalog.setCreatedDate(LocalDateTime.now());
        Catalog saved = catalogRepository.save(catalog);
        if (albumIds != null && !albumIds.isEmpty()) {
            for (Integer albumId : albumIds) {
                if (albumId != null) {
                    albumRepository.findById(albumId).ifPresent(album -> {
                        album.setCatalog(saved);
                        albumRepository.save(album);
                    });
                }
            }
        }
        return saved;
    }
    @Transactional
    public Catalog updateCatalogWithAlbums(Integer catalogId, String name, String desc, BigDecimal price, List<Integer> albumIds) {
        Catalog catalog = catalogRepository.findById(catalogId)
            .orElseThrow(() -> new IllegalArgumentException("Catalog not found with ID: " + catalogId));
        catalog.setCatalogName(name);
        catalog.setDescription(desc);
        catalog.setPrice(price);
        Catalog saved = catalogRepository.save(catalog);

        // Fetch currently assigned albums
        List<Album> currentlyAssigned = albumRepository.findByCatalog_CatalogId(catalogId);
        List<Integer> targetIds = (albumIds != null) ? albumIds : List.of();

        // 1. Remove albums no longer selected (disassociate only, never delete album)
        for (Album a : currentlyAssigned) {
            if (!targetIds.contains(a.getAlbumId())) {
                a.setCatalog(null);
                albumRepository.save(a);
            }
        }

        // 2. Add newly selected albums
        for (Integer albumId : targetIds) {
            if (albumId != null) {
                albumRepository.findById(albumId).ifPresent(album -> {
                    if (album.getCatalog() == null || !catalogId.equals(album.getCatalog().getCatalogId())) {
                        album.setCatalog(saved);
                        albumRepository.save(album);
                    }
                });
            }
        }

        return saved;
    }
    @Transactional
    public void deleteCatalog(Integer id) {
        // Disassociate any albums before deleting catalog
        List<Album> albums = albumRepository.findByCatalog_CatalogId(id);
        for (Album a : albums) {
            a.setCatalog(null);
            albumRepository.save(a);
        }
        catalogRepository.deleteById(id);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllTracks() {
        return nativeList(
            "SELECT t.TrackID, a.AlbumTitle, t.TrackNumber, t.TrackTitle, t.Duration, t.AudioFileURL " +
            "FROM dbo.[TRACK] t JOIN dbo.[ALBUM] a ON t.AlbumID = a.AlbumID " +
            "ORDER BY a.AlbumTitle, t.TrackNumber");
    }

    @Transactional(readOnly = true)
    public Optional<com.melodymart.artistgenretrack.model.Track> getTrackById(Integer id) {
        return trackRepository.findById(id);
    }

    @Transactional
    public com.melodymart.artistgenretrack.model.Track saveTrack(com.melodymart.artistgenretrack.model.Track track) {
        return trackRepository.save(track);
    }

    @Transactional
    public void deleteTrack(Integer id) {
        trackRepository.deleteById(id);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // ORDERS & PAYMENTS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllOrders() {
        return nativeList(
            "SELECT o.OrderID, u.FirstName + ' ' + u.LastName AS Customer, u.Email, " +
            "o.OrderDate, o.OrderStatus, o.TotalAmount " +
            "FROM dbo.[ORDERS] o " +
            "JOIN dbo.[LISTENER] l ON o.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "ORDER BY o.OrderDate DESC");
    }

    @Transactional(readOnly = true)
    public Object[] getOrderDetail(Long orderId) {
        List<Object[]> r = nativeList(
            "SELECT o.OrderID, u.FirstName + ' ' + u.LastName AS Customer, u.Email, " +
            "o.OrderDate, o.OrderStatus, o.TotalAmount " +
            "FROM dbo.[ORDERS] o " +
            "JOIN dbo.[LISTENER] l ON o.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "WHERE o.OrderID = " + orderId);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getOrderItems(Long orderId) {
        return nativeList(
            "SELECT oi.ItemNo, " +
            "COALESCE(a.AlbumTitle, c.CatalogName, 'Unknown') AS ItemName, " +
            "oi.Quantity, oi.UnitPrice " +
            "FROM dbo.[ORDER_ITEM] oi " +
            "LEFT JOIN dbo.[ALBUM] a ON oi.AlbumID = a.AlbumID " +
            "LEFT JOIN dbo.[Catalog] c ON oi.CatalogID = c.CatalogID " +
            "WHERE oi.OrderID = " + orderId + " ORDER BY oi.ItemNo");
    }

    @Transactional(readOnly = true)
    public Object[] getPaymentByOrder(Long orderId) {
        List<Object[]> r = nativeList(
            "SELECT PaymentID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus " +
            "FROM dbo.[PAYMENT] WHERE OrderID = " + orderId);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllPayments() {
        return nativeList(
            "SELECT p.PaymentID, p.OrderID, u.FirstName + ' ' + u.LastName AS Customer, " +
            "p.PaymentDate, p.Amount, p.PaymentMethod, p.TransactionReference, p.PaymentStatus " +
            "FROM dbo.[PAYMENT] p " +
            "JOIN dbo.[ORDERS] o ON p.OrderID = o.OrderID " +
            "JOIN dbo.[LISTENER] l ON o.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "ORDER BY p.PaymentDate DESC");
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String status) {
        em.createNativeQuery("UPDATE dbo.[ORDERS] SET OrderStatus = ? WHERE OrderID = ?")
          .setParameter(1, status)
          .setParameter(2, orderId)
          .executeUpdate();
    }

    @Transactional
    public void declineOrder(Long orderId) {
        Object[] order = getOrderDetail(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        String currentStatus = (order[4] != null) ? order[4].toString() : "";
        if (!"Pending".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Only pending orders can be declined. Current status is " + currentStatus + ".");
        }
        // Update OrderStatus to 'Cancelled' (valid status in schema CK_ORDERS_OrderStatus)
        em.createNativeQuery("UPDATE dbo.[ORDERS] SET OrderStatus = 'Cancelled' WHERE OrderID = ?")
          .setParameter(1, orderId)
          .executeUpdate();
    }

    @Transactional
    public void deleteOrder(Long orderId) {
        if (orderId == null) return;
        // 1. Delete PAYMENT records
        em.createNativeQuery("DELETE FROM dbo.[PAYMENT] WHERE OrderID = ?")
          .setParameter(1, orderId)
          .executeUpdate();
        // 2. Delete ORDER_ITEM records
        em.createNativeQuery("DELETE FROM dbo.[ORDER_ITEM] WHERE OrderID = ?")
          .setParameter(1, orderId)
          .executeUpdate();
        // 3. Delete ORDERS record
        em.createNativeQuery("DELETE FROM dbo.[ORDERS] WHERE OrderID = ?")
          .setParameter(1, orderId)
          .executeUpdate();
    }

    @SuppressWarnings("unchecked")
    @Transactional
    public void acceptOrder(Long orderId) {
        Object[] order = getOrderDetail(orderId);
        if (order == null) {
            throw new IllegalArgumentException("Order not found with ID: " + orderId);
        }
        String currentStatus = (order[4] != null) ? order[4].toString() : "";
        if (!"Pending".equalsIgnoreCase(currentStatus)) {
            throw new IllegalStateException("Only pending orders can be accepted. Current status is " + currentStatus + ".");
        }

        // 1. Update order status to Completed
        em.createNativeQuery("UPDATE dbo.[ORDERS] SET OrderStatus = 'Completed' WHERE OrderID = ?")
          .setParameter(1, orderId)
          .executeUpdate();

        // 2. Fetch listener ID
        List<?> listenerRes = em.createNativeQuery("SELECT ListenerID FROM dbo.[ORDERS] WHERE OrderID = ?")
          .setParameter(1, orderId)
          .getResultList();

        if (listenerRes.isEmpty() || listenerRes.get(0) == null) {
            return;
        }
        Integer listenerId = ((Number) listenerRes.get(0)).intValue();

        // 3. Ensure DIGITAL_LIBRARY exists
        List<?> libCheck = em.createNativeQuery("SELECT LibraryID FROM dbo.[DIGITAL_LIBRARY] WHERE ListenerID = ?")
          .setParameter(1, listenerId)
          .getResultList();

        Integer libraryId;
        if (libCheck.isEmpty()) {
            List<?> libRes = em.createNativeQuery(
                "INSERT INTO dbo.[DIGITAL_LIBRARY] (ListenerID, CreatedDate, LastUpdatedDate) " +
                "OUTPUT INSERTED.LibraryID " +
                "VALUES (?, GETDATE(), GETDATE())")
              .setParameter(1, listenerId)
              .getResultList();
            libraryId = ((Number) libRes.get(0)).intValue();
        } else {
            libraryId = ((Number) libCheck.get(0)).intValue();
            em.createNativeQuery("UPDATE dbo.[DIGITAL_LIBRARY] SET LastUpdatedDate = GETDATE() WHERE LibraryID = ?")
              .setParameter(1, libraryId)
              .executeUpdate();
        }

        // 4. Fetch all order items
        List<Object[]> items = em.createNativeQuery(
            "SELECT AlbumID, CatalogID FROM dbo.[ORDER_ITEM] WHERE OrderID = ?")
          .setParameter(1, orderId)
          .getResultList();

        for (Object[] row : items) {
            Integer albumId = (row[0] != null) ? ((Number) row[0]).intValue() : null;
            Integer catalogId = (row[1] != null) ? ((Number) row[1]).intValue() : null;

            if (albumId != null) {
                Number count = (Number) em.createNativeQuery(
                    "SELECT COUNT(*) FROM dbo.[LIBRARY_ITEMS] WHERE LibraryID = ? AND AlbumID = ?")
                  .setParameter(1, libraryId)
                  .setParameter(2, albumId)
                  .getSingleResult();

                if (count.intValue() == 0) {
                    em.createNativeQuery(
                        "INSERT INTO dbo.[LIBRARY_ITEMS] (LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus) " +
                        "VALUES (?, ?, NULL, GETDATE(), 'Album', 'Active')")
                      .setParameter(1, libraryId)
                      .setParameter(2, albumId)
                      .executeUpdate();
                } else {
                    em.createNativeQuery(
                        "UPDATE dbo.[LIBRARY_ITEMS] SET AccessStatus = 'Active' WHERE LibraryID = ? AND AlbumID = ?")
                      .setParameter(1, libraryId)
                      .setParameter(2, albumId)
                      .executeUpdate();
                }
            } else if (catalogId != null) {
                List<?> catAlbumIds = em.createNativeQuery("SELECT AlbumID FROM dbo.[ALBUM] WHERE CatalogID = ?")
                  .setParameter(1, catalogId)
                  .getResultList();
                for (Object aIdObj : catAlbumIds) {
                    Integer caId = ((Number) aIdObj).intValue();
                    Number count = (Number) em.createNativeQuery(
                        "SELECT COUNT(*) FROM dbo.[LIBRARY_ITEMS] WHERE LibraryID = ? AND AlbumID = ?")
                      .setParameter(1, libraryId)
                      .setParameter(2, caId)
                      .getSingleResult();

                    if (count.intValue() == 0) {
                        em.createNativeQuery(
                            "INSERT INTO dbo.[LIBRARY_ITEMS] (LibraryID, AlbumID, CatalogID, AddedDate, PurchaseType, AccessStatus) " +
                            "VALUES (?, ?, ?, GETDATE(), 'Catalog', 'Active')")
                          .setParameter(1, libraryId)
                          .setParameter(2, caId)
                          .setParameter(3, catalogId)
                          .executeUpdate();
                    } else {
                        em.createNativeQuery(
                            "UPDATE dbo.[LIBRARY_ITEMS] SET AccessStatus = 'Active', AddedDate = GETDATE() WHERE LibraryID = ? AND AlbumID = ?")
                          .setParameter(1, libraryId)
                          .setParameter(2, caId)
                          .executeUpdate();
                    }
                }
            }
        }
    }

    @Transactional
    public void toggleLibraryItemStatus(Long libraryItemId, String status) {
        em.createNativeQuery("UPDATE dbo.[LIBRARY_ITEMS] SET AccessStatus = ? WHERE LibraryItemID = ?")
          .setParameter(1, status)
          .setParameter(2, libraryItemId)
          .executeUpdate();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // DIGITAL LIBRARY & DOWNLOADS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllDigitalLibraries() {
        return nativeList(
            "SELECT dl.LibraryID, u.FirstName + ' ' + u.LastName AS Owner, u.Email, " +
            "dl.CreatedDate, dl.LastUpdatedDate " +
            "FROM dbo.[DIGITAL_LIBRARY] dl " +
            "JOIN dbo.[LISTENER] l ON dl.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "ORDER BY dl.CreatedDate DESC");
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllLibraryItems() {
        return nativeList(
            "SELECT li.LibraryItemID, u.FirstName + ' ' + u.LastName AS Owner, " +
            "a.AlbumTitle, li.AddedDate, li.PurchaseType, li.AccessStatus " +
            "FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "JOIN dbo.[LISTENER] lst ON dl.ListenerID = lst.ListenerID " +
            "JOIN dbo.[USER] u ON lst.ListenerID = u.UserID " +
            "JOIN dbo.[ALBUM] a ON li.AlbumID = a.AlbumID " +
            "ORDER BY li.AddedDate DESC");
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllDownloads() {
        return nativeList(
            "SELECT d.DownloadID, u.FirstName + ' ' + u.LastName AS Owner, a.AlbumTitle, " +
            "d.DownloadDateTime, d.FileFormat, d.FileSize, d.DownloadStatus " +
            "FROM dbo.[DOWNLOAD] d " +
            "JOIN dbo.[LIBRARY_ITEMS] li ON d.LibraryItemID = li.LibraryItemID " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "JOIN dbo.[LISTENER] lst ON dl.ListenerID = lst.ListenerID " +
            "JOIN dbo.[USER] u ON lst.ListenerID = u.UserID " +
            "JOIN dbo.[ALBUM] a ON li.AlbumID = a.AlbumID " +
            "ORDER BY d.DownloadDateTime DESC");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUPPORT: REVIEWS, COMPLAINTS, FAQ, PROMOTIONS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllReviews() {
        return nativeList(
            "SELECT r.ReviewID, u.FirstName + ' ' + u.LastName AS Listener, " +
            "a.AlbumTitle, r.Rating, r.Comment, r.ReviewDate " +
            "FROM dbo.[REVIEW] r " +
            "JOIN dbo.[LISTENER] l ON r.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "JOIN dbo.[ALBUM] a ON r.AlbumID = a.AlbumID " +
            "ORDER BY r.ReviewDate DESC");
    }

    @Transactional(readOnly = true)
    public Object[] getReviewDetail(Long reviewId) {
        List<Object[]> r = nativeList(
            "SELECT r.ReviewID, u.FirstName + ' ' + u.LastName AS Listener, " +
            "a.AlbumTitle, r.Rating, r.Comment, r.ReviewDate, a.AlbumID, u.UserID " +
            "FROM dbo.[REVIEW] r " +
            "JOIN dbo.[LISTENER] l ON r.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "JOIN dbo.[ALBUM] a ON r.AlbumID = a.AlbumID " +
            "WHERE r.ReviewID = " + reviewId);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        em.createNativeQuery("DELETE FROM dbo.[REVIEW] WHERE ReviewID = ?")
          .setParameter(1, reviewId)
          .executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllComplaints() {
        return nativeList(
            "SELECT c.ComplaintID, u.FirstName + ' ' + u.LastName AS Listener, " +
            "c.Subject, c.ComplaintDate, c.Status, c.Response " +
            "FROM dbo.[COMPLAINT] c " +
            "JOIN dbo.[LISTENER] l ON c.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "ORDER BY c.ComplaintDate DESC");
    }

    @Transactional(readOnly = true)
    public Object[] getComplaintDetail(Long complaintId) {
        List<Object[]> r = nativeList(
            "SELECT c.ComplaintID, u.FirstName + ' ' + u.LastName AS Listener, " +
            "u.Email, c.Subject, c.Description, c.ComplaintDate, c.Status, c.Response, " +
            "c.ResolvedDate, u.UserID " +
            "FROM dbo.[COMPLAINT] c " +
            "JOIN dbo.[LISTENER] l ON c.ListenerID = l.ListenerID " +
            "JOIN dbo.[USER] u ON l.ListenerID = u.UserID " +
            "WHERE c.ComplaintID = " + complaintId);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional
    public void updateComplaintStatus(Long complaintId, String status) {
        updateComplaint(complaintId, status, null);
    }

    @Transactional
    public void updateComplaint(Long complaintId, String status, String response) {
        if ("InProgress".equalsIgnoreCase(status)) {
            status = "In Progress";
        }
        if (response != null && !response.isBlank()) {
            em.createNativeQuery(
                "UPDATE dbo.[COMPLAINT] SET Status = ?, Response = ?, " +
                "ResolvedDate = CASE WHEN ? IN ('Resolved', 'Closed') AND ResolvedDate IS NULL THEN GETDATE() ELSE ResolvedDate END " +
                "WHERE ComplaintID = ?")
              .setParameter(1, status)
              .setParameter(2, response.trim())
              .setParameter(3, status)
              .setParameter(4, complaintId)
              .executeUpdate();
        } else {
            em.createNativeQuery(
                "UPDATE dbo.[COMPLAINT] SET Status = ?, " +
                "ResolvedDate = CASE WHEN ? IN ('Resolved', 'Closed') AND ResolvedDate IS NULL THEN GETDATE() ELSE ResolvedDate END " +
                "WHERE ComplaintID = ?")
              .setParameter(1, status)
              .setParameter(2, status)
              .setParameter(3, complaintId)
              .executeUpdate();
        }
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllFaqs() {
        return nativeList("SELECT FAQID, Question, Answer, FAQStatus, CreatedDate FROM dbo.[FAQ] ORDER BY CreatedDate DESC");
    }

    @Transactional(readOnly = true)
    public Object[] getFaqById(Long faqId) {
        List<Object[]> r = nativeList("SELECT FAQID, Question, Answer, FAQStatus, CreatedDate, UpdatedDate FROM dbo.[FAQ] WHERE FAQID = " + faqId);
        return r.isEmpty() ? null : r.get(0);
    }

    @Transactional
    public void addFaq(String question, String answer, String status) {
        em.createNativeQuery(
            "INSERT INTO dbo.[FAQ] (Question, Answer, FAQStatus, CreatedDate, UpdatedDate) VALUES (?,?,?,GETDATE(),GETDATE())")
          .setParameter(1, question)
          .setParameter(2, answer)
          .setParameter(3, status)
          .executeUpdate();
    }

    @Transactional
    public void updateFaq(Long faqId, String question, String answer, String status) {
        em.createNativeQuery(
            "UPDATE dbo.[FAQ] SET Question = ?, Answer = ?, FAQStatus = ?, UpdatedDate = GETDATE() WHERE FAQID = ?")
          .setParameter(1, question)
          .setParameter(2, answer)
          .setParameter(3, status)
          .setParameter(4, faqId)
          .executeUpdate();
    }

    @Transactional
    public void deleteFaq(Long faqId) {
        em.createNativeQuery("DELETE FROM dbo.[FAQ] WHERE FAQID = " + faqId).executeUpdate();
    }

    // ══════════════════════════════════════════════════════════════════════════
    // PROMOTIONS CRUD
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllPromotions() {
        return nativeList(
            "SELECT PromotionID, PromotionName, Description, DiscountType, DiscountValue, " +
            "StartDate, EndDate, PromotionStatus FROM dbo.[PROMOTION] ORDER BY StartDate DESC");
    }

    @Transactional(readOnly = true)
    public Object[] getPromotionById(Long promotionId) {
        List<Object[]> r = nativeList(
            "SELECT PromotionID, PromotionName, Description, DiscountType, DiscountValue, " +
            "StartDate, EndDate, PromotionStatus FROM dbo.[PROMOTION] WHERE PromotionID = " + promotionId);
        return r.isEmpty() ? null : r.get(0);
    }

    private java.sql.Timestamp parseDateTime(String dt) {
        if (dt == null || dt.isBlank()) return null;
        String clean = dt.replace("T", " ").trim();
        if (clean.length() == 16) {
            clean += ":00";
        }
        return java.sql.Timestamp.valueOf(clean);
    }

    @Transactional
    public void createPromotion(String name, String description, String discountType,
                                 java.math.BigDecimal discountValue, String startDate, String endDate,
                                 String status) {
        em.createNativeQuery(
            "INSERT INTO dbo.[PROMOTION] (PromotionName, Description, DiscountType, DiscountValue, StartDate, EndDate, PromotionStatus) " +
            "VALUES (?,?,?,?,?,?,?)")
          .setParameter(1, name)
          .setParameter(2, description)
          .setParameter(3, discountType)
          .setParameter(4, discountValue)
          .setParameter(5, parseDateTime(startDate))
          .setParameter(6, parseDateTime(endDate))
          .setParameter(7, status)
          .executeUpdate();
    }

    @Transactional
    public void updatePromotion(Long id, String name, String description, String discountType,
                                 java.math.BigDecimal discountValue, String startDate, String endDate,
                                 String status) {
        em.createNativeQuery(
            "UPDATE dbo.[PROMOTION] SET PromotionName=?, Description=?, DiscountType=?, " +
            "DiscountValue=?, StartDate=?, EndDate=?, PromotionStatus=? WHERE PromotionID=?")
          .setParameter(1, name)
          .setParameter(2, description)
          .setParameter(3, discountType)
          .setParameter(4, discountValue)
          .setParameter(5, parseDateTime(startDate))
          .setParameter(6, parseDateTime(endDate))
          .setParameter(7, status)
          .setParameter(8, id)
          .executeUpdate();
    }

    @Transactional
    public void deletePromotion(Long id) {
        // FK ON DELETE CASCADE on ALBUM_PROMOTION handles child rows
        em.createNativeQuery("DELETE FROM dbo.[PROMOTION] WHERE PromotionID = ?")
          .setParameter(1, id)
          .executeUpdate();
    }

    @Transactional
    public void deactivatePromotion(Long id) {
        em.createNativeQuery("UPDATE dbo.[PROMOTION] SET PromotionStatus='Cancelled' WHERE PromotionID=?")
          .setParameter(1, id)
          .executeUpdate();
    }

    // ── Album-Promotion management ────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Object[]> getAlbumsForPromotion(Long promotionId) {
        return nativeList(
            "SELECT a.AlbumID, a.AlbumTitle, ar.ArtistName, a.Price " +
            "FROM dbo.[ALBUM_PROMOTION] ap " +
            "JOIN dbo.[ALBUM] a ON ap.AlbumID = a.AlbumID " +
            "JOIN dbo.[ARTIST] ar ON a.ArtistID = ar.ArtistID " +
            "WHERE ap.PromotionID = " + promotionId + " ORDER BY a.AlbumTitle");
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAlbumsNotInPromotion(Long promotionId) {
        return nativeList(
            "SELECT a.AlbumID, a.AlbumTitle, ar.ArtistName, a.Price " +
            "FROM dbo.[ALBUM] a " +
            "JOIN dbo.[ARTIST] ar ON a.ArtistID = ar.ArtistID " +
            "WHERE a.AlbumID NOT IN (" +
            "  SELECT AlbumID FROM dbo.[ALBUM_PROMOTION] WHERE PromotionID = " + promotionId +
            ") ORDER BY a.AlbumTitle");
    }

    @Transactional
    public void assignAlbumToPromotion(Integer albumId, Long promotionId) {
        // Check for existing entry to avoid duplicate key violation
        Number count = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM dbo.[ALBUM_PROMOTION] WHERE AlbumID=? AND PromotionID=?")
          .setParameter(1, albumId)
          .setParameter(2, promotionId)
          .getSingleResult();
        if (count.intValue() == 0) {
            em.createNativeQuery("INSERT INTO dbo.[ALBUM_PROMOTION] (AlbumID, PromotionID) VALUES (?,?)")
              .setParameter(1, albumId)
              .setParameter(2, promotionId)
              .executeUpdate();
        }
    }

    @Transactional
    public void removeAlbumFromPromotion(Integer albumId, Long promotionId) {
        em.createNativeQuery("DELETE FROM dbo.[ALBUM_PROMOTION] WHERE AlbumID=? AND PromotionID=?")
          .setParameter(1, albumId)
          .setParameter(2, promotionId)
          .executeUpdate();
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllAlbumPromotions() {
        return nativeList(
            "SELECT ap.AlbumID, a.AlbumTitle, ap.PromotionID, p.PromotionName, p.DiscountType, p.DiscountValue, p.PromotionStatus " +
            "FROM dbo.[ALBUM_PROMOTION] ap " +
            "JOIN dbo.[ALBUM] a ON ap.AlbumID = a.AlbumID " +
            "JOIN dbo.[PROMOTION] p ON ap.PromotionID = p.PromotionID " +
            "ORDER BY a.AlbumTitle ASC");
    }

    // ── Listener-facing: active promotions for an album ───────────────────────

    @Transactional(readOnly = true)
    public List<Object[]> getActivePromotionsForAlbum(Integer albumId) {
        return nativeList(
            "SELECT p.PromotionID, p.PromotionName, p.DiscountType, p.DiscountValue, p.EndDate " +
            "FROM dbo.[ALBUM_PROMOTION] ap " +
            "JOIN dbo.[PROMOTION] p ON ap.PromotionID = p.PromotionID " +
            "WHERE ap.AlbumID = " + albumId +
            "  AND p.PromotionStatus = 'Active' " +
            "  AND p.StartDate <= GETDATE() AND p.EndDate >= GETDATE()");
    }

    // ══════════════════════════════════════════════════════════════════════════
    // SUPER ADMIN: USERS & ADMINISTRATORS
    // ══════════════════════════════════════════════════════════════════════════

    @Transactional(readOnly = true)
    public List<Object[]> getAllUsers() {
        return nativeList(
            "SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.PhoneNumber, " +
            "u.RegistrationDate, u.AccountStatus, " +
            "CASE WHEN a.AdminID IS NOT NULL THEN 'Admin' " +
            "     WHEN l.ListenerID IS NOT NULL THEN 'Listener' ELSE 'User' END AS UserType, " +
            "COALESCE(a.AdminRole, '') AS AdminRole " +
            "FROM dbo.[USER] u " +
            "LEFT JOIN dbo.[ADMINISTRATOR] a ON u.UserID = a.AdminID " +
            "LEFT JOIN dbo.[LISTENER] l ON u.UserID = l.ListenerID " +
            "ORDER BY u.RegistrationDate DESC");
    }

    @Transactional(readOnly = true)
    public List<Object[]> getAllAdmins() {
        return nativeList(
            "SELECT u.UserID, u.FirstName, u.LastName, u.Email, u.PhoneNumber, " +
            "a.AdminRole, u.AccountStatus, u.RegistrationDate " +
            "FROM dbo.[ADMINISTRATOR] a " +
            "JOIN dbo.[USER] u ON a.AdminID = u.UserID " +
            "ORDER BY u.FirstName ASC");
    }
}
