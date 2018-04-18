package com.github.shynixn.petblocks.core.logic.persistence.entity;

import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;

import java.util.UUID;

public abstract class PlayerData extends PersistenceObject implements PlayerMeta {
    private String name;
    private UUID uuid;

    /**
     * Returns the name of the playerData
     *
     * @return playerData
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * Sets the name of the playerData
     *
     * @param name name
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the uuid of the playerData
     *
     * @return uuid
     */
    @Override
    public UUID getUUID() {
        return this.uuid;
    }

    /**
     * Sets the uuid of the playerData
     *
     * @param uuid uuid
     */
    @Override
    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
