package com.partyplaylist.service;

import com.partyplaylist.dao.PartyDao;
import com.partyplaylist.dao.PartyMemberDao;
import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.PartyMember;
import com.partyplaylist.model.Track;
import com.partyplaylist.model.enums.MemberRole;
import com.partyplaylist.model.enums.PartyPhase;
import com.partyplaylist.model.enums.PartyStatus;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class PartyService {

    private static final String CODE_ALPHABET = "ABCDEFGHJKMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 6;
    private static final int MAX_TITLE_LENGTH = 100;
    private static final int MIN_SUGGESTIONS = 1;
    private static final int MAX_SUGGESTIONS = 50;

    private final PartyDao partyDao;
    private final PartyMemberDao memberDao;
    private final TrackDao trackDao;
    private final FileStorageService fileStorage;
    private final SecureRandom random = new SecureRandom();

    public PartyService(PartyDao partyDao, PartyMemberDao memberDao, TrackDao trackDao,
                        FileStorageService fileStorage) {
        this.partyDao = partyDao;
        this.memberDao = memberDao;
        this.trackDao = trackDao;
        this.fileStorage = fileStorage;
    }

    public Party create(String title, String maxSuggestions, Long hostId) {
        validate(title, maxSuggestions);

        Party party = new Party();
        party.setHostId(hostId);
        party.setTitle(title.trim());
        party.setMaxSuggestionsPerGuest(Integer.parseInt(maxSuggestions.trim()));
        party.setJoinCode(generateJoinCode());

        partyDao.save(party);
        return party;
    }

    public Party update(Long partyId, String title, String maxSuggestions, Long userId) {
        Party party = getHostedParty(partyId, userId);
        validate(title, maxSuggestions);

        party.setTitle(title.trim());
        party.setMaxSuggestionsPerGuest(Integer.parseInt(maxSuggestions.trim()));
        partyDao.update(party);
        return party;
    }

    public void delete(Long partyId, Long userId) {
        getHostedParty(partyId, userId);
        List<Track> tracks = trackDao.findByParty(partyId);
        partyDao.delete(partyId);
        for (Track track : tracks) {
            fileStorage.delete(track.getAudioPath());
            fileStorage.delete(track.getCoverPath());
        }
    }

    public Party getById(Long partyId) {
        return partyDao.findById(partyId)
                .orElseThrow(() -> new NotFoundException("Вечеринка не найдена"));
    }

    public Party getHostedParty(Long partyId, Long userId) {
        Party party = getById(partyId);
        if (!party.getHostId().equals(userId)) {
            throw new AccessDeniedException("Это может делать только хост вечеринки");
        }
        return party;
    }

    public Party join(String code, Long userId) {
        if (code == null || code.isBlank()) {
            throw new ValidationException("code", "Введите код вечеринки");
        }
        Party party = partyDao.findByJoinCode(code.trim().toUpperCase())
                .orElseThrow(() -> new ValidationException("code", "Вечеринка с таким кодом не найдена"));

        if (party.getStatus() == PartyStatus.FINISHED) {
            throw new BusinessException("Эта вечеринка уже закончилась");
        }
        if (memberDao.find(party.getId(), userId).isEmpty()) {
            memberDao.add(new PartyMember(party.getId(), userId, MemberRole.GUEST));
        }
        return party;
    }

    public void leave(Long partyId, Long userId) {
        Party party = getById(partyId);
        if (party.getHostId().equals(userId)) {
            throw new BusinessException("Хост не может выйти из своей вечеринки, её можно только удалить");
        }
        memberDao.remove(partyId, userId);
    }

    public void start(Long partyId, Long userId) {
        Party party = getHostedParty(partyId, userId);
        if (party.getStatus() != PartyStatus.DRAFT) {
            throw new BusinessException("Начать можно только вечеринку, которая ещё не началась");
        }
        party.setStatus(PartyStatus.LIVE);
        partyDao.update(party);
    }

    public void finish(Long partyId, Long userId) {
        Party party = getHostedParty(partyId, userId);
        if (party.getStatus() != PartyStatus.LIVE) {
            throw new BusinessException("Завершить можно только идущую вечеринку");
        }
        party.setStatus(PartyStatus.FINISHED);
        partyDao.update(party);
    }

    public void changePhase(Long partyId, String phase, Long userId) {
        Party party = getHostedParty(partyId, userId);
        if (party.getStatus() != PartyStatus.LIVE) {
            throw new BusinessException("Фазу можно менять только во время вечеринки");
        }
        try {
            party.setPhase(PartyPhase.valueOf(phase));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ValidationException("phase", "Неизвестная фаза");
        }
        partyDao.update(party);
    }

    public List<Party> findByMember(Long userId) {
        return partyDao.findByMember(userId);
    }

    public MemberRole getRole(Long partyId, Long userId) {
        Optional<PartyMember> member = memberDao.find(partyId, userId);
        return member.map(PartyMember::role).orElse(null);
    }

    private void validate(String title, String maxSuggestions) {
        Map<String, String> errors = new HashMap<>();

        if (title == null || title.isBlank()) {
            errors.put("title", "Введите название вечеринки");
        } else if (title.trim().length() > MAX_TITLE_LENGTH) {
            errors.put("title", "Название не длиннее " + MAX_TITLE_LENGTH + " символов");
        }

        try {
            int max = Integer.parseInt(maxSuggestions.trim());
            if (max < MIN_SUGGESTIONS || max > MAX_SUGGESTIONS) {
                errors.put("maxSuggestions", "Лимит треков от " + MIN_SUGGESTIONS + " до " + MAX_SUGGESTIONS);
            }
        } catch (NumberFormatException | NullPointerException e) {
            errors.put("maxSuggestions", "Введите число");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private String generateJoinCode() {
        String code;
        do {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < CODE_LENGTH; i++) {
                sb.append(CODE_ALPHABET.charAt(random.nextInt(CODE_ALPHABET.length())));
            }
            code = sb.toString();
        } while (partyDao.existsByJoinCode(code));
        return code;
    }
}
