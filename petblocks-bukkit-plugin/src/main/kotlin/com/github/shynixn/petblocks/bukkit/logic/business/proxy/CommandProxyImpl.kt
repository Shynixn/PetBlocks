package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.command.PlayerCommand
import com.github.shynixn.petblocks.api.business.command.SourceCommand
import org.bukkit.command.CommandSender
import org.bukkit.command.defaults.BukkitCommand
import org.bukkit.entity.Player

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
class CommandProxyImpl(
    command: String,
    description: String,
    usage: String,
    permission: String,
    permissionMessage: String,
    aliases: List<String>,
    private val instance: Any
) : BukkitCommand(command) {
    /**
     * Init.
     */
    init {
        this.setDescription(description)
        this.usage = usage
        this.permission = permission
        this.permissionMessage = permissionMessage
        this.aliases = aliases
    }

    /**
     * Gets called when the user enters a command.
     */
    override fun execute(commandSender: CommandSender, alias: String, args: Array<out String>): Boolean {
        if (!commandSender.hasPermission(this.permission!!)) {
            commandSender.sendMessage(this.permissionMessage!!)
            return true
        }

        if (instance is PlayerCommand && commandSender is Player) {
            instance.onPlayerExecuteCommand(commandSender, args)
        }

        if (instance is SourceCommand) {
            instance.onExecuteCommand(commandSender, args)
        }

        return true
    }
}