package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.extension.async
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetBlocksPostSaveEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetBlocksPreSaveEntity
import com.google.inject.Inject
import java.util.concurrent.CompletableFuture

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
class PersistencePetMetaServiceImpl @Inject constructor(
    private val proxyService: ProxyService,
    private val petMetaRepository: PetMetaRepository,
    private val concurrencyService: ConcurrencyService,
    private val eventService: EventService,
    private val aiService: AIService
) : PersistencePetMetaService {
    private val cacheInternal = HashMap<String, PetMeta>()

    /**
     * Gets all currently loaded pet metas.
     */
    override val cache: List<PetMeta>
        get() {
            return cacheInternal.values.toList()
        }

    /**
     * Initialize.
     */
    init {
        sync(concurrencyService, 0L, 20 * 60L * 5) {
            cacheInternal.values.forEach { p ->
                save(p).thenAcceptSafely {}
            }
        }
    }

    /**
     * Clears the cache of the player and saves the allocated resources.
     * Should only be called once a player leaves the server.
     */
    override fun <P> clearResources(player: P): CompletableFuture<Void?> {
        val completableFuture = CompletableFuture<Void?>()
        val playerUUID = proxyService.getPlayerUUID(player)

        if (!cacheInternal.containsKey(playerUUID)) {
            return completableFuture
        }

        val petMeta = cacheInternal[playerUUID]!!
        val completable = save(petMeta)
        cacheInternal.remove(playerUUID)

        completable.thenAcceptSafely {
            completableFuture.complete(null)
        }

        return completableFuture
    }

    /**
     * Gets the petMeta from the player. This call will never return null.
     */
    override fun <P> getPetMetaFromPlayer(player: P): PetMeta {
        val playerUUID = proxyService.getPlayerUUID(player)
        val playerName = proxyService.getPlayerName(player)

        if (!cacheInternal.containsKey(playerUUID)) {
            // Blocks the calling (main) thread and should not be executed on an normal server.
            return petMetaRepository.getOrCreateFromPlayerIdentifiers(playerName, playerUUID)
        }

        return cacheInternal[playerUUID]!!
    }

    /**
     * Returns future with a list of all stored [PetMeta].
     * As not all PetMeta data is available during runtime this call completes in the future.
     */
    override fun getAll(): CompletableFuture<List<PetMeta>> {
        val completableFuture = CompletableFuture<List<PetMeta>>()

        async(concurrencyService) {
            val items = petMetaRepository.getAll()

            sync(concurrencyService) {
                items.forEach { item ->
                    if (!cacheInternal.containsKey(item.playerMeta.uuid)) {
                        cacheInternal[item.playerMeta.uuid] = item
                    }
                }

                completableFuture.complete(cacheInternal.values.toList())
            }
        }

        return completableFuture
    }

    /**
     * Gets or creates petMeta from the player.
     * Should only be called once a player joins the server.
     */
    override fun <P> refreshPetMetaFromRepository(player: P): CompletableFuture<PetMeta> {
        val playerUUID = proxyService.getPlayerUUID(player)
        val playerName = proxyService.getPlayerName(player)
        val completableFuture = CompletableFuture<PetMeta>()

        async(concurrencyService) {
            val petMeta = petMetaRepository.getOrCreateFromPlayerIdentifiers(playerName, playerUUID)

            sync(concurrencyService) {
                cacheInternal[playerUUID] = petMeta
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }

    /**
     * Closes all resources immediately.
     */
    override fun close() {
        for (player in cacheInternal.keys) {
            petMetaRepository.save(cacheInternal[player]!!)
        }

        cacheInternal.clear()
    }

    /**
     * Saves the given [petMeta] instance and returns a future.
     */
    override fun save(petMeta: PetMeta): CompletableFuture<PetMeta> {
        val completableFuture = CompletableFuture<PetMeta>()

        eventService.callEvent(PetBlocksPreSaveEntity(petMeta))

        // Only use clones for saving otherwise concurrency exceptions may or may not occur.
        // AIS have to be deeply cloned using serializers.
        val petMetaClone = petMeta.clone()
        val clonedAis = petMetaClone.aiGoals.asSequence()
            .map { e -> Pair(aiService.serializeAiBase(e), e.type) }
            .map { e -> aiService.deserializeAiBase<AIBase>(e.second, e.first) }
            .toMutableList()
        petMetaClone.aiGoals.clear()
        petMetaClone.aiGoals.addAll(clonedAis)

        async(concurrencyService) {
            petMetaRepository.save(petMetaClone)

            sync(concurrencyService) {
                eventService.callEvent(PetBlocksPostSaveEntity(petMeta))
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }
}