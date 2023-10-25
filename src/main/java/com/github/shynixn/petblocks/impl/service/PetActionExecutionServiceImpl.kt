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
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.permissions.Permission
import org.bukkit.permissions.PermissionAttachment
import org.bukkit.permissions.PermissionAttachmentInfo
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PetActionExecutionServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val scriptService: ScriptService,
    private val placeHolderService: PlaceHolderService
) : PetActionExecutionService {
    private val commandSender = PetBlocksCommandSender(Bukkit.getConsoleSender())

    /**
     * Executes an pet action.
     */
    override suspend fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition) {
        val player = pet.player

        for (action in petActionDefinition.actions) {
            if (pet.isDisposed) {
                break
            }

            if (action.debug) {
                plugin.logger.log(Level.INFO, "> Start pet action ${action.name} ${action.actionType}.")
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
                } as Boolean

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
            } else if (action.actionType == PetActionType.JAVASCRIPT) {
                executeJavaScriptAction(player, pet, action)
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
                Bukkit.getServer().dispatchCommand(commandSender, executionCommand)
            }

            if (action.debug) {
                plugin.logger.log(Level.INFO, "End executing command '${executionCommand}'.")
            }
        }
    }

    private suspend fun executeJavaScriptAction(player: Player, pet: Pet, action: PetAction) {
        if (action.initial != null && !pet.javaScriptMemory.containsKey(action.variable)) {
            pet.javaScriptMemory[action.variable!!] = action.initial!!
        }

        val parsedJs =
            placeHolderService.replacePlaceHolders(player, action.js!!, pet)

        if (action.debug) {
            plugin.logger.log(Level.INFO, "Start evaluating JavaScript '${parsedJs}'.")
        }

        val result = withContext(plugin.asyncDispatcher) {
            scriptService.evaluate(parsedJs)
        }

        if (action.debug) {
            plugin.logger.log(Level.INFO, "End evaluating JavaScript '${result}'.")
        }

        val memory = pet.javaScriptMemory

        if (result == null && memory.containsKey(action.variable)) {
            memory.remove(action.variable)
            return
        }

        memory[action.variable!!] = result.toString()
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

    private class PetBlocksCommandSender(private val handle: CommandSender) : CommandSender {
        override fun isOp(): Boolean {
            return handle.isOp
        }

        override fun setOp(value: Boolean) {
            handle.isOp = value
        }

        override fun isPermissionSet(name: String): Boolean {
            return handle.isPermissionSet(name)
        }

        override fun isPermissionSet(perm: Permission): Boolean {
            return handle.isPermissionSet(perm)
        }

        override fun hasPermission(name: String): Boolean {
            return handle.hasPermission(name)
        }

        override fun hasPermission(perm: Permission): Boolean {
            return handle.hasPermission(perm)
        }

        override fun addAttachment(plugin: Plugin, name: String, value: Boolean): PermissionAttachment {
            return handle.addAttachment(plugin, name, value)
        }

        override fun addAttachment(plugin: Plugin): PermissionAttachment {
            return handle.addAttachment(plugin)
        }

        override fun addAttachment(plugin: Plugin, name: String, value: Boolean, ticks: Int): PermissionAttachment? {
            return handle.addAttachment(plugin, name, value, ticks)
        }

        override fun addAttachment(plugin: Plugin, ticks: Int): PermissionAttachment? {
            return handle.addAttachment(plugin, ticks)
        }

        override fun removeAttachment(attachment: PermissionAttachment) {
            handle.removeAttachment(attachment)
        }

        override fun recalculatePermissions() {
            handle.recalculatePermissions()
        }

        override fun getEffectivePermissions(): MutableSet<PermissionAttachmentInfo> {
            return handle.effectivePermissions
        }

        override fun sendMessage(message: String) {
        }

        override fun sendMessage(vararg messages: String?) {
        }

        override fun sendMessage(sender: UUID?, message: String) {
        }

        override fun sendMessage(sender: UUID?, vararg messages: String?) {
        }

        override fun getServer(): Server {
            return handle.server
        }

        override fun getName(): String {
            return "PetBlocksCommandSender"
        }

        override fun spigot(): CommandSender.Spigot {
            return handle.spigot()
        }
    }
}
