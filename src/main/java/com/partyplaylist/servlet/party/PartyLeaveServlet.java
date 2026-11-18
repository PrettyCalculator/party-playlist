package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/leave")
public class PartyLeaveServlet extends HttpServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = WebUtils.getUserId(req);
        Long partyId = WebUtils.getId(req, "id");
        try {
            partyService.leave(partyId, userId);
            WebUtils.flash(req, "Вы вышли из вечеринки");
            WebUtils.redirect(req, resp, "/parties");
        } catch (BusinessException e) {
            WebUtils.flashError(req, e.getMessage());
            WebUtils.redirect(req, resp, "/party?id=" + partyId);
        }
    }
}
