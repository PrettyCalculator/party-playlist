package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.service.QueueService;
import com.partyplaylist.service.TrackService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class AbstractPartyActionServlet extends BaseServlet {
    protected PartyService partyService;
    protected QueueService queueService;
    protected TrackService trackService;

    @Override
    public void init() {
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
        queueService = (QueueService) getServletContext().getAttribute(ContextKeys.QUEUE_SERVICE);
        trackService = (TrackService) getServletContext().getAttribute(ContextKeys.TRACK_SERVICE);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Long userId = getUserId(req);
        Long partyId = getId(req, "partyId");
        try {
            String message = handle(req, partyId, userId);
            if (message != null) {
                flash(req, message);
            }
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
        } catch (ValidationException e) {
            flashError(req, e.getErrors().values().iterator().next());
        }
        redirect(req, resp, "/party?id=" + partyId);
    }

    protected abstract String handle(HttpServletRequest req, Long partyId, Long userId);
}
