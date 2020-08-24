package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetPostSpawnEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetPreSpawnEntity
import com.google.inject.Inject
import java.util.*
import kotlin.collections.HashMap

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
class PetServiceImpl @Inject constructor(
    private val concurrencyService: ConcurrencyService,
    private val petMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService,
    private val configurationService: ConfigurationService,
    private val proxyService: ProxyService,
    private val entityService: EntityService,
    private val eventService: EventService
) : PetService {
    private val pets = HashMap<String, PetProxy>()

    /**
     * Gets or spawns the pet of the given player.
     * An empty optional gets returned if the pet cannot spawn by one of the following reasons:
     * Current world, player has not got permission, region is disabled for pets, PreSpawnEvent was cancelled or Pet is not available due to Ai State.
     * For example HealthAI defines pet ai as 0 which results into impossibility to spawn.
     */
    override fun <P> getOrSpawnPetFromPlayer(player: P): Optional<PetProxy> {
        val playerUUID = proxyService.getPlayerUUID(player)

        if (hasPet(player)) {
            return Optional.of(pets[playerUUID]!!)
        }

        if (!petMetaService.hasPetMeta(player)) {
            return Optional.empty()
        }

        val petMeta = petMetaService.getPetMetaFromPlayer(player)
        val cancelled = eventService.callEvent(PetPreSpawnEntity(player as Any, petMeta))

        if (cancelled) {
            return Optional.empty()
        }

        val playerLocation = proxyService.getPlayerLocation<Any, P>(player)
        val petProxy: PetProxy

        try {
            petProxy = entityService.spawnPetProxy(playerLocation, petMeta)
        } catch (e: Exception) {
            loggingService.warn("Failed to spawn pet.", e)
            return Optional.empty()
        }

        pets[playerUUID] = petProxy
        petMeta.enabled = true

        eventService.callEvent(PetPostSpawnEntity(player as Any, petProxy))

        concurrencyService.runTaskSync(1L) {
            petProxy.triggerTick()
        }

        return Optional.of(petProxy)
    }

    /**
     * Gets if the given [player] has got an active pet.
     */
    override fun <P> hasPet(player: P): Boolean {
        val playerUUID = proxyService.getPlayerUUID(player)

        if (!pets.containsKey(playerUUID)) {
            return false
        }

        if (!petMetaService.hasPetMeta(player)) {
            return false
        }

        val petAllowed = isPetAllowedToBeInSpawnState(player)
        val pet = pets[playerUUID]!!

        if (!petAllowed) {
            if (!pet.isDead) {
                pet.remove()
            }

            val petMeta = petMetaService.getPetMetaFromPlayer(player)
            petMeta.enabled = false
            this.pets.remove(playerUUID)
            return false
        }

        if (pet.isDead) {
            val petMeta = petMetaService.getPetMetaFromPlayer(player)
            petMeta.enabled = false
            this.pets.remove(playerUUID)
            return false
        }

        return true
    }

    /**
     * Clears the allocated player resources.
     * Should not be called by external plugins.
     */
    override fun <P> clearPlayerResources(player: P) {
        val playerUUID = proxyService.getPlayerUUID(player)

        if (this.pets.containsKey(playerUUID)) {
            this.pets.remove(playerUUID)
        }
    }

    /**
     * Tries to find the pet from the given entity.
     */
    override fun <E> findPetByEntity(entity: E): PetProxy? {
        for (pet in pets.values) {
            if (pet.getHeadArmorstand<Any>() == entity || (pet.getHitBoxLivingEntity<Any>().isPresent && pet.getHitBoxLivingEntity<Any>()
                    .get() == entity)
            ) {
                val owner = pet.getPlayer<Any>()
                if (hasPet(owner)) {
                    return pet
                }
            }
        }

        return null
    }

    /**
     * Is the pet allowed to be spawned.
     */
    private fun <P> isPetAllowedToBeInSpawnState(player: P): Boolean {
        if (!proxyService.hasPermission(player, Permission.CALL)) {
            return false
        }

        val playerLocation = proxyService.getPlayerLocation<Any, P>(player)
        val playerPosition = proxyService.toPosition(playerLocation)

        return isAllowedToSpawnAtPosition(playerPosition)
    }

    /**
     * Gets if the pet is allowed to spawn at the given position.
     */
    private fun isAllowedToSpawnAtPosition(position: Position): Boolean {
        val includedWorlds = configurationService.findValue<List<String>>("world.included")
        val excludedWorlds = configurationService.findValue<List<String>>("world.excluded")

        when {
            includedWorlds.contains("all") -> return !excludedWorlds.contains(position.worldName)
            excludedWorlds.contains("all") -> return includedWorlds.contains(position.worldName)
            else -> loggingService.warn("Please add 'all' to excluded or included worlds inside of the config.yml")
        }

        return true
    }
}