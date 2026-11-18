package com.partyplaylist.servlet.auth;

import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.User;
import com.partyplaylist.service.UserService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends BaseServlet {
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute(ContextKeys.USER_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        forward(req, resp, "auth/login");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            User user = userService.login(username, password);
            login(req, user);
            redirect(req, resp, "/parties");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("username", username);
            forward(req, resp, "auth/login");
        }
    }
}
