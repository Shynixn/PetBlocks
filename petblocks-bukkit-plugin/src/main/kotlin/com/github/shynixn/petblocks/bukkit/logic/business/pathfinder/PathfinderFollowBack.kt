package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowBack
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toPosition
import com.github.shynixn.petblocks.core.logic.business.extension.relativeBack
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import org.bukkit.GameMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class PathfinderFollowBack(
    private val pet: PetProxy,
    aiFollowBack: AIFollowBack,
    private val livingEntity: LivingEntity,
    private val player: Player
) : BasePathfinder(aiFollowBack) {
    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !livingEntity.isDead && player.gameMode != GameMode.SPECTATOR
    }

    /**
     * On execute.
     */
    override fun onExecute() {
        val position = player.location.toPosition().relativeBack(-1.0)
        pet.teleport(position)
    }
}