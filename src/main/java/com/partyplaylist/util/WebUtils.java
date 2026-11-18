package com.partyplaylist.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;

public final class WebUtils {

    public static final String USER_SESSION_ATTR = "user";

    private WebUtils() {}


    public static Object getUser(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : session.getAttribute(USER_SESSION_ATTR);
    }

    public static void setUser(HttpServletRequest req, Object user) {
        if (user == null) {
            HttpSession session = req.getSession(false);
            if (session != null) {
                session.removeAttribute(USER_SESSION_ATTR);
            }
        } else {
            req.getSession(true).setAttribute(USER_SESSION_ATTR, user);
        }
    }


    public static void redirect(HttpServletRequest req,
                                HttpServletResponse resp,
                                String path) throws IOException {
        resp.sendRedirect(req.getContextPath() + path);
    }
}