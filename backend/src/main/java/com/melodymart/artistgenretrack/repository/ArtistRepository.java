package com.melodymart.artistgenretrack.repository;
import com.melodymart.artistgenretrack.model.Artist;

import com.melodymart.artistgenretrack.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArtistRepository extends JpaRepository<Artist, Integer> {
    List<Artist> findAllByOrderByArtistNameAsc();
}
