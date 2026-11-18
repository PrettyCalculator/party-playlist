package com.partyplaylist.filter;

import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.NotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebFilter(filterName = "errorFilter")
public class ErrorFilter extends HttpFilter {

    @Override
    protected void doFilter(HttpServletRequest req, HttpServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        try {
            chain.doFilter(req, resp);
        } catch (NotFoundException e) {
            resp.sendError(HttpServletResponse.SC_NOT_FOUND);
        } catch (AccessDeniedException e) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN);
        }
    }
}
