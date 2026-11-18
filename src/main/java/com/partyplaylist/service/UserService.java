package com.partyplaylist.service;

import com.partyplaylist.dao.UserDao;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.exception.ServiceException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService {

    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]{3,32}$");

    private final UserDao userDao;

    public UserService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(String username, String password) {
        Map<String, String> errors = new HashMap<>();
        String name = username == null ? "" : username.trim();

        if (!USERNAME_PATTERN.matcher(name).matches()) {
            errors.put("username", "Логин: от 3 до 32 символов, латинские буквы, цифры и _");
        } else if (userDao.existsByUsername(name)) {
            errors.put("username", "Такой логин уже занят");
        }
        if (password == null || password.isEmpty()) {
            errors.put("password", "Введите пароль");
        }
        if (!errors.isEmpty()) {
            throw new ValidationException(errors);
        }

        User user = new User(name, hash(password));
        userDao.save(user);
        return user;
    }

    public User login(String username, String password) {
        if (username == null || password == null) {
            throw new ValidationException("login", "Неверный логин или пароль");
        }
        Optional<User> user = userDao.findByUsername(username.trim());
        if (user.isEmpty() || !user.get().getPasswordHash().equals(hash(password))) {
            throw new ValidationException("login", "Неверный логин или пароль");
        }
        return user.get();
    }

    public User findById(Long id) {
        return userDao.findById(id)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
    }

    private String hash(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ServiceException("Алгоритм SHA-256 недоступен", e);
        }
    }
}
