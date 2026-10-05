package com.melodymart.artistgenretrack.repository;

import com.melodymart.artistgenretrack.model.Track;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TrackRepository extends JpaRepository<Track, Integer> {
    List<Track> findByAlbum_AlbumIdOrderByTrackNumberAsc(Integer albumId);

    @Query("SELECT t FROM Track t JOIN t.album a WHERE a.albumId IN :albumIds ORDER BY a.albumTitle ASC, t.trackNumber ASC")
    List<Track> findTracksByAlbumIds(@Param("albumIds") List<Integer> albumIds);
}

