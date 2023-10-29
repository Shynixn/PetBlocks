@file:Suppress("unused")

package com.github.shynixn.petblocks.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.packet.api.InteractionType
import com.github.shynixn.mcutils.packet.api.event.PacketEvent
import com.github.shynixn.mcutils.packet.api.packet.PacketInInteractEntity
import com.github.shynixn.mcutils.packet.api.packet.PacketInSteerVehicle
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetListener @Inject constructor(
    private val petService: PetService,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val physicObjectService: PhysicObjectService
) : Listener {
    /**
     * Gets called when a player joins the server.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        plugin.launch {
            val pets = petService.getPetsFromPlayer(event.player)

            if (pets.isNotEmpty()) {
                plugin.logger.log(Level.INFO, "Loaded [${pets.size}] pets for player ${event.player.name}.")
            }
        }
    }

    /**
     * Gets called when a player quits the server.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        plugin.launch {
            val pets = petService.getPetsFromPlayer(event.player)
            petService.clearCache(event.player)

            if (pets.isNotEmpty()) {
                plugin.logger.log(Level.INFO, "Unloaded pets of player ${event.player.name}.")
            }
        }
    }

    @EventHandler
    fun onPacketEvent(event: PacketEvent) {
        val packet = event.packet

        if (packet is PacketInSteerVehicle) {
            val physicObject = physicObjectService.findPhysicObjectById(packet.entityId) as PetEntityImpl?
            physicObject?.ride(event.player, packet.forward, packet.isJumping)
            return
        }

        if (packet is PacketInInteractEntity) {
            val physicObject = physicObjectService.findPhysicObjectById(packet.entityId) as PetEntityImpl? ?: return

            if (packet.actionType == InteractionType.LEFT_CLICK) {
                physicObject.leftClick(event.player)
            } else if (packet.actionType == InteractionType.OTHER) {
                physicObject.rightClick(event.player)
            }
        }
    }
}
