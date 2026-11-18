package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/join")
public class PartyJoinServlet extends HttpServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = WebUtils.getUserId(req);
        String code = req.getParameter("code");
        try {
            Party party = partyService.join(code, userId);
            WebUtils.redirect(req, resp, "/party?id=" + party.getId());
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("code", code);
            req.setAttribute("parties", partyService.findByMember(userId));
            WebUtils.forward(req, resp, "parties/list");
        } catch (BusinessException e) {
            WebUtils.flashError(req, e.getMessage());
            WebUtils.redirect(req, resp, "/parties");
        }
    }
}
