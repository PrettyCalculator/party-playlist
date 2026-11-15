package com.partyplaylist.dao;

import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.model.PartyTrack;
import com.partyplaylist.model.enums.PartyTrackStatus;

import java.util.List;
import java.util.Optional;

public interface PartyTrackDao {

    void save(PartyTrack partyTrack);

    Optional<PartyTrack> findById(Long id);

    boolean exists(Long partyId, Long trackId);

    List<QueueItem> findQueue(Long partyId, Long userId);

    Optional<QueueItem> findCurrent(Long partyId);

    Optional<QueueItem> findLastPlayed(Long partyId);

    void updateStatus(Long id, PartyTrackStatus status);

    int countSuggestedBy(Long partyId, Long userId);
}
