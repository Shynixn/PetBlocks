@file:Suppress("CAST_NEVER_SUCCEEDS", "unused")

package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIFlyRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIGroundRiding
import com.github.shynixn.petblocks.api.persistence.entity.AIWearing
import com.github.shynixn.petblocks.api.sponge.event.PetBlocksLoginEvent
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.sponge.logic.business.extension.toTextString
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.manipulator.mutable.entity.SneakingData
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.entity.LeashEntityEvent
import org.spongepowered.api.event.entity.MoveEntityEvent
import org.spongepowered.api.event.entity.living.humanoid.player.RespawnPlayerEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.event.world.chunk.LoadChunkEvent
import org.spongepowered.api.event.world.chunk.UnloadChunkEvent
import java.util.*
import kotlin.collections.HashSet

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
    private val debugService: PetDebugService,
    private val configurationService: ConfigurationService
) {
    private val joinCooldown = 20 * 6L
    private val alreadyLoading = HashSet<UUID>()

    /**
     * Gets called when a player joins the server. Join the pet if it was already enabled last time.
     */
    @Listener
    fun onPlayerJoinEvent(event: ClientConnectionEvent.Join) {
        val joinMessage = event.message.toTextString()

        if (joinMessage == "PetBlocksRunTime") {
            this.loadPetBlocks(event.targetEntity)
        } else {
            val uuid = event.targetEntity.uniqueId

            if (alreadyLoading.contains(uuid)) {
                return
            }

            alreadyLoading.add(uuid)

            sync(concurrencyService, joinCooldown) {
                this.loadPetBlocks(event.targetEntity)
            }
        }
    }

    /**
     * Gets called when the pet meta data would be prepared for a player.
     */
    @Listener
    fun onPetBlocksLoginEvent(event: PetBlocksLoginEvent) {
        val overwrite = configurationService.findValue<Boolean>("global-configuration.overwrite-previous-pet")

        if (overwrite) {
            val newPetMeta = configurationService.generateDefaultPetMeta(event.player.uniqueId.toString(), event.player.name)
            persistencePetMetaService.save(newPetMeta)
            persistencePetMetaService.refreshPetMetaFromRepository(event.player).thenAcceptSafely {
                performFirstSpawn(event.player)
            }
        }

        if (event.petMeta.new) {
            performFirstSpawn(event.player)
        }
    }

    /**
     * Gets called when a player quits the server. Clear pet resources and fix enable state in persistence.
     */
    @Listener
    fun onPlayerQuitEvent(event: ClientConnectionEvent.Disconnect) {
        if (petService.hasPet(event.targetEntity)) {
            val pet = petService.getOrSpawnPetFromPlayer(event.targetEntity).get()

            (pet.getHeadArmorstand() as EntityPetProxy).deleteFromWorld()
            pet.getHitBoxLivingEntity<EntityPetProxy>().ifPresent { p ->
                p.deleteFromWorld()
            }
        }

        persistencePetMetaService.clearResources(event.targetEntity)
        debugService.unRegister(event.targetEntity)
    }

    /**
     * Avoids saving the pet into the chunk data.
     */
    @Listener
    fun onChunkUnloadEvent(event: UnloadChunkEvent) {
        for (entity in event.targetChunk.entities) {
            if (entity is EntityPetProxy) {
                entity.deleteFromWorld()
            }
        }
    }

    /**
     * Avoids loading an invalid pet into a chunk.
     */
    @Listener
    fun onChunkLoadEvent(event: LoadChunkEvent) {
        entityService.cleanUpInvalidEntities(event.targetChunk.entities.toList())
    }

    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @Listener
    fun onEntityInteractEvent(event: InteractEntityEvent) {
        val optPet = petService.findPetByEntity(event.targetEntity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @Listener
    fun onEntityInteractEventSecondary(event: InteractEntityEvent.Secondary) {
        val optPet = petService.findPetByEntity(event.targetEntity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @Listener
    fun onEntityInteractEventSecondary(event: InteractEntityEvent.Primary) {
        val optPet = petService.findPetByEntity(event.targetEntity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when a player presses the sneak button and removes the pet of the players head if present.
     *
     * @param event event
     */
    @Listener
    fun onEntityToggleSneakEvent(event: ChangeDataHolderEvent) {
        if (event.targetHolder !is Player || !petService.hasPet(event.targetHolder)) {
            return
        }

        if (!event.targetHolder.get(SneakingData::class.java).get().sneaking().get()) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.targetHolder).get()

        for (name in configurationService.findValue<List<String>>("global-configuration.disable-on-sneak")) {
            for (ai in pet.meta.aiGoals.toTypedArray()) {
                if (ai.type == name) {
                    pet.meta.aiGoals.remove(ai)
                }
            }
        }
    }

    /**
     * Gets called when an animal gets leashed and cancels it for all pet entities.
     *
     * @param event event
     */
    @Listener
    fun onEntityLeashEvent(event: LeashEntityEvent) {
        val optPet = petService.findPetByEntity(event.targetEntity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Handles pet despawning and respawning on player respawn.
     */
    @Listener
    fun onPlayerRespawnEvent(event: RespawnPlayerEvent) {
        if (!petService.hasPet(event.targetEntity)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.targetEntity).get()
        pet.remove()

        val warpDelay = configurationService.findValue<Int>("global-configuration.respawn-delay") * 20L

        sync(concurrencyService, warpDelay) {
            petService.getOrSpawnPetFromPlayer(event.targetEntity)
        }
    }

    /**
     * Handles pet despawning and respawning on player teleport.
     */
    @Listener
    fun onPlayerTeleportEvent(event: MoveEntityEvent.Teleport) {
        if (event.targetEntity !is Player) {
            return
        }

        if (!petService.hasPet(event.targetEntity)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(event.targetEntity).get()


        if (event.toTransform.extent.name != event.fromTransform.extent.name) {
            pet.remove()

            val warpDelay = configurationService.findValue<Int>("global-configuration.teleport-delay") * 20L

            sync(concurrencyService, warpDelay) {
                petService.getOrSpawnPetFromPlayer(event.targetEntity)
            }

            return
        }

        if (event.targetEntity.passengers.isEmpty()) {
            return
        }

        val fallOffHead = configurationService.findValue<Boolean>("global-configuration.teleport-fall")

        if (fallOffHead) {
            pet.meta.aiGoals.removeIf { a -> a is AIGroundRiding || a is AIFlyRiding || a is AIWearing }
            return
        }

        event.isCancelled = true
        pet.teleport(event.toTransform)
    }

    /**
     * Performs the first spawn of the pet if enabled.
     */
    private fun performFirstSpawn(player: Player) {
        val applyPetOnFirstSpawn = configurationService.findValue<Boolean>("global-configuration.apply-pet-on-first-spawn")

        if (applyPetOnFirstSpawn) {
            petService.getOrSpawnPetFromPlayer(player)
        }
    }

    /**
     * Loads the PetBlocks data.
     */
    private fun loadPetBlocks(player: Player) {
        if (!player.isOnline) {
            return
        }

        persistencePetMetaService.refreshPetMetaFromRepository(player).thenAcceptSafely { petMeta ->
            if (player.isOnline) {
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
                Sponge.getEventManager().post(joinEvent)
            }

            if (alreadyLoading.contains(player.uniqueId)) {
                alreadyLoading.remove(player.uniqueId)
            }
        }
    }
}