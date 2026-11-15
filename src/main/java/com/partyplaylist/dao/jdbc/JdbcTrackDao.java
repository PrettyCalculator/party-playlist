package com.partyplaylist.dao.jdbc;

import com.partyplaylist.dao.DaoException;
import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.model.Track;
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

public class JdbcTrackDao implements TrackDao {

    private static final String COLUMNS =
            "id, title, artist, genre_id, energy, duration_sec, source_type, " +
            "audio_path, youtube_url, cover_path, uploaded_by, created_at";

    private static final String INSERT =
            "INSERT INTO tracks (title, artist, genre_id, energy, duration_sec, source_type, " +
            "audio_path, youtube_url, cover_path, uploaded_by) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    private static final String UPDATE =
            "UPDATE tracks SET title = ?, artist = ?, genre_id = ?, energy = ?, duration_sec = ?, " +
            "source_type = ?, audio_path = ?, youtube_url = ?, cover_path = ? WHERE id = ?";
    private static final String DELETE = "DELETE FROM tracks WHERE id = ?";
    private static final String FIND_BY_ID = "SELECT " + COLUMNS + " FROM tracks WHERE id = ?";
    private static final String FIND_ALL = "SELECT " + COLUMNS + " FROM tracks ORDER BY created_at DESC";
    private static final String FIND_BY_UPLOADER =
            "SELECT " + COLUMNS + " FROM tracks WHERE uploaded_by = ? ORDER BY created_at DESC";

    private final DataSource dataSource;

    public JdbcTrackDao(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void save(Track track) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(INSERT, Statement.RETURN_GENERATED_KEYS)) {
            setTrackParams(ps, track);
            ps.setLong(10, track.getUploadedBy());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    track.setId(keys.getLong("id"));
                    track.setCreatedAt(keys.getObject("created_at", LocalDateTime.class));
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
            ps.setLong(10, track.getId());
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
                    return Optional.of(mapRow(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new DaoException("Не удалось найти трек с id " + id, e);
        }
    }

    @Override
    public List<Track> findAll() {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_ALL)) {
            return readList(ps);
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить список треков", e);
        }
    }

    @Override
    public List<Track> findByUploader(Long userId) {
        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(FIND_BY_UPLOADER)) {
            ps.setLong(1, userId);
            return readList(ps);
        } catch (SQLException e) {
            throw new DaoException("Не удалось получить треки пользователя " + userId, e);
        }
    }

    @Override
    public List<Track> search(String query, Integer genreId) {
        StringBuilder sql = new StringBuilder("SELECT " + COLUMNS + " FROM tracks WHERE 1 = 1");
        List<Object> params = new ArrayList<>();
        if (query != null && !query.isBlank()) {
            sql.append(" AND (title ILIKE ? OR artist ILIKE ?)");
            String pattern = "%" + query.trim() + "%";
            params.add(pattern);
            params.add(pattern);
        }
        if (genreId != null) {
            sql.append(" AND genre_id = ?");
            params.add(genreId);
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection con = dataSource.getConnection();
             PreparedStatement ps = con.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            return readList(ps);
        } catch (SQLException e) {
            throw new DaoException("Не удалось выполнить поиск треков", e);
        }
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
        if (track.getDurationSec() != null) {
            ps.setInt(5, track.getDurationSec());
        } else {
            ps.setNull(5, Types.INTEGER);
        }
        ps.setString(6, track.getSourceType().name());
        ps.setString(7, track.getAudioPath());
        ps.setString(8, track.getYoutubeUrl());
        ps.setString(9, track.getCoverPath());
    }

    private List<Track> readList(PreparedStatement ps) throws SQLException {
        List<Track> tracks = new ArrayList<>();
        try (ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                tracks.add(mapRow(rs));
            }
        }
        return tracks;
    }

    private Track mapRow(ResultSet rs) throws SQLException {
        Track track = new Track();
        track.setId(rs.getLong("id"));
        track.setTitle(rs.getString("title"));
        track.setArtist(rs.getString("artist"));
        track.setGenreId(rs.getObject("genre_id", Integer.class));
        track.setEnergy(rs.getInt("energy"));
        track.setDurationSec(rs.getObject("duration_sec", Integer.class));
        track.setSourceType(TrackSource.valueOf(rs.getString("source_type")));
        track.setAudioPath(rs.getString("audio_path"));
        track.setYoutubeUrl(rs.getString("youtube_url"));
        track.setCoverPath(rs.getString("cover_path"));
        track.setUploadedBy(rs.getLong("uploaded_by"));
        track.setCreatedAt(rs.getObject("created_at", LocalDateTime.class));
        return track;
    }
}
