package com.partyplaylist.servlet;

import com.partyplaylist.exception.NotFoundException;
import com.partyplaylist.model.Track;
import com.partyplaylist.service.FileStorageService;
import com.partyplaylist.service.TrackService;
import com.partyplaylist.util.ContextKeys;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@WebServlet("/media")
public class MediaServlet extends BaseServlet {
    private TrackService trackService;
    private FileStorageService fileStorage;

    @Override
    public void init() {
        trackService = (TrackService) getServletContext().getAttribute(ContextKeys.TRACK_SERVICE);
        fileStorage = (FileStorageService) getServletContext().getAttribute(ContextKeys.FILE_STORAGE_SERVICE);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        Track track = trackService.findById(getId(req, "track"));
        String path = "cover".equals(req.getParameter("type")) ? track.getCoverPath() : track.getAudioPath();
        if (path == null) {
            throw new NotFoundException("Файл не найден");
        }

        Path file = fileStorage.resolve(path);
        if (!Files.exists(file)) {
            throw new NotFoundException("Файл не найден");
        }

        String contentType = getServletContext().getMimeType(file.getFileName().toString());
        resp.setContentType(contentType == null ? "application/octet-stream" : contentType);
        resp.setContentLengthLong(Files.size(file));
        Files.copy(file, resp.getOutputStream());
    }
}
