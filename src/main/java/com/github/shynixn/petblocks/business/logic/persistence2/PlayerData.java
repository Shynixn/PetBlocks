package com.github.shynixn.petblocks.business.logic.persistence2;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Created by Shynixn
 */
public class PlayerData {

    private long id;
    private String name;
    private UUID uuid;

    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getUUID() {
        return this.uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(this.uuid);
    }

    public static PlayerData from(Player player) {
        PlayerData playerStats = new PlayerData();
        playerStats.setName(player.getName());
        playerStats.setUuid(player.getUniqueId());
        return playerStats;
    }

    public static PlayerData from(ResultSet resultSet) throws SQLException {
        PlayerData playerStats = new PlayerData();
        playerStats.id = resultSet.getLong("id");
        playerStats.name = resultSet.getString("name");
        playerStats.uuid = UUID.fromString(resultSet.getString("uuid"));
        return playerStats;
    }
}
