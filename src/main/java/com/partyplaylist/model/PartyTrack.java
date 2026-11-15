package com.partyplaylist.model;

import com.partyplaylist.model.enums.PartyTrackStatus;

import java.time.LocalDateTime;

public class PartyTrack {
    private Long id;
    private Long partyId;
    private Long trackId;
    private Long suggestedBy;
    private PartyTrackStatus status = PartyTrackStatus.QUEUED;
    private LocalDateTime addedAt;
    private LocalDateTime playedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPartyId() { return partyId; }
    public void setPartyId(Long partyId) { this.partyId = partyId; }

    public Long getTrackId() { return trackId; }
    public void setTrackId(Long trackId) { this.trackId = trackId; }

    public Long getSuggestedBy() { return suggestedBy; }
    public void setSuggestedBy(Long suggestedBy) { this.suggestedBy = suggestedBy; }

    public PartyTrackStatus getStatus() { return status; }
    public void setStatus(PartyTrackStatus status) { this.status = status; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

    public LocalDateTime getPlayedAt() { return playedAt; }
    public void setPlayedAt(LocalDateTime playedAt) { this.playedAt = playedAt; }
}
