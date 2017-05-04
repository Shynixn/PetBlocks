package com.github.shynixn.petblocks.business.logic.persistence2;

import com.github.shynixn.petblocks.lib.util.DbConnectionContext;
import com.github.shynixn.petblocks.lib.util.IDatabaseController;
import com.github.shynixn.petblocks.lib.util.SQLProvider;
import org.apache.commons.io.IOUtils;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Pattern;

/**
 * Created by Shynixn
 */
public class Factory {

    private static DbConnectionContext connectionContext;
    private static SQLProvider sqlProvider;

    public static IPetDataController<PetData> createPetDataController() {
        if (sqlProvider == null)
            throw new IllegalArgumentException("Factory is disabled!");
        if (Bukkit.getServer() != null && Bukkit.getServer().isPrimaryThread())
            throw new IllegalArgumentException("Cannot open controller on main thread.");
        return new PetRepository(connectionContext, sqlProvider);
    }

    public static IDatabaseController<PlayerData> createPlayerDataController() {
        if (sqlProvider == null)
            throw new IllegalArgumentException("Factory is disabled!");
        if (Bukkit.getServer() != null && Bukkit.getServer().isPrimaryThread())
            throw new IllegalArgumentException("Cannot open controller on main thread.");
        return new PlayerDataRepository(connectionContext, sqlProvider);
    }

    public static synchronized void enable(final Plugin plugin) {
        if (sqlProvider != null)
            return;
        sqlProvider = new SQLProvider() {
            @Override
            protected String readStringFromFile(String fileName) throws IOException {
                System.out.println("FILE: " + "sql/" + fileName + ".sql");
                try (InputStream stream = plugin.getResource("sql/" + fileName + ".sql")) {
                    return IOUtils.toString(stream, "UTF-8");
                }
            }
        };
        if (plugin.getConfig().getBoolean("sql.local")) {
            try {
                System.out.println("DATAFOLDER: " + plugin.getDataFolder());
                if (!plugin.getDataFolder().exists())
                    plugin.getDataFolder().mkdir();
                File file = new File(plugin.getDataFolder(), "PetBlocks.db");
                if (!file.exists())
                    file.createNewFile();
                connectionContext = DbConnectionContext.from("org.sqlite.JDBC", "jdbc:sqlite:" + file.getAbsolutePath());
                try(Connection connection = connectionContext.getConnection())
                {
                    connectionContext.execute("PRAGMA foreign_keys=ON", connection);
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                try (Connection connection = connectionContext.getConnection()) {
                    for (String data : sqlProvider.getString("create-sqlite").split(Pattern.quote(";"))) {
                        connectionContext.executeUpdate(data, connection);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {

            FileConfiguration c = plugin.getConfig();
            try {
                connectionContext = DbConnectionContext.from("com.mysql.jdbc.Driver", "jdbc:mysql://"
                        , c.getString("sql.host")
                        , c.getInt("sql.port")
                        , c.getString("sql.database")
                        , c.getString("sql.username")
                        , c.getString("sql.password"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {

                try (Connection connection = connectionContext.getConnection()) {
                    connectionContext.executeUpdate(sqlProvider.getString("create-mysql"), connection);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

    }

    public static synchronized void disable() {
        if (connectionContext != null)
            connectionContext.close();
        if (sqlProvider != null)
            sqlProvider.clear();
        sqlProvider = null;
        connectionContext = null;
    }
}
