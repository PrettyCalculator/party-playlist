package com.partyplaylist.model;

import com.partyplaylist.model.enums.TrackSource;
import com.partyplaylist.model.enums.TrackStatus;

import java.time.LocalDateTime;

public class Track {
    private Long id;
    private Long partyId;
    private Long suggestedBy;
    private String title;
    private String artist;
    private Integer genreId;
    private int energy;
    private TrackSource sourceType;
    private String audioPath;
    private String youtubeUrl;
    private String coverPath;
    private TrackStatus status = TrackStatus.QUEUED;
    private LocalDateTime addedAt;
    private LocalDateTime playedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }

    public Long getSuggestedBy() { return suggestedBy; }
    public void setSuggestedBy(Long suggestedBy) { this.suggestedBy = suggestedBy; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getArtist() { return artist; }
    public void setArtist(String artist) { this.artist = artist; }

    public Integer getGenreId() { return genreId; }
    public void setGenreId(Integer genreId) { this.genreId = genreId; }

    public int getEnergy() { return energy; }
    public void setEnergy(int energy) { this.energy = energy; }

    public TrackSource getSourceType() { return sourceType; }
    public void setSourceType(TrackSource sourceType) { this.sourceType = sourceType; }

    public String getAudioPath() { return audioPath; }
    public void setAudioPath(String audioPath) { this.audioPath = audioPath; }

    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }

    public String getCoverPath() { return coverPath; }
    public void setCoverPath(String coverPath) { this.coverPath = coverPath; }

    public TrackStatus getStatus() { return status; }
    public void setStatus(TrackStatus status) { this.status = status; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }

    public boolean isFile() {
        return sourceType == TrackSource.FILE;
    }
}
