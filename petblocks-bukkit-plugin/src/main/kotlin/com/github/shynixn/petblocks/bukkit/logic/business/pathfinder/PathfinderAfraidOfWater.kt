package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.AIAfraidOfWater
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toId
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class PathfinderAfraidOfWater(
    private val pet: PetProxy,
    private val aiAfraidOfWater: AIAfraidOfWater,
    private val livingEntity: LivingEntity
) : BasePathfinder(aiAfraidOfWater) {

    private var lastPlayTime = 0L

    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        try {
            val currentMilliseconds = System.currentTimeMillis()

            if (livingEntity.location.block.type.toId() == MaterialType.WATER.numericId || livingEntity.location.block.type.toId() == MaterialType.STATIONARY_WATER.numericId
            ) {
                if (currentMilliseconds - lastPlayTime > this.aiAfraidOfWater.stoppingDelay * 1000) {
                    lastPlayTime = currentMilliseconds

                    particleService.playParticle(livingEntity.location, aiAfraidOfWater.particle, pet.getPlayer<Player>())

                    val escape = pet.getPlayer<Player>().location.toVector().subtract(pet.getLocation<Location>().toVector()).normalize().multiply(2)
                    escape.y = 1.5
                    pet.setVelocity(escape)
                }
            }

        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderAfraidOfWater.", e)
        }

        return false
    }
}