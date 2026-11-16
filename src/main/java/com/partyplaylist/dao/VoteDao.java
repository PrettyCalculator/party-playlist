package com.partyplaylist.dao;

import com.partyplaylist.model.Vote;

import java.util.Optional;

public interface VoteDao {

    Optional<Vote> find(Long userId, Long partyTrackId);

    void vote(Vote vote);

    void removeVote(Long userId, Long partyTrackId);
}
