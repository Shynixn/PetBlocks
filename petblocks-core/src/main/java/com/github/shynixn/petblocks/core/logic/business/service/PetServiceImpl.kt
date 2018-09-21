package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.business.entity.PetBlock
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject
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
class PetServiceImpl @Inject constructor(private val concurrencyService: ConcurrencyService, private val petMetaService: PersistencePetMetaService, private val loggingService: LoggingService) : PetService {
    private var petBlockController: PetBlockController<*>? = null

    /**
     * Tries to find the PetBlock from the given entity.
     */
    override fun <E> findPetBlockByEntity(entity: E): Optional<PetBlock<Any, Any>> {
        petBlockController!!.all.forEach { petBlock ->
            if (petBlock.armorStand == entity || petBlock.engineEntity == entity) {
                return Optional.of(petBlock)
            }
        }

        return Optional.empty()
    }

    /**
     * Checks if the player with the given [uuid] has an active pet.
     */
    override fun hasPet(uuid: UUID): Boolean {
        return petBlockController!!.getFromUUID(uuid).isPresent
    }

    /**
     * Gets or spawns the pet of the given player uuid.
     */
    override fun getOrSpawnPetBlockFromPlayerUUID(uuid: UUID): CompletableFuture<PetBlock<Any, Any>> {
        initializeDependencies()

        val completableFuture = CompletableFuture<PetBlock<Any, Any>>()

        petMetaService.getOrCreateFromPlayerUUID(uuid).thenAccept { petMeta ->
            val optExistingPetBlock = petBlockController!!.getFromUUID(uuid)

            if (optExistingPetBlock.isPresent && !optExistingPetBlock.get().isDead) {
                completableFuture.complete(optExistingPetBlock.get())
            } else {
                if (optExistingPetBlock.isPresent) {
                    petBlockController!!.remove(optExistingPetBlock.get())
                }

                try {
                    val petBlock = petBlockController!!.createFromUUID(uuid, petMeta)
                    petBlock.meta.isEnabled = true
                    petBlockController!!.store(petBlock)

                    completableFuture.complete(petBlock)
                } catch (e: Exception) {
                    loggingService.error("Pet could not be spawned. Please report the error message to the plugin author.", e)
                }
            }
        }

        return completableFuture
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