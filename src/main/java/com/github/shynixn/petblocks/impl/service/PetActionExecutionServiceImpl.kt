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
import com.github.shynixn.petblocks.enumeration.PetActionConditionType
import com.github.shynixn.petblocks.enumeration.PetActionType
import com.google.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.command.CommandSender
import org.bukkit.command.ConsoleCommandSender
import org.bukkit.conversations.Conversation
import org.bukkit.conversations.ConversationAbandonedEvent
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

    override suspend fun executeAction(eventPlayer: Player, pet: Pet, petActionDefinition: PetActionDefinition) {
        for (action in petActionDefinition.actions) {
            if (pet.isDisposed) {
                break
            }

            if (action.debug) {
                plugin.logger.log(Level.INFO, "> Start pet action ${action.name} ${action.actionType}.")
            }

            if (action.permission != null && !eventPlayer.hasPermission(action.permission!!)) {
                if (action.debug) {
                    plugin.logger.log(
                        Level.INFO,
                        "Player ${eventPlayer.name} does not have permission ${action.permission}."
                    )
                }

                continue
            }

            if (action.condition != null) {
                val condition = action.condition!!

                if (action.debug) {
                    plugin.logger.log(Level.INFO, "Start evaluating condition '${condition.type}'.")
                }

                if (condition.type == PetActionConditionType.STRING_EQUALS) {
                    val leftEscaped = placeHolderService.replacePlaceHolders(eventPlayer, condition.left!!, pet)
                    val rightEscaped = placeHolderService.replacePlaceHolders(eventPlayer, condition.right!!, pet)
                    val conditionResult = rightEscaped == leftEscaped

                    if (action.debug) {
                        plugin.logger.log(
                            Level.INFO,
                            "End evaluating condition, $leftEscaped == $rightEscaped -> ${conditionResult}."
                        )
                    }

                    if (!conditionResult) {
                        continue
                    }
                } else if (condition.type == PetActionConditionType.JAVASCRIPT) {
                    val placeHolderParsedCondition =
                        placeHolderService.replacePlaceHolders(eventPlayer, condition.js!!, pet)
                    val conditionResult = withContext(plugin.asyncDispatcher) {
                        scriptService.evaluate(placeHolderParsedCondition)
                    } as Boolean

                    if (action.debug) {
                        plugin.logger.log(
                            Level.INFO,
                            "End evaluating condition, $placeHolderParsedCondition -> ${conditionResult}."
                        )
                    }

                    if (!conditionResult) {
                        continue
                    }
                }
            }

            if (action.actionType == PetActionType.COMMAND) {
                executeCommandAction(eventPlayer, pet, action)
            } else if (action.actionType == PetActionType.DELAY) {
                executeDelayAction(action)
            } else if (action.actionType == PetActionType.JAVASCRIPT) {
                executeJavaScriptAction(eventPlayer, pet, action)
            }
        }
    }

    private fun executeCommandAction(eventPlayer: Player, pet: Pet, action: PetAction) {
        for (command in action.run) {
            var executionCommand = if (command.startsWith("/")) {
                command.substring(1)
            } else {
                command
            }

            executionCommand = placeHolderService.replacePlaceHolders(eventPlayer, executionCommand, pet)

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

    private class PetBlocksCommandSender(private val handle: CommandSender) : ConsoleCommandSender{
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

        override fun isConversing(): Boolean {
            return false
        }

        override fun acceptConversationInput(input: String) {
        }

        override fun beginConversation(conversation: Conversation): Boolean {
            return true
        }

        override fun abandonConversation(conversation: Conversation) {
        }

        override fun abandonConversation(conversation: Conversation, details: ConversationAbandonedEvent) {
        }

        override fun sendRawMessage(message: String) {
        }

        override fun sendRawMessage(sender: UUID?, message: String) {
        }
    }
}
