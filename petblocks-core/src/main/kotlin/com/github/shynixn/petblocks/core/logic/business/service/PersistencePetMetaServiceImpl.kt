package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.EventService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.extension.async
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetBlocksPostSaveEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetBlocksPreSaveEntity
import com.google.inject.Inject
import java.util.*
import java.util.concurrent.CompletableFuture
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
class PersistencePetMetaServiceImpl @Inject constructor(
    private val proxyService: ProxyService,
    private val petMetaRepository: PetMetaRepository,
    private val concurrencyService: ConcurrencyService,
    private val eventService: EventService
) : PersistencePetMetaService {
    private val cache = HashMap<String, PetMeta>()

    /**
     * Initialize.
     */
    init {
        sync(concurrencyService, 0L, 20 * 60L * 5) {
            cache.values.forEach { p ->
                save(p)
            }
        }
    }

    /**
     * Clears the cache of the player and saves the allocated resources.
     * Should only be called once a player leaves the server.
     */
    override fun <P> clearResources(player: P)  : CompletableFuture<Void?>{
        val playerProxy = proxyService.findPlayerProxyObject(player)
        val completableFuture = CompletableFuture<Void?>()

        if (!cache.containsKey(playerProxy.uniqueId)) {
            return completableFuture
        }

        val petMeta = cache[playerProxy.uniqueId]!!
        val completable = save(petMeta)
        cache.remove(playerProxy.uniqueId)

        completable.thenAccept {
            completableFuture.complete(null)
        }

        return completableFuture
    }

    /**
     * Gets the petMeta from the player. This call will never return null.
     */
    override fun <P> getPetMetaFromPlayer(player: P): PetMeta {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        if (!cache.containsKey(playerProxy.uniqueId)) {
            // Blocks the calling (main) thread and should not be executed on an normal server.
            return petMetaRepository.getOrCreateFromPlayerIdentifiers(playerProxy.name, playerProxy.uniqueId)
        }

        return cache[playerProxy.uniqueId]!!
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
                    if (!cache.containsKey(item.playerMeta.uuid)) {
                        cache[item.playerMeta.uuid] = item
                    }
                }

                completableFuture.complete(cache.values.toList())
            }
        }

        return completableFuture
    }

    /**
     * Gets or creates petMeta from the player.
     * Should only be called once a player joins the server.
     */
    override fun <P> refreshPetMetaFromRepository(player: P): CompletableFuture<PetMeta> {
        val playerProxy = proxyService.findPlayerProxyObject(player)
        val completableFuture = CompletableFuture<PetMeta>()

        async(concurrencyService) {
            val petMeta = petMetaRepository.getOrCreateFromPlayerIdentifiers(playerProxy.name, playerProxy.uniqueId)

            sync(concurrencyService) {
                cache[playerProxy.uniqueId] = petMeta
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }

    /**
     * Closes all resources immediately.
     */
    override fun close() {
        for(player in cache.keys){
            petMetaRepository.save(cache[player]!!)
        }

        cache.clear()
    }

    /**
     * Saves the given [petMeta] instance and returns a future.
     */
    override fun save(petMeta: PetMeta): CompletableFuture<PetMeta> {
        val completableFuture = CompletableFuture<PetMeta>()

        eventService.callEvent(PetBlocksPreSaveEntity(petMeta))

        async(concurrencyService) {
            petMetaRepository.save(petMeta)

            sync(concurrencyService) {
                eventService.callEvent(PetBlocksPostSaveEntity(petMeta))
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }
}