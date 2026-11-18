package com.partyplaylist.servlet;

import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.model.User;
import com.partyplaylist.util.SessionKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public abstract class BaseServlet extends HttpServlet {

    protected Long getUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        User user = session == null ? null : (User) session.getAttribute(SessionKeys.USER);
        if (user == null) {
            throw new AccessDeniedException("Нужно войти на сайт");
        }
        return user.getId();
    }

    protected Long getId(HttpServletRequest req, String name) {
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

    protected void login(HttpServletRequest req, User user) {
        req.getSession().setAttribute(SessionKeys.USER, user);
        req.changeSessionId();
    }

    protected void flash(HttpServletRequest req, String message) {
        req.getSession().setAttribute(SessionKeys.FLASH, message);
    }

    protected void flashError(HttpServletRequest req, String message) {
        req.getSession().setAttribute(SessionKeys.FLASH_ERROR, message);
    }

    protected void redirect(HttpServletRequest req, HttpServletResponse resp, String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }

    protected void forward(HttpServletRequest req, HttpServletResponse resp, String view)
            throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/" + view + ".jsp").forward(req, resp);
    }
}
