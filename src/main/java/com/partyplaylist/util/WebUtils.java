package com.partyplaylist.util;

import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.model.User;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class WebUtils {
    public static final String USER = "user";
    public static final String FLASH = "flash";
    public static final String FLASH_ERROR = "flashError";

    private WebUtils() {
    }

    public static User getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) {
            return null;
        }
        return (User) session.getAttribute(USER);
    }

    public static Long getUserId(HttpServletRequest req) {
        User user = getUser(req);
        if (user == null) {
            throw new AccessDeniedException("Нужно войти на сайт");
        }
        return user.getId();
    }

    public static void login(HttpServletRequest req, User user) {
        req.getSession().setAttribute(USER, user);
        req.changeSessionId();
    }

    public static Long getId(HttpServletRequest req, String name) {
        String value = req.getParameter(name);
        if (value == null) {
            throw new NotFoundException("Страница не найдена");
        }
        try {
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            throw new NotFoundException("Страница не найдена");
        }
    }

    public static void flash(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH, message);
    }

    public static void flashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(FLASH_ERROR, message);
    }

    public static void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    public static void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }
}
