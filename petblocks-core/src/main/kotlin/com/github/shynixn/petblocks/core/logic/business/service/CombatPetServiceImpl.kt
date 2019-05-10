package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.CombatPetService
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.persistence.entity.AIFleeInCombat
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject

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
class CombatPetServiceImpl @Inject constructor(
    concurrencyService: ConcurrencyService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService
) : CombatPetService, Runnable {

    private val fleeCache = HashSet<Any>()

    /**
     * Initialize.
     */
    init {
        sync(concurrencyService, 0L, 20L) {
            this.run()
        }
    }

    /**
     * Lets the pet flee and reappears after some time.
     */
    override fun flee(pet: PetProxy) {
        val count = pet.meta.aiGoals.count { p -> p is AIFleeInCombat }

        if (count == 0) {
            return
        }

        if (count > 1) {
            loggingService.warn("Player ${pet.meta.playerMeta.name} has registered multiple ${AIType.FLEE_IN_COMBAT.type}. Please check your configuration.")
        }

        if (fleeCache.contains(pet.getPlayer())) {
            return
        }

        val aiBase = pet.meta.aiGoals.first { a -> a is AIFleeInCombat } as AIFleeInCombat
        aiBase.currentAppearsInSeconds = aiBase.reAppearsInSeconds

        fleeCache.add(pet.getPlayer())

        pet.remove()
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
        if (fleeCache.isEmpty()) {
            return
        }

        for (player in fleeCache.toTypedArray()) {
            val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)

            val aiBase = petMeta.aiGoals.firstOrNull { a -> a is AIFleeInCombat }

            if (aiBase == null) {
                fleeCache.remove(player)
            }

            val aiHealth = aiBase as AIFleeInCombat

            when {
                aiHealth.currentAppearsInSeconds > 0 -> aiHealth.currentAppearsInSeconds--

                else -> {
                    persistencePetMetaService.save(petMeta)
                    fleeCache.remove(player)
                }
            }
        }
    }

    /**
     * Clears the allocated resources from the given [player].
     */
    override fun <P> clearResources(player: P) {
        if (player is Any && fleeCache.contains(player)) {
            fleeCache.remove(player)
        }
    }
}