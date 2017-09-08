package com.github.shynixn.petblocks.business.logic.persistence;

import com.github.shynixn.petblocks.api.persistence.controller.IFileController;
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.controller.PlayerMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.persistence.controller.EngineRepository;
import com.github.shynixn.petblocks.business.logic.persistence.controller.ParticleEffectDataRepository;
import com.github.shynixn.petblocks.business.logic.persistence.controller.PetDataRepository;
import com.github.shynixn.petblocks.business.logic.persistence.controller.PlayerDataRepository;
import com.github.shynixn.petblocks.lib.ExtensionHikariConnectionContext;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.regex.Pattern;

public class Factory {

    private static ExtensionHikariConnectionContext connectionContext;

    public static PlayerMetaController createPlayerDataController() {
        return new PlayerDataRepository(connectionContext);
    }

    public static ParticleEffectMetaController createParticleEffectController() {
        return new ParticleEffectDataRepository(connectionContext);
    }

    public static IFileController<EngineContainer> createEngineController() {
        return new EngineRepository(JavaPlugin.getPlugin(PetBlocksPlugin.class));
    }

    public static PetMetaController createPetDataController() {
        return new PetDataRepository(connectionContext);
    }

    public static void disable() {
        if (connectionContext == null)
            return;
        connectionContext.close();
        connectionContext = null;
    }

    public synchronized static void initialize(Plugin plugin) {
        if (connectionContext != null)
            return;
        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be null!");
        final ExtensionHikariConnectionContext.SQlRetriever retriever = fileName -> {
            try (InputStream stream = plugin.getResource("sql/" + fileName + ".sql")) {
                return IOUtils.toString(stream, "UTF-8");
            } catch (final IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot read file.", fileName);
                throw new RuntimeException(e);
            }
        };
        if (!plugin.getConfig().getBoolean("sql.enabled")) {
            try {
                if (!plugin.getDataFolder().exists())
                    plugin.getDataFolder().mkdir();
                final File file = new File(plugin.getDataFolder(), "PetBlocks.db");
                if (!file.exists())
                    file.createNewFile();
                connectionContext = ExtensionHikariConnectionContext.from(ExtensionHikariConnectionContext.SQLITE_DRIVER, "jdbc:sqlite:" + file.getAbsolutePath(), retriever);
                try (Connection connection = connectionContext.getConnection()) {
                    connectionContext.execute("PRAGMA foreign_keys=ON", connection);
                }
            } catch (final SQLException e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot execute statement.", e);
            } catch (final IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot read file.", e);
            }
            try (Connection connection = connectionContext.getConnection()) {
                for (final String data : connectionContext.getStringFromFile("create-sqlite").split(Pattern.quote(";"))) {
                    connectionContext.executeUpdate(data, connection);
                }
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot execute creation.", e);
            }
        } else {
            final FileConfiguration c = plugin.getConfig();
            try {
                connectionContext = ExtensionHikariConnectionContext.from(ExtensionHikariConnectionContext.MYSQL_DRIVER, "jdbc:mysql://"
                        , c.getString("sql.host")
                        , c.getInt("sql.port")
                        , c.getString("sql.database")
                        , c.getString("sql.username")
                        , c.getString("sql.password")
                        , retriever);
            } catch (final IOException e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot connect to MySQL database!", e);
                Bukkit.getLogger().log(Level.WARNING, "Trying to connect to SQLite database....", e);
                connectionContext = null;
                plugin.getConfig().set("sql.enabled", false);
                Factory.initialize(plugin);
                return;
            }
            try (Connection connection = connectionContext.getConnection()) {
                for (final String data : connectionContext.getStringFromFile("create-mysql").split(Pattern.quote(";"))) {
                    connectionContext.executeUpdate(data, connection);
                }
            } catch (final Exception e) {
                Bukkit.getLogger().log(Level.WARNING, "Cannot execute creation.", e);
                Bukkit.getLogger().log(Level.WARNING, "Trying to connect to SQLite database....", e);
                connectionContext = null;
                plugin.getConfig().set("sql.enabled", false);
                Factory.initialize(plugin);
            }
        }
    }
}
