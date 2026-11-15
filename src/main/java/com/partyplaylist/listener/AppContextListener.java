package com.partyplaylist.listener;

import com.partyplaylist.util.AppConfig;
import com.partyplaylist.util.ConnectionPool;
import com.partyplaylist.util.ContextKeys;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Точка сборки приложения: при старте создаёт пул соединений, папки файлового
 * хранилища и (на следующем шаге) DAO и сервисы, складывая их в ServletContext.
 * При остановке приложения закрывает пул.
 */
@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        AppConfig config = new AppConfig();

        HikariDataSource dataSource = ConnectionPool.create(config);
        checkConnection(dataSource);

        Path storageDir = Path.of(config.get("storage.dir")).toAbsolutePath();
        try {
            Files.createDirectories(storageDir.resolve("audio"));
            Files.createDirectories(storageDir.resolve("covers"));
        } catch (IOException e) {
            dataSource.close();
            throw new IllegalStateException("Не удалось создать хранилище " + storageDir, e);
        }

        ctx.setAttribute(ContextKeys.CONFIG, config);
        ctx.setAttribute(ContextKeys.DATA_SOURCE, dataSource);
        ctx.setAttribute(ContextKeys.STORAGE_DIR, storageDir);

        // TODO: создать DAO и сервисы и положить их в контекст

        ctx.log("Party Playlist запущен, хранилище: " + storageDir);
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        Object ds = sce.getServletContext().getAttribute(ContextKeys.DATA_SOURCE);
        if (ds instanceof HikariDataSource hikari) {
            hikari.close();
        }
    }

    private void checkConnection(HikariDataSource dataSource) {
        try (Connection ignored = dataSource.getConnection()) {
            // соединение открылось — БД доступна
        } catch (SQLException e) {
            dataSource.close();
            throw new IllegalStateException("Нет подключения к БД: " + e.getMessage(), e);
        }
    }
}
