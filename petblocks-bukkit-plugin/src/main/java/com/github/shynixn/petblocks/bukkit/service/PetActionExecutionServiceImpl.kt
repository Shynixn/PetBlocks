package com.github.shynixn.petblocks.bukkit.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.petblocks.bukkit.Pet
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
    private val placeHolderService: PlaceHolderService,
    private val conditionService: ConditionService
) : PetActionExecutionService {
    private val coolDown = HashSet<Pet>()

    /**
     * Executes an pet action.
     */
    override suspend fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition) {
        if (coolDown.contains(pet)) {
            return
        }

        // Handle coolDown
        coolDown.add(pet)
        plugin.launch {
            delay(petActionDefinition.cooldown.ticks)
            coolDown.remove(pet)
        }

        // Handle execution.
        val player = pet.player

        for (action in petActionDefinition.actions) {
            if (action.condition != null) {
                // If condition evaluates to false, do not execute action.
                val placeHolderParsedCondition =
                    placeHolderService.replacePetPlaceHolders(player, pet, action.condition!!)
                val conditionResult = conditionService.evaluate(placeHolderParsedCondition)
                if (!conditionResult) {
                    continue
                }
            }

            if (action.actionType == PetActionType.COMMAND) {
                executeCommandAction(pet, action)
            } else if (action.actionType == PetActionType.DELAY) {
                executeDelayAction(action)
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
