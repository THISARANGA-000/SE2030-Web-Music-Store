package com.melodymart.artistgenretrack.model;

import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "GENRE", schema = "dbo")
public class Genre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GenreID")
    private Integer genreId;

    @Column(name = "GenreName", nullable = false, unique = true, length = 50)
    private String genreName;

    @Column(name = "Description", length = 255)
    private String description;

    public Genre() {
    }

    public Genre(String genreName, String description) {
        this.genreName = genreName;
        this.description = description;
    }

    public Integer getGenreId() {
        return genreId;
    }

    public void setGenreId(Integer genreId) {
        this.genreId = genreId;
    }

    public String getGenreName() {
        return genreName;
    }

    public void setGenreName(String genreName) {
        this.genreName = genreName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Genre genre = (Genre) o;
        return Objects.equals(genreId, genre.genreId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(genreId);
    }
}
