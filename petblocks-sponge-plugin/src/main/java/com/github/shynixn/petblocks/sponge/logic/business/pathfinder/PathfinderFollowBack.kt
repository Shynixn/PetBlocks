package com.github.shynixn.petblocks.sponge.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowBack
import com.github.shynixn.petblocks.core.logic.business.extension.relativeBack
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import com.github.shynixn.petblocks.sponge.logic.business.extension.gameMode
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.gamemode.GameModes

class PathfinderFollowBack(
    private val pet: PetProxy,
    aiFollowBack: AIFollowBack,
    private val livingEntity: Living,
    private val player: Player
) : BasePathfinder(aiFollowBack) {

    private val loggingService = PetBlocksApi.resolve(LoggingService::class.java)

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !livingEntity.isRemoved && player.gameMode != GameModes.SPECTATOR
    }

    /**
     * On execute.
     */
    override fun onExecute() {
        try {
            val position = player.transform.toPosition().add(0.0, -0.5).relativeBack(-1.0)
            pet.teleport(position)
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderFollowBack.", e)
        }
    }
}