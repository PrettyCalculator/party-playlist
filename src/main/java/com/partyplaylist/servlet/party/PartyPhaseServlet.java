package com.partyplaylist.servlet.party;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/phase")
public class PartyPhaseServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        partyService.changePhase(partyId, req.getParameter("phase"), userId);
        return "Фаза изменена";
    }
}
