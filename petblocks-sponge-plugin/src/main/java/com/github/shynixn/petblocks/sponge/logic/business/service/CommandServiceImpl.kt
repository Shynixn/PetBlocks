@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.service.CommandService
import com.github.shynixn.petblocks.sponge.logic.business.extension.translateToText
import com.github.shynixn.petblocks.sponge.logic.business.proxy.CommandProxyImpl
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.args.GenericArguments
import org.spongepowered.api.command.spec.CommandSpec
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text

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
class CommandServiceImpl @Inject constructor(private val pluginContainer: PluginContainer) : CommandService {
    /**
     * Registers a command executor.
     */
    override fun registerCommandExecutor(command: String, commandExecutorInstance: Any) {
        if (command != "petblocksreload") {
            throw IllegalArgumentException("This registering process is only available for petblocksreload!")
        }

        val description = "Reloads the petblock config and lang file."
        val permission = "petblocks.command.reload"
        val permissionMessage = "You don't have permission."

        val commandSpec = CommandSpec.builder()
                .description(description.translateToText())
                .permission(permission)
                .executor(CommandProxyImpl(permission, permissionMessage, commandExecutorInstance)).build()

        Sponge.getCommandManager().register(this.pluginContainer, commandSpec, command)
    }

    /**
     * Registers a command executor.
     */
    override fun registerCommandExecutor(commandConfiguration: Map<String, Any>, commandExecutorInstance: Any) {
        if (commandConfiguration.containsKey("enabled") && commandConfiguration["enabled"] == false) {
            return
        }

        val command = commandConfiguration["command"] as String
        val description = commandConfiguration["description"] as String
        val permission = commandConfiguration["permission"] as String
        val permissionMessage = commandConfiguration["permission-message"] as String

        val commandSpec = CommandSpec.builder()
                .description(description.translateToText())
                .permission(permission)
                .executor(CommandProxyImpl(permission, permissionMessage, commandExecutorInstance))
                .arguments(GenericArguments.optionalWeak(GenericArguments.remainingRawJoinedStrings(Text.of("text"))))
                .build()

        Sponge.getCommandManager().register(this.pluginContainer, commandSpec, command)
    }

    /**
     * Parses the command arguments to a pair of a nullable player and the string after the from point.
     */
    override fun <P> parseCommand(sender: Any, args: Array<out String>, from: Int): Pair<P, String> {
        val builder = StringBuilder()
        val player: Player?

        for (i in from until args.size) {
            if (i + 1 == args.size && Sponge.getGame().server.getPlayer(args[i]).isPresent) {
                player = Sponge.getGame().server.getPlayer(args[i]).get()
                return Pair(player as P, builder.toString())
            }

            if (builder.isNotEmpty()) {
                builder.append(' ')
            }

            builder.append(args[i])
        }

        return if (sender is Player) {
            Pair(sender as P, builder.toString())
        } else {
            Pair(null as P, builder.toString())
        }
    }
}