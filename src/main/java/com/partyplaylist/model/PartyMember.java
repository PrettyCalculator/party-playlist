package com.partyplaylist.model;

import com.partyplaylist.model.enums.MemberRole;

import java.time.LocalDateTime;

public record PartyMember(Long partyId, Long userId, MemberRole role, LocalDateTime joinedAt) {

    public PartyMember(Long partyId, Long userId, MemberRole role) {
        this(partyId, userId, role, null);
    }
}
