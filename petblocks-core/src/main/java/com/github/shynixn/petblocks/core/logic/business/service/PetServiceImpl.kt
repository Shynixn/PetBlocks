package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.business.entity.PetBlock
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.core.logic.compatibility.Config
import com.google.inject.Inject
import java.util.*
import java.util.concurrent.CompletableFuture
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
class PetServiceImpl @Inject constructor(private val petMetaService: PersistencePetMetaService, private val loggingService: LoggingService, private val concurrencyService: ConcurrencyService) : PetService, Runnable {
    private val pets = HashMap<PetBlock<Any, Any>, PetProxy>()
    private var petBlockController: PetBlockController<*>? = null

    init {
        concurrencyService.runTaskSync(0L, 20L * 60 * 5, this)
    }

    /**
     * Gets or spawns the pet of the given player uniqueId.
     */
    override fun getOrSpawnPetFromPlayerUUID(uuid: UUID): CompletableFuture<PetProxy> {
        initializeDependencies()

        val completableFuture = CompletableFuture<PetProxy>()

        if (!Config.getInstance<Any>().allowPetSpawningByUUID(uuid)) {
            return completableFuture
        }

        petMetaService.getOrCreateFromPlayerUUID(uuid).thenAcceptSafely { petMeta ->
            val optExistingPetBlock = petBlockController!!.getFromUUID(uuid)

            if (optExistingPetBlock.isPresent && !optExistingPetBlock.get().isDead) {
                if (!pets.containsKey(optExistingPetBlock.get())) {
                    pets[optExistingPetBlock.get()] = petBlockController!!.createfromPetBlock(optExistingPetBlock.get())
                }

                completableFuture.complete(pets[optExistingPetBlock.get()])
            } else {
                if (optExistingPetBlock.isPresent) {
                    petBlockController!!.remove(optExistingPetBlock.get())
                }

                try {
                    val petBlock = petBlockController!!.createFromUUID(uuid, petMeta)
                    petBlock.meta.isEnabled = true
                    petBlockController!!.store(petBlock)

                    if (!pets.containsKey(petBlock)) {
                        pets[petBlock] = petBlockController!!.createfromPetBlock(petBlock)
                    }

                    completableFuture.complete(pets[petBlock])
                } catch (e: Exception) {
                    loggingService.error("Pet could not be spawned. Please report the error message to the plugin author.", e)
                }
            }
        }

        return completableFuture
    }

    /**
     * Tries to find the pet from the given entity.
     */
    override fun <E> findPetByEntity(entity: E): Optional<PetProxy> {
        initializeDependencies()

        petBlockController!!.all.forEach { petBlock ->
            if (petBlock.armorStand == entity || petBlock.engineEntity == entity) {
                if (!pets.containsKey(petBlock)) {
                    pets[petBlock] = petBlockController!!.createfromPetBlock(petBlock)
                }

                return Optional.of(pets[petBlock]!!)
            }
        }

        return Optional.empty()
    }

    /**
     * Checks if the player with the given [uuid] has an active pet.
     */
    override fun hasPet(uuid: UUID): Boolean {
        initializeDependencies()


        return petBlockController!!.getFromUUID(uuid).isPresent
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
     *
     * @see java.lang.Thread.run
     */
    override fun run() {
        pets.keys.toTypedArray().forEach { petblock ->
            if (petblock.isDead) {
                this.pets.remove(petblock)
            }
        }
    }

    /**
     * Helper.
     */
    private fun initializeDependencies() {
        if (petBlockController == null) {
            petBlockController = PetBlocksApi.getDefaultPetBlockController<Any>() as PetBlockController<*>
        }
    }
}