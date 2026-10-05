package com.melodymart.libraryplaylist.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * JPA entity for the PLAYLIST table.
 * Maps to dbo.[PLAYLIST] in MelodyMartDB.
 * ddl-auto=none — Hibernate does NOT create/modify this table.
 */
@Entity
@Table(name = "PLAYLIST", schema = "dbo")
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PlaylistID")
    private Integer playlistId;

    @Column(name = "ListenerID", nullable = false)
    private Integer listenerId;

    @Column(name = "PlaylistName", nullable = false, length = 100)
    private String playlistName;

    @Column(name = "CreatedDate", nullable = false)
    private LocalDateTime createdDate;

    @Column(name = "LastUpdatedDate", nullable = false)
    private LocalDateTime lastUpdatedDate;

    @OneToMany(mappedBy = "playlist", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("addedDate ASC")
    private List<PlaylistItem> items = new ArrayList<>();

    public Playlist() {
    }

    public Playlist(Integer listenerId, String playlistName) {
        this.listenerId = listenerId;
        this.playlistName = playlistName;
        this.createdDate = LocalDateTime.now();
        this.lastUpdatedDate = LocalDateTime.now();
    }

    // ─── Getters / Setters ──────────────────────────────────────────

    public Integer getPlaylistId() { return playlistId; }
    public void setPlaylistId(Integer playlistId) { this.playlistId = playlistId; }

    public Integer getListenerId() { return listenerId; }
    public void setListenerId(Integer listenerId) { this.listenerId = listenerId; }

    public String getPlaylistName() { return playlistName; }
    public void setPlaylistName(String playlistName) { this.playlistName = playlistName; }

    public LocalDateTime getCreatedDate() { return createdDate; }
    public void setCreatedDate(LocalDateTime createdDate) { this.createdDate = createdDate; }

    public LocalDateTime getLastUpdatedDate() { return lastUpdatedDate; }
    public void setLastUpdatedDate(LocalDateTime lastUpdatedDate) { this.lastUpdatedDate = lastUpdatedDate; }

    public List<PlaylistItem> getItems() { return items; }
    public void setItems(List<PlaylistItem> items) { this.items = items; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Playlist p = (Playlist) o;
        return Objects.equals(playlistId, p.playlistId);
    }

    @Override
    public int hashCode() { return Objects.hash(playlistId); }
}
