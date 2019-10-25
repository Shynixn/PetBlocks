package com.github.shynixn.petblocks.core.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.AIBuffEffect

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
class PathfinderBuffEffect(private val aiBuffEffect: AIBuffEffect, private val pet: PetProxy) : BasePathfinder(aiBuffEffect) {
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val proxyService = PetBlocksApi.resolve(ProxyService::class.java)
    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val soundService = PetBlocksApi.resolve(SoundService::class.java)

    private var lastPlay = System.currentTimeMillis()

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !pet.isDead
    }

    /**
     * On execute.
     */
    override fun onExecute() {
        try {
            val currentMilliSeconds = System.currentTimeMillis()

            if (currentMilliSeconds - lastPlay < aiBuffEffect.coolDown * 1000) {
                return
            }

            lastPlay = currentMilliSeconds
            val player = pet.getPlayer<Any>()
            proxyService.applyPotionEffect(player, aiBuffEffect.effect)
            displayParticle()
            displaySound()
        } catch (e: Exception) {
            loggingService.warn("Failed to execute BuffEffectPathfinder.", e)
        }
    }

    /**
     * Displays the particles.
     */
    private fun displayParticle() {
        if (!pet.meta.particleEnabled) {
            return
        }

        for (location in proxyService.getPointsBetweenLocations(pet.getLocation<Any>(), proxyService.getPlayerLocation(pet.getPlayer<Any>()), 20)) {
            particleService.playParticle(location, aiBuffEffect.particle, pet.getPlayer<Any>())
        }
    }

    /**
     * Displays the sound.
     */
    private fun displaySound() {
        if (!pet.meta.soundEnabled) {
            return
        }

        soundService.playSound(proxyService.getPlayerLocation<Any, Any>(pet.getPlayer()), aiBuffEffect.sound, pet.getPlayer<Any>())
    }
}