package com.partyplaylist.dao;

import com.partyplaylist.model.Genre;

import java.util.List;
import java.util.Optional;

public interface GenreDao {

    List<Genre> findAll();

    Optional<Genre> findById(Integer id);
}
