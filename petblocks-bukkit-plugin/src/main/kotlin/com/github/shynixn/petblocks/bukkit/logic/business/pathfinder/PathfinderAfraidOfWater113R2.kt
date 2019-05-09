package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.AIAfraidOfWater
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toId
import net.minecraft.server.v1_13_R2.EntityCreature
import net.minecraft.server.v1_13_R2.PathfinderGoalRandomStrollLand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class PathfinderAfraidOfWater113R2(
    private val pet: PetProxy,
    private val aiAfraidOfWater: AIAfraidOfWater,
    private val livingEntity: LivingEntity,
    nmsLivingEntity: Any
) : PathfinderGoalRandomStrollLand(nmsLivingEntity as EntityCreature, 1.0, 0.0f) {

    private var remainingDelay = 0L

    private val particleService = PetBlocksApi.resolve(ParticleService::class.java)
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)

    /**
     * OnExecute.
     */
    override fun e() {
        super.e()

        try {
            if (livingEntity.location.block.type.toId() == MaterialType.WATER.numericId || livingEntity.location.block.type.toId() == MaterialType.STATIONARY_WATER.numericId) {
                remainingDelay = System.currentTimeMillis() + aiAfraidOfWater.stoppingDelay * 1000
            }

            if (remainingDelay > System.currentTimeMillis()) {
                particleService.playParticle(livingEntity.location, aiAfraidOfWater.particle, pet.getPlayer<Player>())
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderAfraid of water.", e)
        }
    }
}