package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/new")
public class PartyCreateServlet extends HttpServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setAttribute("title", "");
        req.setAttribute("maxSuggestions", "5");
        WebUtils.forward(req, resp, "parties/form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = WebUtils.getUserId(req);
        String title = req.getParameter("title");
        String maxSuggestions = req.getParameter("maxSuggestions");
        try {
            Party party = partyService.create(title, maxSuggestions, userId);
            WebUtils.flash(req, "Вечеринка создана. Код для гостей: " + party.getJoinCode());
            WebUtils.redirect(req, resp, "/party?id=" + party.getId());
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("title", title);
            req.setAttribute("maxSuggestions", maxSuggestions);
            WebUtils.forward(req, resp, "parties/form");
        }
    }
}
