package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository
import com.github.shynixn.petblocks.core.logic.business.extension.async
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

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
class PersistencePetMetaServiceImpl @Inject constructor(private val concurrencyService: ConcurrencyService, private val proxyService: ProxyService, private val petMetaRepository: PetMetaRepository, private val petRepository: PetRepository) : PersistencePetMetaService {
    /**
     * Returns [CompletableFuture] with a list of stored [PetMeta].
     */
    override fun getAll(): CompletableFuture<List<PetMeta>> {
        val completableFuture = CompletableFuture<List<PetMeta>>()

        val activePetMetas = petRepository.getAll().map { p -> p.meta }

        async(concurrencyService) {
            val petMetaList = ArrayList(petMetaRepository.getAll())

            petMetaList.toTypedArray().forEach { item ->
                activePetMetas.forEach { active ->
                    if (active.id == item.id) {
                        petMetaList.remove(item)
                        petMetaList.add(active)
                    }
                }
            }

            sync(concurrencyService) {
                completableFuture.complete(petMetaList)
            }
        }

        return completableFuture
    }

    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    override fun getOrCreateFromPlayerUUID(uuid: UUID): CompletableFuture<PetMeta> {
        val completableFuture = CompletableFuture<PetMeta>()

        if (petRepository.hasPet(uuid)) {
            val meta = petRepository.getFromPlayerUUID(uuid).meta

            sync(concurrencyService) {
                completableFuture.complete(meta)
            }
        } else {
            val playerProxy = proxyService.findPlayerProxyObjectFromUUID(uuid)
            val playerName = playerProxy.get().name

            if (playerProxy.isPresent) {
                async(concurrencyService) {
                    val petMeta = petMetaRepository.getOrCreateFromPlayerIdentifiers(playerName, uuid)

                    sync(concurrencyService) {
                        completableFuture.complete(petMeta)
                    }
                }
            }
        }
        return completableFuture
    }

    /**
     * Saves the given [petMeta] instance and returns a [CompletableFuture] with the same petMeta instance.
     */
    override fun save(petMeta: PetMeta): CompletableFuture<PetMeta> {
        val completableFuture = CompletableFuture<PetMeta>()

        completableFuture.exceptionally { throwable ->
            throw RuntimeException("Failed to perform PetBlocks task.", throwable)
        }

        async(concurrencyService) {
            petMetaRepository.save(petMeta)

            sync(concurrencyService) {
                completableFuture.complete(petMeta)
            }
        }

        return completableFuture
    }
}