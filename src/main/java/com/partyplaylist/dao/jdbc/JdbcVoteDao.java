package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.VoteDao;
import com.partyplaylist.model.Vote;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

public class JdbcVoteDao implements VoteDao {

    private static final String UPSERT =
            "INSERT INTO votes (user_id, party_track_id, value) VALUES (?, ?, ?) " +
            "ON CONFLICT (user_id, party_track_id) DO UPDATE SET value = EXCLUDED.value, created_at = now()";
    private static final String DELETE =
            "DELETE FROM votes WHERE user_id = ? AND party_track_id = ?";

    private final DataSource dataSource;

    public JdbcVoteDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void vote(Vote vote) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPSERT)) {
            ps.setLong(1, vote.userId());
            ps.setLong(2, vote.partyTrackId());
            ps.setInt(3, vote.value());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось сохранить голос за трек " + vote.partyTrackId(), e);
        }
    }

    @Override
    public void removeVote(Long userId, Long partyTrackId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, userId);
            ps.setLong(2, partyTrackId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось удалить голос за трек " + partyTrackId, e);
        }
    }
}
