package com.github.shynixn.petblocks.core.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.AIParticle

class PathfinderParticle(private val aiParticle: AIParticle, private val pet: PetProxy) : BasePathfinder(aiParticle) {
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)
    private val proxyService = PetBlocksApi.resolve(ProxyService::class.java)
    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
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
            if (!pet.meta.particleEnabled) {
                return
            }

            val currentMilliSeconds = System.currentTimeMillis()

            if (currentMilliSeconds - lastPlay < aiParticle.delayBetweenPlaying * 1000) {
                return
            }

            lastPlay = currentMilliSeconds
            val petPosition = proxyService.toPosition(pet.getLocation<Any>())
            petPosition.add(aiParticle.offset)
            particleService.playParticle(
                proxyService.toLocation<Any>(petPosition),
                aiParticle.particle,
                pet.getPlayer<Any>()
            )
        } catch (e: Exception) {
            loggingService.warn("Failed to execute BuffEffectPathfinder.", e)
        }
    }
}
