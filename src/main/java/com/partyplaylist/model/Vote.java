package com.partyplaylist.model;

import java.time.LocalDateTime;

public record Vote(Long userId, Long trackId, int value, LocalDateTime createdAt) {
    public Vote {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Голос должен быть +1 или -1, а не " + value);
        }
    }

    public Vote(Long userId, Long trackId, int value) {
        this(userId, trackId, value, null);
    }
}
