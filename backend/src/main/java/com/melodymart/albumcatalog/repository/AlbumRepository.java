package com.melodymart.albumcatalog.repository;
import com.melodymart.albumcatalog.model.Album;

import com.melodymart.albumcatalog.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Integer> {

    List<Album> findByGenre_GenreId(Integer genreId);

    List<Album> findByArtist_ArtistId(Integer artistId);

    List<Album> findByCatalog_CatalogId(Integer catalogId);

    @Query("SELECT a FROM Album a WHERE " +
           "(:keyword IS NULL OR LOWER(a.albumTitle) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(a.artist.artistName) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:genreId IS NULL OR a.genre.genreId = :genreId) AND " +
           "(:artistId IS NULL OR a.artist.artistId = :artistId) " +
           "ORDER BY a.albumTitle ASC")
    List<Album> searchAndFilterAlbums(@Param("keyword") String keyword,
                                      @Param("genreId") Integer genreId,
                                      @Param("artistId") Integer artistId);
}
