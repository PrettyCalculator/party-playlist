package com.partyplaylist.servlet.party;

import com.partyplaylist.service.PartyService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/delete")
public class PartyDeleteServlet extends HttpServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = WebUtils.getUserId(req);
        partyService.delete(WebUtils.getId(req, "id"), userId);
        WebUtils.flash(req, "Вечеринка удалена");
        WebUtils.redirect(req, resp, "/parties");
    }
}
