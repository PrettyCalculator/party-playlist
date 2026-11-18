package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/vote")
public class PartyVoteServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        int value;
        try {
            value = Integer.parseInt(req.getParameter("value"));
        } catch (NumberFormatException e) {
            throw new ValidationException("vote", "Неверный голос");
        }
        queueService.vote(WebUtils.getId(req, "trackId"), userId, value);
        return null;
    }
}
