package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/parties/join")
public class PartyJoinServlet extends BaseServlet {
    private PartyService partyService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        String code = req.getParameter("code");
        try {
            Party party = partyService.join(code, userId);
            redirect(req, resp, "/party?id=" + party.getId());
        } catch (ValidationException e) {
            req.setAttribute("errors", e.getErrors());
            req.setAttribute("code", code);
            req.setAttribute("parties", partyService.findByMember(userId));
            forward(req, resp, "parties/list");
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
            redirect(req, resp, "/parties");
        }
    }
}
