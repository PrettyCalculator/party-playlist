package com.partyplaylist.servlet.party;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/finish")
public class PartyFinishServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        partyService.finish(partyId, userId);
        return "Вечеринка завершена";
    }
}
