package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/leave")
public class PartyLeaveServlet extends BaseServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = getUserId(req);
        Long partyId = getId(req, "id");
        try {
            partyService.leave(partyId, userId);
            flash(req, "Вы вышли из вечеринки");
            redirect(req, resp, "/parties");
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
            redirect(req, resp, "/party?id=" + partyId);
        }
    }
}
