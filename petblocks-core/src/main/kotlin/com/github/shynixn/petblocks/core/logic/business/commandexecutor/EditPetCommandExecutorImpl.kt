package com.github.shynixn.petblocks.core.logic.business.commandexecutor

import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.business.command.*
import com.github.shynixn.petblocks.core.logic.persistence.entity.ChatMessageEntity
import com.google.inject.Inject

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class EditPetCommandExecutorImpl @Inject constructor(
    editPetAICommand: EditPetAICommand,
    editPetDisableCommand: EditPetDisableCommand,
    editPetEnableCommand: EditPetEnableCommand,
    killNextCommand: EditPetKillNextCommand,
    editPetRenameCommand: EditPetRenameCommand,
    editPetResetCommand: EditPetResetCommand,
    editPetSkinCommand: EditPetSkinCommand,
    editPetToggleCommand: EditPetToggleCommand,
    editPetToggleParticleCommand: EditPetToggleParticleCommand,
    editPetToggleSoundCommand: EditPetToggleSoundCommand,
    private val messageService: MessageService,
    private val proxyService: ProxyService,
    private val configurationService: ConfigurationService
) : SourceCommand {
    private val commands = ArrayList<SourceCommand>()

    init {
        commands.addAll(arrayOf(editPetAICommand, editPetDisableCommand, editPetEnableCommand,
            killNextCommand, editPetRenameCommand, editPetResetCommand, editPetSkinCommand,
            editPetToggleCommand, editPetToggleParticleCommand, editPetToggleSoundCommand))
    }

    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        var called = false

        commands.forEach { command ->
            val commandCalled = command.onExecuteCommand(source, args)

            if (commandCalled) {
                called = true
            }
        }

        if (called) {
            return true
        }

        val command = "/" + configurationService.findValue<String>("commands.petblocks.command") + " "
        val senderName = if (proxyService.isPlayer(source)) {
            proxyService.findPlayerProxyObject(source).name
        } else {
            "Unknown"
        }

        if (args.size == 1 && args[0].equals("3", ignoreCase = true)) {
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ", false)
            messageService.sendSourceMessage(source, "", false)
            this.sendMessage(source,
                "togglesound [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Toggles all pet sounds of the given player.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "togglesound",
                    command + "togglesound " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "toggleparticle [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Toggles all pet particles of the given player.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "toggleparticle",
                    command + "toggleparticle " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "killnext",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Kills the nearest entity to the player. Does not kill other players.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "killnext",
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌3/3┐                            ", false)
            messageService.sendSourceMessage(source, "", false)
        } else if (args.size == 1 && args[0].equals("2", ignoreCase = true)) {
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ", false)
            messageService.sendSourceMessage(source, "", false)
            this.sendMessage(source,
                "ai <path> [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Adds and removes the ais from the given config path.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "ai pet",
                    command + "ai mycustomais.aicontainer1",
                    command + "ai mycustomais.aicontainer2 " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "skin <path> [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Sets the skin from the given config path.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "skin pet.skin",
                    command + "skin gui.player-head-skins.block-1-1.set-skin",
                    command + "skin mycustomskins.skincontainer2 " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "rename <name> [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Renames the pet.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "rename Beast",
                    command + "rename My awesome Pet",
                    command + "rename My Pet " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌2/3┐                            ", false)
            messageService.sendSourceMessage(source, "", false)
        } else {
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ", false)
            if (proxyService.isPlayer(source)) {
                messageService.sendSourceMessage(source, "", false)
                messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.ITALIC + "Move your mouse over the commands to display tooltips!", false)
            }
            messageService.sendSourceMessage(source, "", false)
            this.sendMessage(source,
                "enable [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Respawns the pet of the given player.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "enable",
                    command + "enable " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "disable [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Removes the pet of the given player.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "disable",
                    command + "disable " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "toggle [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Enables or disables the pet.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "toggle",
                    command + "toggle " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source,
                "reset [player]",
                arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET,
                    "Resets the pet data to the default pet data.",
                    ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET,
                    command + "reset",
                    command + "reset " + senderName,
                    ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "", false)
            messageService.sendSourceMessage(source,
                ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌1/3┐                            ", false)
            messageService.sendSourceMessage(source, "", false)
        }

        return true
    }

    /**
     * Sends a message to the commandSender.
     */
    private fun <S> sendMessage(commandSender: S, message: String, hoverText: Array<String>) {
        val command = "/" + configurationService.findValue<String>("commands.petblocks.command") + " "
        val prefix = configurationService.findValue<String>("messages.prefix")

        if (proxyService.isPlayer(commandSender)) {
            val builder = StringBuilder()
            for (s in hoverText) {
                if (builder.isNotEmpty()) {
                    builder.append('\n')
                }
                builder.append(s)
            }

            var fullCommand = command + message
            when {
                fullCommand.contains("<") -> fullCommand = fullCommand.substring(0, fullCommand.indexOf("<"))
                fullCommand.contains("[") -> fullCommand = fullCommand.substring(0, fullCommand.indexOf("["))
                fullCommand.contains("-") -> fullCommand = fullCommand.substring(0, fullCommand.indexOf("-"))
            }

            val finalFullCommand = fullCommand
            val internalMessage = ChatMessageEntity().appendComponent()
                .append(prefix + command + message)
                .setClickAction(ChatClickAction.SUGGEST_COMMAND, finalFullCommand)
                .appendHoverComponent().append(builder.toString()).getRoot()

            this.messageService.sendPlayerMessage(commandSender, internalMessage)
        } else {
            messageService.sendSourceMessage(commandSender, command + message)
        }
    }
}