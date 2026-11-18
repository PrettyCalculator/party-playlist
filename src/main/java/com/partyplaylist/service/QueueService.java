package com.partyplaylist.service;

import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.dao.VoteDao;
import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.Track;
import com.partyplaylist.model.Vote;
import com.partyplaylist.model.enums.PartyPhase;
import com.partyplaylist.model.enums.PartyStatus;
import com.partyplaylist.model.enums.TrackStatus;

import java.util.List;
import java.util.Optional;

public class QueueService {

    private static final String NOT_MEMBER_MESSAGE = "Вы не участник этой вечеринки";
    private static final String FINISHED_MESSAGE = "Эта вечеринка уже закончилась";

    private final TrackDao trackDao;
    private final VoteDao voteDao;
    private final PartyService partyService;

    public QueueService(TrackDao trackDao, VoteDao voteDao, PartyService partyService) {
        this.trackDao = trackDao;
        this.voteDao = voteDao;
        this.partyService = partyService;
    }

    public void vote(Long trackId, Long userId, int value) {
        if (value != 1 && value != -1) {
            throw new ValidationException("vote", "Голос может быть только +1 или -1");
        }
        Track track = getTrack(trackId);
        Party party = partyService.getById(track.getPartyId());
        if (!isMember(party.getId(), userId)) {
            throw new AccessDeniedException(NOT_MEMBER_MESSAGE);
        }
        if (isFinished(party)) {
            throw new BusinessException(FINISHED_MESSAGE);
        }
        if (track.getStatus() != TrackStatus.QUEUED) {
            throw new BusinessException("Голосовать можно только за треки в очереди");
        }

        Optional<Vote> current = voteDao.find(userId, trackId);
        if (current.isPresent() && current.get().value() == value) {
            voteDao.removeVote(userId, trackId);
        } else {
            voteDao.vote(new Vote(userId, trackId, value));
        }
    }

    public void reject(Long trackId, Long userId) {
        Track track = getTrack(trackId);
        partyService.getHostedParty(track.getPartyId(), userId);
        if (track.getStatus() != TrackStatus.QUEUED) {
            throw new BusinessException("Отклонить можно только трек из очереди");
        }
        trackDao.updateStatus(trackId, TrackStatus.REJECTED);
    }

    public List<QueueItem> getQueue(Long partyId, Long userId) {
        if (!isMember(partyId, userId)) {
            throw new AccessDeniedException(NOT_MEMBER_MESSAGE);
        }
        return trackDao.findQueue(partyId, userId);
    }

    public Optional<QueueItem> getCurrent(Long partyId, Long userId) {
        if (!isMember(partyId, userId)) {
            throw new AccessDeniedException(NOT_MEMBER_MESSAGE);
        }
        return trackDao.findCurrent(partyId);
    }

    public Optional<QueueItem> next(Long partyId, Long userId) {
        Party party = partyService.getHostedParty(partyId, userId);
        if (party.getStatus() != PartyStatus.LIVE) {
            throw new BusinessException("Переключать треки можно только во время вечеринки");
        }

        Optional<QueueItem> current = trackDao.findCurrent(partyId);
        List<QueueItem> queue = trackDao.findQueue(partyId, userId);
        QueueItem next = chooseNext(queue, party.getPhase());

        Long currentId = current.map(QueueItem::trackId).orElse(null);
        Long nextId = next == null ? null : next.trackId();
        trackDao.switchCurrent(currentId, nextId);

        return Optional.ofNullable(next);
    }

    private QueueItem chooseNext(List<QueueItem> queue, PartyPhase phase) {
        if (queue.isEmpty()) {
            return null;
        }
        for (QueueItem item : queue) {
            if (phase.accepts(item.energy())) {
                return item;
            }
        }
        return queue.get(0);
    }

    private Track getTrack(Long trackId) {
        return trackDao.findById(trackId)
                .orElseThrow(() -> new NotFoundException("Трек не найден"));
    }

    private boolean isMember(Long partyId, Long userId) {
        return partyService.getRole(partyId, userId) != null;
    }

    private boolean isFinished(Party party) {
        return party.getStatus() == PartyStatus.FINISHED;
    }
}
