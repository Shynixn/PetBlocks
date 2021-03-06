package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIFleeInCombat
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
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
    private val petService: PetService,
    private val proxyService: ProxyService,
    private val loggingService: LoggingService
) : CombatPetService, Runnable {


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
    override fun flee(petMeta: PetMeta) {
        val count = petMeta.aiGoals.count { p -> p is AIFleeInCombat }

        if (count == 0) {
            return
        }

        if (count > 1) {
            loggingService.warn("Player ${petMeta.playerMeta.name} has registered multiple ${AIType.FLEE_IN_COMBAT.type}. Please check your configuration.")
        }

        val aiBase = petMeta.aiGoals.first { a -> a is AIFleeInCombat } as AIFleeInCombat
        aiBase.currentAppearsInSeconds = aiBase.reAppearsInSeconds

        val player = proxyService.getPlayerFromUUID<Any>(petMeta.playerMeta.uuid)

        if (petService.hasPet(player)) {
            petService.getOrSpawnPetFromPlayer(player).get().remove()
        }
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
        for (petMeta in persistencePetMetaService.cache) {
            val aiBase = petMeta.aiGoals.firstOrNull { a -> a is AIFleeInCombat } as AIFleeInCombat? ?: continue

            if (aiBase.currentAppearsInSeconds < 1) {
                continue
            }

            aiBase.currentAppearsInSeconds = aiBase.currentAppearsInSeconds - 1

            if (aiBase.currentAppearsInSeconds < 1) {
                val player = proxyService.getPlayerFromUUID<Any>(petMeta.playerMeta.uuid)

                petService.getOrSpawnPetFromPlayer(player)
            }
        }
    }
}