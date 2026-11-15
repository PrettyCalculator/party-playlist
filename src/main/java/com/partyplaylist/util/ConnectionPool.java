package com.partyplaylist.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

public final class ConnectionPool {

    private ConnectionPool() {
    }

    public static HikariDataSource create(AppConfig config) {
        HikariConfig hc = new HikariConfig();
        hc.setDriverClassName("org.postgresql.Driver");
        hc.setJdbcUrl(config.get("db.url"));
        hc.setUsername(config.get("db.user"));
        hc.setPassword(config.get("db.password"));
        hc.setMaximumPoolSize(config.getInt("db.pool.size"));
        hc.setPoolName("party-playlist-pool");
        return new HikariDataSource(hc);
    }
}
