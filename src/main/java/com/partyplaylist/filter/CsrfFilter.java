package com.partyplaylist.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter(filterName = "csrfFilter")
public class CsrfFilter extends HttpFilter {

    public static final String SESSION_TOKEN = "csrfToken";
    public static final String PARAMETER = "_csrf";

    private final SecureRandom random = new SecureRandom();

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpSession session = req.getSession();
        String token = (String) session.getAttribute(SESSION_TOKEN);
        if (token == null) {
            token = generateToken();
            session.setAttribute(SESSION_TOKEN, token);
        }

        if ("POST".equals(req.getMethod()) && !token.equals(req.getParameter(PARAMETER))) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        chain.doFilter(req, resp);
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
}
