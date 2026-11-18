package com.partyplaylist.servlet.party;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/start")
public class PartyStartServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        partyService.start(partyId, userId);
        return "Вечеринка началась!";
    }
}
