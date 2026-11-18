package com.partyplaylist.servlet.track;

import com.partyplaylist.dto.TrackForm;
import com.partyplaylist.exception.AccessDeniedException;
import com.partyplaylist.exception.BusinessException;
import com.partyplaylist.exception.ValidationException;
import com.partyplaylist.model.Party;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/party/suggest")
@MultipartConfig
public class TrackSuggestServlet extends AbstractTrackFormServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        Party party = partyService.getById(getId(req, "partyId"));
        if (partyService.getRole(party.getId(), userId) == null) {
            throw new AccessDeniedException("Вы не участник этой вечеринки");
        }
        TrackForm empty = new TrackForm("", "", "", "3", "FILE", "");
        showForm(req, resp, party, empty, null, null);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        Long userId = getUserId(req);
        Long partyId = getId(req, "partyId");
        TrackForm form = readForm(req);
        try {
            trackService.suggest(partyId, form, req.getPart("audio"), req.getPart("cover"), userId);
            flash(req, "Трек добавлен в очередь");
            redirect(req, resp, "/party?id=" + partyId);
        } catch (ValidationException e) {
            showForm(req, resp, partyService.getById(partyId), form, e.getErrors(), null);
        } catch (BusinessException e) {
            flashError(req, e.getMessage());
            redirect(req, resp, "/party?id=" + partyId);
        }
    }
}
