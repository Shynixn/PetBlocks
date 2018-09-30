package com.github.shynixn.petblocks.sponge.logic.business.proxy

import com.github.shynixn.petblocks.api.business.command.PlayerCommand
import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.sponge.logic.business.extension.sendMessage
import org.spongepowered.api.command.CommandResult
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.command.args.CommandContext
import org.spongepowered.api.command.spec.CommandExecutor
import org.spongepowered.api.entity.living.player.Player
import java.util.*

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
class CommandProxyImpl(private val permission: String, private val permissionMessage: String, private val instance: Any) : CommandExecutor {
    /**
     * Gets called when a command source calls the command with the given context.
     */
    override fun execute(src: CommandSource, args: CommandContext): CommandResult {
        val tmpArguments = args.getAll<String>("text")
        val preArguments = ArrayList<String>()

        for (s in tmpArguments) {
            if (s.contains(" ")) {
                preArguments.addAll(s.split(" "))
            } else {
                preArguments.add(s)
            }
        }

        val arguments = preArguments.toTypedArray()

        if (src is Player) {
            if (!src.hasPermission(this.permission)) {
                src.sendMessage(this.permissionMessage)
            } else {
                if (instance is PlayerCommand) {
                    instance.onPlayerExecuteCommand(src, arguments)
                }
            }
        }

        if (instance is SourceCommand) {
            instance.onExecuteCommand(src, arguments)
        }

        return CommandResult.success()
    }
}