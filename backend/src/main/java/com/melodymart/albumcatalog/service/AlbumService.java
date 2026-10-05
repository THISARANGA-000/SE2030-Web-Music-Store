package com.melodymart.albumcatalog.service;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.artistgenretrack.repository.GenreRepository;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.albumcatalog.model.Album;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.repository.ArtistRepository;

import com.melodymart.albumcatalog.model.Album;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.albumcatalog.repository.AlbumRepository;
import com.melodymart.artistgenretrack.repository.ArtistRepository;
import com.melodymart.artistgenretrack.repository.GenreRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final GenreRepository genreRepository;
    private final ArtistRepository artistRepository;

    @Autowired
    public AlbumService(AlbumRepository albumRepository,
                        GenreRepository genreRepository,
                        ArtistRepository artistRepository) {
        this.albumRepository = albumRepository;
        this.genreRepository = genreRepository;
        this.artistRepository = artistRepository;
    }

    @Transactional(readOnly = true)
    public List<Album> getAllAlbums() {
        return albumRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Album> getAlbumById(Integer id) {
        return albumRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public Album getAlbumWithDetails(Integer id) {
        Optional<Album> albumOpt = albumRepository.findById(id);
        if (albumOpt.isPresent()) {
            Album album = albumOpt.get();
            // Trigger tracks lazy loading initialization
            album.getTracks().size();
            return album;
        }
        return null;
    }

    @Transactional(readOnly = true)
    public List<Album> searchAndFilterAlbums(String keyword, Integer genreId, Integer artistId) {
        String trimmedKeyword = (keyword != null && !keyword.trim().isEmpty()) ? keyword.trim() : null;
        return albumRepository.searchAndFilterAlbums(trimmedKeyword, genreId, artistId);
    }

    @Transactional(readOnly = true)
    public List<Genre> getAllGenres() {
        return genreRepository.findAllByOrderByGenreNameAsc();
    }

    @Transactional(readOnly = true)
    public List<Artist> getAllArtists() {
        return artistRepository.findAllByOrderByArtistNameAsc();
    }

    /**
     * Retrieves the centralized application settings from the MelodyMartApplicationSettings Singleton.
     *
     * @return the shared Singleton instance
     */
    public com.melodymart.config.MelodyMartApplicationSettings getApplicationSettings() {
        return com.melodymart.config.MelodyMartApplicationSettings.getInstance();
    }

    /**
     * Gets the default catalog page size configured in the Singleton application settings.
     */
    public int getDefaultPageSize() {
        return com.melodymart.config.MelodyMartApplicationSettings.getInstance().getDefaultPageSize();
    }
}

