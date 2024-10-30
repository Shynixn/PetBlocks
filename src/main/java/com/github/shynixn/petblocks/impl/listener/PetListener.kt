@file:Suppress("unused")

package com.github.shynixn.petblocks.impl.listener

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.packet.api.event.PacketAsyncEvent
import com.github.shynixn.mcutils.packet.api.meta.enumeration.InteractionType
import com.github.shynixn.mcutils.packet.api.meta.enumeration.PacketVersion
import com.github.shynixn.mcutils.packet.api.packet.PacketInInteractEntity
import com.github.shynixn.mcutils.packet.api.packet.PacketInSteerVehicle
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.github.shynixn.petblocks.impl.service.PetActionExecutionServiceImpl
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.entity.Player
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
    private val petActionExecutionService: PetActionExecutionService,
    private val physicObjectService: PhysicObjectService,
    private val configurationService: ConfigurationService
) : Listener {
    private val petsToReceiveOnJoinKey = "pet.receivePetsOnJoin"
    private val steerVehicleCache = HashMap<Player, Int>()

    /**
     * Gets called when a player joins the server.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        plugin.launch {
            val player = event.player
            val pets = petService.getPetsFromPlayer(player)

            if (pets.isNotEmpty()) {
                plugin.logger.log(Level.FINE, "Loaded [${pets.size}] pets for player ${player.name}.")
            }

            // Apply pets from config
            val petsToReceive = configurationService.findValue<List<Map<String, String>>>(petsToReceiveOnJoinKey)

            for (petToReceive in petsToReceive) {
                val name = petToReceive["name"]
                val template = petToReceive["template"]

                val matchingPet = pets.firstOrNull { e -> e.name.equals(name, true) }

                if (matchingPet == null) {
                    Bukkit.getServer().dispatchCommand(
                        PetActionExecutionServiceImpl.PetBlocksCommandSender(Bukkit.getConsoleSender()),
                        "petblocks create ${name} ${template} ${player.name}"
                    )
                }
            }
        }
    }

    /**
     * Gets called when a player quits the server.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        plugin.launch {
            petService.clearCache(event.player)
        }
        steerVehicleCache.remove(event.player)
    }

    @EventHandler
    fun onPlayerDismountEvent(event: PlayerToggleSneakEvent) {
        // Compatibility to remount the pet on sneak in lower minecraft version.
        if (Version.serverVersion.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1)) {
            return
        }
        plugin.launch {
            val pets = petService.getPetsFromPlayer(event.player)

            for (pet in pets) {
                if (pet.isRiding()) {
                    delay(3.ticks)
                    pet.ride()
                }
            }
        }
    }

    @EventHandler
    fun onPacketEvent(event: PacketAsyncEvent) {
        val packet = event.packet

        if (packet is PacketInSteerVehicle) {
            val player = event.player

            if (packet.version == PacketVersion.V1) {
                plugin.launch {
                    val physicObject = physicObjectService.findPhysicObjectById(packet.entityId) as PetEntityImpl?
                    physicObject?.ride(player, packet.forward, packet.isJumping, packet.isShift)
                }
                return
            }

            if (packet.version == PacketVersion.V2) {
                plugin.launch {
                    val physicObject =
                        physicObjectService.findPhysicObjectById(packet.entityId) as PetEntityImpl? ?: return@launch

                    if (steerVehicleCache.containsKey(player)) {
                        val vehicleDirection = steerVehicleCache[player]

                        if (vehicleDirection == 1 && packet.isForwardMove) {
                            // Skip
                        } else if (vehicleDirection == 2 && packet.isForwardMove) {
                            // Skip
                        } else {
                            steerVehicleCache.remove(player)
                        }
                    } else {
                        if (packet.isForwardMove) {
                            steerVehicleCache[player] = 1
                            plugin.launch {
                                while (player.isOnline && steerVehicleCache.contains(player) && physicObject.pet.isRiding()) {
                                    physicObject.ride(player, 0.5, false, false)
                                    delay(5.ticks)
                                }
                            }
                        } else if (packet.isBackWardMove) {
                            steerVehicleCache[player] = 2
                            plugin.launch {
                                while (player.isOnline && steerVehicleCache.contains(player) && physicObject.pet.isRiding()) {
                                    physicObject.ride(player, -0.5, false, false)
                                    delay(5.ticks)
                                }
                            }
                        }
                    }

                    if (packet.isShift) {
                        physicObject.ride(player, 0.0, false, true)
                    }

                    if (packet.isJumping) {
                        physicObject.ride(player, 0.0, true, false)
                    }
                }
            }

            return
        }

        if (packet is PacketInInteractEntity) {
            plugin.launch {
                val physicObject =
                    physicObjectService.findPhysicObjectById(packet.entityId) as PetEntityImpl? ?: return@launch

                if (packet.actionType == InteractionType.ATTACK) {
                    physicObject.leftClick(event.player)
                } else {
                    physicObject.rightClick(event.player)
                }
            }
        }
    }
}
