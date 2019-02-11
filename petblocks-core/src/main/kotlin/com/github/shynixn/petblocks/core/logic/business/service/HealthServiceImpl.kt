package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class HealthServiceImpl @Inject constructor(
    concurrencyService: ConcurrencyService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val loggingService: LoggingService,
    private val proxyService: ProxyService
) : HealthService, Runnable {
    private val regainingPlayers = HashSet<Any>()

    /**
     * Initialize.
     */
    init {
        sync(concurrencyService, 0L, 20L) {
            this.run()
        }
    }

    /**
     * Damages the given [pet] with the given [damage].
     * The pet needs a health ai otherwise this operation gets ignored.
     */
    override fun damagePet(pet: PetProxy, damage: Double) {
        val petMeta = pet.meta
        val count = petMeta.aiGoals.count { p -> p is AIHealth }

        if (count == 0) {
            return
        }

        if (count > 1) {
            loggingService.warn("Player ${petMeta.playerMeta.name} has registered multiple ${AIType.HEALTH.type}. Please check your configuration.")
        }

        val aiHealth = petMeta.aiGoals.first { a -> a is AIHealth } as AIHealth
        aiHealth.health = aiHealth.health - damage

        this.registerForHealthRegain(petMeta)

        if (aiHealth.health <= 0) {
            pet.remove()
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
        if(regainingPlayers.isEmpty()){
            return
        }

        for (player in regainingPlayers.toTypedArray()) {
            val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)

            val aiBase = petMeta.aiGoals.firstOrNull { a -> a is AIHealth }

            if (aiBase == null) {
                regainingPlayers.remove(player)
            }

            val aiHealth = aiBase as AIHealth

            when {
                aiHealth.currentRespawningDelay > 0 -> aiHealth.currentRespawningDelay--
                aiBase.health < aiBase.maxHealth -> aiBase.health++

                else -> {
                    persistencePetMetaService.save(petMeta)
                    regainingPlayers.remove(player)
                }
            }
        }
    }

    /**
     * Registers the given [petMeta] for health regain.
     * The pet needs a health ai otherwise this operation gets ignored.
     */
    override fun registerForHealthRegain(petMeta: PetMeta) {
        if (regainingPlayers.contains(petMeta.playerMeta.uuid)) {
            return
        }

        val count = petMeta.aiGoals.count { p -> p is AIHealth }

        if (count == 0) {
            return
        }

        val playerProxy = proxyService.findPlayerProxyObjectFromUUID(petMeta.playerMeta.uuid)
        regainingPlayers.add(playerProxy.handle)
    }

    /**
     * Clears the allocated resources from the given [player].
     */
    override fun <P> close(player: P) {
        if (player is Any && regainingPlayers.contains(player)) {
            regainingPlayers.remove(player)
        }
    }
}