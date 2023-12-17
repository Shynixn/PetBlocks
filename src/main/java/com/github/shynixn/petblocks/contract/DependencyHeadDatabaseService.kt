package com.github.shynixn.petblocks.contract

import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack

interface DependencyHeadDatabaseService : Listener {
    /**
     * Gets an itemStack from the given headDatabaseId.
     * Returns null if not found.
     */
    fun getItemStackFromId(headDatabaseId: String): ItemStack?

    /**
     * Registers the next click of the given player to apply the skin.
     */
    fun registerPlayerForNextClick(player: Player, petName: String)
}
