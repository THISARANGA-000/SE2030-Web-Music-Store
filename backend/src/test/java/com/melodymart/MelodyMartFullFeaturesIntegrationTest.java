package com.melodymart;
import com.melodymart.common.service.ListenerService;
import com.melodymart.common.repository.AdministratorRepository;
import com.melodymart.complaintreview.service.ComplaintService;
import com.melodymart.libraryplaylist.model.PlaylistItem;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.artistgenretrack.repository.TrackRepository;
import com.melodymart.libraryplaylist.model.Playlist;
import com.melodymart.common.model.Administrator;
import com.melodymart.libraryplaylist.service.PlaylistService;
import com.melodymart.common.repository.ListenerRepository;
import com.melodymart.common.model.Listener;
import com.melodymart.artistgenretrack.model.Track;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class MelodyMartFullFeaturesIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PlaylistService playlistService;

    @Autowired
    private ComplaintService complaintService;

    @Autowired
    private ListenerService listenerService;

    @Autowired
    private com.melodymart.common.service.AdminService adminService;

    @Autowired
    private AlbumRepository albumRepository;

    @Autowired
    private TrackRepository trackRepository;

    @Autowired
    private ListenerRepository listenerRepository;

    @Autowired
    private AdministratorRepository administratorRepository;

    private Listener listenerA;
    private Listener listenerB;
    private Administrator admin;

    @BeforeEach
    public void setUp() {
        List<Listener> listeners = listenerRepository.findAll();
        assertThat(listeners).hasSizeGreaterThanOrEqualTo(2);
        listenerA = listeners.get(0);
        listenerB = listeners.get(1);

        List<Administrator> admins = administratorRepository.findAll();
        assertThat(admins).isNotEmpty();
        admin = admins.get(0);
    }

    /** Helper to create a test listener session */
    private MockHttpSession createListenerSession(Listener l) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", l.getUserId());
        session.setAttribute("userEmail", l.getEmail());
        session.setAttribute("userName", l.getFirstName() + " " + l.getLastName());
        session.setAttribute("userRole", "LISTENER");
        return session;
    }

    /** Helper to create a test admin session */
    private MockHttpSession createAdminSession(Administrator a) {
        MockHttpSession session = new MockHttpSession();
        session.setAttribute("userId", a.getUserId());
        session.setAttribute("userEmail", a.getEmail());
        session.setAttribute("userName", a.getFirstName() + " " + a.getLastName());
        session.setAttribute("userRole", "ADMIN");
        session.setAttribute("adminRole", a.getAdminRole());
        return session;
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST A: Existing Functionality & Regression
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test A: Regression - Public pages load successfully")
    void testA_RegressionPublicPages() throws Exception {
        // Albums page
        mockMvc.perform(get("/albums"))
                .andExpect(status().isOk())
                .andExpect(view().name("album-catalog/albums"))
                .andExpect(model().attributeExists("albums", "genres", "artists"));

        // Catalogs page
        mockMvc.perform(get("/catalogs"))
                .andExpect(status().isOk())
                .andExpect(view().name("album-catalog/catalogs"))
                .andExpect(model().attributeExists("catalogs"));

        // Promotions page
        mockMvc.perform(get("/promotions"))
                .andExpect(status().isOk())
                .andExpect(view().name("faq-promotion/promotions"))
                .andExpect(model().attributeExists("promotions"));

        // FAQs page
        mockMvc.perform(get("/faqs"))
                .andExpect(status().isOk())
                .andExpect(view().name("faq-promotion/faqs"))
                .andExpect(model().attributeExists("faqs"));

        // Album details for first album
        List<Album> allAlbums = albumRepository.findAll();
        if (!allAlbums.isEmpty()) {
            Integer firstId = allAlbums.get(0).getAlbumId();
            mockMvc.perform(get("/albums/" + firstId))
                    .andExpect(status().isOk())
                    .andExpect(view().name("album-catalog/album-detail"))
                    .andExpect(model().attributeExists("album"));
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST B: Playlist CRUD
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test B: Playlist CRUD - Create, Rename, List, Delete")
    void testB_PlaylistCrud() throws Exception {
        MockHttpSession session = createListenerSession(listenerA);
        Integer listenerId = listenerA.getUserId();

        // 1. Create "Workout"
        mockMvc.perform(post("/playlists")
                        .session(session)
                        .param("playlistName", "Workout"))
                .andExpect(status().is3xxRedirection());

        List<Playlist> list = playlistService.getPlaylistsForListener(listenerId);
        Optional<Playlist> workoutOpt = list.stream()
                .filter(p -> "Workout".equals(p.getPlaylistName()))
                .findFirst();
        assertThat(workoutOpt).isPresent();
        Integer workoutId = workoutOpt.get().getPlaylistId();

        // 2. Rename "Workout" -> "Workout Music"
        mockMvc.perform(post("/playlists/" + workoutId + "/edit")
                        .session(session)
                        .param("playlistName", "Workout Music"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/playlists/" + workoutId));

        Optional<Playlist> renamedOpt = playlistService.getPlaylistForListener(workoutId, listenerId);
        assertThat(renamedOpt).isPresent();
        assertThat(renamedOpt.get().getPlaylistName()).isEqualTo("Workout Music");

        // 3. Create another playlist: "Chill"
        mockMvc.perform(post("/playlists")
                        .session(session)
                        .param("playlistName", "Chill"))
                .andExpect(status().is3xxRedirection());

        List<Playlist> updatedList = playlistService.getPlaylistsForListener(listenerId);
        Optional<Playlist> chillOpt = updatedList.stream()
                .filter(p -> "Chill".equals(p.getPlaylistName()))
                .findFirst();
        assertThat(chillOpt).isPresent();
        Integer chillId = chillOpt.get().getPlaylistId();

        // 4. Delete "Chill"
        mockMvc.perform(post("/playlists/" + chillId + "/delete")
                        .session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/playlists"));

        Optional<Playlist> deletedChill = playlistService.getPlaylistForListener(chillId, listenerId);
        assertThat(deletedChill).isEmpty();

        // Clean up workout
        playlistService.deletePlaylist(workoutId, listenerId);
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST C & D & E: Playlist Album / Track Add, Duplicate, Remove (Purchased only)
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test C, D, E: Playlist Items - Add Purchased Album/Track, Duplicates, Remove, Reject Unpurchased")
    void testC_D_E_PlaylistItems() throws Exception {
        MockHttpSession session = createListenerSession(listenerA);
        Integer listenerId = listenerA.getUserId();

        // Create playlist
        Playlist playlist = playlistService.createPlaylist(listenerId, "Favorites Test");
        Integer playlistId = playlist.getPlaylistId();

        // 1. Test unpurchased rejection using listenerB (who has 0 purchases) on valid existing albums/tracks
        MockHttpSession sessionB = createListenerSession(listenerB);
        Integer listenerBId = listenerB.getUserId();
        Playlist playlistB = playlistService.createPlaylist(listenerBId, "Unpurchased Test Playlist");
        Integer playlistBId = playlistB.getPlaylistId();

        try {
            Integer existingAlbumId = albumRepository.findAll().get(0).getAlbumId();
            Integer existingTrackId = trackRepository.findAll().get(0).getTrackId();

            // Verify unpurchased album is rejected by backend
            mockMvc.perform(post("/playlists/" + playlistBId + "/albums/" + existingAlbumId)
                            .session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists/" + playlistBId))
                    .andExpect(flash().attribute("errorMsg", "You can only add albums that you have purchased."));

            // Verify unpurchased track is rejected by backend
            mockMvc.perform(post("/playlists/" + playlistBId + "/tracks/" + existingTrackId)
                            .session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists/" + playlistBId))
                    .andExpect(flash().attribute("errorMsg", "You can only add tracks from albums you have purchased."));
        } finally {
            playlistService.deletePlaylist(playlistBId, listenerBId);
        }

        // 2. Test purchased album addition, duplicate detection, and deletion for listenerA
        try {
            List<Album> purchasedAlbums = playlistService.getPurchasedAlbumsForListener(listenerId);
            if (!purchasedAlbums.isEmpty()) {
                Album purchasedAlbum = purchasedAlbums.get(0);
                Integer albId = purchasedAlbum.getAlbumId();

                // 1. Add purchased album
                mockMvc.perform(post("/playlists/" + playlistId + "/albums/" + albId)
                                .session(session))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/playlists/" + playlistId))
                        .andExpect(flash().attribute("successMsg", "Album added to playlist."));

                // 2. Duplicate album attempt
                mockMvc.perform(post("/playlists/" + playlistId + "/albums/" + albId)
                                .session(session))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/playlists/" + playlistId))
                        .andExpect(flash().attribute("errorMsg", "This album is already in the playlist."));

                // 3. Remove item
                List<PlaylistItem> items = playlistService.getItemsForPlaylist(playlistId, listenerId);
                assertThat(items).isNotEmpty();
                PlaylistItem it = items.get(0);

                mockMvc.perform(post("/playlists/" + playlistId + "/items/" + it.getPlaylistItemId() + "/delete")
                                .session(session))
                        .andExpect(status().is3xxRedirection())
                        .andExpect(redirectedUrl("/playlists/" + playlistId))
                        .andExpect(flash().attribute("successMsg", "Item removed from playlist."));
            }

        } finally {
            playlistService.deletePlaylist(playlistId, listenerId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST F: Strict Playlist Ownership Protection
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test F: Playlist Ownership - Listener B cannot access or modify Listener A's playlist")
    void testF_PlaylistOwnership() throws Exception {
        Integer listenerAId = listenerA.getUserId();
        MockHttpSession sessionB = createListenerSession(listenerB);

        // Listener A creates a playlist
        Playlist playlistA = playlistService.createPlaylist(listenerAId, "Secret Playlist A");
        Integer playlistAId = playlistA.getPlaylistId();

        try {
            // 1. Listener B attempts GET /playlists/{playlistAId} -> should redirect with error
            mockMvc.perform(get("/playlists/" + playlistAId).session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists"))
                    .andExpect(flash().attribute("errorMsg", "Playlist not found or access denied."));

            // 2. Listener B attempts GET /playlists/{playlistAId}/edit
            mockMvc.perform(get("/playlists/" + playlistAId + "/edit").session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists"))
                    .andExpect(flash().attribute("errorMsg", "Playlist not found or access denied."));

            // 3. Listener B attempts POST /playlists/{playlistAId}/edit (rename)
            mockMvc.perform(post("/playlists/" + playlistAId + "/edit")
                            .session(sessionB)
                            .param("playlistName", "Hacked Name"))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists"))
                    .andExpect(flash().attribute("errorMsg", "Playlist not found or access denied."));

            // Verify name was NOT changed
            Playlist freshA = playlistService.getPlaylistForListener(playlistAId, listenerAId).orElseThrow();
            assertThat(freshA.getPlaylistName()).isEqualTo("Secret Playlist A");

            // 4. Listener B attempts POST /playlists/{playlistAId}/delete
            mockMvc.perform(post("/playlists/" + playlistAId + "/delete").session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists"))
                    .andExpect(flash().attribute("errorMsg", "Playlist not found or access denied."));

            // Verify playlist A still exists
            assertThat(playlistService.getPlaylistForListener(playlistAId, listenerAId)).isPresent();

            // 5. Listener B attempts to add album to Listener A's playlist
            Integer albumId = albumRepository.findAll().get(0).getAlbumId();
            mockMvc.perform(post("/playlists/" + playlistAId + "/albums/" + albumId).session(sessionB))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/playlists"))
                    .andExpect(flash().attribute("errorMsg", "Playlist not found or access denied."));

            assertThat(playlistService.getItemsForPlaylist(playlistAId, listenerAId)).isEmpty();

        } finally {
            playlistService.deletePlaylist(playlistAId, listenerAId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST G, H, I: Complaint Creation, Listing, Details
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test G, H, I: Complaint Flow - Submit, List, View Details")
    void testG_H_I_ComplaintFlow() throws Exception {
        MockHttpSession session = createListenerSession(listenerA);
        Integer listenerId = listenerA.getUserId();

        // G. Submit complaint
        mockMvc.perform(post("/complaints")
                        .session(session)
                        .param("subject", "Test Audio Bug")
                        .param("description", "Track 2 cut off early."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/complaints?success=true"));

        // H. List complaints for listener
        mockMvc.perform(get("/complaints").session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("complaint-review/complaints"))
                .andExpect(model().attributeExists("complaints"));

        List<Object[]> complaints = complaintService.getComplaintsForListener(listenerId);
        assertThat(complaints).isNotEmpty();
        Object[] latest = complaints.get(0);
        Integer complaintId = (Integer) latest[0];
        assertThat(latest[1]).isEqualTo("Test Audio Bug");
        assertThat(latest[4]).isEqualTo("Open"); // initial status
        assertThat(latest[5]).isNull(); // response is null initially
        assertThat(latest[6]).isNull(); // resolvedDate is null initially

        // I. View complaint details
        mockMvc.perform(get("/complaints/" + complaintId).session(session))
                .andExpect(status().isOk())
                .andExpect(view().name("complaint-review/complaint-detail"))
                .andExpect(model().attributeExists("complaint"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST J: Strict Complaint Ownership Protection
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test J: Complaint Ownership - Listener B cannot view Listener A's complaint")
    void testJ_ComplaintOwnership() throws Exception {
        Integer listenerAId = listenerA.getUserId();

        // Submit complaint for Listener A
        complaintService.createComplaint(listenerAId, "Private Issue for Listener A", "Confidential details.");
        List<Object[]> complaintsA = complaintService.getComplaintsForListener(listenerAId);
        Integer complaintAId = (Integer) complaintsA.get(0)[0];

        // Listener B tries to view it
        MockHttpSession sessionB = createListenerSession(listenerB);
        mockMvc.perform(get("/complaints/" + complaintAId).session(sessionB))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/complaints"))
                .andExpect(flash().attribute("errorMsg", "Complaint not found or access denied."));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST K: Admin Complaint Management Flow
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test K: Admin Complaint Flow - Admin updates status & response, listener sees update")
    void testK_AdminComplaintFlow() throws Exception {
        Integer listenerAId = listenerA.getUserId();
        complaintService.createComplaint(listenerAId, "Feedback on UI", "Navigation is great.");

        List<Object[]> complaintsA = complaintService.getComplaintsForListener(listenerAId);
        Integer complaintId = (Integer) complaintsA.get(0)[0];

        // Admin logs in and updates status to 'In Progress' with response
        MockHttpSession adminSession = createAdminSession(admin);
        mockMvc.perform(post("/admin/support/complaints/" + complaintId + "/status")
                        .session(adminSession)
                        .param("status", "In Progress")
                        .param("response", "Thank you for the feedback. We are reviewing it."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/support/complaints"));

        // Listener A logs in and views complaint
        MockHttpSession sessionA = createListenerSession(listenerA);
        mockMvc.perform(get("/complaints/" + complaintId).session(sessionA))
                .andExpect(status().isOk())
                .andExpect(view().name("complaint-review/complaint-detail"));

        Object[] updatedComplaint = complaintService.getComplaintForListener(complaintId, listenerAId);
        assertThat(updatedComplaint[4]).isEqualTo("In Progress");
        assertThat(updatedComplaint[5]).isEqualTo("Thank you for the feedback. We are reviewing it.");
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST L: Session Protection
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Test L: Session Protection - Unauthenticated access redirects to /login")
    void testL_SessionProtection() throws Exception {
        mockMvc.perform(get("/playlists"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/playlists/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/complaints"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/complaints/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(get("/my-library"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // DIGITAL LIBRARY: Revoke item ownership test
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Library Removal: Listener can only revoke own active library item")
    void testLibrary_RevokeOwnership() throws Exception {
        // Test with non-existent or foreign IDs
        boolean res = listenerService.removeLibraryItem(99999, 99999);
        assertThat(res).isFalse();

        // Foreign listener attempt via HTTP route
        MockHttpSession sessionB = createListenerSession(listenerB);
        mockMvc.perform(post("/my-library/remove/99999").session(sessionB))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/my-library"))
                .andExpect(flash().attribute("errorMsg", "Unable to remove item. Access denied or item not found."));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ALBUM PAGE INTEGRATION: Add to Playlist from Album page
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Album Page Integration: Add album and track to playlist from album page")
    void testAlbumPage_AddToPlaylist() throws Exception {
        MockHttpSession sessionA = createListenerSession(listenerA);
        Integer listenerAId = listenerA.getUserId();

        Playlist pl = playlistService.createPlaylist(listenerAId, "Album Page Test Playlist");
        Integer plId = pl.getPlaylistId();

        try {
            Integer albumId = albumRepository.findAll().get(0).getAlbumId();
            Integer trackId = trackRepository.findAll().get(0).getTrackId();

            // Add album from album page
            mockMvc.perform(post("/playlists/add-album-from-page")
                            .session(sessionA)
                            .param("albumId", albumId.toString())
                            .param("playlistId", plId.toString()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/albums/" + albumId))
                    .andExpect(flash().attribute("successMsg", "Album added to your playlist!"));

            // Duplicate attempt
            mockMvc.perform(post("/playlists/add-album-from-page")
                            .session(sessionA)
                            .param("albumId", albumId.toString())
                            .param("playlistId", plId.toString()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/albums/" + albumId))
                    .andExpect(flash().attribute("errorMsg", "This album is already in that playlist."));

            // Add track from album page
            mockMvc.perform(post("/playlists/add-track-from-page")
                            .session(sessionA)
                            .param("trackId", trackId.toString())
                            .param("playlistId", plId.toString())
                            .param("albumId", albumId.toString()))
                    .andExpect(status().is3xxRedirection())
                    .andExpect(redirectedUrl("/albums/" + albumId))
                    .andExpect(flash().attribute("successMsg", "Track added to your playlist!"));

            assertThat(playlistService.getItemsForPlaylist(plId, listenerAId)).hasSize(2);
        } finally {
            playlistService.deletePlaylist(plId, listenerAId);
        }
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // LISTENER REVIEWS: Submit rating, validation, and viewing
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Listener Reviews: Submit review, rating validation, and view in my-reviews")
    void testListenerReviews() throws Exception {
        MockHttpSession sessionA = createListenerSession(listenerA);
        Integer albumId = albumRepository.findAll().get(0).getAlbumId();

        // 1. Invalid rating (> 5)
        mockMvc.perform(post("/albums/" + albumId + "/reviews")
                        .session(sessionA)
                        .param("rating", "10")
                        .param("comment", "Amazing!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attribute("errorMsg", "Please select a rating between 1 and 5 stars."));

        // 2. Valid review submission
        mockMvc.perform(post("/albums/" + albumId + "/reviews")
                        .session(sessionA)
                        .param("rating", "5")
                        .param("comment", "Outstanding production and crystal clear mastering."))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/albums/" + albumId + "#reviews"));

        // 3. View my reviews page
        mockMvc.perform(get("/my-reviews").session(sessionA))
                .andExpect(status().isOk())
                .andExpect(view().name("common/my-reviews"))
                .andExpect(model().attributeExists("reviews"));
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // ORDER LIFECYCLE: Checkout is Pending -> Admin Accept -> Library Activated -> Admin Delete
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Order Lifecycle: Checkout creates Pending order, Admin Accept grants Library, Admin Delete removes order")
    void testOrderLifecycle_CheckoutPending_AdminAccept_AdminDelete() throws Exception {
        MockHttpSession sessionA = createListenerSession(listenerA);
        MockHttpSession sessionAdmin = createAdminSession(admin);
        Integer listenerId = listenerA.getUserId();

        Integer albumId = albumRepository.findAll().get(0).getAlbumId();

        // 1. Add album to cart
        mockMvc.perform(post("/cart/add")
                        .session(sessionA)
                        .param("albumId", albumId.toString()))
                .andExpect(status().is3xxRedirection());

        // 2. Checkout
        org.springframework.test.web.servlet.MvcResult checkoutResult = mockMvc.perform(post("/checkout")
                        .session(sessionA)
                        .param("paymentMethod", "Credit Card"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/checkout/success?orderId=*"))
                .andReturn();

        String redirectedUrl = checkoutResult.getResponse().getRedirectedUrl();
        Integer orderId = Integer.parseInt(redirectedUrl.substring(redirectedUrl.indexOf("orderId=") + 8));

        // 3. Find created order
        List<java.util.Map<String, Object>> listenerOrders = listenerService.getOrdersForListener(listenerId);
        assertThat(listenerOrders).isNotEmpty();
        java.util.Map<String, Object> createdOrder = listenerOrders.stream()
                .filter(o -> orderId.equals(o.get("orderId")))
                .findFirst()
                .orElseThrow();
        String orderStatus = (String) createdOrder.get("orderStatus");

        // MUST BE PENDING - NOT AUTO-ACCEPTED
        assertThat(orderStatus).isEqualTo("Pending");

        // 4. Verify Admin sees Pending order
        mockMvc.perform(get("/admin/orders").session(sessionAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders/list"))
                .andExpect(model().attributeExists("orders"));

        // 5. Admin accepts order
        mockMvc.perform(post("/admin/orders/" + orderId + "/accept").session(sessionAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"))
                .andExpect(flash().attributeExists("successMsg"));

        // Verify status is now Completed in DB
        List<java.util.Map<String, Object>> updatedOrders = listenerService.getOrdersForListener(listenerId);
        java.util.Map<String, Object> acceptedOrder = updatedOrders.stream()
                .filter(o -> orderId.equals(o.get("orderId")))
                .findFirst()
                .orElseThrow();
        assertThat(acceptedOrder.get("orderStatus")).isEqualTo("Completed");

        // 6. Verify Digital Library now has the album Active
        List<java.util.Map<String, Object>> library = listenerService.getDigitalLibraryForListener(listenerId);
        boolean inLibrary = library.stream().anyMatch(li -> albumId.equals(li.get("albumId")) && "Active".equals(li.get("accessStatus")));
        assertThat(inLibrary).isTrue();

        // 7. Admin Deletes order
        mockMvc.perform(post("/admin/orders/" + orderId + "/delete").session(sessionAdmin))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/orders"))
                .andExpect(flash().attributeExists("successMsg"));

        // Verify order is deleted
        List<java.util.Map<String, Object>> remainingOrders = listenerService.getOrdersForListener(listenerId);
        boolean orderStillExists = remainingOrders.stream().anyMatch(o -> orderId.equals(o.get("orderId")));
        assertThat(orderStillExists).isFalse();
    }

    // ─────────────────────────────────────────────────────────────────────────────
    // TEST M: Enhanced Review & Complaint Management Flow
    // ─────────────────────────────────────────────────────────────────────────────
    @Test
    @DisplayName("Review & Complaint Flow: Listener edit/delete review & delete own complaint, Admin detail & status update")
    void testEnhancedReviewAndComplaintFlow() throws Exception {
        MockHttpSession sessionA = createListenerSession(listenerA);
        MockHttpSession sessionB = createListenerSession(listenerB);
        MockHttpSession sessionAdmin = createAdminSession(admin);
        Integer listenerAId = listenerA.getUserId();
        Integer listenerBId = listenerB.getUserId();

        // ── 1. Complaint Deletion Ownership Test ──
        complaintService.createComplaint(listenerAId, "Listener A Issue", "Detailed description for Listener A");
        List<Object[]> complaintsA = complaintService.getComplaintsForListener(listenerAId);
        Integer compAId = (Integer) complaintsA.get(0)[0];

        // Listener B tries to delete Listener A's complaint -> Should fail / redirect with error
        mockMvc.perform(post("/complaints/" + compAId + "/delete").session(sessionB))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/complaints"))
                .andExpect(flash().attribute("errorMsg", "Complaint not found or access denied."));

        // Listener A deletes own complaint -> Should succeed
        mockMvc.perform(post("/complaints/" + compAId + "/delete").session(sessionA))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/complaints"))
                .andExpect(flash().attribute("successMsg", "Complaint deleted successfully."));

        // ── 2. Admin Review & Complaint Detail Views ──
        // Create new complaint for Admin inspection
        complaintService.createComplaint(listenerAId, "Admin Inspection Issue", "Requires admin review.");
        List<Object[]> freshComplaints = complaintService.getComplaintsForListener(listenerAId);
        Integer inspectionCompId = (Integer) freshComplaints.get(0)[0];

        // Admin opens complaint detail
        mockMvc.perform(get("/admin/support/complaints/" + inspectionCompId).session(sessionAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/support/complaint-detail"))
                .andExpect(model().attributeExists("complaint"));

        // Admin updates status & response from detail page
        mockMvc.perform(post("/admin/support/complaints/" + inspectionCompId + "/status")
                        .session(sessionAdmin)
                        .param("status", "Resolved")
                        .param("response", "This has been resolved by our support engineers.")
                        .param("returnUrl", "/admin/support/complaints/" + inspectionCompId))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/support/complaints/" + inspectionCompId))
                .andExpect(flash().attributeExists("successMsg"));

        // Admin views reviews list
        mockMvc.perform(get("/admin/reviews").session(sessionAdmin))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/support/reviews"))
                .andExpect(model().attributeExists("reviews"));

        List<Object[]> allReviews = adminService.getAllReviews();
        if (!allReviews.isEmpty()) {
            Long firstReviewId = ((Number) allReviews.get(0)[0]).longValue();
            // Admin opens review detail
            mockMvc.perform(get("/admin/support/reviews/" + firstReviewId).session(sessionAdmin))
                    .andExpect(status().isOk())
                    .andExpect(view().name("admin/support/review-detail"))
                    .andExpect(model().attributeExists("review"));
        }
    }
}
