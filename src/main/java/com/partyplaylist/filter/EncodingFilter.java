package com.partyplaylist.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@WebFilter(filterName = "encodingFilter")
public class EncodingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        req.setCharacterEncoding(StandardCharsets.UTF_8.name());
        resp.setCharacterEncoding(StandardCharsets.UTF_8.name());
        chain.doFilter(req, resp);
    }
}
