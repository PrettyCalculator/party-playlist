package com.partyplaylist.servlet.auth;

import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.User;
import com.partyplaylist.service.UserService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {
    private UserService userService;

    @Override
    public void init() {
        userService = (UserService) getServletContext().getAttribute(ContextKeys.USER_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebUtils.forward(req, resp, "auth/register");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter("username");
        String password = req.getParameter("password");
        try {
            User user = userService.register(username, password);
            WebUtils.login(req, user);
            WebUtils.flash(req, "Добро пожаловать, " + user.getUsername() + "!");
            WebUtils.redirect(req, resp, "/parties");
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("username", username);
            WebUtils.forward(req, resp, "auth/register");
        }
    }
}
