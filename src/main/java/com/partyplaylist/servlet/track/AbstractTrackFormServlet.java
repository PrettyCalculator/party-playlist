package com.partyplaylist.servlet.track;

import com.partyplaylist.dto.TrackForm;
import com.partyplaylist.model.Party;
import com.partyplaylist.model.Track;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.service.TrackService;
import com.partyplaylist.servlet.BaseServlet;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.Map;

public abstract class AbstractTrackFormServlet extends BaseServlet {
    protected TrackService trackService;
    protected PartyService partyService;

    @Override
    public void init() {
        trackService = (TrackService) getServletContext().getAttribute(ContextKeys.TRACK_SERVICE);
        partyService = (PartyService) getServletContext().getAttribute(ContextKeys.PARTY_SERVICE);
    }

    protected TrackForm readForm(HttpServletRequest req) {
        return new TrackForm(
                req.getParameter("title"),
                req.getParameter("artist"),
                req.getParameter("genreId"),
                req.getParameter("energy"),
                req.getParameter("sourceType"),
                req.getParameter("youtubeUrl")
        );
    }

    protected void showForm(HttpServletRequest req, HttpServletResponse resp, Party party, TrackForm form,
                            Map<String, String> errors, Track track) throws ServletException, IOException {
        req.setAttribute("party", party);
        req.setAttribute("form", form);
        req.setAttribute("errors", errors);
        req.setAttribute("track", track);
        req.setAttribute("genres", trackService.getGenres());
        forward(req, resp, "tracks/form");
    }
}
