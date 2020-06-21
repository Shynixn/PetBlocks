package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.NavigationService
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowOwner
import com.github.shynixn.petblocks.api.persistence.entity.AIHopping
import com.github.shynixn.petblocks.bukkit.logic.business.extension.distanceSafely
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toPosition
import com.github.shynixn.petblocks.core.logic.business.extension.relativeFront
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class PathfinderFollowOwner(
    private val pet: PetProxy,
    private val aiFollowBack: AIFollowOwner,
    private val livingEntity: LivingEntity,
    private val player: Player
) : BasePathfinder(aiFollowBack) {

    private var lastLocation: Location? = null
    private val navigationService = PetBlocksApi.resolve(NavigationService::class.java)
    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return try {
            !livingEntity.isDead && player.gameMode != GameMode.SPECTATOR && player.location.distanceSafely(
                livingEntity.location
            ) >= aiFollowBack.distanceToOwner
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
                player.location.distanceSafely(livingEntity.location) > aiFollowBack.maxRange -> {
                    pet.teleport<Any>(player.location.toPosition().relativeFront(3.0))
                    false
                }

                player.location.distanceSafely(livingEntity.location) < aiFollowBack.distanceToOwner -> false
                else -> !(lastLocation != null && lastLocation!!.distanceSafely(player.location) > 2)
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowOwner.", e)
            false
        }
    }

    /**
     * On start executing.
     */
    override fun onStartExecuting() {
        try {
            lastLocation = player.location.clone()

            val speed = if (pet.meta.aiGoals.firstOrNull { p -> p is AIHopping } != null) {
                aiFollowBack.speed + 1.0
            } else {
                aiFollowBack.speed
            }

            navigationService.navigateToLocation(pet, player.location, speed)
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowOwner.", e)
        }
    }
}