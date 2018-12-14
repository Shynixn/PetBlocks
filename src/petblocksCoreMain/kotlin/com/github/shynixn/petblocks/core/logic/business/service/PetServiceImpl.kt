package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.proxy.CompletableFutureProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository
import com.github.shynixn.petblocks.core.logic.business.extension.sync

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
    private val petMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService,
    private val configurationService: ConfigurationService,
    private val proxyService: ProxyService,
    private val dependencyService: DependencyService,
    private val petRepository: PetRepository,
    private val concurrencyService: ConcurrencyService,
    private val entityService: EntityService
) : PetService {
    init {
        concurrencyService.runTaskSync(0L, 20L * 60 * 5) {
            this.run()
        }
    }

    /**
     * Gets or spawns the pet of the given player uniqueId.
     */
    override fun getOrSpawnPetFromPlayerUUID(uuid: String): CompletableFutureProxy<PetProxy> {
        val completableFuture = concurrencyService.createCompletableFuture<PetProxy>()

        val playerProxy = proxyService.findPlayerProxyObjectFromUUID(uuid) ?: return completableFuture

        if (!isAllowedToSpawn(playerProxy.position, playerProxy.getLocation())) {
            return completableFuture
        }

        if (hasPet(uuid)) {
            sync(concurrencyService) {
                completableFuture.complete(petRepository.getFromPlayerUUID(uuid))
            }
        } else {
            petMetaService.getOrCreateFromPlayerUUID(uuid).thenAccept { petMeta ->
                val petProxy = entityService.spawnPetProxy(playerProxy.getLocation<Any>(), petMeta)
                petMeta.enabled = true
                petMetaService.save(petMeta)
                petRepository.save(petProxy)
                completableFuture.complete(petProxy)
            }
        }

        return completableFuture
    }

    /**
     * Tries to find the pet from the given entity.
     */
    override fun <E> findPetByEntity(entity: E): PetProxy? {
        petRepository.getAll().forEach { petBlock ->
            if (!petBlock.isDead) {
                if (petBlock.getHeadArmorstand<Any>() == entity || petBlock.getHitBoxLivingEntity<Any>() == entity) {
                    return petBlock
                }
            }
        }

        return null
    }

    /**
     * Checks if the player with the given [uuid] has an active pet.
     */
    override fun hasPet(uuid: String): Boolean {
        return petRepository.hasPet(uuid) && !petRepository.getFromPlayerUUID(uuid).isDead
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
        petRepository.getAll().toTypedArray().forEach { pet ->
            if (pet.isDead) {
                petRepository.remove(pet)
            }
        }
    }

    /**
     * Gets if the pet is allowed to spawn at the given position.
     */
    private fun isAllowedToSpawn(position: Position, location: Any): Boolean {
        val includedWorlds = configurationService.findValue<List<String>>("world.included")
        val excludedWorlds = configurationService.findValue<List<String>>("world.excluded")

        when {
            includedWorlds.contains("all") -> return !excludedWorlds.contains(position.worldName) && isAllowedToSpawnInWorldGuardRegion(location)
            excludedWorlds.contains("all") -> return includedWorlds.contains(position.worldName) && isAllowedToSpawnInWorldGuardRegion(location)
            else -> loggingService.warn("Please add 'all' to excluded or included worlds inside of the config.yml")
        }

        return true
    }

    /**
     * Gets if the pet is allowed to spawn in WorldGuard region.
     */
    private fun isAllowedToSpawnInWorldGuardRegion(location: Any): Boolean {
        if (!dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
            return true
        }

        val worldGuardService = PetBlocksApi.resolve<DependencyWorldGuardService>(DependencyWorldGuardService::class)
        val includedRegions = configurationService.findValue<List<String>>("region.included")
        val excludedRegions = configurationService.findValue<List<String>>("region.excluded")

        try {
            when {
                includedRegions.contains("all") -> return worldGuardService.getRegionNames(location).none { excludedRegions.contains(it) }
                excludedRegions.contains("all") -> return worldGuardService.getRegionNames(location).any { includedRegions.contains(it) }
                else -> loggingService.warn("Please add 'all' to excluded or included regions inside of the config.yml")
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to handle region spawning.", e)
        }

        return true
    }
}