package com.partyplaylist.listener;

import com.partyplaylist.dao.GenreDao;
import com.partyplaylist.dao.PartyDao;
import com.partyplaylist.dao.PartyMemberDao;
import com.partyplaylist.dao.TrackDao;
import com.partyplaylist.dao.UserDao;
import com.partyplaylist.dao.VoteDao;
import com.partyplaylist.dao.jdbc.JdbcGenreDao;
import com.partyplaylist.dao.jdbc.JdbcPartyDao;
import com.partyplaylist.dao.jdbc.JdbcPartyMemberDao;
import com.partyplaylist.dao.jdbc.JdbcTrackDao;
import com.partyplaylist.dao.jdbc.JdbcUserDao;
import com.partyplaylist.dao.jdbc.JdbcVoteDao;
import com.partyplaylist.service.FileStorageService;
import com.partyplaylist.service.PartyService;
import com.partyplaylist.service.QueueService;
import com.partyplaylist.service.TrackService;
import com.partyplaylist.service.UserService;
import com.partyplaylist.util.AppConfig;
import com.partyplaylist.util.ConnectionPool;
import com.partyplaylist.util.ContextKeys;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

@WebListener
public class AppContextListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext ctx = sce.getServletContext();
        AppConfig config = new AppConfig();

        HikariDataSource dataSource = ConnectionPool.create(config);
        checkConnection(dataSource);

        UserDao userDao = new JdbcUserDao(dataSource);
        GenreDao genreDao = new JdbcGenreDao(dataSource);
        TrackDao trackDao = new JdbcTrackDao(dataSource);
        PartyDao partyDao = new JdbcPartyDao(dataSource);
        PartyMemberDao partyMemberDao = new JdbcPartyMemberDao(dataSource);
        VoteDao voteDao = new JdbcVoteDao(dataSource);

        Path storageDir = Path.of(config.get("storage.dir")).toAbsolutePath();
        FileStorageService fileStorageService = new FileStorageService(storageDir);
        UserService userService = new UserService(userDao);
        PartyService partyService = new PartyService(partyDao, partyMemberDao, trackDao, fileStorageService);
        TrackService trackService = new TrackService(trackDao, genreDao, fileStorageService, partyService);
        QueueService queueService = new QueueService(trackDao, voteDao, partyService);

        ctx.setAttribute(ContextKeys.CONFIG, config);
        ctx.setAttribute(ContextKeys.DATA_SOURCE, dataSource);
        ctx.setAttribute(ContextKeys.USER_SERVICE, userService);
        ctx.setAttribute(ContextKeys.FILE_STORAGE_SERVICE, fileStorageService);
        ctx.setAttribute(ContextKeys.TRACK_SERVICE, trackService);
        ctx.setAttribute(ContextKeys.PARTY_SERVICE, partyService);
        ctx.setAttribute(ContextKeys.QUEUE_SERVICE, queueService);

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
        } catch (SQLException e) {
            dataSource.close();
            throw new IllegalStateException("Нет подключения к БД: " + e.getMessage(), e);
        }
    }
}
