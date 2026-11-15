package com.partyplaylist.dao;

import com.partyplaylist.model.Vote;

public interface VoteDao {

    void vote(Vote vote);

    void removeVote(Long userId, Long partyTrackId);
}
