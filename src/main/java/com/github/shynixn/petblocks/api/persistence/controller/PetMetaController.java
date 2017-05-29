package com.github.shynixn.petblocks.api.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import org.bukkit.entity.Player;

/**
 * Created by Shynixn
 */
public interface PetMetaController extends IDatabaseController<PetMeta> {
    /**
     * Returns the petdata from the given player
     * @param player player
     * @return petData
     */
    PetMeta getByPlayer(Player player);

    /**
     * Checks if the player has got an entry in the database
     * @param player player
     * @return hasEntry
     */
    boolean hasEntry(Player player);
}
