package com.melodymart.albumcatalog.controller;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.service.AlbumService;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.common.service.AdminService;
import com.melodymart.complaintreview.service.ReviewService;
import com.melodymart.libraryplaylist.service.PlaylistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;

@Controller
public class AlbumController {

    private final AlbumService albumService;
    private final AdminService adminService;
    private final PlaylistService playlistService;
    private final ReviewService reviewService;

    @Autowired
    public AlbumController(AlbumService albumService,
                           AdminService adminService,
                           PlaylistService playlistService,
                           ReviewService reviewService) {
        this.albumService = albumService;
        this.adminService = adminService;
        this.playlistService = playlistService;
        this.reviewService = reviewService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/albums";
    }

    @GetMapping("/albums")
    public String listAlbums(@RequestParam(name = "search", required = false) String search,
                             @RequestParam(name = "genreId", required = false) Integer genreId,
                             @RequestParam(name = "artistId", required = false) Integer artistId,
                             Model model) {
        List<Album> albums = albumService.searchAndFilterAlbums(search, genreId, artistId);
        List<Genre> genres = albumService.getAllGenres();
        List<Artist> artists = albumService.getAllArtists();

        model.addAttribute("albums", albums);
        model.addAttribute("genres", genres);
        model.addAttribute("artists", artists);
        model.addAttribute("currentSearch", search);
        model.addAttribute("currentGenreId", genreId);
        model.addAttribute("currentArtistId", artistId);
        model.addAttribute("defaultPageSize", albumService.getDefaultPageSize());

        return "album-catalog/albums";
    }

    @GetMapping("/albums/{id}")
    public String albumDetails(@PathVariable("id") Integer id, HttpSession session, Model model) {
        Album album = albumService.getAlbumWithDetails(id);
        if (album == null) {
            return "redirect:/albums";
        }
        model.addAttribute("album", album);
        // Pass active promotions for this album
        model.addAttribute("activePromotions", adminService.getActivePromotionsForAlbum(id));

        // Pass album reviews
        List<Map<String, Object>> reviews = reviewService.getReviewsForAlbum(id);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());

