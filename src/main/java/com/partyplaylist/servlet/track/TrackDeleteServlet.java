package com.partyplaylist.servlet.track;

import com.partyplaylist.servlet.party.AbstractPartyActionServlet;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;

@WebServlet("/party/track/delete")
public class TrackDeleteServlet extends AbstractPartyActionServlet {

    @Override
    protected String handle(HttpServletRequest req, Long partyId, Long userId) {
        trackService.delete(getId(req, "trackId"), userId);
        return "Трек удалён";
    }
}
