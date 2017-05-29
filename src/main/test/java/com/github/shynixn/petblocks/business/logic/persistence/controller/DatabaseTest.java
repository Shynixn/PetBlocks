package com.github.shynixn.petblocks.business.logic.persistence.controller;

import ch.vorburger.mariadb4j.DB;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.Assert;
import org.junit.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;

public class DatabaseTest {

    @Test
    public void enableDatabaseSQLiteTest() {
        try {
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("org.sqlite.JDBC");
            config.setConnectionTestQuery("SELECT 1");
            config.setJdbcUrl("jdbc:sqlite:PetBlocks.db");
            config.setMaxLifetime(60000);
            config.setIdleTimeout(45000);
            config.setMaximumPoolSize(50);
            HikariDataSource ds = new HikariDataSource(config);
            ds.close();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void enableDatabaseMySQLTest() {
        try {
            final DB database = DB.newEmbeddedDB(3306);
            database.start();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=")) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("CREATE DATABASE db");
                }
            }

            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setConnectionTestQuery("SELECT 1");
            config.setJdbcUrl("jdbc:mysql://localhost:3306/db");
            config.setMaxLifetime(60000);
            config.setIdleTimeout(45000);
            config.setMaximumPoolSize(50);
            HikariDataSource ds = new HikariDataSource(config);
            ds.close();
            database.stop();
        } catch (Exception ex) {
            ex.printStackTrace();
            Assert.fail();
        }
    }
}
