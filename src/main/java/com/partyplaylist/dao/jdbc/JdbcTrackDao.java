package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.model.Track;
import com.partyplaylist.model.enums.TrackSource;
import com.partyplaylist.model.enums.TrackStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcTrackDao implements TrackDao {

    private static final String COLUMNS =
            "id, party_id, suggested_by, title, artist, genre_id, energy, source_type, " +
            "audio_path, youtube_url, cover_path, status, added_at, played_at";

    private static final String INSERT =
            "INSERT INTO tracks (title, artist, genre_id, energy, source_type, audio_path, youtube_url, " +
            "cover_path, party_id, suggested_by, status) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE tracks SET title = ?, artist = ?, genre_id = ?, energy = ?, source_type = ?, " +
            "audio_path = ?, youtube_url = ?, cover_path = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM tracks WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT " + COLUMNS + " FROM tracks WHERE id = ?";
    private static final String FIND_BY_PARTY = "SELECT " + COLUMNS + " FROM tracks WHERE party_id = ?";
    private static final String UPDATE_STATUS = "UPDATE tracks SET status = ? WHERE id = ?";
    private static final String UPDATE_STATUS_PLAYED = "UPDATE tracks SET status = ?, played_at = now() WHERE id = ?";
    private static final String COUNT_SUGGESTED =
            "SELECT COUNT(*) FROM tracks WHERE party_id = ? AND suggested_by = ?";

    private static final String SELECT_ITEMS =
            "SELECT t.id, t.title, t.artist, g.name AS genre_name, t.energy, t.source_type, " +
            "t.audio_path, t.youtube_url, t.cover_path, t.suggested_by, u.username, t.status, t.added_at, " +
            "COALESCE(SUM(v.value), 0) AS rating, " +
            "COALESCE(MAX(CASE WHEN v.user_id = ? THEN v.value END), 0) AS my_vote " +
            "FROM tracks t " +
            "JOIN users u ON u.id = t.suggested_by " +
            "LEFT JOIN genres g ON g.id = t.genre_id " +
            "LEFT JOIN votes v ON v.track_id = t.id " +
            "WHERE t.party_id = ? AND t.status = ? " +
            "GROUP BY t.id, u.username, g.name ";

    private static final String FIND_QUEUE = SELECT_ITEMS + "ORDER BY rating DESC, t.added_at";
    private static final String FIND_CURRENT = SELECT_ITEMS + "LIMIT 1";

    private final DataSource dataSource;

    public JdbcTrackDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Track track) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setTrackParams(ps, track);
            ps.setLong(9, track.getPartyId());
            ps.setLong(10, track.getSuggestedBy());
            ps.setString(11, track.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    track.setId(keys.getLong("id"));
                    track.setAddedAt(keys.getObject("added_at", LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось сохранить трек " + track.getTitle(), e);
        }
    }

    @Override
    public void update(Track track) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            setTrackParams(ps, track);
            ps.setLong(9, track.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось обновить трек с id " + track.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось удалить трек с id " + id, e);
        }
    }

    @Override
    public Optional<Track> findById(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapTrack(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти трек с id " + id, e);
        }
    }

    @Override
    public List<Track> findByParty(Long partyId) {
        List<Track> tracks = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_PARTY)) {
            ps.setLong(1, partyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    tracks.add(mapTrack(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить треки вечеринки " + partyId, e);
        }
        return tracks;
    }

    @Override
    public List<QueueItem> findQueue(Long partyId, Long userId) {
        return findItems(FIND_QUEUE, partyId, userId, TrackStatus.QUEUED);
    }

    @Override
    public Optional<QueueItem> findCurrent(Long partyId) {
        List<QueueItem> items = findItems(FIND_CURRENT, partyId, null, TrackStatus.PLAYING);
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.get(0));
    }

    @Override
    public void updateStatus(Long id, TrackStatus status) {
        String sql = status == TrackStatus.PLAYED ? UPDATE_STATUS_PLAYED : UPDATE_STATUS;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось обновить статус трека с id " + id, e);
        }
    }

    @Override
    public void switchCurrent(Long currentId, Long nextId) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement playedPs = con.prepareStatement(UPDATE_STATUS_PLAYED);
                 PreparedStatement playingPs = con.prepareStatement(UPDATE_STATUS)) {
                if (currentId != null) {
                    playedPs.setString(1, TrackStatus.PLAYED.name());
                    playedPs.setLong(2, currentId);
                    playedPs.executeUpdate();
                }
                if (nextId != null) {
                    playingPs.setString(1, TrackStatus.PLAYING.name());
                    playingPs.setLong(2, nextId);
                    playingPs.executeUpdate();
                }
                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось переключить текущий трек", e);
        }
    }

    @Override
    public int countSuggestedBy(Long partyId, Long userId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(COUNT_SUGGESTED)) {
            ps.setLong(1, partyId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось посчитать треки пользователя в вечеринке " + partyId, e);
        }
    }

    private List<QueueItem> findItems(String sql, Long partyId, Long userId, TrackStatus status) {
        List<QueueItem> items = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            if (userId != null) {
                ps.setLong(1, userId);
            } else {
                ps.setNull(1, Types.BIGINT);
            }
            ps.setLong(2, partyId);
            ps.setString(3, status.name());
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    items.add(mapQueueItem(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить очередь вечеринки " + partyId, e);
        }
        return items;
    }

    private void setTrackParams(PreparedStatement ps, Track track) throws SQLException {
        ps.setString(1, track.getTitle());
        ps.setString(2, track.getArtist());
        if (track.getGenreId() != null) {
            ps.setInt(3, track.getGenreId());
        } else {
            ps.setNull(3, Types.INTEGER);
        }
        ps.setInt(4, track.getEnergy());
        ps.setString(5, track.getSourceType().name());
        ps.setString(6, track.getAudioPath());
        ps.setString(7, track.getYoutubeUrl());
        ps.setString(8, track.getCoverPath());
    }

    private Track mapTrack(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setId(rs.getLong("id"));
        track.setPartyId(rs.getLong("party_id"));
        track.setSuggestedBy(rs.getLong("suggested_by"));
        track.setTitle(rs.getString("title"));
        track.setArtist(rs.getString("artist"));
        track.setGenreId(rs.getObject("genre_id", Integer.class));
        track.setEnergy(rs.getInt("energy"));
        track.setSourceType(TrackSource.valueOf(rs.getString("source_type")));
        track.setAudioPath(rs.getString("audio_path"));
        track.setYoutubeUrl(rs.getString("youtube_url"));
        track.setCoverPath(rs.getString("cover_path"));
        track.setStatus(TrackStatus.valueOf(rs.getString("status")));
        track.setAddedAt(rs.getObject("added_at", LocalDateTime.class));
        track.setPlayedAt(rs.getObject("played_at", LocalDateTime.class));
        return track;
    }

    private QueueItem mapQueueItem(ResultSet rs) throws SQLException {
        return new QueueItem(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getString("genre_name"),
                rs.getInt("energy"),
                TrackSource.valueOf(rs.getString("source_type")),
                rs.getString("audio_path"),
                rs.getString("youtube_url"),
                rs.getString("cover_path"),
                rs.getLong("suggested_by"),
                rs.getString("username"),
                TrackStatus.valueOf(rs.getString("status")),
                rs.getObject("added_at", LocalDateTime.class),
                rs.getInt("rating"),
                rs.getInt("my_vote")
        );
    }
}
