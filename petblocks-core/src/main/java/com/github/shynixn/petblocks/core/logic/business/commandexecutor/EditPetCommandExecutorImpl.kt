package com.github.shynixn.petblocks.core.logic.business.commandexecutor

import com.github.shynixn.petblocks.api.business.commandexecutor.EditPetCommandExecutor
import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.business.command.EditPetKillNextCommand
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
class EditPetCommandExecutorImpl @Inject constructor(private val killNextCommand: EditPetKillNextCommand, private val messageService: MessageService, private val proxyService: ProxyService, private val configurationService: ConfigurationService) : EditPetCommandExecutor {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        val called = killNextCommand.onExecuteCommand(source, args)

        if (called) {
            return true
        }

        val command = "/" + configurationService.findValue<String>("petblocks-configuration.command") + " "
        val senderName = proxyService.getNameOfInstance(source)
        
        if (args.size == 1 && args[0].equals("3", ignoreCase = true)) {
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ")
            messageService.sendSourceMessage(source, "")
            this.sendMessage(source, "hat [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Starts wearing the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "hat", command + "hat " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "ride [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Starts riding the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "ride", command + "ride " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "item-name <text> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Changes the name of the PetBlock item when it is inside of the inventory of the player.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "item-name Petblock", command + "item-name Amazing Beast", command + "item-name My block " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "item-lore <line> <text> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Changes the lore of the PetBlock item when it is inside of the inventory of the player.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "item-lore 1 Beast", command + "item-lore 2 This is my pet", command + "item-lore 2 PetBlock " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "killnext", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Kills the nearest entity to the player. Does not kill other players.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "killnext", ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌3/3┐                            ")
            messageService.sendSourceMessage(source, "")
        } else if (args.size == 1 && args[0].equals("2", ignoreCase = true)) {
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ")
            messageService.sendSourceMessage(source, "")
            this.sendMessage(source, "engine <number> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Changes the engine being used of the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "engine 1", command + "engine 2 " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "costume <category> <number> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Changes the costume of the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "costume simple-blocks 1", command + "costume simple-blocks 1 " + senderName, command + "costume colored-blocks 2", command + "costume player-heads 3", command + "costume minecraft-heads 1", ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "rename <name> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Renames the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "rename Beast", command + "rename My awesome Pet", command + "rename My Pet " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "skin <account/url> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Replaces the costume of the PetBlock with the given skin.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "skin Shynixn", command + "skin Shynixn " + senderName, command + "skin http://textures.minecraft.net/texture/707dab2cbebea539b64d5ad246f9ccc1fcda7aa94b88e59fc2829852f46071", ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "particle <number> [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Changes the particle of the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "particle 2", command + "particle 3 " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌2/3┐                            ")
            messageService.sendSourceMessage(source, "")
        } else {
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + ChatColor.UNDERLINE + "                   PetBlocks " + "                       ")
            if (proxyService.isPlayer(source)) {
                messageService.sendSourceMessage(source, "")
                messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.ITALIC + "Move your mouse over the commands to display tooltips!")
            }
            messageService.sendSourceMessage(source, "")
            this.sendMessage(source, "enable [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Respawns the PetBlock of the given player.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "enable", command + "enable " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "disable [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Removes the PetBlock of the given player.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "disable", command + "disable " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "toggle [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Enables or disables the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "toggle", command + "toggle " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            this.sendMessage(source, "toggle-sound [player]", arrayOf(ChatColor.BLUE.toString() + "Description:" + ChatColor.RESET, "Enables or disables the sounds of the PetBlock.", ChatColor.YELLOW.toString() + "Examples:" + ChatColor.RESET, command + "toggle-sound", command + "toggle-sound " + senderName, ChatColor.GOLD.toString() + "<<Click me>>"))
            messageService.sendSourceMessage(source, "")
            messageService.sendSourceMessage(source, ChatColor.DARK_GREEN.toString() + "" + ChatColor.BOLD + "" + ChatColor.UNDERLINE + "                           ┌1/3┐                            ")
            messageService.sendSourceMessage(source, "")
        }

        return true
    }

    /**
     * Sends a message to the commandSender.
     */
    private fun <S> sendMessage(commandSender: S, message: String, hoverText: Array<String>) {
        val command = "/" + configurationService.findValue<String>("petblocks-configuration.command") + " "
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
            messageService.sendSourceMessage(commandSender, prefix + command + message)
        }
    }
}