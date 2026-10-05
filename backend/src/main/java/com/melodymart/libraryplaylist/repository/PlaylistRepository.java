package com.melodymart.libraryplaylist.repository;

import com.melodymart.libraryplaylist.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Integer> {

    /** All playlists owned by the given listener, newest first */
    List<Playlist> findByListenerIdOrderByCreatedDateDesc(Integer listenerId);

    /** Find a playlist only if it belongs to the given listener (ownership check) */
    Optional<Playlist> findByPlaylistIdAndListenerId(Integer playlistId, Integer listenerId);

    /** Count playlists for a listener */
    long countByListenerId(Integer listenerId);
}
