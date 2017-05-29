package com.github.shynixn.petblocks.api.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta;
import org.bukkit.entity.Player;

/**
 * Created by Shynixn
 */
public interface PlayerMetaController extends IDatabaseController<PlayerMeta> {

    /**
     * Creates a new playerData from the given player
     * @param player player
     * @return playerData
     */
    PlayerMeta create(Player player);
}
