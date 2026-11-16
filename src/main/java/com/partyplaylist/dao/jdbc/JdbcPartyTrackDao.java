package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.PartyTrackDao;
import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.model.PartyTrack;
import com.partyplaylist.model.enums.PartyTrackStatus;
import com.partyplaylist.model.enums.TrackSource;

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

public class JdbcPartyTrackDao implements PartyTrackDao {

    private static final String INSERT =
            "INSERT INTO party_tracks (party_id, track_id, suggested_by, status) VALUES (?, ?, ?, ?)";
    private static final String FIND_BY_ID =
            "SELECT id, party_id, track_id, suggested_by, status, added_at, played_at " +
            "FROM party_tracks WHERE id = ?";
    private static final String EXISTS =
            "SELECT 1 FROM party_tracks WHERE party_id = ? AND track_id = ?";
    private static final String UPDATE_STATUS =
            "UPDATE party_tracks SET status = ? WHERE id = ?";
    private static final String UPDATE_STATUS_PLAYED =
            "UPDATE party_tracks SET status = ?, played_at = now() WHERE id = ?";
    private static final String COUNT_SUGGESTED =
            "SELECT COUNT(*) FROM party_tracks WHERE party_id = ? AND suggested_by = ?";

    private static final String SELECT_ITEMS =
            "SELECT pt.id, pt.track_id, t.title, t.artist, t.energy, t.source_type, " +
            "t.audio_path, t.youtube_url, t.cover_path, u.username, pt.status, pt.added_at, " +
            "COALESCE(SUM(v.value), 0) AS rating, " +
            "COALESCE(MAX(CASE WHEN v.user_id = ? THEN v.value END), 0) AS my_vote " +
            "FROM party_tracks pt " +
            "JOIN tracks t ON t.id = pt.track_id " +
            "JOIN users u ON u.id = pt.suggested_by " +
            "LEFT JOIN votes v ON v.party_track_id = pt.id " +
            "WHERE pt.party_id = ? AND pt.status = ? " +
            "GROUP BY pt.id, t.id, u.username ";

    private static final String FIND_QUEUE = SELECT_ITEMS + "ORDER BY rating DESC, pt.added_at";
    private static final String FIND_CURRENT = SELECT_ITEMS + "LIMIT 1";

    private final DataSource dataSource;

    public JdbcPartyTrackDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(PartyTrack partyTrack) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, partyTrack.getPartyId());
            ps.setLong(2, partyTrack.getTrackId());
            ps.setLong(3, partyTrack.getSuggestedBy());
            ps.setString(4, partyTrack.getStatus().name());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    partyTrack.setId(keys.getLong("id"));
                    partyTrack.setAddedAt(keys.getObject("added_at", LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось добавить трек в очередь вечеринки " + partyTrack.getPartyId(), e);
        }
    }

    @Override
    public Optional<PartyTrack> findById(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapPartyTrack(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти трек в очереди с id " + id, e);
        }
    }

    @Override
    public boolean exists(Long partyId, Long trackId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(EXISTS)) {
            ps.setLong(1, partyId);
            ps.setLong(2, trackId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось проверить трек в очереди вечеринки " + partyId, e);
        }
    }

    @Override
    public List<QueueItem> findQueue(Long partyId, Long userId) {
        return findItems(FIND_QUEUE, partyId, userId, PartyTrackStatus.QUEUED);
    }

    @Override
    public Optional<QueueItem> findCurrent(Long partyId) {
        List<QueueItem> items = findItems(FIND_CURRENT, partyId, null, PartyTrackStatus.PLAYING);
        if (items.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(items.get(0));
    }

    @Override
    public void updateStatus(Long id, PartyTrackStatus status) {
        String sql = status == PartyTrackStatus.PLAYED ? UPDATE_STATUS_PLAYED : UPDATE_STATUS;
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, status.name());
            ps.setLong(2, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось обновить статус трека в очереди с id " + id, e);
        }
    }

    @Override
    public void switchCurrent(Long currentId, Long nextId) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement playedPs = con.prepareStatement(UPDATE_STATUS_PLAYED);
                 PreparedStatement playingPs = con.prepareStatement(UPDATE_STATUS)) {
                if (currentId != null) {
                    playedPs.setString(1, PartyTrackStatus.PLAYED.name());
                    playedPs.setLong(2, currentId);
                    playedPs.executeUpdate();
                }
                if (nextId != null) {
                    playingPs.setString(1, PartyTrackStatus.PLAYING.name());
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
            throw new DaoException("Не удалось посчитать предложенные треки в вечеринке " + partyId, e);
        }
    }

    private List<QueueItem> findItems(String sql, Long partyId, Long userId, PartyTrackStatus status) {
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
            throw new DaoException("Не удалось получить треки вечеринки " + partyId, e);
        }
        return items;
    }

    private PartyTrack mapPartyTrack(ResultSet rs) throws SQLException {
        PartyTrack partyTrack = new PartyTrack();
        partyTrack.setId(rs.getLong("id"));
        partyTrack.setPartyId(rs.getLong("party_id"));
        partyTrack.setTrackId(rs.getLong("track_id"));
        partyTrack.setSuggestedBy(rs.getLong("suggested_by"));
        partyTrack.setStatus(PartyTrackStatus.valueOf(rs.getString("status")));
        partyTrack.setAddedAt(rs.getObject("added_at", LocalDateTime.class));
        partyTrack.setPlayedAt(rs.getObject("played_at", LocalDateTime.class));
        return partyTrack;
    }

    private QueueItem mapQueueItem(ResultSet rs) throws SQLException {
        return new QueueItem(
                rs.getLong("id"),
                rs.getLong("track_id"),
                rs.getString("title"),
                rs.getString("artist"),
                rs.getInt("energy"),
                TrackSource.valueOf(rs.getString("source_type")),
                rs.getString("audio_path"),
                rs.getString("youtube_url"),
                rs.getString("cover_path"),
                rs.getString("username"),
                PartyTrackStatus.valueOf(rs.getString("status")),
                rs.getObject("added_at", LocalDateTime.class),
                rs.getInt("rating"),
                rs.getInt("my_vote")
        );
    }
}
