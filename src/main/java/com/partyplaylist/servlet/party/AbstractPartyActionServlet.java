package com.partyplaylist.servlet.party;

import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.service.QueueService;
import com.partyplaylist.service.TrackService;
import com.partyplaylist.util.ContextKeys;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public abstract class AbstractPartyActionServlet extends HttpServlet {
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
        Long userId = WebUtils.getUserId(req);
        Long partyId = WebUtils.getId(req, "partyId");
        try {
            String message = handle(req, partyId, userId);
            if (message != null) {
                WebUtils.flash(req, message);
            }
        } catch (BusinessException e) {
            WebUtils.flashError(req, e.getMessage());
        } catch (ValidationException e) {
            WebUtils.flashError(req, e.getErrors().values().iterator().next());
        }
        WebUtils.redirect(req, resp, "/party?id=" + partyId);
    }

    protected abstract String handle(HttpServletRequest req, Long partyId, Long userId);
}
