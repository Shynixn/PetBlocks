package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.business.service.PersistenceService
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
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
class PersistenceServiceImpl @Inject constructor(private val plugin: Plugin, private val petBlockController: PetBlockController<Player>, private val petMetaController: PetMetaController<Player>) : PersistenceService {
    /**
     * Returns [CompletableFuture] with a list of stored [PetMeta].
     */
    override fun getAll(): CompletableFuture<List<PetMeta>> {
        val completableFuture = CompletableFuture<List<PetMeta>>()
        val activePetMetas = petBlockController.all.map { p -> p.meta }

        plugin.server.scheduler.runTaskAsynchronously(plugin, {
            val petMetaList = petMetaController.all

            var i = 0
            while (i < petMetaList.size) {
                val petMeta = petMetaList[i]

                if (activePetMetas.count { p -> p.playerMeta.uuid == petMeta.playerMeta.uuid } > 0) {
                    petMetaList.removeAt(i)
                    i = 0
                }

                i++
            }

            completableFuture.complete(petMetaList)
        })

        return completableFuture
    }

    /**
     * Returns [CompletableFuture] with optional newly created [PetMeta] instance or empty optional if not found.
     */
    override fun <P> getFromPlayer(player: P): CompletableFuture<Optional<PetMeta>> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be an BukkitPlayer!")
        }

        val completableFuture = CompletableFuture<Optional<PetMeta>>()

        val optPetBlock = petBlockController.getFromPlayer(player)
        if (optPetBlock.isPresent) {
            val meta = optPetBlock.get().meta
            plugin.server.scheduler.runTaskAsynchronously(plugin, {
                completableFuture.complete(Optional.of(meta))
            })
        }

        plugin.server.scheduler.runTaskAsynchronously(plugin, {
            val optResult = petMetaController.getFromPlayer(player)
            completableFuture.complete(optResult)
        })

        return completableFuture
    }

    /**
     * Saves the given [petMeta] instance and returns a [CompletableFuture] with the same petMeta instance.
     */
    override fun save(petMeta: PetMeta): CompletableFuture<PetMeta> {
        val completableFuture = CompletableFuture<PetMeta>()

        plugin.server.scheduler.runTaskAsynchronously(plugin, {
            petMetaController.store(petMeta)
            completableFuture.complete(petMeta)
        })

        return completableFuture
    }
}