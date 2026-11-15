package com.partyplaylist.dao;

import com.partyplaylist.model.Party;

import java.util.List;
import java.util.Optional;

public interface PartyDao {

    void save(Party party);

    void update(Party party);

    void delete(Long id);

    Optional<Party> findById(Long id);

    Optional<Party> findByJoinCode(String joinCode);

    List<Party> findByMember(Long userId);

    boolean existsByJoinCode(String joinCode);
}
