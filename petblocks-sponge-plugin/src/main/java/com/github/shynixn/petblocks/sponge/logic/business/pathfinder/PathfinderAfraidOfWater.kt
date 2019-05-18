package com.github.shynixn.petblocks.sponge.logic.business.pathfinder

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.AIAfraidOfWater
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import com.github.shynixn.petblocks.sponge.logic.business.extension.toId
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.toVector
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.World

class PathfinderAfraidOfWater(
    private val pet: PetProxy,
    private val aiAfraidOfWater: AIAfraidOfWater,
    private val livingEntity: Living
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

                    particleService.playParticle(livingEntity.transform, aiAfraidOfWater.particle, pet.getPlayer<Player>())

                    val escape = pet.getPlayer<Player>().transform.toPosition().subtract(pet.getLocation<Transform<World>>().toPosition()).toVector().normalize().mul(2.0)

                    pet.setVelocity(Vector3d(escape.x, 1.5, escape.z))
                }
            }

        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderAfraidOfWater.", e)
        }

        return false
    }
}