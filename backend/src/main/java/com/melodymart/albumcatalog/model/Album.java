package com.melodymart.albumcatalog.model;
import com.melodymart.artistgenretrack.model.Genre;
import com.melodymart.artistgenretrack.model.Artist;
import com.melodymart.artistgenretrack.model.Track;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Entity
@Table(name = "ALBUM", schema = "dbo")
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AlbumID")
    private Integer albumId;

    @Column(name = "AlbumTitle", nullable = false, length = 150)
    private String albumTitle;

    @Column(name = "ReleaseDate")
    private LocalDate releaseDate;

    @Column(name = "AlbumStatus", nullable = false, length = 20)
    private String albumStatus = "Available";

    @Column(name = "Price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "Description", columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(name = "CoverImageURL", length = 255)
    private String coverImageUrl;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "ArtistID", nullable = false)
    private Artist artist;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "GenreID", nullable = false)
    private Genre genre;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "CatalogID")
    private Catalog catalog;

    @OneToMany(mappedBy = "album", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("trackNumber ASC")
    private List<Track> tracks = new ArrayList<>();

    public Album() {
    }

    public Integer getAlbumId() {
        return albumId;
    }

    public void setAlbumId(Integer albumId) {
        this.albumId = albumId;
    }

    public String getAlbumTitle() {
        return albumTitle;
    }

    public void setAlbumTitle(String albumTitle) {
        this.albumTitle = albumTitle;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getAlbumStatus() {
        return albumStatus;
    }

    public void setAlbumStatus(String albumStatus) {
        this.albumStatus = albumStatus;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public static String normalizeCoverImageUrl(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return null;

        // If external web URL (http:// or https://), retain as is
        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            return trimmed;
        }

        // Normalize backslashes to forward slashes
        String normalized = trimmed.replace('\\', '/');

        // If it starts with /assets/covers/
        if (normalized.startsWith("/assets/covers/")) {
            return normalized;
        }
        // If it starts with assets/covers/ (missing leading slash)
        if (normalized.startsWith("assets/covers/")) {
            return "/" + normalized;
        }

        // If user entered Windows local path C:/Users/.../image.jpg or /some/path/image.jpg
        if (normalized.contains("/")) {
            int lastSlash = normalized.lastIndexOf('/');
            String filename = normalized.substring(lastSlash + 1).trim();
            if (!filename.isEmpty()) {
                return "/assets/covers/" + filename;
            }
        }

        // If it is simply a filename like "sanda_eliya.jpg"
        return "/assets/covers/" + normalized;
    }

    public String getCoverImageUrl() {
        if (coverImageUrl == null || coverImageUrl.trim().isEmpty()) {
            return null;
        }
        return normalizeCoverImageUrl(coverImageUrl);
    }

    public void setCoverImageUrl(String coverImageUrl) {
        this.coverImageUrl = normalizeCoverImageUrl(coverImageUrl);
    }

    public Artist getArtist() {
        return artist;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public Genre getGenre() {
        return genre;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public Catalog getCatalog() {
        return catalog;
    }

    public void setCatalog(Catalog catalog) {
        this.catalog = catalog;
    }

    public List<Track> getTracks() {
        return tracks;
    }

    public void setTracks(List<Track> tracks) {
        this.tracks = tracks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Album album = (Album) o;
        return Objects.equals(albumId, album.albumId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(albumId);
    }
}
