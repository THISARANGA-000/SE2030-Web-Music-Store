package com.melodymart;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.pricing.BaseAlbumPrice;
import com.melodymart.albumcatalog.pricing.PricingComponent;
import com.melodymart.albumcatalog.pricing.PromotionPriceDecorator;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.service.AlbumService;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.artistgenretrack.repository.ArtistRepository;
import com.melodymart.artistgenretrack.repository.GenreRepository;
import com.melodymart.common.model.Listener;
import com.melodymart.common.repository.ListenerRepository;
import com.melodymart.common.service.ListenerService;
import com.melodymart.faqpromotion.service.PromotionService;
import com.melodymart.faqpromotion.strategy.FixedAmountDiscountStrategy;
import com.melodymart.faqpromotion.strategy.PercentageDiscountStrategy;
import com.melodymart.ordersales.model.Cart;
import com.melodymart.ordersales.service.CartService;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
public class PromotionPriceCalculationTest {

    @Autowired
    private AlbumService albumService;

    @Autowired
    private CartService cartService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private PromotionService promotionService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private ArtistRepository artistRepository;

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private ListenerRepository listenerRepository;

    @PersistenceContext
    private EntityManager em;

    private Listener testListener;
    private Artist testArtist;
    private Genre testGenre;

    private Album createAlbum(String title, BigDecimal price) {
        Album album = new Album();
        album.setAlbumTitle(title);
        album.setPrice(price);
        album.setReleaseDate(LocalDate.now());
        album.setAlbumStatus("Available");
        album.setArtist(testArtist);
        album.setGenre(testGenre);
        return albumRepository.save(album);
    }

    private Integer createPromotion(String name, String type, BigDecimal value, String status, int startOffsetDays, int endOffsetDays) {
        Number id = (Number) em.createNativeQuery(
                "INSERT INTO dbo.[PROMOTION] (PromotionName, Description, DiscountType, DiscountValue, StartDate, EndDate, PromotionStatus) " +
                "OUTPUT INSERTED.PromotionID " +
                "VALUES (?, ?, ?, ?, DATEADD(DAY, ?, GETDATE()), DATEADD(DAY, ?, GETDATE()), ?)")
                .setParameter(1, name)
                .setParameter(2, name + " desc")
                .setParameter(3, type)
                .setParameter(4, value)
                .setParameter(5, startOffsetDays)
                .setParameter(6, endOffsetDays)
                .setParameter(7, status)
                .getSingleResult();
        return id.intValue();
    }

    private void linkAlbumPromotion(Integer albumId, Integer promoId) {
        em.createNativeQuery("INSERT INTO dbo.[ALBUM_PROMOTION] (AlbumID, PromotionID) VALUES (?, ?)")
                .setParameter(1, albumId)
                .setParameter(2, promoId)
                .executeUpdate();
    }

    @BeforeEach
    void setUp() {
        testArtist = artistRepository.findAll().stream().findFirst().orElseGet(() -> {
            Artist art = new Artist();
            art.setArtistName("Promo Test Artist");
            art.setCountry("Sri Lanka");
            return artistRepository.save(art);
        });

        testGenre = genreRepository.findAll().stream().findFirst().orElseGet(() -> {
            Genre g = new Genre();
            g.setGenreName("Promo Test Genre");
            return genreRepository.save(g);
        });

        testListener = listenerRepository.findAll().stream().findFirst().orElseGet(() -> {
            Listener l = new Listener();
            l.setEmail("promotest@melodymart.com");
            l.setPasswordHash("hashed_secret");
            l.setFirstName("Promo");
            l.setLastName("Tester");
            l.setAccountStatus("Active");
            return listenerRepository.save(l);
        });

        cartService.clearCartForListener(testListener.getUserId());
    }

    @Test
    @DisplayName("Strategy & Decorator Pattern: Percentage and FixedAmount calculation correctness")
    void testStrategyAndDecoratorPatterns() {
        BigDecimal original = new BigDecimal("20.00");
        PricingComponent base = new BaseAlbumPrice(original);

        // 20% discount on $20.00 -> $16.00
        PricingComponent perc = new PromotionPriceDecorator(base, new PercentageDiscountStrategy(), new BigDecimal("20.00"));
        assertThat(perc.getPrice()).isEqualByComparingTo(new BigDecimal("16.00"));

        // $3.00 fixed discount on $20.00 -> $17.00
        PricingComponent fixed = new PromotionPriceDecorator(base, new FixedAmountDiscountStrategy(), new BigDecimal("3.00"));
        assertThat(fixed.getPrice()).isEqualByComparingTo(new BigDecimal("17.00"));
    }

