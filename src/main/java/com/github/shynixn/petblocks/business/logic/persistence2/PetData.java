package com.github.shynixn.petblocks.business.logic.persistence2;

import org.bukkit.entity.Player;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Created by Shynixn
 */
public class PetData {

    private long id;
    private String name;
    private long playerId;

    public long getPlayerId() {
        return this.playerId;
    }

    public void setPlayerId(long playerId) {
        this.playerId = playerId;
    }

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


    public static PetData from(PlayerData playerData)
    {
        final PetData petData = new PetData();
        petData.setPlayerId(playerData.getId());
        return petData;
    }

    public static PetData from(ResultSet resultSet) throws SQLException {
        final PetData petData = new PetData();
        petData.setId(resultSet.getLong("id"));
        petData.setName(resultSet.getString("name"));
        return petData;
    }
}
