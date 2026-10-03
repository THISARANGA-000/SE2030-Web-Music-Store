package com.melodymart.common.service;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.common.model.Listener;
import com.melodymart.ordersales.service.CartService;
import com.melodymart.ordersales.model.CartItem;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.artistgenretrack.model.Artist;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.ordersales.model.CartItem;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ListenerService {

    private final CatalogRepository catalogRepository;
    private final AlbumRepository albumRepository;
    private final CartService cartService;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public ListenerService(CatalogRepository catalogRepository,
                           AlbumRepository albumRepository,
                           CartService cartService) {
        this.catalogRepository = catalogRepository;
        this.albumRepository = albumRepository;
        this.cartService = cartService;
    }

    // ─── Catalogs ─────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Catalog> getAllCatalogs(String search) {
        if (search != null && !search.isBlank()) {
            return catalogRepository.findByCatalogNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(search.trim(), search.trim());
        }
        return catalogRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Catalog> getCatalogById(Integer id) {
        return catalogRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Album> getAlbumsForCatalog(Integer catalogId) {
        return albumRepository.findByCatalog_CatalogId(catalogId);
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public Map<String, Object> search(String query) {
        Map<String, Object> results = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            results.put("query", "");
            results.put("albums", List.of());
            results.put("catalogs", List.of());
            results.put("totalCount", 0);
            return results;
        }
        String q = query.trim();

        // Search albums by title or artist
        List<Album> albums = albumRepository.searchAndFilterAlbums(q, null, null);

        // Search catalogs by name or description
        List<Catalog> catalogs = catalogRepository.findByCatalogNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(q, q);

        results.put("query", q);
        results.put("albums", albums);
        results.put("catalogs", catalogs);
        results.put("totalCount", albums.size() + catalogs.size());
        return results;
    }

    // ─── Checkout / Purchase ──────────────────────────────────────────────────

    @Transactional
    public Integer checkout(Integer listenerId, String paymentMethod) {
        if (listenerId == null) {
            throw new IllegalArgumentException("You must be logged in to complete a purchase.");
        }

        Cart cart = cartService.getCartForListener(listenerId);
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Your cart is empty.");
        }

        BigDecimal totalAmount = cart.getTotalAmount();
        String method = (paymentMethod != null && !paymentMethod.isBlank()) ? paymentMethod : "Demo Payment";

        // 1. Insert into ORDERS with Pending status
        List<?> orderRes = em.createNativeQuery(
            "INSERT INTO dbo.[ORDERS] (ListenerID, OrderDate, OrderStatus, TotalAmount) " +
            "OUTPUT INSERTED.OrderID " +
            "VALUES (?, GETDATE(), 'Pending', ?)")
          .setParameter(1, listenerId)
          .setParameter(2, totalAmount)
          .getResultList();

        if (orderRes == null || orderRes.isEmpty() || orderRes.get(0) == null) {
            throw new IllegalStateException("Failed to create Order: OrderID could not be generated.");
        }
        Number orderIdNum = (Number) orderRes.get(0);
        Integer orderId = orderIdNum.intValue();

        // 2. Insert into ORDER_ITEM
        int itemIndex = 1;
        for (CartItem ci : cart.getItems()) {
            Integer albumId = (ci.getAlbum() != null) ? ci.getAlbum().getAlbumId() : null;
            Integer catalogId = (ci.getCatalog() != null) ? ci.getCatalog().getCatalogId() : null;

            em.createNativeQuery(
                "INSERT INTO dbo.[ORDER_ITEM] (OrderID, ItemNo, AlbumID, CatalogID, Quantity, UnitPrice) " +
                "VALUES (?, ?, ?, ?, ?, ?)")
              .setParameter(1, orderId)
              .setParameter(2, itemIndex++)
              .setParameter(3, albumId)
              .setParameter(4, catalogId)
              .setParameter(5, ci.getQuantity())
              .setParameter(6, ci.getUnitPrice())
              .executeUpdate();
        }

        // 3. Insert into PAYMENT (Demo Payment, Completed)
        String txnRef = "TXN-" + System.currentTimeMillis() + "-" + (int)(Math.random() * 900 + 100);
        em.createNativeQuery(
            "INSERT INTO dbo.[PAYMENT] (OrderID, PaymentDate, Amount, PaymentMethod, TransactionReference, PaymentStatus) " +
            "VALUES (?, GETDATE(), ?, ?, ?, 'Completed')")
          .setParameter(1, orderId)
          .setParameter(2, totalAmount)
          .setParameter(3, method)
          .setParameter(4, txnRef)
          .executeUpdate();

        // 4. Clear cart
        cartService.clearCartForListener(listenerId);

        return orderId;
    }

    @Transactional(readOnly = true)
    public int getPendingOrdersCountForListener(Integer listenerId) {
        if (listenerId == null) return 0;
        Number count = (Number) em.createNativeQuery(
            "SELECT COUNT(*) FROM dbo.[ORDERS] WHERE ListenerID = ? AND OrderStatus = 'Pending'")
          .setParameter(1, listenerId)
          .getSingleResult();
        return count != null ? count.intValue() : 0;
    }

    // ─── Purchase History / Orders ────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Integer> getDeclinedOrderIdsForListener(Integer listenerId) {
        if (listenerId == null) return List.of();
        List<Number> orderIds = em.createNativeQuery(
            "SELECT OrderID FROM dbo.[ORDERS] WHERE ListenerID = ? AND OrderStatus = 'Cancelled'")
          .setParameter(1, listenerId)
          .getResultList();
        
        List<Integer> result = new ArrayList<>();
        for (Number id : orderIds) {
            result.add(id.intValue());
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getOrdersForListener(Integer listenerId) {
        if (listenerId == null) return List.of();

        List<Object[]> rows = em.createNativeQuery(
            "SELECT o.OrderID, o.OrderDate, o.OrderStatus, o.TotalAmount, " +
            "COALESCE(p.PaymentStatus, 'Completed'), COALESCE(p.PaymentMethod, 'Credit Card'), " +
            "COALESCE(p.TransactionReference, 'N/A') " +
            "FROM dbo.[ORDERS] o " +
            "LEFT JOIN dbo.[PAYMENT] p ON o.OrderID = p.OrderID " +
            "WHERE o.ListenerID = ? " +
            "ORDER BY o.OrderDate DESC")
          .setParameter(1, listenerId)
          .getResultList();

        List<Map<String, Object>> orders = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            Integer orderId = ((Number) r[0]).intValue();
            map.put("orderId", orderId);
            map.put("orderDate", r[1]);
            map.put("orderStatus", r[2]);
            map.put("totalAmount", r[3]);
            map.put("paymentStatus", r[4]);
            map.put("paymentMethod", r[5]);
            map.put("transactionRef", r[6]);

            // Query items for this order
            List<Object[]> itemRows = em.createNativeQuery(
                "SELECT oi.ItemNo, COALESCE(a.AlbumTitle, c.CatalogName, 'Music Item') AS Title, " +
                "oi.Quantity, oi.UnitPrice, " +
                "CASE WHEN oi.AlbumID IS NOT NULL THEN 'Album' ELSE 'Catalog' END AS ItemType, " +
                "a.CoverImageUrl, art.ArtistName " +
                "FROM dbo.[ORDER_ITEM] oi " +
                "LEFT JOIN dbo.[ALBUM] a ON oi.AlbumID = a.AlbumID " +
                "LEFT JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID " +
                "LEFT JOIN dbo.[Catalog] c ON oi.CatalogID = c.CatalogID " +
                "WHERE oi.OrderID = ? " +
                "ORDER BY oi.ItemNo ASC")
              .setParameter(1, orderId)
              .getResultList();

            List<Map<String, Object>> items = new ArrayList<>();
            for (Object[] ir : itemRows) {
                Map<String, Object> im = new HashMap<>();
                im.put("itemNo", ir[0]);
                im.put("title", ir[1]);
                im.put("quantity", ir[2]);
                im.put("unitPrice", ir[3]);
                im.put("itemType", ir[4]);
                im.put("coverImageUrl", ir[5]);
                im.put("artistName", ir[6] != null ? ir[6] : "Bundle");
                items.add(im);
            }
            map.put("items", items);
            orders.add(map);
        }

        return orders;
    }

    // ─── Digital Library (Listener Side) ──────────────────────────────────────

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Map<String, Object>> getDigitalLibraryForListener(Integer listenerId) {
        if (listenerId == null) return List.of();

        List<Object[]> rows = em.createNativeQuery(
            "SELECT li.LibraryItemID, a.AlbumID, a.AlbumTitle, " +
            "COALESCE(art.ArtistName, 'Unknown Artist') AS ArtistName, " +
            "COALESCE(g.GenreName, 'General') AS GenreName, " +
            "a.CoverImageUrl, li.AddedDate, li.PurchaseType, li.AccessStatus, " +
            "(SELECT COUNT(*) FROM dbo.[DOWNLOAD] d WHERE d.LibraryItemID = li.LibraryItemID) AS DownloadCount " +
            "FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "JOIN dbo.[ALBUM] a ON li.AlbumID = a.AlbumID " +
            "LEFT JOIN dbo.[ARTIST] art ON a.ArtistID = art.ArtistID " +
            "LEFT JOIN dbo.[GENRE] g ON a.GenreID = g.GenreID " +
            "WHERE dl.ListenerID = ? AND li.AccessStatus = 'Active' " +
            "ORDER BY li.AddedDate DESC")
          .setParameter(1, listenerId)
          .getResultList();

        List<Map<String, Object>> library = new ArrayList<>();
        for (Object[] r : rows) {
            Map<String, Object> map = new HashMap<>();
            map.put("libraryItemId", r[0]);
            map.put("albumId", r[1]);
            map.put("albumTitle", r[2]);
            map.put("artistName", r[3]);
            map.put("genreName", r[4]);
            map.put("coverImageUrl", r[5]);
            map.put("addedDate", r[6]);
            map.put("purchaseType", r[7]);
            map.put("accessStatus", r[8]);
            map.put("downloadCount", r[9]);
            library.add(map);
        }

        return library;
    }

    // ─── Download Action ──────────────────────────────────────────────────────

    @Transactional
    public boolean recordDownload(Integer listenerId, Integer libraryItemId) {
        if (listenerId == null || libraryItemId == null) return false;

        // Verify that this library item belongs to this listener and is Active
        List<?> check = em.createNativeQuery(
            "SELECT li.LibraryItemID FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.LibraryItemID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .setParameter(2, libraryItemId)
          .getResultList();

        if (check.isEmpty()) {
            return false;
        }

        // Insert download log (45MB simulated package)
        em.createNativeQuery(
            "INSERT INTO dbo.[DOWNLOAD] (LibraryItemID, DownloadDateTime, FileFormat, FileSize, DownloadStatus) " +
            "VALUES (?, GETDATE(), 'MP3', 45000000, 'Completed')")
          .setParameter(1, libraryItemId)
          .executeUpdate();

        return true;
    }

    // ─── Remove / Revoke Library Item ─────────────────────────────────────────

    @Transactional
    public boolean removeLibraryItem(Integer listenerId, Integer libraryItemId) {
        if (listenerId == null || libraryItemId == null) return false;

        // Verify ownership and set AccessStatus to 'Revoked'
        // Only updates if the library item belongs to this listener and is currently Active
        int updated = em.createNativeQuery(
            "UPDATE li SET li.AccessStatus = 'Revoked' " +
            "FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.LibraryItemID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .setParameter(2, libraryItemId)
          .executeUpdate();

        return updated > 0;
    }
}
