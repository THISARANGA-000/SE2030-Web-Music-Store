package com.melodymart.libraryplaylist.controller;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.artistgenretrack.model.Track;
import com.melodymart.artistgenretrack.repository.TrackRepository;
import com.melodymart.libraryplaylist.model.Playlist;
import com.melodymart.libraryplaylist.model.PlaylistItem;
import com.melodymart.libraryplaylist.service.PlaylistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Listener-side playlist management controller.
 *
 * SECURITY: Every route checks session authentication.
 *           Playlist ownership is verified server-side via listenerId from session.
 *           A listener can never access, edit, or delete another listener's playlist.
 */
@Controller
@RequestMapping("/playlists")
public class PlaylistController {

    private final PlaylistService playlistService;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @Autowired
    public PlaylistController(PlaylistService playlistService,
                              AlbumRepository albumRepository,
                              TrackRepository trackRepository) {
        this.playlistService = playlistService;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }

    // ─── Auth guard ────────────────────────────────────────────────────────────

    private String checkListenerAccess(HttpSession session) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";
        if ("ADMIN".equals(session.getAttribute("userRole"))) return "redirect:/admin/dashboard";
        return null;
    }

    // ─── List playlists ────────────────────────────────────────────────────────

    @GetMapping({"", "/"})
    public String myPlaylists(HttpSession session, Model model,
                              @RequestParam(name = "msg", required = false) String msg,
                              @RequestParam(name = "error", required = false) String error) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        List<Playlist> playlists = playlistService.getPlaylistsForListener(userId);
        model.addAttribute("playlists", playlists);
        if (msg != null) model.addAttribute("successMsg", msg);
        if (error != null) model.addAttribute("errorMsg", error);
        return "digital-library-playlist/playlists";
    }

    // ─── Create form ───────────────────────────────────────────────────────────

    @GetMapping("/new")
    public String createPlaylistForm(HttpSession session, Model model) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;
        model.addAttribute("playlistName", "");
        return "digital-library-playlist/playlist-form";
    }

    // ─── Create POST ───────────────────────────────────────────────────────────

    @PostMapping({"", "/"})
    public String createPlaylist(@RequestParam(name = "playlistName", defaultValue = "") String playlistName,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");

        if (playlistName.isBlank()) {
            model.addAttribute("errorMsg", "Playlist name is required.");
            model.addAttribute("playlistName", playlistName);
            return "digital-library-playlist/playlist-form";
        }

        try {
            Playlist p = playlistService.createPlaylist(userId, playlistName);
            ra.addFlashAttribute("successMsg", "Playlist created successfully.");
            return "redirect:/playlists/" + p.getPlaylistId();
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMsg", e.getMessage());
            model.addAttribute("playlistName", playlistName);
            return "digital-library-playlist/playlist-form";
        }
    }

    // ─── View playlist ─────────────────────────────────────────────────────────

    @GetMapping("/{playlistId}")
    public String viewPlaylist(@PathVariable Integer playlistId,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra,
                               @RequestParam(name = "msg", required = false) String msg,
                               @RequestParam(name = "error", required = false) String error) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        Optional<Playlist> playlistOpt = playlistService.getPlaylistForListener(playlistId, userId);

        if (playlistOpt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
            return "redirect:/playlists";
        }

        Playlist playlist = playlistOpt.get();
        List<PlaylistItem> items = playlistService.getItemsForPlaylist(playlistId, userId);
        List<Album> purchasedAlbums = playlistService.getPurchasedAlbumsForListener(userId);
        List<Track> purchasedTracks = playlistService.getPurchasedTracksForListener(userId);

        model.addAttribute("playlist", playlist);
        model.addAttribute("items", items);
        model.addAttribute("allAlbums", purchasedAlbums);
        model.addAttribute("allTracks", purchasedTracks);
        if (msg != null) model.addAttribute("successMsg", msg);
        if (error != null) model.addAttribute("errorMsg", error);
        return "digital-library-playlist/playlist-detail";
    }

    // ─── Edit/Rename form ──────────────────────────────────────────────────────

    @GetMapping("/{playlistId}/edit")
    public String editPlaylistForm(@PathVariable Integer playlistId,
                                   HttpSession session,
                                   Model model,
                                   RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        Optional<Playlist> playlistOpt = playlistService.getPlaylistForListener(playlistId, userId);
        if (playlistOpt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
            return "redirect:/playlists";
        }

        model.addAttribute("playlist", playlistOpt.get());
        return "digital-library-playlist/playlist-edit";
    }

    // ─── Edit/Rename POST ──────────────────────────────────────────────────────

    @PostMapping("/{playlistId}/edit")
    public String editPlaylist(@PathVariable Integer playlistId,
                               @RequestParam(name = "playlistName", defaultValue = "") String playlistName,
                               HttpSession session,
                               Model model,
                               RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");

        if (playlistName.isBlank()) {
            Optional<Playlist> playlistOpt = playlistService.getPlaylistForListener(playlistId, userId);
            if (playlistOpt.isEmpty()) {
                ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
                return "redirect:/playlists";
            }
            model.addAttribute("errorMsg", "Playlist name cannot be empty.");
            model.addAttribute("playlist", playlistOpt.get());
            return "digital-library-playlist/playlist-edit";
        }

        boolean updated = playlistService.renamePlaylist(playlistId, userId, playlistName);
        if (!updated) {
            ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
            return "redirect:/playlists";
        }
        ra.addFlashAttribute("successMsg", "Playlist renamed successfully.");
        return "redirect:/playlists/" + playlistId;
    }

    // ─── Delete ────────────────────────────────────────────────────────────────

    @PostMapping("/{playlistId}/delete")
    public String deletePlaylist(@PathVariable Integer playlistId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        boolean deleted = playlistService.deletePlaylist(playlistId, userId);
        if (deleted) {
            ra.addFlashAttribute("successMsg", "Playlist deleted.");
        } else {
            ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
        }
        return "redirect:/playlists";
    }

    // ─── Add Album ─────────────────────────────────────────────────────────────

    @PostMapping("/{playlistId}/albums/{albumId}")
    public String addAlbum(@PathVariable Integer playlistId,
                           @PathVariable Integer albumId,
                           HttpSession session,
                           RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        String result = playlistService.addAlbumToPlaylist(playlistId, userId, albumId);

        switch (result) {
            case "ok"            -> ra.addFlashAttribute("successMsg", "Album added to playlist.");
            case "duplicate"     -> ra.addFlashAttribute("errorMsg", "This album is already in the playlist.");
            case "not_purchased" -> ra.addFlashAttribute("errorMsg", "You can only add albums that you have purchased.");
            case "album_not_found" -> ra.addFlashAttribute("errorMsg", "Album not found.");
            default              -> {
                ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
                return "redirect:/playlists";
            }
        }
        return "redirect:/playlists/" + playlistId;
    }

    // ─── Add Track ─────────────────────────────────────────────────────────────

    @PostMapping("/{playlistId}/tracks/{trackId}")
    public String addTrack(@PathVariable Integer playlistId,
                           @PathVariable Integer trackId,
                           HttpSession session,
                           RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        String result = playlistService.addTrackToPlaylist(playlistId, userId, trackId);

        switch (result) {
            case "ok"            -> ra.addFlashAttribute("successMsg", "Track added to playlist.");
            case "duplicate"     -> ra.addFlashAttribute("errorMsg", "This track is already in the playlist.");
            case "not_purchased" -> ra.addFlashAttribute("errorMsg", "You can only add tracks from albums you have purchased.");
            case "track_not_found" -> ra.addFlashAttribute("errorMsg", "Track not found.");
            default              -> {
                ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
                return "redirect:/playlists";
            }
        }
        return "redirect:/playlists/" + playlistId;
    }

    // ─── Remove Item ───────────────────────────────────────────────────────────

    @PostMapping("/{playlistId}/items/{itemId}/delete")
    public String removeItem(@PathVariable Integer playlistId,
                             @PathVariable Integer itemId,
                             HttpSession session,
                             RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        Optional<Playlist> playlistOpt = playlistService.getPlaylistForListener(playlistId, userId);
        if (playlistOpt.isEmpty()) {
            ra.addFlashAttribute("errorMsg", "Playlist not found or access denied.");
            return "redirect:/playlists";
        }
        boolean removed = playlistService.removePlaylistItem(playlistId, itemId, userId);
        if (!removed) {
            ra.addFlashAttribute("errorMsg", "Item not found in playlist.");
        } else {
            ra.addFlashAttribute("successMsg", "Item removed from playlist.");
        }
        return "redirect:/playlists/" + playlistId;
    }

    // ─── Add to playlist from album page ─────────────────────────────────────
    // POST /albums/{albumId}/add-to-playlist with form param playlistId
    // This is handled here to keep playlist logic together.

    @PostMapping("/add-album-from-page")
    public String addAlbumFromPage(@RequestParam("albumId") Integer albumId,
                                   @RequestParam("playlistId") Integer playlistId,
                                   @RequestParam(name = "returnUrl", defaultValue = "/albums") String returnUrl,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        String result = playlistService.addAlbumToPlaylist(playlistId, userId, albumId);

        switch (result) {
            case "ok"            -> ra.addFlashAttribute("successMsg", "Album added to your playlist!");
            case "duplicate"     -> ra.addFlashAttribute("errorMsg", "This album is already in that playlist.");
            case "not_purchased" -> ra.addFlashAttribute("errorMsg", "You can only add albums that you have purchased.");
            default              -> ra.addFlashAttribute("errorMsg", "Could not add album to playlist.");
        }
        // Return to album detail page
        return "redirect:/albums/" + albumId;
    }

    // ─── Add track from album page ───────────────────────────────────────────

    @PostMapping("/add-track-from-page")
    public String addTrackFromPage(@RequestParam("trackId") Integer trackId,
                                   @RequestParam("playlistId") Integer playlistId,
                                   @RequestParam(name = "albumId", required = false) Integer albumId,
                                   HttpSession session,
                                   RedirectAttributes ra) {
        String redirect = checkListenerAccess(session);
        if (redirect != null) return redirect;

        Integer userId = (Integer) session.getAttribute("userId");
        String result = playlistService.addTrackToPlaylist(playlistId, userId, trackId);

        switch (result) {
            case "ok"            -> ra.addFlashAttribute("successMsg", "Track added to your playlist!");
            case "duplicate"     -> ra.addFlashAttribute("errorMsg", "This track is already in that playlist.");
            case "not_purchased" -> ra.addFlashAttribute("errorMsg", "You can only add tracks from albums you have purchased.");
            default              -> ra.addFlashAttribute("errorMsg", "Could not add track to playlist.");
        }
        if (albumId != null) {
            return "redirect:/albums/" + albumId;
        }
        return "redirect:/playlists/" + playlistId;
    }

    // ─── Direct Form POSTs (supporting both /{playlistId}/albums/{id} and param-based forms) ──

    @PostMapping("/{playlistId}/add-album")
    public String addAlbumDirect(@PathVariable Integer playlistId,
                                 @RequestParam("albumId") Integer albumId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        return addAlbum(playlistId, albumId, session, ra);
    }

    @PostMapping("/{playlistId}/add-track")
    public String addTrackDirect(@PathVariable Integer playlistId,
                                 @RequestParam("trackId") Integer trackId,
                                 HttpSession session,
                                 RedirectAttributes ra) {
        return addTrack(playlistId, trackId, session, ra);
    }
}
