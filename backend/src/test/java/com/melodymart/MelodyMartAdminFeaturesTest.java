package com.melodymart;
import com.melodymart.common.repository.AdministratorRepository;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.common.model.Administrator;
import com.melodymart.albumcatalog.repository.CatalogRepository;
import com.melodymart.common.service.AdminService;
import com.melodymart.common.model.Listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MelodyMartAdminFeaturesTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdminService adminService;

    @Autowired
    private AdministratorRepository administratorRepository;

    @Autowired
    private CatalogRepository catalogRepository;

    @Autowired
    private AlbumRepository albumRepository;

    private Administrator superAdmin;

    @BeforeEach
    public void setUp() {
        List<Administrator> admins = administratorRepository.findAll();
        assertThat(admins).isNotEmpty();
        superAdmin = admins.stream()
            .filter(a -> "SuperAdmin".equals(a.getAdminRole()))
            .findFirst()
            .orElse(admins.get(0));
    }

    private MockHttpSession createAdminSession(Administrator a) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", a.getUserId());
        session.setAttribute("userEmail", a.getEmail());
        session.setAttribute("userName", a.getFirstName() + " " + a.getLastName());
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("adminRole", a.getAdminRole());
        return session;
    }

    @Test
    @DisplayName("Admin FAQ CRUD Flow: View, Add, Edit, Delete")
    void testFaqCrudFlow() throws Exception {
        MockHttpSession session = createAdminSession(superAdmin);

        // 1. View FAQ list
        mockMvc.perform(get("/admin/faqs").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/support/faq"))
                .andExpect(model().attributeExists("faqs"));

        // 2. Add new FAQ
        mockMvc.perform(post("/admin/support/faq/add")
                .session(session)
                .param("question", "How do I test FAQ CRUD?")
                .param("answer", "By running automated JUnit MockMvc tests.")
                .param("faqStatus", "Published"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/support/faq"))
                .andExpect(flash().attributeExists("successMsg"));

        // Find the created FAQ
        List<Object[]> allFaqs = adminService.getAllFaqs();
        Object[] createdFaq = allFaqs.stream()
                .filter(f -> "How do I test FAQ CRUD?".equals(f[1]))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created FAQ not found"));
        Long faqId = ((Number) createdFaq[0]).longValue();

        // 3. Open Edit Form
        mockMvc.perform(get("/admin/support/faq/edit/" + faqId).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/support/faq-form"))
                .andExpect(model().attribute("formMode", "edit"))
                .andExpect(model().attributeExists("faq"));

        // 4. Submit Edit
        mockMvc.perform(post("/admin/support/faq/edit/" + faqId)
                .session(session)
                .param("question", "How do I test FAQ CRUD (Updated)?")
                .param("answer", "By running automated JUnit MockMvc tests with assertions.")
                .param("faqStatus", "Published"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/support/faq"))
                .andExpect(flash().attributeExists("successMsg"));

        // Verify update
        Object[] updated = adminService.getFaqById(faqId);
        assertThat(updated).isNotNull();
        assertThat(updated[1]).isEqualTo("How do I test FAQ CRUD (Updated)?");

        // 5. Delete FAQ
        mockMvc.perform(post("/admin/support/faq/delete/" + faqId).session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/support/faq"))
                .andExpect(flash().attributeExists("successMsg"));

        assertThat(adminService.getFaqById(faqId)).isNull();
    }

    @Test
    @DisplayName("Admin Dashboard Quick Action Routes: All accessible")
    void testDashboardQuickActionRoutes() throws Exception {
        MockHttpSession session = createAdminSession(superAdmin);

        // Dashboard
        mockMvc.perform(get("/admin/dashboard").session(session)).andExpect(status().isOk());
        // Listeners
        mockMvc.perform(get("/admin/listeners").session(session)).andExpect(status().isOk());
        // Albums
        mockMvc.perform(get("/admin/albums").session(session)).andExpect(status().isOk());
        // Artists
        mockMvc.perform(get("/admin/artists").session(session)).andExpect(status().isOk());
        // Genres
        mockMvc.perform(get("/admin/genres").session(session)).andExpect(status().isOk());
        // Catalogs
        mockMvc.perform(get("/admin/catalogs").session(session)).andExpect(status().isOk());
        // Orders
        mockMvc.perform(get("/admin/orders").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/orders/list").session(session)).andExpect(status().isOk());
        // Payments
        mockMvc.perform(get("/admin/payments").session(session)).andExpect(status().isOk());
        // Digital Library
        mockMvc.perform(get("/admin/library").session(session)).andExpect(status().isOk());
        // Downloads
        mockMvc.perform(get("/admin/downloads").session(session)).andExpect(status().isOk());
        // Support: Complaints, Reviews, FAQs, Promotions
        mockMvc.perform(get("/admin/complaints").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/reviews").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/faqs").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/promotions").session(session)).andExpect(status().isOk());
        // Users & Admins
        mockMvc.perform(get("/admin/users").session(session)).andExpect(status().isOk());
        mockMvc.perform(get("/admin/administrators").session(session)).andExpect(status().isOk());
    }

    @Test
    @DisplayName("Admin Order Decline Flow")
    void testOrderDeclineFlow() throws Exception {
        MockHttpSession session = createAdminSession(superAdmin);

        List<Object[]> orders = adminService.getAllOrders();
        Object[] pendingOrder = orders.stream()
                .filter(o -> "Pending".equalsIgnoreCase(o[4] != null ? o[4].toString() : ""))
                .findFirst()
                .orElse(null);

        if (pendingOrder != null) {
            Long orderId = ((Number) pendingOrder[0]).longValue();

            // Open order detail
            mockMvc.perform(get("/admin/orders/" + orderId).session(session))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/orders/detail"))
                    .andExpect(model().attributeExists("order", "orderItems"));

            // Decline order
            mockMvc.perform(post("/admin/orders/" + orderId + "/decline").session(session))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/admin/orders/" + orderId))
                    .andExpect(flash().attributeExists("successMsg"));

            // Verify status in DB is Cancelled
            Object[] declined = adminService.getOrderDetail(orderId);
            assertThat(declined).isNotNull();
            assertThat(declined[4]).isEqualTo("Cancelled");
        }
    }

    @Test
    @DisplayName("Catalog Album Management: Associate albums on create, add/remove on edit")
    void testCatalogAlbumManagement() throws Exception {
        MockHttpSession session = createAdminSession(superAdmin);

        List<Album> albums = albumRepository.findAll();
        assertThat(albums.size()).isGreaterThanOrEqualTo(2);
        Album album1 = albums.get(0);
        Album album2 = albums.get(1);

        // 1. Create Catalog with Album 1 and Album 2
        mockMvc.perform(post("/admin/catalog/catalogs/add")
                .session(session)
                .param("catalogName", "Test Bundle 2026")
                .param("description", "A test bundle with selected albums")
                .param("price", "19.99")
                .param("albumIds", album1.getAlbumId().toString(), album2.getAlbumId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/catalog/catalogs"))
                .andExpect(flash().attributeExists("successMsg"));

        // Verify catalog created and albums associated
        List<Catalog> catalogs = catalogRepository.findAll();
        Catalog created = catalogs.stream()
                .filter(c -> "Test Bundle 2026".equals(c.getCatalogName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created catalog not found"));

        List<Album> catAlbums = albumRepository.findByCatalog_CatalogId(created.getCatalogId());
        assertThat(catAlbums).extracting(Album::getAlbumId)
                .contains(album1.getAlbumId(), album2.getAlbumId());

        // 2. Edit Catalog: Remove Album 2, keep Album 1
        mockMvc.perform(post("/admin/catalog/catalogs/edit/" + created.getCatalogId())
                .session(session)
                .param("catalogName", "Test Bundle 2026 (Updated)")
                .param("description", "Updated bundle description")
                .param("price", "15.99")
                .param("albumIds", album1.getAlbumId().toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/catalog/catalogs"))
                .andExpect(flash().attributeExists("successMsg"));

        // Verify changes: album1 still in catalog, album2 removed from catalog (not deleted)
        List<Album> updatedCatAlbums = albumRepository.findByCatalog_CatalogId(created.getCatalogId());
        assertThat(updatedCatAlbums).extracting(Album::getAlbumId)
                .contains(album1.getAlbumId())
                .doesNotContain(album2.getAlbumId());

        // Verify album2 still exists in database
        assertThat(albumRepository.findById(album2.getAlbumId())).isPresent();

        // 3. Listener side catalog display check
        mockMvc.perform(get("/catalogs/" + created.getCatalogId()))
                .andExpect(status().isOk())
                .andExpect(view().name("album-catalog/catalog-detail"))
                .andExpect(model().attributeExists("catalog", "albums"));

        // Cleanup test catalog
        adminService.deleteCatalog(created.getCatalogId());
    }

    @Test
    @DisplayName("Album Cover Image Normalization, Preservation, and Static Resource Access")
    void testAlbumCoverImageFlow() throws Exception {
        MockHttpSession session = createAdminSession(superAdmin);

        // 1. Test URL Normalization rules in Album model
        assertThat(Album.normalizeCoverImageUrl("sanda_eliya.jpg"))
                .isEqualTo("/assets/covers/sanda_eliya.jpg");
        assertThat(Album.normalizeCoverImageUrl("assets/covers/sanda_eliya.jpg"))
                .isEqualTo("/assets/covers/sanda_eliya.jpg");
        assertThat(Album.normalizeCoverImageUrl("/assets/covers/sanda_eliya.jpg"))
                .isEqualTo("/assets/covers/sanda_eliya.jpg");
        assertThat(Album.normalizeCoverImageUrl("C:\\Users\\User\\Pictures\\sanda_eliya.jpg"))
                .isEqualTo("/assets/covers/sanda_eliya.jpg");
        assertThat(Album.normalizeCoverImageUrl("C:/Users/User/Pictures/sanda_eliya.jpg"))
                .isEqualTo("/assets/covers/sanda_eliya.jpg");
        assertThat(Album.normalizeCoverImageUrl("https://images.unsplash.com/photo-12345"))
                .isEqualTo("https://images.unsplash.com/photo-12345");
        assertThat(Album.normalizeCoverImageUrl("   "))
                .isNull();
        assertThat(Album.normalizeCoverImageUrl(null))
                .isNull();

        // 2. Test Admin Add Album with Cover Image
        List<com.melodymart.artistgenretrack.model.Artist> artists = adminService.getAllArtists();
        List<com.melodymart.artistgenretrack.model.Genre> genres = adminService.getAllGenres();
        assertThat(artists).isNotEmpty();
        assertThat(genres).isNotEmpty();

        mockMvc.perform(post("/admin/catalog/albums/add")
                .session(session)
                .param("albumTitle", "Test Cover Album")
                .param("artistId", artists.get(0).getArtistId().toString())
                .param("genreId", genres.get(0).getGenreId().toString())
                .param("price", "12.50")
                .param("albumStatus", "Available")
                .param("coverImageUrl", "test_cover_image.jpg")
                .param("description", "Testing cover image normalization"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/catalog/albums"))
                .andExpect(flash().attributeExists("successMsg"));

        List<Album> createdList = albumRepository.findByArtist_ArtistId(artists.get(0).getArtistId());
        Album created = createdList.stream()
                .filter(a -> "Test Cover Album".equals(a.getAlbumTitle()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Created album not found"));

        // Verify normalized local cover path stored in DB
        assertThat(created.getCoverImageUrl()).isEqualTo("/assets/covers/test_cover_image.jpg");

        // 3. Test Admin Edit Album WITHOUT changing coverImageUrl (blank input)
        mockMvc.perform(post("/admin/catalog/albums/edit/" + created.getAlbumId())
                .session(session)
                .param("albumTitle", "Test Cover Album (Updated Title)")
                .param("artistId", artists.get(0).getArtistId().toString())
                .param("genreId", genres.get(0).getGenreId().toString())
                .param("price", "14.99")
                .param("albumStatus", "Available")
                .param("coverImageUrl", "") // Blank -> keep unchanged
                .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/catalog/albums"))
                .andExpect(flash().attributeExists("successMsg"));

        Album afterEmptyEdit = albumRepository.findById(created.getAlbumId()).orElseThrow();
        assertThat(afterEmptyEdit.getAlbumTitle()).isEqualTo("Test Cover Album (Updated Title)");
        // CoverImageURL must remain unchanged!
        assertThat(afterEmptyEdit.getCoverImageUrl()).isEqualTo("/assets/covers/test_cover_image.jpg");

        // 4. Test Admin Edit Album WITH new coverImageUrl (e.g. external web URL)
        mockMvc.perform(post("/admin/catalog/albums/edit/" + created.getAlbumId())
                .session(session)
                .param("albumTitle", "Test Cover Album (Updated Title)")
                .param("artistId", artists.get(0).getArtistId().toString())
                .param("genreId", genres.get(0).getGenreId().toString())
                .param("price", "14.99")
                .param("albumStatus", "Available")
                .param("coverImageUrl", "https://images.unsplash.com/photo-music")
                .param("description", "Updated description"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/catalog/albums"));

        Album afterNewImageEdit = albumRepository.findById(created.getAlbumId()).orElseThrow();
        assertThat(afterNewImageEdit.getCoverImageUrl()).isEqualTo("https://images.unsplash.com/photo-music");

        // 5. Test Static Resource Serving for /assets/covers/README.txt
        mockMvc.perform(get("/assets/covers/README.txt"))
                .andExpect(status().isOk());

        // 6. Test Public Album Views Rendering
        mockMvc.perform(get("/albums"))
                .andExpect(status().isOk())
                .andExpect(view().name("album-catalog/albums"))
                .andExpect(model().attributeExists("albums"));

        mockMvc.perform(get("/albums/" + created.getAlbumId()))
                .andExpect(status().isOk())
                .andExpect(view().name("album-catalog/album-detail"))
                .andExpect(model().attributeExists("album"));

        // Cleanup test album
        adminService.deleteAlbum(created.getAlbumId());
    }
}
