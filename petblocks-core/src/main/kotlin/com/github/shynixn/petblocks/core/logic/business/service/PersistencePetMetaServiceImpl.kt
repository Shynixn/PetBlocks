package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.proxy.CompletableFutureProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.extension.async
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
class PersistencePetMetaServiceImpl @Inject constructor(
    private val proxyService: ProxyService,
    private val petMetaRepository: PetMetaRepository,
    private val concurrencyService: ConcurrencyService
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
     * Returns [CompletableFutureProxy] with a list of stored [PetMeta].
     */
    override fun getAll(): CompletableFutureProxy<List<PetMeta>> {
        val completableFuture = proxyService.createCompletableFuture<List<PetMeta>>()

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
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    override fun getOrCreateFromPlayerUUID(uuid: String): CompletableFutureProxy<PetMeta> {
        val completableFuture = proxyService.createCompletableFuture<PetMeta>()

        if (cache.containsKey(uuid)) {
            sync(concurrencyService) {
                completableFuture.complete(cache[uuid]!!)
            }

            return completableFuture
        }

        val playerProxy = proxyService.findPlayerProxyObjectFromUUID(uuid)!!
        val playerName = playerProxy.name

        async(concurrencyService) {
            val petMeta = petMetaRepository.getOrCreateFromPlayerIdentifiers(playerName, uuid)

            sync(concurrencyService) {
                cache[uuid] = petMeta
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }

    /**
     * Clears the cache of the player.
     */
    override fun cleanResources(uuid: String) {
        if (cache.containsKey(uuid)) {
            save(cache[uuid]!!)
            cache.remove(uuid)
        }
    }

    /**
     * Saves the given [petMeta] instance and returns a [CompletableFutureProxy] with the same petMeta instance.
     */
    override fun save(petMeta: PetMeta): CompletableFutureProxy<PetMeta> {
        val completableFuture = proxyService.createCompletableFuture<PetMeta>()

        async(concurrencyService) {
            petMetaRepository.save(petMeta)

            sync(concurrencyService) {
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }
}