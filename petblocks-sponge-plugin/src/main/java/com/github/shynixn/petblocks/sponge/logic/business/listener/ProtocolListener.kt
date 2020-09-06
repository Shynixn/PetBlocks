package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.ProtocolService
import com.google.inject.Inject
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.network.ClientConnectionEvent

class ProtocolListener @Inject constructor(private val protocolService: ProtocolService) {
    /**
     * Gets called when a player joins the server.
     * Registers the player for packet watching.
     */
    @Listener
    fun onPlayerJoinEvent(event: ClientConnectionEvent.Join) {
        protocolService.registerPlayer(event.targetEntity)
    }

    /**
     * Gets called when a player quits the server.
     * UnRegisters the player for packet watching.
     */
    @Listener
    fun onPlayerQuitEvent(event: ClientConnectionEvent.Disconnect) {
        protocolService.unRegisterPlayer(event.targetEntity)
    }
}
