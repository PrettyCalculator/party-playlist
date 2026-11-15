package com.partyplaylist.dao;

import com.partyplaylist.model.Track;

import java.util.List;
import java.util.Optional;

public interface TrackDao {

    void save(Track track);

    void update(Track track);

    void delete(Long id);

    Optional<Track> findById(Long id);

    List<Track> findAll();

    List<Track> findByUploader(Long userId);

    List<Track> search(String query, Integer genreId);
}
