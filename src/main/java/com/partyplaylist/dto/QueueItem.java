package com.partyplaylist.dto;

import com.partyplaylist.model.enums.TrackSource;
import com.partyplaylist.model.enums.TrackStatus;

import java.time.LocalDateTime;

public record QueueItem(
        Long trackId,
        String title,
        String artist,
        String genreName,
        int energy,
        TrackSource sourceType,
        String audioPath,
        String youtubeUrl,
        String coverPath,
        Long suggestedById,
        String suggestedBy,
        TrackStatus status,
        LocalDateTime addedAt,
        int rating,
        int myVote
) {
    public String youtubeId() {
        if (youtubeUrl == null) {
            return null;
        }
        return youtubeUrl.substring(youtubeUrl.indexOf("v=") + 2);
    }
}
