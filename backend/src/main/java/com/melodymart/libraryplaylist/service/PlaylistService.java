package com.melodymart.libraryplaylist.service;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.artistgenretrack.model.Track;
import com.melodymart.artistgenretrack.repository.TrackRepository;
import com.melodymart.libraryplaylist.model.Playlist;
import com.melodymart.libraryplaylist.model.PlaylistItem;
import com.melodymart.libraryplaylist.repository.PlaylistItemRepository;
import com.melodymart.libraryplaylist.repository.PlaylistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Business logic for listener-owned playlists.
 *
 * OWNERSHIP SECURITY:
 * Every operation that modifies or reads a specific playlist first verifies
 * that the playlist belongs to the requesting listener (via listenerId from session).
 * We never trust a bare playlistId from the URL without also checking listenerId.
 */
@Service
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistItemRepository playlistItemRepository;
    private final AlbumRepository albumRepository;
    private final TrackRepository trackRepository;

    @PersistenceContext
    private EntityManager em;

    @Autowired
    public PlaylistService(PlaylistRepository playlistRepository,
                           PlaylistItemRepository playlistItemRepository,
                           AlbumRepository albumRepository,
                           TrackRepository trackRepository) {
        this.playlistRepository = playlistRepository;
        this.playlistItemRepository = playlistItemRepository;
        this.albumRepository = albumRepository;
        this.trackRepository = trackRepository;
    }

    // ─── Create ──────────────────────────────────────────────────────────────

    @Transactional
    public Playlist createPlaylist(Integer listenerId, String name) {
        if (listenerId == null) throw new IllegalArgumentException("Must be logged in to create a playlist.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Playlist name is required.");
        String trimmed = name.trim();
        if (trimmed.length() > 100) trimmed = trimmed.substring(0, 100);
        Playlist p = new Playlist(listenerId, trimmed);
        return playlistRepository.save(p);
    }

    // ─── List ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<Playlist> getPlaylistsForListener(Integer listenerId) {
        if (listenerId == null) return List.of();
        return playlistRepository.findByListenerIdOrderByCreatedDateDesc(listenerId);
    }

    // ─── View one (ownership enforced) ────────────────────────────────────────

    @Transactional(readOnly = true)
    public Optional<Playlist> getPlaylistForListener(Integer playlistId, Integer listenerId) {
        return playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
    }

    // ─── Rename ───────────────────────────────────────────────────────────────

    @Transactional
    public boolean renamePlaylist(Integer playlistId, Integer listenerId, String newName) {
        if (newName == null || newName.isBlank()) throw new IllegalArgumentException("Playlist name cannot be empty.");
        Optional<Playlist> opt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (opt.isEmpty()) return false; // not found or not owned
        Playlist p = opt.get();
        p.setPlaylistName(newName.trim().length() > 100 ? newName.trim().substring(0, 100) : newName.trim());
        p.setLastUpdatedDate(LocalDateTime.now());
        playlistRepository.save(p);
        return true;
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public boolean deletePlaylist(Integer playlistId, Integer listenerId) {
        Optional<Playlist> opt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (opt.isEmpty()) return false; // not found or not owned
        playlistRepository.delete(opt.get()); // cascade deletes items
        return true;
    }

    // ─── Purchased Content for Playlist Selection ────────────────────────────

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Album> getPurchasedAlbumsForListener(Integer listenerId) {
        if (listenerId == null) return List.of();
        List<Number> albumIds = em.createNativeQuery(
            "SELECT DISTINCT li.AlbumID FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .getResultList();

        if (albumIds.isEmpty()) return List.of();
        List<Integer> ids = albumIds.stream().map(Number::intValue).toList();
        return albumRepository.findAllById(ids);
    }

    @SuppressWarnings("unchecked")
    @Transactional(readOnly = true)
    public List<Track> getPurchasedTracksForListener(Integer listenerId) {
        if (listenerId == null) return List.of();
        List<Number> albumIds = em.createNativeQuery(
            "SELECT DISTINCT li.AlbumID FROM dbo.[LIBRARY_ITEMS] li " +
            "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
            "WHERE dl.ListenerID = ? AND li.AccessStatus = 'Active'")
          .setParameter(1, listenerId)
          .getResultList();

        if (albumIds.isEmpty()) return List.of();
        List<Integer> ids = albumIds.stream().map(Number::intValue).toList();
        return trackRepository.findTracksByAlbumIds(ids);
    }

    // ─── Add Album ────────────────────────────────────────────────────────────

    /**
     * @return "ok" on success, "duplicate" if already added, "not_purchased" if unpurchased, "playlist_not_found" / "album_not_found"
     */
    @Transactional
    public String addAlbumToPlaylist(Integer playlistId, Integer listenerId, Integer albumId) {
        if (playlistId == null || listenerId == null || albumId == null) return "not_found";

        // 1. Verify playlist belongs to this listener
        Optional<Playlist> playlistOpt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (playlistOpt.isEmpty()) return "playlist_not_found";

        // 2. Verify album exists
        Optional<Album> albumOpt = albumRepository.findById(albumId);
        if (albumOpt.isEmpty()) return "album_not_found";

        // 3. Security: Check active library ownership
        List<?> libraryCheck = em.createNativeQuery(
                "SELECT 1 FROM dbo.[LIBRARY_ITEMS] li " +
                "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
                "WHERE dl.ListenerID = ? AND li.AlbumID = ? AND li.AccessStatus = 'Active'")
            .setParameter(1, listenerId)
            .setParameter(2, albumId)
            .getResultList();
            
        if (libraryCheck.isEmpty()) {
            return "not_purchased";
        }

        if (playlistItemRepository.existsByPlaylist_PlaylistIdAndAlbum_AlbumId(playlistId, albumId)) {
            return "duplicate";
        }

        Playlist playlist = playlistOpt.get();
        PlaylistItem item = new PlaylistItem(playlist, albumOpt.get());
        playlistItemRepository.save(item);

        // Update LastUpdatedDate
        playlist.setLastUpdatedDate(LocalDateTime.now());
        playlistRepository.save(playlist);

        return "ok";
    }

    // ─── Add Track ────────────────────────────────────────────────────────────

    /**
     * @return "ok" on success, "duplicate" if already added, "not_purchased" if unpurchased, "playlist_not_found" / "track_not_found"
     */
    @Transactional
    public String addTrackToPlaylist(Integer playlistId, Integer listenerId, Integer trackId) {
        if (playlistId == null || listenerId == null || trackId == null) return "not_found";

        // 1. Verify playlist belongs to this listener
        Optional<Playlist> playlistOpt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (playlistOpt.isEmpty()) return "playlist_not_found";

        // 2. Verify track exists
        Optional<Track> trackOpt = trackRepository.findById(trackId);
        if (trackOpt.isEmpty()) return "track_not_found";
        
        Integer albumId = trackOpt.get().getAlbum().getAlbumId();
        
        // 3. Security: Check active library ownership of the album
        List<?> libraryCheck = em.createNativeQuery(
                "SELECT 1 FROM dbo.[LIBRARY_ITEMS] li " +
                "JOIN dbo.[DIGITAL_LIBRARY] dl ON li.LibraryID = dl.LibraryID " +
                "WHERE dl.ListenerID = ? AND li.AlbumID = ? AND li.AccessStatus = 'Active'")
            .setParameter(1, listenerId)
            .setParameter(2, albumId)
            .getResultList();
            
        if (libraryCheck.isEmpty()) {
            return "not_purchased";
        }

        if (playlistItemRepository.existsByPlaylist_PlaylistIdAndTrack_TrackId(playlistId, trackId)) {
            return "duplicate";
        }

        Playlist playlist = playlistOpt.get();
        PlaylistItem item = new PlaylistItem(playlist, trackOpt.get());
        playlistItemRepository.save(item);

        playlist.setLastUpdatedDate(LocalDateTime.now());
        playlistRepository.save(playlist);

        return "ok";
    }

    // ─── Remove Item ──────────────────────────────────────────────────────────

    /**
     * Remove a playlist item, verifying it belongs to the given listener's playlist.
     * @return true if removed, false if not found/not owned
     */
    @Transactional
    public boolean removePlaylistItem(Integer playlistId, Integer itemId, Integer listenerId) {
        // First confirm the playlist belongs to this listener
        Optional<Playlist> playlistOpt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (playlistOpt.isEmpty()) return false;

        // Then confirm the item belongs to this playlist
        Optional<PlaylistItem> itemOpt = playlistItemRepository.findByPlaylistItemIdAndPlaylist_PlaylistId(itemId, playlistId);
        if (itemOpt.isEmpty()) return false;

        playlistItemRepository.delete(itemOpt.get());

        Playlist playlist = playlistOpt.get();
        playlist.setLastUpdatedDate(LocalDateTime.now());
        playlistRepository.save(playlist);

        return true;
    }

    // ─── Get items for a playlist (ownership enforced) ────────────────────────

    @Transactional(readOnly = true)
    public List<PlaylistItem> getItemsForPlaylist(Integer playlistId, Integer listenerId) {
        // Verify ownership first
        Optional<Playlist> playlistOpt = playlistRepository.findByPlaylistIdAndListenerId(playlistId, listenerId);
        if (playlistOpt.isEmpty()) return List.of();
        return playlistItemRepository.findByPlaylist_PlaylistIdOrderByAddedDateAsc(playlistId);
    }
}
