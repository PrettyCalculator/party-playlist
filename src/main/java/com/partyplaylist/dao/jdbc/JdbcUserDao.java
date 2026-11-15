package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.UserDao;
import com.partyplaylist.model.User;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.Optional;

public class JdbcUserDao implements UserDao {

    private static final String INSERT =
            "INSERT INTO users (username, password_hash) VALUES (?, ?)";
    private static final String FIND_BY_ID =
            "SELECT id, username, password_hash, created_at FROM users WHERE id = ?";
    private static final String FIND_BY_USERNAME =
            "SELECT id, username, password_hash, created_at FROM users WHERE username = ?";
    private static final String EXISTS_BY_USERNAME =
            "SELECT 1 FROM users WHERE username = ?";

    private final DataSource dataSource;

    public JdbcUserDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(User user) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPasswordHash());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    user.setId(keys.getLong("id"));
                    user.setCreatedAt(keys.getObject("created_at", LocalDateTime.class));
                }
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось сохранить пользователя " + user.getUsername(), e);
        }
    }

    @Override
    public Optional<User> findById(Long id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти пользователя с id " + id, e);
        }
    }

    @Override
    public Optional<User> findByUsername(String username) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти пользователя " + username, e);
        }
    }

    @Override
    public boolean existsByUsername(String username) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(EXISTS_BY_USERNAME)) {
            ps.setString(1, username);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось проверить пользователя " + username, e);
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getLong("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return user;
    }
}
