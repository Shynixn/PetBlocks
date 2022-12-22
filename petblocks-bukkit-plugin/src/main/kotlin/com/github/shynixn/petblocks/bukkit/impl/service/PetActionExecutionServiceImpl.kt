package com.github.shynixn.petblocks.bukkit.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PetActionExecutionService
import com.github.shynixn.petblocks.bukkit.contract.PlaceHolderService
import com.github.shynixn.petblocks.bukkit.entity.PetAction
import com.github.shynixn.petblocks.bukkit.entity.PetActionCommandLevelType
import com.github.shynixn.petblocks.bukkit.entity.PetActionDefinition
import com.github.shynixn.petblocks.bukkit.entity.PetActionType
import com.google.inject.Inject
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin

class PetActionExecutionServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val placeHolderService: PlaceHolderService
) : PetActionExecutionService {
    /**
     * Executes an pet action.
     */
    override fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition) {
        plugin.launch {
            for (action in petActionDefinition.actions) {
                if (action.actionType == PetActionType.COMMAND) {
                    executeCommandAction(pet, action)
                } else if (action.actionType == PetActionType.DELAY) {
                    executeDelayAction(action)
                }
            }
        }
    }

    private fun executeCommandAction(pet: Pet, action: PetAction) {
        for (command in action.run) {
            var executionCommand = if (command.startsWith("/")) {
                command.substring(1)
            } else {
                command
            }

            executionCommand = placeHolderService.replacePetPlaceHolders(pet.player, pet, executionCommand)

            if (action.level == PetActionCommandLevelType.PLAYER) {
                Bukkit.getServer().dispatchCommand(pet.player, executionCommand)
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), executionCommand)
            }
        }
    }

    private suspend fun executeDelayAction(action: PetAction) {
        if (action.ticks <= 0) {
            return
        }

        delay(action.ticks.ticks)
    }
}
