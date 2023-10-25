package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.asyncDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.contract.ScriptService
import com.github.shynixn.petblocks.entity.PetAction
import com.github.shynixn.petblocks.entity.PetActionDefinition
import com.github.shynixn.petblocks.enumeration.PetActionCommandLevelType
import com.github.shynixn.petblocks.enumeration.PetActionType
import com.google.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetActionExecutionServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val scriptService: ScriptService,
    private val placeHolderService: PlaceHolderService
) : PetActionExecutionService {

    /**
     * Executes an pet action.
     */
    override suspend fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition) {
        val player = pet.player

        for (action in petActionDefinition.actions) {
            if (action.debug) {
                plugin.logger.log(Level.INFO, "Start pet action ${action.name} ${action.actionType}.")
            }

            if (action.condition != null) {
                // If condition evaluates to false, do not execute action.
                val placeHolderParsedCondition =
                    placeHolderService.replacePlaceHolders(player, action.condition!!, pet)

                if (action.debug) {
                    plugin.logger.log(Level.INFO, "Start evaluating condition '${placeHolderParsedCondition}'.")
                }

                val conditionResult = withContext(plugin.asyncDispatcher) {
                    scriptService.evaluate(placeHolderParsedCondition)
                }

                if (action.debug) {
                    plugin.logger.log(Level.INFO, "End evaluating condition, result ${conditionResult}.")
                }

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

            executionCommand = placeHolderService.replacePlaceHolders(pet.player, executionCommand, pet)

            if (action.debug) {
                plugin.logger.log(Level.INFO, "Start executing command '${executionCommand}'.")
            }

            if (action.level == PetActionCommandLevelType.PLAYER) {
                Bukkit.getServer().dispatchCommand(pet.player, executionCommand)
            } else {
                Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), executionCommand)
            }

            if (action.debug) {
                plugin.logger.log(Level.INFO, "End executing command '${executionCommand}'.")
            }
        }
    }

    private suspend fun executeDelayAction(action: PetAction) {
        if (action.ticks <= 0) {
            return
        }

        if (action.debug) {
            plugin.logger.log(Level.INFO, "Start delay '${action.ticks}'.")
        }

        delay(action.ticks.ticks)

        if (action.debug) {
            plugin.logger.log(Level.INFO, "End delay '${action.ticks}'.")
        }
    }
}
