package com.melodymart.artistgenretrack.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "ARTIST", schema = "dbo")
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ArtistID")
    private Integer artistId;

    @Column(name = "ArtistName", nullable = false, length = 100)
    private String artistName;

    @Column(name = "Biography", columnDefinition = "NVARCHAR(MAX)")
    private String biography;

    @Column(name = "Country", length = 50)
    private String country;

    public Artist() {
    }

    public Artist(String artistName, String biography, String country) {
        this.artistName = artistName;
        this.biography = biography;
        this.country = country;
    }

    public Integer getArtistId() {
        return artistId;
    }

    public void setArtistId(Integer artistId) {
        this.artistId = artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getBiography() {
        return biography;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Artist artist = (Artist) o;
        return Objects.equals(artistId, artist.artistId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(artistId);
    }
}
