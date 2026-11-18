package com.partyplaylist.servlet.party;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/reject")
public class PartyRejectServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        queueService.reject(getId(req, "trackId"), userId);
        return "Трек отклонён";
    }
}
