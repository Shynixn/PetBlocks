@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.service.CommandService
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.CommandProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.CommandRegisteredProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.nms.VersionSupport
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.SimpleCommandMap
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin

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
class CommandServiceImpl @Inject constructor(private val plugin: Plugin) : CommandService {
    private val version = VersionSupport.getServerVersion()

    /**
     * Registers a command executor.
     */
    override fun registerCommandExecutor(command: String, commandExecutorInstance: Any) {
        if (plugin !is JavaPlugin) {
            throw IllegalArgumentException("Plugin has to be a JavaPlugin!")
        }

        plugin.getCommand(command).executor = CommandRegisteredProxyImpl(commandExecutorInstance)
    }

    /**
     * Parses the command arguments to a pair of a nullable player and the string after the from point.
     */
    override fun <P> parseCommand(sender: Any, args: Array<out String>, from: Int): Pair<P, String> {
        val builder = StringBuilder()
        val player: Player?

        for (i in from until args.size) {
            if (i + 1 == args.size && Bukkit.getPlayer(args[i]) != null) {
                player = Bukkit.getPlayer(args[i])
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

    /**
     * Registers a command executor.
     */
    override fun registerCommandExecutor(commandConfiguration: Map<String, Any>, commandExecutorInstance: Any) {
        if (plugin !is JavaPlugin) {
            throw IllegalArgumentException("Plugin has to be a JavaPlugin!")
        }

        val command = commandConfiguration["command"] as String
        val description = commandConfiguration["description"] as String
        val usage = commandConfiguration["useage"] as String
        val permission = commandConfiguration["permission"] as String
        val permissionMessage = commandConfiguration["permission-message"] as String

        val commandExecutor = CommandProxyImpl(command, description, usage, permission, permissionMessage, commandExecutorInstance)
        val clazz = Class.forName("org.bukkit.craftbukkit.VERSION.CraftServer".replace("VERSION", version.versionText))
        val server = clazz.cast(Bukkit.getServer())
        val map = server.javaClass.getDeclaredMethod("getCommandMap").invoke(server) as SimpleCommandMap
        map.register(command, commandExecutor)
    }
}