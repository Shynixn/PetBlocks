@file:Suppress("unused")

package com.github.shynixn.petblocks.impl.listener

import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PetService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.event.player.PlayerToggleSneakEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetListener @Inject constructor(
    private val petService: PetService,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService
) : Listener {
    /**
     * Gets called when a player joins the server.
     */
    @EventHandler
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val pets = petService.getPetsFromPlayer(event.player)

        if (pets.isNotEmpty()) {
            plugin.logger.log(Level.INFO, "Loaded [${pets.size}] pets for player ${event.player.name}.")
        }
    }

    /**
     * Gets called when a player quits the server.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        val pets = petService.getPetsFromPlayer(event.player)
        petService.clearCache(event.player)

        if (pets.isNotEmpty()) {
            plugin.logger.log(Level.INFO, "Unloaded pets of player ${event.player.name}.")
        }
    }

    /**
     * Gets called when a player sneaks on the server.
     */
    @EventHandler
    suspend fun onPlayerSneakEvent(event: PlayerToggleSneakEvent) {
        val pets = petService.getPetsFromPlayer(event.player)
        pets.size
    }
}
