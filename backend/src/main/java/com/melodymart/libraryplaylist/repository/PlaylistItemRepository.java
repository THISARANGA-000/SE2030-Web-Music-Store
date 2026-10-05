package com.melodymart.libraryplaylist.repository;

import com.melodymart.libraryplaylist.model.PlaylistItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistItemRepository extends JpaRepository<PlaylistItem, Integer> {

    /** All items in a playlist */
    List<PlaylistItem> findByPlaylist_PlaylistIdOrderByAddedDateAsc(Integer playlistId);

    /** Find a specific item only if it belongs to the given playlist (prevents cross-playlist access) */
    Optional<PlaylistItem> findByPlaylistItemIdAndPlaylist_PlaylistId(Integer itemId, Integer playlistId);

    /** Check if a track is already in the playlist */
    boolean existsByPlaylist_PlaylistIdAndTrack_TrackId(Integer playlistId, Integer trackId);

    /** Check if an album is already in the playlist */
    boolean existsByPlaylist_PlaylistIdAndAlbum_AlbumId(Integer playlistId, Integer albumId);
}
