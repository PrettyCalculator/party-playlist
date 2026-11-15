package com.partyplaylist.dto;

import com.partyplaylist.model.enums.PartyTrackStatus;
import com.partyplaylist.model.enums.TrackSource;

import java.time.LocalDateTime;

public record QueueItem(
        Long partyTrackId,
        Long trackId,
        String title,
        String artist,
        int energy,
        TrackSource sourceType,
        String audioPath,
        String youtubeUrl,
        String coverPath,
        String suggestedBy,
        PartyTrackStatus status,
        LocalDateTime addedAt,
        int rating,
        int myVote
) {
}