        double avgRating = 0.0;
        if (!reviews.isEmpty()) {
            double sum = 0.0;
            for (Map<String, Object> r : reviews) {
                Number ratingNum = (Number) r.get("rating");
                if (ratingNum != null) sum += ratingNum.doubleValue();
            }
            avgRating = Math.round((sum / reviews.size()) * 10.0) / 10.0;
        }
        model.addAttribute("avgRating", avgRating);

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId != null && !"ADMIN".equals(session.getAttribute("userRole"))) {
            model.addAttribute("userPlaylists", playlistService.getPlaylistsForListener(userId));
            boolean hasRev = reviewService.hasListenerReviewedAlbum(userId, id);
            model.addAttribute("hasReviewed", hasRev);
            model.addAttribute("canReview", reviewService.canListenerReviewAlbum(userId, id));
            if (hasRev) {
                Map<String, Object> myRev = reviews.stream()
                        .filter(r -> userId.equals(r.get("listenerId")))
                        .findFirst()
                        .orElse(null);
                model.addAttribute("userReview", myRev);
            }
        } else {
            model.addAttribute("hasReviewed", false);
            model.addAttribute("canReview", false);
        }

        return "album-catalog/album-detail";
    }

    // ─── Dedicated Write-Review Page (from My Library) ───────────────────────

    /**
     * GET /my-library/reviews/write/{albumId}
     * Displays a clean, dedicated page for writing a review.
     * Security: listener must be logged in, must have purchased the album,
     * and must not have already reviewed it.
     */
    @GetMapping("/my-library/reviews/write/{albumId}")
    public String writeReviewPage(@PathVariable("albumId") Integer albumId,
                                  HttpSession session,
                                  Model model,
                                  RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        if ("ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/admin/dashboard";

        Album album = albumService.getAlbumWithDetails(albumId);
        if (album == null) {
            ra.addFlashAttribute("errorMsg", "Album not found.");
            return "redirect:/my-library";
        }

        // Ownership / eligibility check: must have purchased and not already reviewed
        if (!reviewService.canListenerReviewAlbum(userId, albumId)) {
            // Distinguish between "already reviewed" and "not purchased"
            if (reviewService.hasListenerReviewedAlbum(userId, albumId)) {
                ra.addFlashAttribute("errorMsg", "You have already submitted a review for this album.");
            } else {
                ra.addFlashAttribute("errorMsg", "You can only review albums you have purchased.");
            }
            return "redirect:/my-library";
        }

        model.addAttribute("album", album);
        return "digital-library-playlist/write-review";
    }

    // ─── Submit Review ────────────────────────────────────────────────────────

    @PostMapping("/albums/{id}/reviews")
    public String submitReview(@PathVariable("id") Integer id,
                               @RequestParam(name = "rating", defaultValue = "5") int rating,
                               @RequestParam(name = "comment", defaultValue = "") String comment,
                               @RequestParam(name = "returnUrl", required = false) String returnUrl,
                               HttpSession session,
                               RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            ra.addFlashAttribute("errorMsg", "Administrators cannot submit listener reviews.");
            return "redirect:/albums/" + id;
        }

        if (rating < 1 || rating > 5) {
            ra.addFlashAttribute("errorMsg", "Please select a rating between 1 and 5 stars.");
            String target = (returnUrl != null && !returnUrl.isBlank()) ? returnUrl : "/albums/" + id + "#reviews";
            return "redirect:" + target;
        }

        try {
            reviewService.createReview(userId, id, rating, comment);
            ra.addFlashAttribute("successMsg", "Review submitted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to submit review: " + e.getMessage());
        }

        // If the form supplied a returnUrl (e.g. /my-library), honour it
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/albums/" + id + "#reviews";
    }

    @PostMapping("/albums/{albumId}/reviews/{reviewId}/edit")
    public String editReview(@PathVariable("albumId") Integer albumId,
                             @PathVariable("reviewId") Integer reviewId,
                             @RequestParam(name = "rating", defaultValue = "5") int rating,
                             @RequestParam(name = "comment", defaultValue = "") String comment,
                             @RequestParam(name = "returnUrl", required = false) String returnUrl,
                             HttpSession session,
                             RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            ra.addFlashAttribute("errorMsg", "Administrators cannot edit listener reviews.");
            return "redirect:/albums/" + albumId;
        }

        try {
            reviewService.updateReview(userId, reviewId, rating, comment);
            ra.addFlashAttribute("successMsg", "Your review has been updated!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to update review: " + e.getMessage());
        }
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/albums/" + albumId + "#reviews";
    }

    @PostMapping("/albums/{albumId}/reviews/{reviewId}/delete")
    public String deleteReview(@PathVariable("albumId") Integer albumId,
                               @PathVariable("reviewId") Integer reviewId,
                               @RequestParam(name = "returnUrl", required = false) String returnUrl,
                               HttpSession session,
                               RedirectAttributes ra) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        
        try {
            reviewService.deleteReview(userId, reviewId);
            ra.addFlashAttribute("successMsg", "Review deleted successfully.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMsg", "Failed to delete review: " + e.getMessage());
        }
        if (returnUrl != null && !returnUrl.isBlank()) {
            return "redirect:" + returnUrl;
        }
        return "redirect:/albums/" + albumId + "#reviews";
    }

    // ─── Listener's Own Submitted Reviews ─────────────────────────────────────

    @GetMapping("/my-reviews")
    public String myReviews(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) {
            return "redirect:/login";
        }
        if ("ADMIN".equals(session.getAttribute("userRole"))) {
            return "redirect:/admin/dashboard";
        }

        List<Map<String, Object>> reviews = reviewService.getReviewsForListener(userId);
        model.addAttribute("reviews", reviews);
        return "common/my-reviews";
    }

    // REST APIs for programmatic verification / testing
    @GetMapping("/api/albums")
    @ResponseBody
    public List<Album> getAlbumsApi(@RequestParam(name = "search", required = false) String search,
                                    @RequestParam(name = "genreId", required = false) Integer genreId,
                                    @RequestParam(name = "artistId", required = false) Integer artistId) {
        return albumService.searchAndFilterAlbums(search, genreId, artistId);
    }

    @GetMapping("/api/albums/{id}")
    @ResponseBody
    public ResponseEntity<Album> getAlbumApi(@PathVariable("id") Integer id) {
        Album album = albumService.getAlbumWithDetails(id);
        if (album == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(album);
    }

    @GetMapping("/api/settings")
    @ResponseBody
    public com.melodymart.config.MelodyMartApplicationSettings getApplicationSettingsApi() {
        return com.melodymart.config.MelodyMartApplicationSettings.getInstance();
    }
}
