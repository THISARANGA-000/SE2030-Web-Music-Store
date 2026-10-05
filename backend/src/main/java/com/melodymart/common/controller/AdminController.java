package com.melodymart.common.controller;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.model.Catalog;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.artistgenretrack.model.Track;
import com.melodymart.common.model.User;
import com.melodymart.common.service.AdminService;
import com.melodymart.common.service.AuthService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final AuthService authService;

    private static final List<String> CATALOG_ROLES  = Arrays.asList("SuperAdmin", "StoreManager", "CatalogCoordinator");
    private static final List<String> ORDER_ROLES    = Arrays.asList("SuperAdmin", "SalesManager");
    private static final List<String> SUPPORT_ROLES  = Arrays.asList("SuperAdmin", "CustomerSupport");
    private static final List<String> SUPER_ONLY     = Arrays.asList("SuperAdmin");
    private static final List<String> ALL_ROLES      = Arrays.asList("SuperAdmin", "StoreManager", "SalesManager", "CustomerSupport", "CatalogCoordinator");

    @Autowired
    public AdminController(AdminService adminService, AuthService authService) {
        this.adminService = adminService;
        this.authService = authService;
    }

    // ─── Security Helpers ────────────────────────────────────────────────────

    private String checkAccess(HttpSession session, List<String> allowedRoles) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        String userRole = (String) session.getAttribute("userRole");
        if (!"ADMIN".equals(userRole)) return "redirect:/albums?accessDenied";
        String adminRole = (String) session.getAttribute("adminRole");
        if (adminRole == null || !allowedRoles.contains(adminRole)) return "redirect:/admin/access-denied";
        return null;
    }

    private void populateAdminModel(Model model, HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null) {
            Optional<User> u = authService.findById(userId);
            u.ifPresent(usr -> {
                model.addAttribute("user", usr);
                model.addAttribute("adminInfo", authService.getAdminInfo(usr));
            });
        }
        model.addAttribute("adminRole", session.getAttribute("adminRole"));
        model.addAttribute("userName", session.getAttribute("userName"));
    }

    // ─── Dashboard ───────────────────────────────────────────────────────────

    @GetMapping({"/dashboard", ""})
    public String dashboard(HttpSession session, Model model) {
        String r = checkAccess(session, ALL_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        String adminRole = (String) session.getAttribute("adminRole");
        Map<String, Object> stats;
        String dashType;
        if ("SuperAdmin".equals(adminRole)) {
            stats = adminService.getDashboardStats(); dashType = "super";
        } else if (ORDER_ROLES.contains(adminRole)) {
            stats = adminService.getOrderStats(); dashType = "order";
        } else if (SUPPORT_ROLES.contains(adminRole)) {
            stats = adminService.getSupportStats(); dashType = "support";
        } else {
            stats = adminService.getCatalogStats(); dashType = "catalog";
        }
        model.addAttribute("stats", stats);
        model.addAttribute("dashboardType", dashType);
        return "admin/dashboard";
    }

    @GetMapping("/access-denied")
    public String accessDenied(HttpSession session, Model model) {
        populateAdminModel(model, session);
        return "admin/access-denied";
    }

    // ─── Listener Management ─────────────────────────────────────────────────

    @GetMapping("/listeners")
    public String listeners(HttpSession session, Model model,
                            @RequestParam(value = "search", required = false) String search) {
        String r = checkAccess(session, ALL_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("listeners", adminService.getAllListeners(search));
        model.addAttribute("search", search);
        return "admin/listeners";
    }

    @GetMapping("/listeners/{id}")
    public String listenerDetail(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, ALL_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("listener", adminService.getListenerDetail(id));
        model.addAttribute("listenerOrders", adminService.getOrdersByListener(id));
        model.addAttribute("listenerReviews", adminService.getReviewsByListener(id));
        return "admin/listener-detail";
    }

    // ─── Catalog: Albums ─────────────────────────────────────────────────────

    @GetMapping({"/albums", "/catalog/albums"})
    public String albums(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("albums", adminService.getAllAlbums());
        return "admin/catalog/albums";
    }

    @GetMapping("/catalog/albums/add")
    public String addAlbumForm(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("album", new Album());
        model.addAttribute("artists", adminService.getAllArtists());
        model.addAttribute("genres", adminService.getAllGenres());
        model.addAttribute("catalogs", adminService.getAllCatalogs());
        model.addAttribute("formMode", "add");
        return "admin/catalog/album-form";
    }

    @PostMapping("/catalog/albums/add")
    public String addAlbumSubmit(HttpSession session,
                                 @RequestParam("albumTitle") String albumTitle,
                                 @RequestParam(value = "releaseDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                                 @RequestParam("albumStatus") String albumStatus,
                                 @RequestParam("price") BigDecimal price,
                                 @RequestParam(value = "description", required = false) String description,
                                 @RequestParam(value = "coverImageUrl", required = false) String coverImageUrl,
                                 @RequestParam("artistId") Integer artistId,
                                 @RequestParam("genreId") Integer genreId,
                                 @RequestParam(value = "catalogId", required = false) Integer catalogId,
                                 RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Album album = new Album();
            album.setAlbumTitle(albumTitle); album.setReleaseDate(releaseDate);
            album.setAlbumStatus(albumStatus); album.setPrice(price);
            album.setDescription(description); album.setCoverImageUrl(coverImageUrl);
            adminService.getArtistById(artistId).ifPresent(album::setArtist);
            adminService.getGenreById(genreId).ifPresent(album::setGenre);
            if (catalogId != null) adminService.getCatalogById(catalogId).ifPresent(album::setCatalog);
            adminService.saveAlbum(album);
            ra.addFlashAttribute("successMsg", "Album added successfully.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage()); }
        return "redirect:/admin/catalog/albums";
    }

    @GetMapping("/catalog/albums/edit/{id}")
    public String editAlbumForm(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        Optional<Album> opt = adminService.getAlbumById(id);
        if (opt.isEmpty()) return "redirect:/admin/catalog/albums";
        populateAdminModel(model, session);
        model.addAttribute("album", opt.get());
        model.addAttribute("artists", adminService.getAllArtists());
        model.addAttribute("genres", adminService.getAllGenres());
        model.addAttribute("catalogs", adminService.getAllCatalogs());
        model.addAttribute("formMode", "edit");
        return "admin/catalog/album-form";
    }

    @PostMapping("/catalog/albums/edit/{id}")
    public String editAlbumSubmit(@PathVariable Integer id, HttpSession session,
                                  @RequestParam("albumTitle") String albumTitle,
                                  @RequestParam(value = "releaseDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate releaseDate,
                                  @RequestParam("albumStatus") String albumStatus,
                                  @RequestParam("price") BigDecimal price,
                                  @RequestParam(value = "description", required = false) String description,
                                  @RequestParam(value = "coverImageUrl", required = false) String coverImageUrl,
                                  @RequestParam("artistId") Integer artistId,
                                  @RequestParam("genreId") Integer genreId,
                                  @RequestParam(value = "catalogId", required = false) Integer catalogId,
                                  RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Optional<Album> opt = adminService.getAlbumById(id);
            if (opt.isEmpty()) { ra.addFlashAttribute("errorMsg", "Not found."); return "redirect:/admin/catalog/albums"; }
            Album a = opt.get();
            a.setAlbumTitle(albumTitle); a.setReleaseDate(releaseDate); a.setAlbumStatus(albumStatus);
            a.setPrice(price); a.setDescription(description);
            if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
                a.setCoverImageUrl(coverImageUrl.trim());
            }
            adminService.getArtistById(artistId).ifPresent(a::setArtist);
            adminService.getGenreById(genreId).ifPresent(a::setGenre);
            a.setCatalog(catalogId != null ? adminService.getCatalogById(catalogId).orElse(null) : null);
            adminService.saveAlbum(a);
            ra.addFlashAttribute("successMsg", "Album updated.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage()); }
        return "redirect:/admin/catalog/albums";
    }

    @PostMapping("/catalog/albums/delete/{id}")
    public String deleteAlbum(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.deleteAlbum(id); ra.addFlashAttribute("successMsg", "Album deleted."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", "Cannot delete: " + e.getMessage()); }
        return "redirect:/admin/catalog/albums";
    }

    // ─── Catalog: Artists ─────────────────────────────────────────────────────

    @GetMapping({"/artists", "/catalog/artists"})
    public String artists(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("artists", adminService.getAllArtists());
        return "admin/catalog/artists";
    }

    @GetMapping("/catalog/artists/add")
    public String addArtistForm(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("artist", new Artist()); model.addAttribute("formMode", "add");
        return "admin/catalog/artist-form";
    }

    @PostMapping("/catalog/artists/add")
    public String addArtistSubmit(HttpSession session, @RequestParam("artistName") String artistName,
                                  @RequestParam(value="biography",required=false) String bio,
                                  @RequestParam(value="country",required=false) String country, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.saveArtist(new Artist(artistName, bio, country)); ra.addFlashAttribute("successMsg","Artist added."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/artists";
    }

    @GetMapping("/catalog/artists/edit/{id}")
    public String editArtistForm(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        Optional<Artist> opt = adminService.getArtistById(id); if (opt.isEmpty()) return "redirect:/admin/catalog/artists";
        populateAdminModel(model, session); model.addAttribute("artist", opt.get()); model.addAttribute("formMode","edit");
        return "admin/catalog/artist-form";
    }

    @PostMapping("/catalog/artists/edit/{id}")
    public String editArtistSubmit(@PathVariable Integer id, HttpSession session,
                                   @RequestParam("artistName") String name,
                                   @RequestParam(value="biography",required=false) String bio,
                                   @RequestParam(value="country",required=false) String country, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Artist a = adminService.getArtistById(id).orElseThrow();
            a.setArtistName(name); a.setBiography(bio); a.setCountry(country);
            adminService.saveArtist(a); ra.addFlashAttribute("successMsg","Artist updated.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/artists";
    }

    @PostMapping("/catalog/artists/delete/{id}")
    public String deleteArtist(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.deleteArtist(id); ra.addFlashAttribute("successMsg","Artist deleted."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Cannot delete (may have albums)."); }
        return "redirect:/admin/catalog/artists";
    }

    // ─── Catalog: Genres ──────────────────────────────────────────────────────

    @GetMapping({"/genres", "/catalog/genres"})
    public String genres(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("genres", adminService.getAllGenres());
        return "admin/catalog/genres";
    }

    @GetMapping("/catalog/genres/add")
    public String addGenreForm(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("genre", new Genre()); model.addAttribute("formMode","add");
        return "admin/catalog/genre-form";
    }

    @PostMapping("/catalog/genres/add")
    public String addGenreSubmit(HttpSession session, @RequestParam("genreName") String name,
                                 @RequestParam(value="description",required=false) String desc, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.saveGenre(new Genre(name, desc)); ra.addFlashAttribute("successMsg","Genre added."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/genres";
    }

    @GetMapping("/catalog/genres/edit/{id}")
    public String editGenreForm(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        Optional<Genre> opt = adminService.getGenreById(id); if (opt.isEmpty()) return "redirect:/admin/catalog/genres";
        populateAdminModel(model, session); model.addAttribute("genre", opt.get()); model.addAttribute("formMode","edit");
        return "admin/catalog/genre-form";
    }

    @PostMapping("/catalog/genres/edit/{id}")
    public String editGenreSubmit(@PathVariable Integer id, HttpSession session,
                                  @RequestParam("genreName") String name,
                                  @RequestParam(value="description",required=false) String desc, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Genre g = adminService.getGenreById(id).orElseThrow();
            g.setGenreName(name); g.setDescription(desc);
            adminService.saveGenre(g); ra.addFlashAttribute("successMsg","Genre updated.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/genres";
    }

    @PostMapping("/catalog/genres/delete/{id}")
    public String deleteGenre(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.deleteGenre(id); ra.addFlashAttribute("successMsg","Genre deleted."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Cannot delete (may have albums)."); }
        return "redirect:/admin/catalog/genres";
    }

    // ─── Catalog: Catalogs ────────────────────────────────────────────────────

    @GetMapping({"/catalogs", "/catalog/catalogs"})
    public String catalogs(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("catalogs", adminService.getAllCatalogs());
        return "admin/catalog/catalogs";
    }

    @GetMapping({"/catalogs/add", "/catalog/catalogs/add"})
    public String addCatalogForm(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("catalog", new Catalog());
        model.addAttribute("albums", adminService.getAllAlbums());
        model.addAttribute("formMode","add");
        return "admin/catalog/catalog-form";
    }

    @PostMapping({"/catalogs/add", "/catalog/catalogs/add"})
    public String addCatalogSubmit(HttpSession session, @RequestParam("catalogName") String name,
                                   @RequestParam(value="description",required=false) String desc,
                                   @RequestParam("price") BigDecimal price,
                                   @RequestParam(value="albumIds", required=false) List<Integer> albumIds,
                                   RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            adminService.saveCatalogWithAlbums(new Catalog(name, desc, price, java.time.LocalDateTime.now()), albumIds);
            ra.addFlashAttribute("successMsg","Catalog created successfully.");
        }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/catalogs";
    }

    @GetMapping({"/catalogs/edit/{id}", "/catalog/catalogs/edit/{id}"})
    public String editCatalogForm(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        Optional<Catalog> opt = adminService.getCatalogById(id); if (opt.isEmpty()) return "redirect:/admin/catalog/catalogs";
        populateAdminModel(model, session);
        List<Album> allAlbums = adminService.getAllAlbums();
        List<Integer> selectedAlbumIds = allAlbums.stream()
            .filter(a -> a.getCatalog() != null && id.equals(a.getCatalog().getCatalogId()))
            .map(Album::getAlbumId)
            .toList();
        model.addAttribute("catalog", opt.get());
        model.addAttribute("albums", allAlbums);
        model.addAttribute("selectedAlbumIds", selectedAlbumIds);
        model.addAttribute("formMode","edit");
        return "admin/catalog/catalog-form";
    }

    @PostMapping({"/catalogs/edit/{id}", "/catalog/catalogs/edit/{id}"})
    public String editCatalogSubmit(@PathVariable Integer id, HttpSession session,
                                    @RequestParam("catalogName") String name,
                                    @RequestParam(value="description",required=false) String desc,
                                    @RequestParam("price") BigDecimal price,
                                    @RequestParam(value="albumIds", required=false) List<Integer> albumIds,
                                    RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            adminService.updateCatalogWithAlbums(id, name, desc, price, albumIds);
            ra.addFlashAttribute("successMsg","Catalog updated successfully.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/catalog/catalogs";
    }

    @PostMapping("/catalog/catalogs/delete/{id}")
    public String deleteCatalog(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.deleteCatalog(id); ra.addFlashAttribute("successMsg","Catalog deleted."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Cannot delete (may have albums)."); }
        return "redirect:/admin/catalog/catalogs";
    }

    // ─── Catalog: Tracks ─────────────────────────────────────────────────────

    @GetMapping({"/tracks", "/catalog/tracks"})
    public String tracks(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("tracks", adminService.getAllTracks());
        return "admin/catalog/tracks";
    }

    @GetMapping("/catalog/tracks/add")
    public String addTrackForm(HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("track", new Track());
        model.addAttribute("albums", adminService.getAllAlbums());
        model.addAttribute("formMode", "add");
        return "admin/catalog/track-form";
    }

    @PostMapping("/catalog/tracks/add")
    public String addTrackSubmit(HttpSession session,
                                 @RequestParam("trackTitle") String trackTitle,
                                 @RequestParam("trackNumber") Integer trackNumber,
                                 @RequestParam("duration") Integer duration,
                                 @RequestParam(name = "audioFileUrl", required = false, defaultValue = "") String audioFileUrl,
                                 @RequestParam("albumId") Integer albumId,
                                 RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Track t = new Track();
            t.setTrackTitle(trackTitle);
            t.setTrackNumber(trackNumber);
            t.setDuration(duration);
            // Normalize blank to null so the template shows "Audio preview unavailable"
            t.setAudioFileUrl(audioFileUrl.isBlank() ? null : audioFileUrl.trim());
            adminService.getAlbumById(albumId).ifPresent(t::setAlbum);
            adminService.saveTrack(t);
            ra.addFlashAttribute("successMsg", "Track added successfully.");
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage()); }
        return "redirect:/admin/catalog/tracks";
    }

    @GetMapping("/catalog/tracks/edit/{id}")
    public String editTrackForm(@PathVariable Integer id, HttpSession session, Model model) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        Optional<Track> track = adminService.getTrackById(id);
        if (track.isEmpty()) return "redirect:/admin/catalog/tracks";
        populateAdminModel(model, session);
        model.addAttribute("track", track.get());
        model.addAttribute("albums", adminService.getAllAlbums());
        model.addAttribute("formMode", "edit");
        return "admin/catalog/track-form";
    }

    @PostMapping("/catalog/tracks/edit/{id}")
    public String editTrackSubmit(@PathVariable Integer id, HttpSession session,
                                  @RequestParam("trackTitle") String trackTitle,
                                  @RequestParam("trackNumber") Integer trackNumber,
                                  @RequestParam("duration") Integer duration,
                                  @RequestParam(name = "audioFileUrl", required = false, defaultValue = "") String audioFileUrl,
                                  @RequestParam("albumId") Integer albumId,
                                  RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try {
            Optional<Track> opt = adminService.getTrackById(id);
            if (opt.isPresent()) {
                Track t = opt.get();
                t.setTrackTitle(trackTitle);
                t.setTrackNumber(trackNumber);
                t.setDuration(duration);
                // Normalize blank to null so the template shows "Audio preview unavailable"
                t.setAudioFileUrl(audioFileUrl.isBlank() ? null : audioFileUrl.trim());
                adminService.getAlbumById(albumId).ifPresent(t::setAlbum);
                adminService.saveTrack(t);
                ra.addFlashAttribute("successMsg", "Track updated successfully.");
            }
        } catch (Exception e) { ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage()); }
        return "redirect:/admin/catalog/tracks";
    }

    @PostMapping("/catalog/tracks/delete/{id}")
    public String deleteTrack(@PathVariable Integer id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, CATALOG_ROLES); if (r != null) return r;
        try { adminService.deleteTrack(id); ra.addFlashAttribute("successMsg", "Track deleted."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", "Cannot delete track."); }
        return "redirect:/admin/catalog/tracks";
    }

    // ─── Orders ───────────────────────────────────────────────────────────────

    @GetMapping({"/orders", "/orders/list"})
    public String orders(HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("stats", adminService.getOrderStats());
        model.addAttribute("orders", adminService.getAllOrders());
        return "admin/orders/list";
    }

    @GetMapping("/orders/{id}")
    public String orderDetail(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("order", adminService.getOrderDetail(id));
        model.addAttribute("orderItems", adminService.getOrderItems(id));
        model.addAttribute("payment", adminService.getPaymentByOrder(id));
        return "admin/orders/detail";
    }

    @PostMapping("/orders/{id}/accept")
    public String acceptOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            adminService.acceptOrder(id);
            ra.addFlashAttribute("successMsg", "Order #" + id + " accepted. Digital library access has been granted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to accept order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/decline")
    public String declineOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            adminService.declineOrder(id);
            ra.addFlashAttribute("successMsg", "Order #" + id + " declined successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to decline order: " + e.getMessage());
        }
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/orders/{id}/delete")
    public String deleteOrder(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            adminService.deleteOrder(id);
            ra.addFlashAttribute("successMsg", "Order #" + id + " deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to delete order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id, @RequestParam("status") String status, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            if ("Completed".equalsIgnoreCase(status)) {
                adminService.acceptOrder(id);
                ra.addFlashAttribute("successMsg", "Order #" + id + " accepted. Digital library access has been granted.");
            } else {
                adminService.updateOrderStatus(id, status);
                ra.addFlashAttribute("successMsg", "Order #" + id + " status updated to " + status + ".");
            }
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to update order: " + e.getMessage());
        }
        return "redirect:/admin/orders";
    }

    @GetMapping({"/payments", "/orders/payments"})
    public String payments(HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("payments", adminService.getAllPayments());
        return "admin/orders/payments";
    }

    @GetMapping({"/payments/{id}", "/orders/payments/{id}"})
    public String paymentDetail(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("payment", adminService.getPaymentDetail(id));
        return "admin/orders/payment-detail";
    }

    @PostMapping({"/payments/{id}/delete", "/orders/payments/{id}/delete"})
    public String deletePayment(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            adminService.deletePayment(id);
            ra.addFlashAttribute("successMsg", "Payment #" + id + " deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to delete payment: " + e.getMessage());
        }
        return "redirect:/admin/orders/payments";
    }

    // ─── Digital Library ──────────────────────────────────────────────────────

    @GetMapping("/library")
    public String library(HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("libraries", adminService.getAllDigitalLibraries());
        model.addAttribute("libraryItems", adminService.getAllLibraryItems());
        return "admin/library/index";
    }

    @PostMapping("/library/items/{id}/status")
    public String toggleLibraryItemStatus(@PathVariable Long id, @RequestParam("status") String status, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        try {
            adminService.toggleLibraryItemStatus(id, status);
            ra.addFlashAttribute("successMsg", "Library Item #" + id + " status set to " + status + ".");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to update status: " + e.getMessage());
        }
        return "redirect:/admin/library";
    }

    @GetMapping("/library-items")
    public String libraryItems(HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("libraryItems", adminService.getAllLibraryItems());
        return "admin/library/items";
    }

    @GetMapping("/downloads")
    public String downloads(HttpSession session, Model model) {
        String r = checkAccess(session, ORDER_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("downloads", adminService.getAllDownloads());
        return "admin/library/downloads";
    }

    // ─── Support: Reviews ────────────────────────────────────────────────────

    @GetMapping({"/reviews", "/support/reviews"})
    public String reviews(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("reviews", adminService.getAllReviews());
        return "admin/support/reviews";
    }

    @GetMapping({"/reviews/{id}", "/support/reviews/{id}"})
    public String reviewDetail(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        Object[] review = adminService.getReviewDetail(id);
        if (review == null) {
            return "redirect:/admin/support/reviews";
        }
        model.addAttribute("review", review);
        return "admin/support/review-detail";
    }

    @PostMapping({"/reviews/{id}/delete", "/support/reviews/{id}/delete"})
    public String deleteReview(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.deleteReview(id);
            ra.addFlashAttribute("successMsg", "Review deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error deleting review: " + e.getMessage());
        }
        return "redirect:/admin/support/reviews";
    }

    // ─── Support: Complaints ─────────────────────────────────────────────────

    @GetMapping({"/complaints", "/support/complaints"})
    public String complaints(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("complaints", adminService.getAllComplaints());
        return "admin/support/complaints";
    }

    @GetMapping({"/complaints/{id}", "/support/complaints/{id}"})
    public String complaintDetail(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        Object[] complaint = adminService.getComplaintDetail(id);
        if (complaint == null) {
            return "redirect:/admin/support/complaints";
        }
        model.addAttribute("complaint", complaint);
        return "admin/support/complaint-detail";
    }

    @PostMapping({"/complaints/{id}/status", "/support/complaints/{id}/status"})
    public String updateComplaintStatus(@PathVariable Long id,
                                        @RequestParam("status") String status,
                                        @RequestParam(value = "response", required = false) String response,
                                        @RequestParam(value = "returnUrl", required = false) String returnUrl,
                                        HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.updateComplaint(id, status, response);
            ra.addFlashAttribute("successMsg", "Complaint updated successfully.");
        }
        catch (Exception e) { ra.addFlashAttribute("errorMsg", "Error: " + e.getMessage()); }
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/admin/support/complaints";
    }

    @PostMapping({"/complaints/{id}/delete", "/support/complaints/{id}/delete"})
    public String deleteComplaint(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.deleteComplaint(id);
            ra.addFlashAttribute("successMsg", "Complaint #" + id + " deleted successfully.");
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("errorMsg", e.getMessage());
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Error deleting complaint: " + e.getMessage());
        }
        return "redirect:/admin/support/complaints";
    }

    // ─── Support: FAQ ────────────────────────────────────────────────────────

    @GetMapping({"/faqs", "/faq", "/support/faq", "/support/faqs"})
    public String faq(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("faqs", adminService.getAllFaqs());
        return "admin/support/faq";
    }

    @GetMapping({"/support/faq/add", "/faqs/add"})
    public String addFaqForm(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session); model.addAttribute("formMode","add");
        return "admin/support/faq-form";
    }

    @PostMapping({"/support/faq/add", "/faqs/add"})
    public String addFaqSubmit(HttpSession session, @RequestParam("question") String question,
                               @RequestParam("answer") String answer,
                               @RequestParam(value="faqStatus",defaultValue="Published") String status, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try { adminService.addFaq(question, answer, status); ra.addFlashAttribute("successMsg","FAQ added successfully."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/support/faq";
    }

    @GetMapping({"/support/faq/edit/{id}", "/faqs/edit/{id}"})
    public String editFaqForm(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        Object[] faq = adminService.getFaqById(id);
        if (faq == null) {
            return "redirect:/admin/support/faq";
        }
        populateAdminModel(model, session);
        model.addAttribute("faq", faq);
        model.addAttribute("formMode", "edit");
        return "admin/support/faq-form";
    }

    @PostMapping({"/support/faq/edit/{id}", "/faqs/edit/{id}"})
    public String editFaqSubmit(@PathVariable Long id,
                                @RequestParam("question") String question,
                                @RequestParam("answer") String answer,
                                @RequestParam(value="faqStatus", defaultValue="Published") String status,
                                HttpSession session,
                                RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.updateFaq(id, question, answer, status);
            ra.addFlashAttribute("successMsg", "FAQ updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to update FAQ: " + e.getMessage());
        }
        return "redirect:/admin/support/faq";
    }

    @PostMapping({"/support/faq/delete/{id}", "/faqs/delete/{id}"})
    public String deleteFaq(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try { adminService.deleteFaq(id); ra.addFlashAttribute("successMsg","FAQ deleted successfully."); }
        catch (Exception e) { ra.addFlashAttribute("errorMsg","Error: "+e.getMessage()); }
        return "redirect:/admin/support/faq";
    }

    // ─── Support: Promotions ─────────────────────────────────────────────────

    @GetMapping({"/promotions", "/support/promotions"})
    public String promotions(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        List<Object[]> promos = adminService.getAllPromotions();
        long activeCount = promos.stream().filter(p -> p[7] != null && "Active".equals(p[7].toString())).count();
        long scheduledCount = promos.stream().filter(p -> p[7] != null && "Scheduled".equals(p[7].toString())).count();
        long expiredCount = promos.stream().filter(p -> p[7] != null && ("Expired".equals(p[7].toString()) || "Cancelled".equals(p[7].toString()))).count();
        model.addAttribute("promotions", promos);
        model.addAttribute("activeCount", activeCount);
        model.addAttribute("scheduledCount", scheduledCount);
        model.addAttribute("expiredCount", expiredCount);
        model.addAttribute("totalCount", promos.size());
        return "admin/support/promotions";
    }

    @GetMapping("/promotions/add")
    public String addPromotionForm(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("formMode", "add");
        return "admin/support/promotion-form";
    }

    @PostMapping("/promotions/add")
    public String addPromotionSubmit(HttpSession session,
                                     @RequestParam("promotionName") String name,
                                     @RequestParam(value = "description", required = false) String description,
                                     @RequestParam("discountType") String discountType,
                                     @RequestParam("discountValue") BigDecimal discountValue,
                                     @RequestParam("startDate") String startDate,
                                     @RequestParam("endDate") String endDate,
                                     @RequestParam("promotionStatus") String status,
                                     RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.createPromotion(name, description, discountType, discountValue, startDate, endDate, status);
            ra.addFlashAttribute("successMsg", "Promotion \"" + name + "\" created successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to create promotion: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/edit/{id}")
    public String editPromotionForm(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        Object[] promo = adminService.getPromotionById(id);
        if (promo == null) {
            return "redirect:/admin/promotions";
        }
        populateAdminModel(model, session);
        model.addAttribute("promo", promo);
        model.addAttribute("formMode", "edit");
        return "admin/support/promotion-form";
    }

    @PostMapping("/promotions/edit/{id}")
    public String editPromotionSubmit(@PathVariable Long id, HttpSession session,
                                      @RequestParam("promotionName") String name,
                                      @RequestParam(value = "description", required = false) String description,
                                      @RequestParam("discountType") String discountType,
                                      @RequestParam("discountValue") BigDecimal discountValue,
                                      @RequestParam("startDate") String startDate,
                                      @RequestParam("endDate") String endDate,
                                      @RequestParam("promotionStatus") String status,
                                      RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.updatePromotion(id, name, description, discountType, discountValue, startDate, endDate, status);
            ra.addFlashAttribute("successMsg", "Promotion updated successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to update promotion: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/deactivate/{id}")
    public String deactivatePromotion(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.deactivatePromotion(id);
            ra.addFlashAttribute("successMsg", "Promotion #" + id + " deactivated (Cancelled).");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to deactivate: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @PostMapping("/promotions/delete/{id}")
    public String deletePromotion(@PathVariable Long id, HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.deletePromotion(id);
            ra.addFlashAttribute("successMsg", "Promotion #" + id + " deleted.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Cannot delete promotion: " + e.getMessage());
        }
        return "redirect:/admin/promotions";
    }

    @GetMapping("/promotions/{id}/albums")
    public String managePromotionAlbums(@PathVariable Long id, HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        Object[] promo = adminService.getPromotionById(id);
        if (promo == null) {
            return "redirect:/admin/promotions";
        }
        populateAdminModel(model, session);
        model.addAttribute("promo", promo);
        model.addAttribute("assignedAlbums", adminService.getAlbumsForPromotion(id));
        model.addAttribute("availableAlbums", adminService.getAlbumsNotInPromotion(id));
        return "admin/support/promotion-albums";
    }

    @PostMapping("/promotions/{id}/albums/assign")
    public String assignAlbumToPromotion(@PathVariable Long id,
                                          @RequestParam("albumId") Integer albumId,
                                          HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.assignAlbumToPromotion(albumId, id);
            ra.addFlashAttribute("successMsg", "Album assigned to promotion.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to assign album: " + e.getMessage());
        }
        return "redirect:/admin/promotions/" + id + "/albums";
    }

    @PostMapping("/promotions/{id}/albums/remove/{albumId}")
    public String removeAlbumFromPromotion(@PathVariable Long id,
                                            @PathVariable Integer albumId,
                                            HttpSession session, RedirectAttributes ra) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        try {
            adminService.removeAlbumFromPromotion(albumId, id);
            ra.addFlashAttribute("successMsg", "Album removed from promotion.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to remove album: " + e.getMessage());
        }
        return "redirect:/admin/promotions/" + id + "/albums";
    }

    @GetMapping({"/album-promotions", "/support/album-promotions"})
    public String albumPromotions(HttpSession session, Model model) {
        String r = checkAccess(session, SUPPORT_ROLES); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("albumPromotions", adminService.getAllAlbumPromotions());
        return "admin/support/album-promotions";
    }

    // ─── Super Admin: Users & Administrators ─────────────────────────────────

    @GetMapping("/users")
    public String users(HttpSession session, Model model) {
        String r = checkAccess(session, SUPER_ONLY); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("users", adminService.getAllUsers());
        return "admin/users/index";
    }

    @GetMapping("/administrators")
    public String administrators(HttpSession session, Model model) {
        String r = checkAccess(session, SUPER_ONLY); if (r != null) return r;
        populateAdminModel(model, session);
        model.addAttribute("admins", adminService.getAllAdmins());
        return "admin/administrators/index";
    }
}
