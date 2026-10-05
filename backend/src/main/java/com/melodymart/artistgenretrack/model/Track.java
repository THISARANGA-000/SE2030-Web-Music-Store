package com.melodymart.artistgenretrack.model;
import com.melodymart.albumcatalog.model.Album;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "TRACK", schema = "dbo")
public class Track {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "TrackID")
    private Integer trackId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AlbumID", nullable = false)
    private Album album;

    @Column(name = "TrackTitle", nullable = false, length = 150)
    private String trackTitle;

    @Column(name = "TrackNumber", nullable = false)
    private Integer trackNumber;

    @Column(name = "Duration", nullable = false)
    private Integer duration; // in seconds

    /**
     * Web path to the local audio track (e.g., /assets/audio/song1.mp3).
     * Physical file: backend/src/main/resources/static/assets/audio/song1.mp3
     * Database: AudioFileURL = /assets/audio/song1.mp3
     * Browser: http://localhost:8080/assets/audio/song1.mp3
     */
    @Column(name = "AudioFileURL", nullable = true, length = 255)
    private String audioFileUrl;

    public Track() {
    }

    public Integer getTrackId() {
        return trackId;
    }

    public void setTrackId(Integer trackId) {
        this.trackId = trackId;
    }

    public Album getAlbum() {
        return album;
    }

    public void setAlbum(Album album) {
        this.album = album;
    }

    public String getTrackTitle() {
        return trackTitle;
    }

    public void setTrackTitle(String trackTitle) {
        this.trackTitle = trackTitle;
    }

    public Integer getTrackNumber() {
        return trackNumber;
    }

    public void setTrackNumber(Integer trackNumber) {
        this.trackNumber = trackNumber;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getAudioFileUrl() {
        return audioFileUrl;
    }

    public void setAudioFileUrl(String audioFileUrl) {
        this.audioFileUrl = audioFileUrl;
    }

    public String getFormattedDuration() {
        if (duration == null) return "0:00";
        int mins = duration / 60;
        int secs = duration % 60;
        return String.format("%d:%02d", mins, secs);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Track track = (Track) o;
        return Objects.equals(trackId, track.trackId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(trackId);
    }
}
