package com.partyplaylist.model;

import java.time.LocalDateTime;

/** Голос пользователя за трек в очереди (M2M пользователь <-> трек в очереди). */
public record Vote(Long userId, Long partyTrackId, int value, LocalDateTime createdAt) {

    /** Голос допускает только +1 (лайк) или -1 (дизлайк). */
    public Vote {
        if (value != 1 && value != -1) {
            throw new IllegalArgumentException("Голос должен быть +1 или -1, а не " + value);
        }
    }

    /** Новый голос, ещё не сохранённый в БД (время проставит БД). */
    public Vote(Long userId, Long partyTrackId, int value) {
        this(userId, partyTrackId, value, null);
    }
}
