package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.NavigationService
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowOwner
import com.github.shynixn.petblocks.api.persistence.entity.AIHopping
import com.github.shynixn.petblocks.bukkit.logic.business.extension.distanceSafely
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

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !livingEntity.isDead && player.gameMode != GameMode.SPECTATOR && player.location.distanceSafely(
            livingEntity.location
        ) >= aiFollowBack.distanceToOwner
    }

    /**
     * Should the goal continue executing.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        return when {
            player.location.distanceSafely(livingEntity.location) > aiFollowBack.maxRange -> {
                pet.teleport(player.location)
                false
            }

            player.location.distanceSafely(livingEntity.location) < aiFollowBack.distanceToOwner -> false
            else -> !(lastLocation != null && lastLocation!!.distanceSafely(player.location) > 2)
        }
    }

    /**
     * On start executing.
     */
    override fun onStartExecuting() {
        lastLocation = player.location.clone()

        val speed = if (pet.meta.aiGoals.firstOrNull { p -> p is AIHopping } != null) {
            aiFollowBack.speed + 1.0
        } else {
            aiFollowBack.speed
        }

        navigationService.navigateToLocation(pet, player.location, speed)
    }
}