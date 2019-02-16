package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.bukkit.event.PetBlocksLoginEvent
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIFlyRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIGroundRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIWearing
import com.github.shynixn.petblocks.bukkit.logic.business.extension.teleportUnsafe
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityInteractEvent
import org.bukkit.event.entity.PlayerLeashEntityEvent
import org.bukkit.event.player.*
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.ChunkUnloadEvent

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class PetListener @Inject constructor(
    private val petService: PetService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val concurrencyService: ConcurrencyService,
    private val entityService: EntityService,
    private val itemService: ItemService,
    private val configurationService: ConfigurationService
) : Listener {
    private val joinCooldown = 20 * 6L
    private val soilMaterial = MaterialType.SOIL

    /**
     * Gets called when a player joins the server. Join the pet if it was already enabled last time.
     */
    @EventHandler
    fun onPlayerJoinEvent(event: PlayerJoinEvent) {
        if (event.joinMessage != null && event.joinMessage == "PetBlocksRunTime") {
            this.loadPetBlocks(event.player)
        } else {
            sync(concurrencyService, joinCooldown) {
                this.loadPetBlocks(event.player)
            }
        }
    }

    /**
     * Gets called when a player quits the server. Clear pet resources and fix enable state in persistence.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        persistencePetMetaService.clearResources(event.player)

        if (petService.hasPet(event.player)) {
            petService.getOrSpawnPetFromPlayer(event.player).get().remove()
        }
    }

    /**
     * Avoids saving the pet into the chunk data.
     */
    @EventHandler
    fun onChunkUnloadEvent(event: ChunkUnloadEvent) {
        for (entity in event.chunk.entities) {
            if (entity is EntityPetProxy) {
                entity.deleteFromWorld()
            }
        }
    }

    /**
     * Avoids loading an invalid pet into a chunk.
     */
    @EventHandler
    fun onChunkLoadEvent(event: ChunkLoadEvent) {
        entityService.cleanUpInvalidEntities(event.chunk.entities.toList())
    }

    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityInteractEvent(event: EntityInteractEvent) {
        val optPet = petService.findPetByEntity(event.entity)

        if (optPet != null && itemService.hasItemStackProperties(itemService.createItemStack(event.block.type), soilMaterial)) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when a player presses the sneak button and removes the pet of the players head if present.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityToggleSneakEvent(event: PlayerToggleSneakEvent) {
        if (!petService.hasPet(event.player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.player).get()

        for (ai in pet.meta.aiGoals.toTypedArray()) {
            if (ai is AIGroundRiding || ai is AIFlyRiding || ai is AIWearing) {
                pet.meta.aiGoals.remove(ai)
            }
        }
    }

    /**
     * Gets called when an animal gets leashed and cancels it for all pet entities.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityLeashEvent(event: PlayerLeashEntityEvent) {
        val optPet = petService.findPetByEntity(event.entity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Handles pet despawning and respawning on player respawn.
     */
    @EventHandler
    fun onPlayerRespawnEvent(event: PlayerRespawnEvent) {
        if (!petService.hasPet(event.player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.player).get()
        pet.remove()

        val warpDelay = configurationService.findValue<Int>("global-configuration.respawn-delay") * 20L

        sync(concurrencyService, warpDelay) {
            petService.getOrSpawnPetFromPlayer(event.player)
        }
    }

    /**
     * Handles pet despawning and respawning on player teleport.
     */
    @EventHandler
    fun onPlayerTeleportEvent(event: PlayerTeleportEvent) {
        if (!petService.hasPet(event.player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.player).get()


        if (event.to.world.name != event.from.world.name) {
            pet.remove()

            val warpDelay = configurationService.findValue<Int>("global-configuration.teleport-delay") * 20L

            sync(concurrencyService, warpDelay) {
                petService.getOrSpawnPetFromPlayer(event.player)
            }

            return
        }

        if (event.player.passenger == null) {
            return
        }

        val fallOffHead = configurationService.findValue<Boolean>("global-configuration.teleport-fall")

        if (fallOffHead) {
            pet.meta.aiGoals.removeIf { a -> a is AIGroundRiding || a is AIFlyRiding || a is AIWearing }
            return
        }

        event.isCancelled = true
        event.player.teleportUnsafe(event.to)
    }

    /**
     * Loads the PetBlocks data.
     */
    private fun loadPetBlocks(player: Player) {
        if (player.isOnline && player.world != null) {
            persistencePetMetaService.refreshPetMetaFromRepository(player).thenAccept { petMeta ->
                val optPet: PetProxy? = if (petMeta.enabled) {
                    val pet = petService.getOrSpawnPetFromPlayer(player)

                    if (pet.isPresent) {
                        pet.get()
                    } else {
                        null
                    }
                } else {
                    null
                }

                val joinEvent = PetBlocksLoginEvent(player, petMeta, optPet)
                Bukkit.getPluginManager().callEvent(joinEvent)
            }
        }
    }
}