    @Test
    @DisplayName("Case 1: No Promotion - Album price = original price, Cart total = original price")
    void testCase1_NoPromotion() {
        Album album = createAlbum("Album No Promo", new BigDecimal("25.00"));

        // Album Service retrieval
        Album retrieved = albumService.getAlbumWithDetails(album.getAlbumId());
        assertThat(retrieved.hasDiscount()).isFalse();
        assertThat(retrieved.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(retrieved.getPrice()).isEqualByComparingTo(new BigDecimal("25.00"));

        // Cart
        cartService.addAlbumToCart(testListener.getUserId(), album.getAlbumId(), 2);
        Cart cart = cartService.getCartForListener(testListener.getUserId());

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(cart.getItems().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
    }

    @Test
    @DisplayName("Case 2: Active Promotion - Album price = discounted price, Cart total = discounted price, Order total = discounted price")
    void testCase2_ActivePromotion() {
        // Original price $20.00, 20% OFF -> $16.00
        Album album = createAlbum("Album Active Promo", new BigDecimal("20.00"));
        Integer promoId = createPromotion("Summer Sale", "Percentage", new BigDecimal("20.00"), "Active", -2, 5);
        linkAlbumPromotion(album.getAlbumId(), promoId);

        // 1. Album Price check
        Album retrieved = albumService.getAlbumWithDetails(album.getAlbumId());
        assertThat(retrieved.hasDiscount()).isTrue();
        assertThat(retrieved.getPrice()).isEqualByComparingTo(new BigDecimal("20.00")); // base price preserved
        assertThat(retrieved.getDiscountedPrice()).isEqualByComparingTo(new BigDecimal("16.00"));
        assertThat(retrieved.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("16.00"));
        assertThat(retrieved.getDiscountLabel()).isEqualTo("20% OFF");

        // 2. Cart Total check (Quantity = 2 -> 2 * $16.00 = $32.00)
        cartService.addAlbumToCart(testListener.getUserId(), album.getAlbumId(), 2);
        Cart cart = cartService.getCartForListener(testListener.getUserId());

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("16.00"));
        assertThat(cart.getItems().get(0).getSubtotal()).isEqualByComparingTo(new BigDecimal("32.00"));
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("32.00"));

        // 3. Order Total & Payment check
        Integer orderId = listenerService.checkout(testListener.getUserId(), "Credit Card");
        assertThat(orderId).isNotNull();

        List<Map<String, Object>> orders = listenerService.getOrdersForListener(testListener.getUserId());
        Map<String, Object> order = orders.stream()
                .filter(o -> orderId.equals(o.get("orderId")))
                .findFirst()
                .orElseThrow();

        assertThat((BigDecimal) order.get("totalAmount")).isEqualByComparingTo(new BigDecimal("32.00"));

        // Verify order items unit price
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) order.get("items");
        assertThat(items).hasSize(1);
        assertThat((BigDecimal) items.get(0).get("unitPrice")).isEqualByComparingTo(new BigDecimal("16.00"));
    }

    @Test
    @DisplayName("Case 3: Expired Promotion - Album price = original price, Cart total = original price")
    void testCase3_ExpiredPromotion() {
        Album album = createAlbum("Album Expired Promo", new BigDecimal("30.00"));
        // Expired promotion: ended 2 days ago
        Integer promoId = createPromotion("Expired Winter Sale", "Percentage", new BigDecimal("50.00"), "Active", -10, -2);
        linkAlbumPromotion(album.getAlbumId(), promoId);

        // Album Price check
        Album retrieved = albumService.getAlbumWithDetails(album.getAlbumId());
        assertThat(retrieved.hasDiscount()).isFalse();
        assertThat(retrieved.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(retrieved.getPrice()).isEqualByComparingTo(new BigDecimal("30.00"));

        // Cart Total check
        cartService.addAlbumToCart(testListener.getUserId(), album.getAlbumId(), 1);
        Cart cart = cartService.getCartForListener(testListener.getUserId());

        assertThat(cart.getItems().get(0).getUnitPrice()).isEqualByComparingTo(new BigDecimal("30.00"));
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("30.00"));
    }

    @Test
    @DisplayName("Case 4: Multiple Albums - Only albums with active promotions receive discounts")
    void testCase4_MultipleAlbums() {
        // Album A: $20.00 with 25% Active Promotion -> $15.00
        Album albumA = createAlbum("Album A Active", new BigDecimal("20.00"));
        Integer promoActive = createPromotion("Flash Deal", "Percentage", new BigDecimal("25.00"), "Active", -1, 3);
        linkAlbumPromotion(albumA.getAlbumId(), promoActive);

        // Album B: $30.00 with No Promotion -> $30.00
        Album albumB = createAlbum("Album B No Promo", new BigDecimal("30.00"));

        // Album C: $40.00 with Expired Promotion ($10.00 off) -> $40.00
        Album albumC = createAlbum("Album C Expired", new BigDecimal("40.00"));
        Integer promoExpired = createPromotion("Old Gala", "FixedAmount", new BigDecimal("10.00"), "Expired", -15, -5);
        linkAlbumPromotion(albumC.getAlbumId(), promoExpired);

        // Verify Album Catalog
        List<Album> albums = albumService.getAllAlbums();
        Album aFromService = albums.stream().filter(a -> a.getAlbumId().equals(albumA.getAlbumId())).findFirst().orElseThrow();
        Album bFromService = albums.stream().filter(a -> a.getAlbumId().equals(albumB.getAlbumId())).findFirst().orElseThrow();
        Album cFromService = albums.stream().filter(a -> a.getAlbumId().equals(albumC.getAlbumId())).findFirst().orElseThrow();

        assertThat(aFromService.hasDiscount()).isTrue();
        assertThat(aFromService.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("15.00"));

        assertThat(bFromService.hasDiscount()).isFalse();
        assertThat(bFromService.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("30.00"));

        assertThat(cFromService.hasDiscount()).isFalse();
        assertThat(cFromService.getEffectivePrice()).isEqualByComparingTo(new BigDecimal("40.00"));

        // Add all 3 albums to cart:
        // A (qty 2) = 2 * 15 = 30
        // B (qty 1) = 1 * 30 = 30
        // C (qty 1) = 1 * 40 = 40
        // Total = 30 + 30 + 40 = 100
        cartService.addAlbumToCart(testListener.getUserId(), albumA.getAlbumId(), 2);
        cartService.addAlbumToCart(testListener.getUserId(), albumB.getAlbumId(), 1);
        cartService.addAlbumToCart(testListener.getUserId(), albumC.getAlbumId(), 1);

        Cart cart = cartService.getCartForListener(testListener.getUserId());
        assertThat(cart.getItems()).hasSize(3);
        assertThat(cart.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));

        // Checkout and verify Order total
        Integer orderId = listenerService.checkout(testListener.getUserId(), "Demo Payment");
        List<Map<String, Object>> orders = listenerService.getOrdersForListener(testListener.getUserId());
        Map<String, Object> order = orders.stream()
                .filter(o -> orderId.equals(o.get("orderId")))
                .findFirst()
                .orElseThrow();

        assertThat((BigDecimal) order.get("totalAmount")).isEqualByComparingTo(new BigDecimal("100.00"));
    }

    @Test
    @DisplayName("Case 5: Base Album Price in Database remains unchanged after promotions")
    void testCase5_PreserveOriginalPrice() {
        Album album = createAlbum("Base Price Persistence Album", new BigDecimal("50.00"));
        Integer promo = createPromotion("Big Sale", "Percentage", new BigDecimal("50.00"), "Active", -1, 5);
        linkAlbumPromotion(album.getAlbumId(), promo);

        // Query album from DB directly
        Album rawAlbum = em.find(Album.class, album.getAlbumId());
        assertThat(rawAlbum.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));

        // Service applies promotion transiently
        albumService.getAlbumWithDetails(album.getAlbumId());

        // Refresh from entity manager to verify column in table was NOT mutated
        em.flush();
        em.clear();
        Album reloaded = em.find(Album.class, album.getAlbumId());
        assertThat(reloaded.getPrice()).isEqualByComparingTo(new BigDecimal("50.00"));
    }
}
