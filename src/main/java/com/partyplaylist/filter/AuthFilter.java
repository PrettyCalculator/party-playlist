package com.partyplaylist.filter;

import com.partyplaylist.util.SessionKeys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@WebFilter(filterName = "authFilter")
public class AuthFilter extends HttpFilter {

    private static final List<String> PUBLIC_PATHS = List.of("", "/", "/login", "/register");
    private static final List<String> GUEST_ONLY_PATHS = List.of("/login", "/register");

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        String path = req.getServletPath();
        HttpSession session = req.getSession(false);
        boolean loggedIn = session != null && session.getAttribute(SessionKeys.USER) != null;
        boolean publicPath = PUBLIC_PATHS.contains(path) || path.startsWith("/static/");

        if (loggedIn && GUEST_ONLY_PATHS.contains(path)) {
            resp.sendRedirect(req.getContextPath() + "/parties");
            return;
        }
        if (!loggedIn && !publicPath) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }
        chain.doFilter(req, resp);
    }
}
