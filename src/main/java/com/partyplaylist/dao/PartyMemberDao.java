package com.partyplaylist.dao;

import com.partyplaylist.model.PartyMember;

import java.util.List;
import java.util.Optional;

public interface PartyMemberDao {

    void add(PartyMember member);

    Optional<PartyMember> find(Long partyId, Long userId);

    List<PartyMember> findByParty(Long partyId);

    void remove(Long partyId, Long userId);
}
