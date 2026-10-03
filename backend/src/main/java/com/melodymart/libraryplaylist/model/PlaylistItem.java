package com.melodymart.libraryplaylist.model;
import com.melodymart.artistgenretrack.model.Track;
import com.melodymart.albumcatalog.model.Album;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * JPA entity for the PLAYLIST_ITEM table.
 * Maps to dbo.[PLAYLIST_ITEM] in MelodyMartDB.
 *
 * Each row represents EITHER a Track OR an Album (enforced by DB CHECK constraint).
 * Exactly one of trackId / albumId will be non-null.
 *
 * ddl-auto=none — Hibernate does NOT create/modify this table.
 */
@Entity
@Table(name = "PLAYLIST_ITEM", schema = "dbo")
public class PlaylistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PlaylistItemID")
    private Integer playlistItemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PlaylistID", nullable = false)
    private Playlist playlist;

    // TrackID is nullable; null means this item is an album
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "TrackID", nullable = true)
    private Track track;

    // AlbumID is nullable; null means this item is a track
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "AlbumID", nullable = true)
    private Album album;

    @Column(name = "AddedDate", nullable = false)
    private LocalDateTime addedDate;

    public PlaylistItem() {
    }

    /** Constructor for a track item */
    public PlaylistItem(Playlist playlist, Track track) {
        this.playlist = playlist;
        this.track = track;
        this.album = null;
        this.addedDate = LocalDateTime.now();
    }

    /** Constructor for an album item */
    public PlaylistItem(Playlist playlist, Album album) {
        this.playlist = playlist;
        this.album = album;
        this.track = null;
        this.addedDate = LocalDateTime.now();
    }

    // ─── Helpers ──────────────────────────────────────────────────────

    /** True if this item is a track entry */
    public boolean isTrackItem() { return track != null; }

    /** True if this item is an album entry */
    public boolean isAlbumItem() { return album != null; }

    // ─── Getters / Setters ────────────────────────────────────────────

    public Integer getPlaylistItemId() { return playlistItemId; }
    public void setPlaylistItemId(Integer playlistItemId) { this.playlistItemId = playlistItemId; }

    public Playlist getPlaylist() { return playlist; }
    public void setPlaylist(Playlist playlist) { this.playlist = playlist; }

    public Track getTrack() { return track; }
    public void setTrack(Track track) { this.track = track; }

    public Album getAlbum() { return album; }
    public void setAlbum(Album album) { this.album = album; }

    public LocalDateTime getAddedDate() { return addedDate; }
    public void setAddedDate(LocalDateTime addedDate) { this.addedDate = addedDate; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PlaylistItem i = (PlaylistItem) o;
        return Objects.equals(playlistItemId, i.playlistItemId);
    }

    @Override
    public int hashCode() { return Objects.hash(playlistItemId); }
}
