package com.partyplaylist.service;

import com.partyplaylist.exception.ServiceException;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

public class FileStorageService {

    private static final String AUDIO_FOLDER = "audio";
    private static final String COVER_FOLDER = "covers";

    private static final List<String> AUDIO_EXTENSIONS = List.of("mp3");
    private static final List<String> COVER_EXTENSIONS = List.of("jpg", "jpeg", "png");

    private final Path storageDir;

    public FileStorageService(Path storageDir) {
        this.storageDir = storageDir;
        try {
            Files.createDirectories(storageDir.resolve(AUDIO_FOLDER));
            Files.createDirectories(storageDir.resolve(COVER_FOLDER));
        } catch (IOException e) {
            throw new ServiceException("Не удалось создать папки хранилища в " + storageDir, e);
        }
    }

    public boolean hasFile(Part part) {
        return part != null && part.getSize() > 0;
    }

    public boolean isValidAudio(Part part) {
        return AUDIO_EXTENSIONS.contains(getExtension(part));
    }

    public boolean isValidCover(Part part) {
        return COVER_EXTENSIONS.contains(getExtension(part));
    }

    public String saveAudio(Part part) {
        return save(part, AUDIO_FOLDER);
    }

    public String saveCover(Part part) {
        return save(part, COVER_FOLDER);
    }

    public Path resolve(String path) {
        return storageDir.resolve(path);
    }

    public void delete(String path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(resolve(path));
        } catch (IOException e) {
            throw new ServiceException("Не удалось удалить файл " + path, e);
        }
    }

    private String save(Part part, String folder) {
        String fileName = UUID.randomUUID() + "." + getExtension(part);
        String path = folder + "/" + fileName;
        try (InputStream in = part.getInputStream()) {
            Files.copy(in, resolve(path));
        } catch (IOException e) {
            throw new ServiceException("Не удалось сохранить файл", e);
        }
        return path;
    }

    private String getExtension(Part part) {
        String name = part.getSubmittedFileName();
        if (name == null || !name.contains(".")) {
            return "";
        }
        return name.substring(name.lastIndexOf('.') + 1).toLowerCase();
    }
}
