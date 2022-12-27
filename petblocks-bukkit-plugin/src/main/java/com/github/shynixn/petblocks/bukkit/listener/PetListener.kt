@file:Suppress("unused")

package com.github.shynixn.petblocks.bukkit.listener

import com.github.shynixn.petblocks.bukkit.service.PetService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetListener @Inject constructor(private val petService: PetService, private val plugin: Plugin) : Listener {
    /**
     * Gets called when a player joins the server.
     */
    @EventHandler
    suspend fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        val pets = petService.getPetsFromPlayer(event.player)

        if (pets.isNotEmpty()) {
            plugin.logger.log(Level.INFO, "Loaded pets for player ${event.player.name}.")
        }
    }

    /**
     * Gets called when a player quits the server.
     */
    @EventHandler
    suspend fun onPlayerQuitEvent(event: PlayerJoinEvent) {
        val pets = petService.getPetsFromPlayer(event.player)
        petService.clearCache(event.player)

        if (pets.isNotEmpty()) {
            plugin.logger.log(Level.INFO, "Unloaded pets for player ${event.player.name}.")
        }
    }
}
