package com.partyplaylist.servlet.track;

import com.partyplaylist.dto.TrackForm;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Track;
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
        Long userId = getUserId(req);
        Track track = trackService.findById(getId(req, "id"));
        try {
            trackService.getEditableTrack(track.getId(), userId);
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
            redirect(req, resp, "/party?id=" + track.getPartyId());
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
        Long userId = getUserId(req);
        Track track = trackService.findById(getId(req, "id"));
        TrackForm form = readForm(req);
        try {
            trackService.update(track.getId(), form, req.getPart("audio"), req.getPart("cover"), userId);
            flash(req, "Трек сохранён");
            redirect(req, resp, "/party?id=" + track.getPartyId());
        } catch (ValidationException e) {
            showForm(req, resp, partyService.getById(track.getPartyId()), form, e.getErrors(), track);
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
            redirect(req, resp, "/party?id=" + track.getPartyId());
        }
    }
}
