package com.partyplaylist.servlet.party;

import com.partyplaylist.service.PartyService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/delete")
public class PartyDeleteServlet extends BaseServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = getUserId(req);
        partyService.delete(getId(req, "id"), userId);
        flash(req, "Вечеринка удалена");
        redirect(req, resp, "/parties");
    }
}
