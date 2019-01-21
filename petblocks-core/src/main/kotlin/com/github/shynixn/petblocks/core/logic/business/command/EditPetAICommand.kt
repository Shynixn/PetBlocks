@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.command

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIBase

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
class EditPetAICommand @Inject constructor(
    private val proxyService: ProxyService,
    private val petMetaService: PersistencePetMetaService,
    private val configurationService: ConfigurationService,
    private val messageService: MessageService,
    private val aiService: AIService,
    private val commandService: CommandService
) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        if (args.size < 2 || !args[0].equals("ai", true)) {
            return false
        }

        val result = commandService.parseCommand<Any?>(source as Any, args, 2)

        if (result.first == null) {
            return false
        }

        val playerProxy = proxyService.findPlayerProxyObject(result.first)

        petMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAccept { petMeta ->
            try {
                val configuration = configurationService.findValue<Map<String, Any>>(args[1])
                var addAmount = 0
                var removeAmount = 0

                if (configuration.containsKey("remove-ai")) {
                    val removeAis = configuration["remove-ai"] as Map<String, Any?>

                    for (mapData in removeAis.keys) {
                        val data = removeAis[mapData] as Map<String, Any?>
                        val aiBase = aiService.deserializeAiBase<AIBase>(data["type"] as String, data)

                        removeAmount += petMeta.aiGoals.filter { a -> a.type == aiBase.type }.size
                        petMeta.aiGoals.removeAll { a -> a.type == aiBase.type }
                    }
                }

                if (configuration.containsKey("add-ai")) {
                    val addAis = configuration["add-ai"] as Map<String, Any?>

                    for (mapData in addAis.keys) {
                        val data = addAis[mapData] as Map<String, Any?>
                        val aiBase = aiService.deserializeAiBase<AIBase>(data["type"] as String, data)

                        removeAmount += petMeta.aiGoals.filter { a -> a.type == aiBase.type }.size
                        petMeta.aiGoals.removeAll { a -> a.type == aiBase.type }
                    }

                    addAmount += addAis.size
                }

                messageService.sendSourceMessage(source, "Added $addAmount new ais and removed $removeAmount ais to/from player ${playerProxy.name}.")
            } catch (e: Exception) {
                messageService.sendSourceMessage(source, ChatColor.RED.toString() + e.message)
            }
        }

        return true
    }
}