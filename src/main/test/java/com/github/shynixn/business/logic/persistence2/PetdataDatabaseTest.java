package com.github.shynixn.business.logic.persistence2;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import com.github.shynixn.petblocks.business.logic.persistence2.Factory;
import com.github.shynixn.petblocks.business.logic.persistence2.IPetDataController;
import com.github.shynixn.petblocks.business.logic.persistence2.PetData;
import com.github.shynixn.petblocks.business.logic.persistence2.PlayerData;
import com.github.shynixn.petblocks.lib.util.IController;
import com.github.shynixn.petblocks.lib.util.IDatabaseController;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by Shynixn
 */
public class PetdataDatabaseTest {

    private static Plugin mockPlugin() {
        YamlConfiguration configuration = new YamlConfiguration();
        configuration.set("sql.local", true);
        configuration.set("sql.host", "localhost");
        configuration.set("sql.port", 3306);
        configuration.set("sql.database", "db");
        configuration.set("sql.username", "root");
        configuration.set("sql.password", "");
        Plugin plugin = mock(Plugin.class);
        when(plugin.getDataFolder()).thenReturn(new File("PetBlocks"));
        when(plugin.getConfig()).thenReturn(configuration);
        when(plugin.getResource(any(String.class))).thenAnswer(invocationOnMock -> {
            String file = invocationOnMock.getArgument(0);
            return Thread.currentThread().getContextClassLoader().getResourceAsStream(file);
        });
        return plugin;
    }

    @BeforeClass
    public static void startMariaDB() {
        try {
            System.out.println("TRY");
            final DB database = DB.newEmbeddedDB(3306);
            database.start();
            try (Connection conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=")) {
                try (Statement statement = conn.createStatement()) {
                    statement.executeUpdate("CREATE DATABASE db");
                }
            }
            System.out.println("STARTED");
        } catch (SQLException | ManagedProcessException e) {
            e.printStackTrace();
        }
    }

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
            HikariConfig config = new HikariConfig();
            config.setDriverClassName("com.mysql.jdbc.Driver");
            config.setConnectionTestQuery("SELECT 1");
            config.setJdbcUrl("jdbc:mysql://localhost:3306/db");
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
    public void insertAndSelectPetDataSQLiteTest() throws ClassNotFoundException {
        Factory.enable(mockPlugin());
        final PetData petData = new PetData();
        petData.setName("Sample");
        try (IController<PetData> petDataIController = Factory.createPetDataController()) {
            for (final PetData pet : petDataIController.getAll()) {
                petDataIController.remove(pet);
            }
            try (IController<PlayerData> playerDataIController = Factory.createPlayerDataController()) {
                for (PlayerData playerData : playerDataIController.getAll()) {
                    playerDataIController.remove(playerData);
                }
                Assert.assertEquals(playerDataIController.size(), 0);
                PlayerData playerData = new PlayerData();
                playerData.setName("Christoph");
                playerData.setUuid(UUID.randomUUID());
                playerDataIController.store(playerData);
                petData.setPlayerId(playerData.getId());
                Assert.assertEquals(playerDataIController.size(), 1);
            }
            petDataIController.store(petData);
            final List<PetData> petDataList = petDataIController.getAll();
            Assert.assertEquals(petDataList.size(), 1);
            Assert.assertEquals(petDataList.get(0).getName(), petData.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void insertAndSelectPetDataMySQLTest() throws ClassNotFoundException {
        Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.local", false);
        Factory.enable(plugin);
        final PetData petData = new PetData();
        petData.setName("Sample");
        try (IController<PetData> petDataIController = Factory.createPetDataController()) {
            try (IController<PlayerData> playerDataIController = Factory.createPlayerDataController()) {
                PlayerData playerData = new PlayerData();
                playerData.setName("Christoph");
                playerData.setUuid(UUID.randomUUID());
                playerDataIController.store(playerData);
                petData.setPlayerId(playerData.getId());
            }
            for (final PetData pet : petDataIController.getAll()) {
                petDataIController.remove(pet);
            }
            petDataIController.store(petData);
            final List<PetData> petDataList = petDataIController.getAll();
            Assert.assertEquals(petDataList.size(), 1);
            Assert.assertEquals(petDataList.get(0).getName(), petData.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void insertAndUpdatePetDataMySQLTest() throws ClassNotFoundException {
        Plugin plugin = mockPlugin();
        plugin.getConfig().set("sql.local", false);
        Factory.enable(plugin);
        final PetData petData = new PetData();
        petData.setName("Eduard");
        try (IDatabaseController<PetData> petDataIController = Factory.createPetDataController()) {
            try (IController<PlayerData> playerDataIController = Factory.createPlayerDataController()) {
                PlayerData playerData = new PlayerData();
                playerData.setName("Christoph");
                playerData.setUuid(UUID.randomUUID());
                playerDataIController.store(playerData);
                petData.setPlayerId(playerData.getId());
            }
            for (final PetData pet : petDataIController.getAll()) {
                petDataIController.remove(pet);
            }
            petDataIController.store(petData);
            Assert.assertEquals(petDataIController.getById(petData.getId()).getName(), petData.getName());
            petData.setName("Franklin");
            petDataIController.store(petData);
            Assert.assertEquals(petDataIController.getById(petData.getId()).getName(), petData.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }

    @Test
    public void insertAndUpdatePetDataSQLiteTest() throws ClassNotFoundException {
        Factory.enable(mockPlugin());
        final PetData petData = new PetData();
        petData.setName("Eduard");
        try (IDatabaseController<PetData> petDataIController = Factory.createPetDataController()) {
            try (IController<PlayerData> playerDataIController = Factory.createPlayerDataController()) {
                PlayerData playerData = new PlayerData();
                playerData.setName("Christoph");
                playerData.setUuid(UUID.randomUUID());
                playerDataIController.store(playerData);
                petData.setPlayerId(playerData.getId());
            }
            for (final PetData pet : petDataIController.getAll()) {
                petDataIController.remove(pet);
            }
            petDataIController.store(petData);
            Assert.assertEquals(petDataIController.getById(petData.getId()).getName(), petData.getName());
            petData.setName("Franklin");
            petDataIController.store(petData);
            Assert.assertEquals(petDataIController.getById(petData.getId()).getName(), petData.getName());
        } catch (Exception e) {
            e.printStackTrace();
            Assert.fail();
        }
    }
}
