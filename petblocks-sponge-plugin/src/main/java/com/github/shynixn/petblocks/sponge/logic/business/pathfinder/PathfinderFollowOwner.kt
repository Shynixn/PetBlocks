package com.github.shynixn.petblocks.sponge.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.NavigationService
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowOwner
import com.github.shynixn.petblocks.api.persistence.entity.AIHopping
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import com.github.shynixn.petblocks.sponge.logic.business.extension.distance
import com.github.shynixn.petblocks.sponge.logic.business.extension.gameMode
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.toTransform
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.gamemode.GameModes
import org.spongepowered.api.world.World

class PathfinderFollowOwner(
    private val pet: PetProxy,
    private val aiFollowBack: AIFollowOwner,
    private val livingEntity: Living,
    private val player: Player
) : BasePathfinder(aiFollowBack) {

    private var lastLocation: Transform<World>? = null
    private val navigationService = PetBlocksApi.resolve(NavigationService::class.java)
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return try {
            !livingEntity.isRemoved && player.gameMode != GameModes.SPECTATOR && player.transform.distance(livingEntity.transform) >= aiFollowBack.distanceToOwner
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowOwner.", e)
            false
        }
    }

    /**
     * Should the goal continue executing.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        return try {
            when {
                player.transform.distance(livingEntity.transform) > aiFollowBack.maxRange -> {
                    pet.teleport(player.transform)
                    false
                }

                player.transform.distance(livingEntity.transform) < aiFollowBack.distanceToOwner -> false
                else -> !(lastLocation != null && lastLocation!!.distance(player.transform) > 2)
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowOwner.", e)
            false
        }
    }

    /**
     * On stop executing.
     */
    override fun onStopExecuting() {
        navigationService.clearNavigation(pet)
    }

    /**
     * On start executing.
     */
    override fun onStartExecuting() {
        try {
            lastLocation = player.transform.toPosition().toTransform()

            val speed = if (pet.meta.aiGoals.firstOrNull { p -> p is AIHopping } != null) {
                aiFollowBack.speed + 1.0
            } else {
                aiFollowBack.speed
            }

            navigationService.navigateToLocation(pet, player.transform, speed)
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowOwner.", e)
        }
    }
}