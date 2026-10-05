package com.melodymart.artistgenretrack.repository;
import com.melodymart.artistgenretrack.model.Genre;

import com.melodymart.artistgenretrack.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Integer> {
    List<Genre> findAllByOrderByGenreNameAsc();
}
