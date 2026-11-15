package com.partyplaylist.model;

import com.partyplaylist.model.enums.PartyPhase;
import com.partyplaylist.model.enums.PartyStatus;

import java.time.LocalDateTime;

public class Party {
    private Long id;
    private Long hostId;
    private String title;
    private String joinCode;
    private PartyPhase phase = PartyPhase.WARMUP;
    private PartyStatus status = PartyStatus.DRAFT;
    private int maxSuggestionsPerGuest = 5;
    private boolean noArtistRepeat = true;
    private LocalDateTime createdAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getHostId() { return hostId; }
    public void setHostId(Long hostId) { this.hostId = hostId; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getJoinCode() { return joinCode; }
    public void setJoinCode(String joinCode) { this.joinCode = joinCode; }

    public PartyPhase getPhase() { return phase; }
    public void setPhase(PartyPhase phase) { this.phase = phase; }

    public PartyStatus getStatus() { return status; }
    public void setStatus(PartyStatus status) { this.status = status; }

    public int getMaxSuggestionsPerGuest() { return maxSuggestionsPerGuest; }
    public void setMaxSuggestionsPerGuest(int maxSuggestionsPerGuest) { this.maxSuggestionsPerGuest = maxSuggestionsPerGuest; }

    public boolean isNoArtistRepeat() { return noArtistRepeat; }
    public void setNoArtistRepeat(boolean noArtistRepeat) { this.noArtistRepeat = noArtistRepeat; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
