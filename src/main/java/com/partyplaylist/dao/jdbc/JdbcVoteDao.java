package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.VoteDao;
import com.partyplaylist.model.Vote;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcVoteDao implements VoteDao {

    private static final String FIND =
            "SELECT user_id, track_id, value, created_at FROM votes WHERE user_id = ? AND track_id = ?";
    private static final String UPSERT =
            "INSERT INTO votes (user_id, track_id, value) VALUES (?, ?, ?) " +
            "ON CONFLICT (user_id, track_id) DO UPDATE SET value = EXCLUDED.value, created_at = now()";
    private static final String DELETE =
            "DELETE FROM votes WHERE user_id = ? AND track_id = ?";

    private final DataSource dataSource;

    public JdbcVoteDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public Optional<Vote> find(Long userId, Long trackId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND)) {
            ps.setLong(1, userId);
            ps.setLong(2, trackId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(new Vote(
                            rs.getLong("user_id"),
                            rs.getLong("track_id"),
                            rs.getInt("value"),
                            rs.getObject("created_at", LocalDateTime.class)
                    ));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти голос за трек " + trackId, e);
        }
    }

    @Override
    public void vote(Vote vote) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPSERT)) {
            ps.setLong(1, vote.userId());
            ps.setLong(2, vote.trackId());
            ps.setInt(3, vote.value());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось сохранить голос за трек " + vote.trackId(), e);
        }
    }

    @Override
    public void removeVote(Long userId, Long trackId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, userId);
            ps.setLong(2, trackId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось удалить голос за трек " + trackId, e);
        }
    }
}
