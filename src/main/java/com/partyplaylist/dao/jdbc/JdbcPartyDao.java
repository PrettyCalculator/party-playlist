package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.PartyDao;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.enums.MemberRole;
import com.partyplaylist.model.enums.PartyPhase;
import com.partyplaylist.model.enums.PartyStatus;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPartyDao implements PartyDao {

    private static final String COLUMNS =
            "p.id, p.host_id, p.title, p.join_code, p.phase, p.status, " +
            "p.max_suggestions_per_guest, p.no_artist_repeat, p.created_at";

    private static final String INSERT_PARTY =
            "INSERT INTO parties (host_id, title, join_code, phase, status, " +
            "max_suggestions_per_guest, no_artist_repeat) VALUES (?, ?, ?, ?, ?, ?, ?)";
    private static final String INSERT_HOST =
            "INSERT INTO party_members (party_id, user_id, role) VALUES (?, ?, ?)";
    private static final String UPDATE =
            "UPDATE parties SET title = ?, phase = ?, status = ?, " +
            "max_suggestions_per_guest = ?, no_artist_repeat = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM parties WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT " + COLUMNS + " FROM parties p WHERE p.id = ?";
    private static final String FIND_BY_JOIN_CODE = "SELECT " + COLUMNS + " FROM parties p WHERE p.join_code = ?";
    private static final String FIND_BY_MEMBER =
            "SELECT " + COLUMNS + " FROM parties p " +
            "JOIN party_members m ON m.party_id = p.id " +
            "WHERE m.user_id = ? ORDER BY p.created_at DESC";
    private static final String EXISTS_BY_JOIN_CODE = "SELECT 1 FROM parties WHERE join_code = ?";

    private final DataSource dataSource;

    public JdbcPartyDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Party party) {
        try (Connection con = dataSource.getConnection()) {
            con.setAutoCommit(false);
            try (PreparedStatement partyPs = con.prepareStatement(INSERT_PARTY, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement hostPs = con.prepareStatement(INSERT_HOST)) {
                partyPs.setLong(1, party.getHostId());
                partyPs.setString(2, party.getTitle());
                partyPs.setString(3, party.getJoinCode());
                partyPs.setString(4, party.getPhase().name());
                partyPs.setString(5, party.getStatus().name());
                partyPs.setInt(6, party.getMaxSuggestionsPerGuest());
                partyPs.setBoolean(7, party.isNoArtistRepeat());
                partyPs.executeUpdate();
                try (ResultSet keys = partyPs.getGeneratedKeys()) {
                    if (keys.next()) {
                        party.setId(keys.getLong("id"));
                        party.setCreatedAt(keys.getObject("created_at", LocalDateTime.class));
                    }
                }

                hostPs.setLong(1, party.getId());
                hostPs.setLong(2, party.getHostId());
                hostPs.setString(3, MemberRole.HOST.name());
                hostPs.executeUpdate();

                con.commit();
            } catch (SQLException e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось создать вечеринку " + party.getTitle(), e);
        }
    }

    @Override
    public void update(Party party) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(UPDATE)) {
            ps.setString(1, party.getTitle());
            ps.setString(2, party.getPhase().name());
            ps.setString(3, party.getStatus().name());
            ps.setInt(4, party.getMaxSuggestionsPerGuest());
            ps.setBoolean(5, party.isNoArtistRepeat());
            ps.setLong(6, party.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось обновить вечеринку с id " + party.getId(), e);
        }
    }

    @Override
    public void delete(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(DELETE)) {
            ps.setLong(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new DaoException("Не удалось удалить вечеринку с id " + id, e);
        }
    }

    @Override
    public Optional<Party> findById(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            return readOne(ps);
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти вечеринку с id " + id, e);
        }
    }

    @Override
    public Optional<Party> findByJoinCode(String joinCode) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_JOIN_CODE)) {
            ps.setString(1, joinCode);
            return readOne(ps);
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти вечеринку с кодом " + joinCode, e);
        }
    }

    @Override
    public List<Party> findByMember(Long userId) {
        List<Party> parties = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_MEMBER)) {
            ps.setLong(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    parties.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить вечеринки пользователя " + userId, e);
        }
        return parties;
    }

    @Override
    public boolean existsByJoinCode(String joinCode) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(EXISTS_BY_JOIN_CODE)) {
            ps.setString(1, joinCode);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось проверить код вечеринки " + joinCode, e);
        }
    }

    private Optional<Party> readOne(PreparedStatement ps) throws SQLException {
        try (ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return Optional.of(mapRow(rs));
            }
            return Optional.empty();
        }
    }

    private Party mapRow(ResultSet rs) throws SQLException {
        Party party = new Party();
        party.setId(rs.getLong("id"));
        party.setHostId(rs.getLong("host_id"));
        party.setTitle(rs.getString("title"));
        party.setJoinCode(rs.getString("join_code"));
        party.setPhase(PartyPhase.valueOf(rs.getString("phase")));
        party.setStatus(PartyStatus.valueOf(rs.getString("status")));
        party.setMaxSuggestionsPerGuest(rs.getInt("max_suggestions_per_guest"));
        party.setNoArtistRepeat(rs.getBoolean("no_artist_repeat"));
        party.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return party;
    }
}
