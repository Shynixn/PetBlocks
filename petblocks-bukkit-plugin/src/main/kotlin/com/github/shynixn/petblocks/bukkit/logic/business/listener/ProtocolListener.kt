package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.ProtocolService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent

class ProtocolListener @Inject constructor(private val protocolService: ProtocolService) : Listener {
    /**
     * Gets called when a player joins the server.
     * Registers the player for packet watching.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        protocolService.registerPlayer(event.player)
    }

    /**
     * Gets called when a player quits the server.
     * UnRegisters the player for packet watching.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        protocolService.unRegisterPlayer(event.player)
    }
}
