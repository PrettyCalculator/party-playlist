package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.PartyMemberDao;
import com.partyplaylist.model.PartyMember;
import com.partyplaylist.model.enums.MemberRole;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPartyMemberDao implements PartyMemberDao {

    private static final String INSERT =
            "INSERT INTO party_members (party_id, user_id, role) VALUES (?, ?, ?)";
    private static final String FIND =
            "SELECT party_id, user_id, role, joined_at FROM party_members WHERE party_id = ? AND user_id = ?";
    private static final String FIND_BY_PARTY =
            "SELECT party_id, user_id, role, joined_at FROM party_members WHERE party_id = ? ORDER BY joined_at";
    private static final String DELETE =
            "DELETE FROM party_members WHERE party_id = ? AND user_id = ?";

    private final DataSource dataSource;

    public JdbcPartyMemberDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void add(PartyMember member) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT)) {
            ps.setLong(1, member.partyId());
            ps.setLong(2, member.userId());
            ps.setString(3, member.role().name());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось добавить участника в вечеринку " + member.partyId(), e);
        }
    }

    @Override
    public Optional<PartyMember> find(Long partyId, Long userId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND)) {
            ps.setLong(1, partyId);
            ps.setLong(2, userId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти участника вечеринки " + partyId, e);
        }
    }

    @Override
    public List<PartyMember> findByParty(Long partyId) {
        List<PartyMember> members = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_PARTY)) {
            ps.setLong(1, partyId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    members.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить участников вечеринки " + partyId, e);
        }
        return members;
    }

    @Override
    public void remove(Long partyId, Long userId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, partyId);
            ps.setLong(2, userId);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось удалить участника из вечеринки " + partyId, e);
        }
    }

    private PartyMember mapRow(ResultSet rs) throws SQLException {
        return new PartyMember(
                rs.getLong("party_id"),
                rs.getLong("user_id"),
                MemberRole.valueOf(rs.getString("role")),
                rs.getObject("joined_at", LocalDateTime.class)
        );
    }
}
