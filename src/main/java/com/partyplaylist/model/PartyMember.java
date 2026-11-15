package com.partyplaylist.model;

import com.partyplaylist.model.enums.MemberRole;

import java.time.LocalDateTime;

/** Связь M2M пользователь <-> вечеринка с ролью участника. */
public record PartyMember(Long partyId, Long userId, MemberRole role, LocalDateTime joinedAt) {

    /** Новый участник, ещё не сохранённый в БД (время вступления проставит БД). */
    public PartyMember(Long partyId, Long userId, MemberRole role) {
        this(partyId, userId, role, null);
    }
}
