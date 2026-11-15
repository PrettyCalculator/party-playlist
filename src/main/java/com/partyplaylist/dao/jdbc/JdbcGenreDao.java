package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.GenreDao;
import com.partyplaylist.model.Genre;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcGenreDao implements GenreDao {

    private static final String FIND_ALL = "SELECT id, name FROM genres ORDER BY id";
    private static final String FIND_BY_ID = "SELECT id, name FROM genres WHERE id = ?";

    private final DataSource dataSource;

    public JdbcGenreDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public List<Genre> findAll() {
        List<Genre> genres = new ArrayList<>();
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                genres.add(mapRow(rs));
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить список жанров", e);
        }
        return genres;
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_ID)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти жанр с id " + id, e);
        }
    }

    private Genre mapRow(ResultSet rs) throws SQLException {
        return new Genre(rs.getInt("id"), rs.getString("name"));
    }
}
