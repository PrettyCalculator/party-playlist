package com.partyplaylist.dto;

public record TrackForm(
        String title,
        String artist,
        String genreId,
        String energy,
        String sourceType,
        String youtubeUrl
) {
}
