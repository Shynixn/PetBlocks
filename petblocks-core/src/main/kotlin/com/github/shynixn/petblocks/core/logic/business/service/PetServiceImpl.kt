package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetPostSpawnEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetPreSpawnEntity
import com.google.inject.Inject
import java.util.*

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
    concurrencyService: ConcurrencyService,
    private val petMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService,
    private val configurationService: ConfigurationService,
    private val proxyService: ProxyService,
    private val entityService: EntityService,
    private val eventService: EventService
) : PetService {

    private val pets = ArrayList<PetProxy>()

    /**
     * Initialize task.
     */
    init {
        concurrencyService.runTaskSync(0L, 20L * 60 * 5) {
            this.run()
        }
    }

    /**
     * Gets or spawns the pet of the given player.
     * An empty optional gets returned if the pet cannot spawn by one of the following reasons:
     * Current world, region is disabled for pets, PreSpawnEvent was cancelled or Pet is not available due to Ai State.
     * For example HealthAI defines pet ai as 0 which results into impossibility to spawn.
     */
    override fun <P> getOrSpawnPetFromPlayer(player: P): Optional<PetProxy> {
        val playerUUID = proxyService.getPlayerUUID(player)
        val playerLocation = proxyService.getPlayerLocation<Any, P>(player)
        val playerPosition = proxyService.toPosition(playerLocation)

        if (hasPet(player)) {
            return Optional.of(pets.first { p -> !p.isDead && p.meta.playerMeta.uuid == playerUUID })
        }

        if (!isAllowedToSpawn(playerPosition)) {
            return Optional.empty()
        }

        val petMeta = petMetaService.getPetMetaFromPlayer(player)
        val cancelled = eventService.callEvent(PetPreSpawnEntity(player as Any, petMeta))

        if (cancelled) {
            return Optional.empty()
        }

        val petProxy: PetProxy

        try {
            petProxy = entityService.spawnPetProxy(playerLocation, petMeta)
        } catch (e: Exception) {
            loggingService.warn("Failed to spawn pet.", e)
            return Optional.empty()
        }

        pets.add(petProxy)

        petMeta.enabled = true
        petMetaService.save(petMeta)

        eventService.callEvent(PetPostSpawnEntity(player as Any, petProxy))

        return Optional.of(petProxy)
    }

    /**
     * Gets if the given [player] has got an active pet.
     */
    override fun <P> hasPet(player: P): Boolean {
        val playerUUID = proxyService.getPlayerUUID(player)
        return pets.firstOrNull { p -> !p.isDead && p.meta.playerMeta.uuid == playerUUID } != null
    }

    /**
     * Tries to find the pet from the given entity.
     */
    override fun <E> findPetByEntity(entity: E): PetProxy? {
        for (pet in pets) {
            if (!pet.isDead) {
                if (pet.getHeadArmorstand<Any>() == entity || (pet.getHitBoxLivingEntity<Any>().isPresent && pet.getHitBoxLivingEntity<Any>().get() == entity)) {
                    return pet
                }
            }
        }

        return null
    }

    /**
     * When an object implementing interface `Runnable` is used
     * to create a thread, starting the thread causes the object's
     * `run` method to be called in that separately executing
     * thread.
     *
     *
     * The general contract of the method `run` is that it may
     * take any action whatsoever.
     */
    private fun run() {
        for (pet in pets.toTypedArray()) {
            if (pet.isDead) {
                pets.remove(pet)
            }
        }
    }

    /**
     * Gets if the pet is allowed to spawn at the given position.
     */
    private fun isAllowedToSpawn(position: Position): Boolean {
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