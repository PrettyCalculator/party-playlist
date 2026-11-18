package com.partyplaylist.servlet.party;

import com.partyplaylist.dto.QueueItem;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.enums.MemberRole;
import com.partyplaylist.model.enums.PartyPhase;
import com.partyplaylist.model.enums.TrackSource;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.service.QueueService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/party")
public class PartyViewServlet extends BaseServlet {
    private PartyService partyService;
    private QueueService queueService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
        queueService = (QueueService) getServletContext().getAttribute(ContextKeys.QUEUE_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        Long partyId = getId(req, "id");

        Party party = partyService.getById(partyId);
        MemberRole role = partyService.getRole(partyId, userId);
        QueueItem current = queueService.getCurrent(partyId, userId).orElse(null);

        req.setAttribute("party", party);
        req.setAttribute("isHost", role == MemberRole.HOST);
        req.setAttribute("queue", queueService.getQueue(partyId, userId));
        req.setAttribute("current", current);
        req.setAttribute("phases", PartyPhase.values());
        if (current != null && current.sourceType() == TrackSource.YOUTUBE) {
            req.setAttribute("youtubeId", current.youtubeId());
        }
        forward(req, resp, "party/view");
    }
}
