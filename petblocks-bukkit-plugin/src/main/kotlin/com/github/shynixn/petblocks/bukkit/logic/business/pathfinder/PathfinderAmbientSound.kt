package com.github.shynixn.petblocks.bukkit.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.AIAmbientSound
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import org.bukkit.GameMode
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

class PathfinderAmbientSound(
    private val pet: PetProxy,
    private val aiAmbientSound: AIAmbientSound,
    private val livingEntity: LivingEntity,
    private val player: Player
) : BasePathfinder(aiAmbientSound) {

    private val soundService = PetBlocksApi.resolve(SoundService::class.java)

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
        if (pet.meta.soundEnabled) {
            return
        }

        val value = Math.random()

        if (value > 0.98) {
            soundService.playSound(livingEntity.location, aiAmbientSound.sound, player)
        }
    }
}