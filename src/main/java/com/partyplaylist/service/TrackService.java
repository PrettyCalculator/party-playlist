package com.partyplaylist.service;

import com.partyplaylist.dao.GenreDao;
import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.dto.TrackForm;
import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Genre;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.Track;
import com.partyplaylist.model.enums.MemberRole;
import com.partyplaylist.model.enums.PartyStatus;
import com.partyplaylist.model.enums.TrackSource;
import com.partyplaylist.model.enums.TrackStatus;
import jakarta.servlet.http.Part;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TrackService {

    private static final Pattern YOUTUBE_PATTERN = Pattern.compile(
            "^(https?://)?(www\\.|m\\.)?(youtube\\.com/(watch\\?v=|shorts/)|youtu\\.be/)([A-Za-z0-9_-]{11}).*$");

    private static final int MAX_TEXT_LENGTH = 200;
    private static final String NOT_MEMBER_MESSAGE = "Вы не участник этой вечеринки";
    private static final String FINISHED_MESSAGE = "Эта вечеринка уже закончилась";

    private final TrackDao trackDao;
    private final GenreDao genreDao;
    private final FileStorageService fileStorage;
    private final PartyService partyService;

    public TrackService(TrackDao trackDao, GenreDao genreDao, FileStorageService fileStorage,
                        PartyService partyService) {
        this.trackDao = trackDao;
        this.genreDao = genreDao;
        this.fileStorage = fileStorage;
        this.partyService = partyService;
    }

    public Track suggest(Long partyId, TrackForm form, Part audio, Part cover, Long userId) {
        Party party = partyService.getById(partyId);
        MemberRole role = partyService.getRole(partyId, userId);
        if (role == null) {
            throw new AccessDeniedException(NOT_MEMBER_MESSAGE);
        }
        if (isFinished(party)) {
            throw new BusinessException(FINISHED_MESSAGE);
        }
        if (role == MemberRole.GUEST
                && trackDao.countSuggestedBy(partyId, userId) >= party.getMaxSuggestionsPerGuest()) {
            throw new BusinessException("Можно предложить не больше " + party.getMaxSuggestionsPerGuest() + " треков");
        }

        validate(form, audio, cover, true);

        Track track = new Track();
        track.setPartyId(partyId);
        track.setSuggestedBy(userId);
        fillFields(track, form);

        if (track.getSourceType() == TrackSource.FILE) {
            track.setAudioPath(fileStorage.saveAudio(audio));
        } else {
            track.setYoutubeUrl(normalizeYoutubeUrl(form.youtubeUrl()));
        }
        if (fileStorage.hasFile(cover)) {
            track.setCoverPath(fileStorage.saveCover(cover));
        }

        trackDao.save(track);
        return track;
    }

    public Track update(Long trackId, TrackForm form, Part audio, Part cover, Long userId) {
        Track track = getEditableTrack(trackId, userId);
        validate(form, audio, cover, !track.isFile());

        String oldAudio = track.getAudioPath();
        String oldCover = track.getCoverPath();
        fillFields(track, form);

        if (track.getSourceType() == TrackSource.FILE) {
            if (fileStorage.hasFile(audio)) {
                track.setAudioPath(fileStorage.saveAudio(audio));
            }
            track.setYoutubeUrl(null);
        } else {
            track.setYoutubeUrl(normalizeYoutubeUrl(form.youtubeUrl()));
            track.setAudioPath(null);
        }
        if (fileStorage.hasFile(cover)) {
            track.setCoverPath(fileStorage.saveCover(cover));
        }

        trackDao.update(track);

        if (oldAudio != null && !oldAudio.equals(track.getAudioPath())) {
            fileStorage.delete(oldAudio);
        }
        if (oldCover != null && !oldCover.equals(track.getCoverPath())) {
            fileStorage.delete(oldCover);
        }
        return track;
    }

    public void delete(Long trackId, Long userId) {
        Track track = getEditableTrack(trackId, userId);
        trackDao.delete(trackId);
        fileStorage.delete(track.getAudioPath());
        fileStorage.delete(track.getCoverPath());
    }

    public Track findById(Long id) {
        return trackDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Трек не найден"));
    }

    public Track getEditableTrack(Long trackId, Long userId) {
        Track track = findById(trackId);
        if (!track.getSuggestedBy().equals(userId)) {
            throw new AccessDeniedException("Можно изменять только свои треки");
        }
        if (track.getStatus() != TrackStatus.QUEUED) {
            throw new BusinessException("Изменить можно только трек, который ещё ждёт в очереди");
        }
        if (isFinished(partyService.getById(track.getPartyId()))) {
            throw new BusinessException(FINISHED_MESSAGE);
        }
        return track;
    }

    public List<Genre> getGenres() {
        return genreDao.findAll();
    }

    private boolean isFinished(Party party) {
        return party.getStatus() == PartyStatus.FINISHED;
    }

    private void validate(TrackForm form, Part audio, Part cover, boolean audioRequired) {
        Map<String, String> errors = new HashMap<>();

        if (isBlank(form.title())) {
            errors.put("title", "Введите название трека");
        } else if (form.title().trim().length() > MAX_TEXT_LENGTH) {
            errors.put("title", "Название не длиннее " + MAX_TEXT_LENGTH + " символов");
        }

        if (isBlank(form.artist())) {
            errors.put("artist", "Введите исполнителя");
        } else if (form.artist().trim().length() > MAX_TEXT_LENGTH) {
            errors.put("artist", "Исполнитель не длиннее " + MAX_TEXT_LENGTH + " символов");
        }

        if (!isBlank(form.genreId())) {
            Integer genreId = parseInt(form.genreId());
            if (genreId == null || genreDao.findById(genreId).isEmpty()) {
                errors.put("genreId", "Выберите жанр из списка");
            }
        }

        Integer energy = parseInt(form.energy());
        if (energy == null || energy < 1 || energy > 5) {
            errors.put("energy", "Энергия должна быть от 1 до 5");
        }

        TrackSource source = parseSource(form.sourceType());
        if (source == null) {
            errors.put("sourceType", "Выберите, как добавить музыку");
        } else if (source == TrackSource.FILE) {
            if (fileStorage.hasFile(audio)) {
                if (!fileStorage.isValidAudio(audio)) {
                    errors.put("audio", "Нужен файл в формате mp3");
                }
            } else if (audioRequired) {
                errors.put("audio", "Загрузите mp3-файл");
            }
        } else if (normalizeYoutubeUrl(form.youtubeUrl()) == null) {
            errors.put("youtubeUrl", "Вставьте ссылку на видео YouTube");
        }

        if (fileStorage.hasFile(cover) && !fileStorage.isValidCover(cover)) {
            errors.put("cover", "Обложка должна быть в формате jpg или png");
        }

        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }
    }

    private void fillFields(Track track, TrackForm form) {
        track.setTitle(form.title().trim());
        track.setArtist(form.artist().trim());
        track.setGenreId(parseInt(form.genreId()));
        track.setEnergy(parseInt(form.energy()));
        track.setSourceType(parseSource(form.sourceType()));
    }

    private String normalizeYoutubeUrl(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = YOUTUBE_PATTERN.matcher(url.trim());
        if (!matcher.matches()) {
            return null;
        }
        return "https://www.youtube.com/watch?v=" + matcher.group(5);
    }

    private TrackSource parseSource(String value) {
        if (value == null) {
            return null;
        }
        try {
            return TrackSource.valueOf(value);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer parseInt(String value) {
        if (isBlank(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
