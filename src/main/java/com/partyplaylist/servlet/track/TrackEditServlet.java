package com.partyplaylist.servlet.track;

import com.partyplaylist.dto.TrackForm;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Track;
import com.partyplaylist.util.WebUtils;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/party/track/edit")
@MultipartConfig
public class TrackEditServlet extends AbstractTrackFormServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = WebUtils.getUserId(req);
        Track track = trackService.findById(WebUtils.getId(req, "id"));
        try {
            trackService.getEditableTrack(track.getId(), userId);
        } catch (BusinessException e) {
            WebUtils.flashError(req, e.getMessage());
            WebUtils.redirect(req, resp, "/party?id=" + track.getPartyId());
            return;
        }

        TrackForm form = new TrackForm(
                track.getTitle(),
                track.getArtist(),
                track.getGenreId() == null ? "" : track.getGenreId().toString(),
                String.valueOf(track.getEnergy()),
                track.getSourceType().name(),
                track.getYoutubeUrl() == null ? "" : track.getYoutubeUrl()
        );
        showForm(req, resp, partyService.getById(track.getPartyId()), form, null, track);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = WebUtils.getUserId(req);
        Track track = trackService.findById(WebUtils.getId(req, "id"));
        TrackForm form = readForm(req);
        try {
            trackService.update(track.getId(), form, req.getPart("audio"), req.getPart("cover"), userId);
            WebUtils.flash(req, "Трек сохранён");
            WebUtils.redirect(req, resp, "/party?id=" + track.getPartyId());
        } catch (ValidationException e) {
            showForm(req, resp, partyService.getById(track.getPartyId()), form, e.getErrors(), track);
        } catch (BusinessException e) {
            WebUtils.flashError(req, e.getMessage());
            WebUtils.redirect(req, resp, "/party?id=" + track.getPartyId());
        }
    }
}
