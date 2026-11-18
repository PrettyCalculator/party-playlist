package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/edit")
public class PartyEditServlet extends BaseServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        Party party = partyService.getHostedParty(getId(req, "id"), userId);
        req.setAttribute("party", party);
        req.setAttribute("title", party.getTitle());
        req.setAttribute("maxSuggestions", String.valueOf(party.getMaxSuggestionsPerGuest()));
        forward(req, resp, "parties/form");
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        Long partyId = getId(req, "id");
        String title = req.getParameter("title");
        String maxSuggestions = req.getParameter("maxSuggestions");
        try {
            partyService.update(partyId, title, maxSuggestions, userId);
            flash(req, "Изменения сохранены");
            redirect(req, resp, "/party?id=" + partyId);
        } catch (ValidationException e) {
            req.setAttribute("party", partyService.getHostedParty(partyId, userId));
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("title", title);
            req.setAttribute("maxSuggestions", maxSuggestions);
            forward(req, resp, "parties/form");
        }
    }
}
