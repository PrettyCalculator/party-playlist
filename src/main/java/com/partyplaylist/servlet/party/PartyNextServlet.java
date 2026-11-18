package com.partyplaylist.servlet.party;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/next")
public class PartyNextServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        if (queueService.next(partyId, userId).isEmpty()) {
            return "Очередь пуста, предложите треки";
        }
        return null;
    }
}
