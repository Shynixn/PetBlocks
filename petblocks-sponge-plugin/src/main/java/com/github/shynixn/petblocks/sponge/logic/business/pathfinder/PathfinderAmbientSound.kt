package com.github.shynixn.petblocks.sponge.logic.business.pathfinder

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.AIAmbientSound
import com.github.shynixn.petblocks.core.logic.business.pathfinder.BasePathfinder
import com.github.shynixn.petblocks.sponge.logic.business.extension.gameMode
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.entity.living.player.gamemode.GameModes

class PathfinderAmbientSound(
    private val pet: PetProxy,
    private val aiAmbientSound: AIAmbientSound,
    private val livingEntity: Living,
    private val player: Player
) : BasePathfinder(aiAmbientSound) {

    private val soundService = PetBlocksApi.resolve(SoundService::class.java)
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
            if (!pet.meta.soundEnabled) {
                return
            }

            val value = Math.random()

            if (value > 0.98) {
                soundService.playSound(livingEntity.transform, aiAmbientSound.sound, player)
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to execute PathfinderAmbientSound.", e)
        }
    }
}