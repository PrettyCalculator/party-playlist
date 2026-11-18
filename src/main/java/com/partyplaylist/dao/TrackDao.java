package com.partyplaylist.dao;

import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.model.Track;
import com.partyplaylist.model.enums.TrackStatus;

import java.util.List;
import java.util.Optional;

public interface TrackDao {

    void save(Track track);

    void update(Track track);

    void delete(Long id);

    Optional<Track> findById(Long id);

    List<Track> findByParty(Long partyId);

    List<QueueItem> findQueue(Long partyId, Long userId);

    Optional<QueueItem> findCurrent(Long partyId);

    void updateStatus(Long id, TrackStatus status);

    void switchCurrent(Long currentId, Long nextId);

    int countSuggestedBy(Long partyId, Long userId);
}
