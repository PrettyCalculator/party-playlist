package com.partyplaylist.dao;

import com.partyplaylist.model.User;

import java.util.Optional;

public interface UserDao {

    void save(User user);

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
}
