package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.sponge.logic.business.extension.sendMessage
import com.github.shynixn.petblocks.sponge.logic.business.extension.toText
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.entity.living.player.Player
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.network.play.server.SPacketChat
import net.minecraft.util.text.ChatType
import net.minecraft.util.text.ITextComponent

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class MessageServiceImpl : MessageService {
    /**
     * Sends a colored console message.
     */
    override fun sendConsoleMessage(message: String) {
        Sponge.getServer().console.sendMessage(message)
    }

    /**
     * Sends a message to the given source.
     */
    override fun <S> sendSourceMessage(source: S, message: String, prefix: Boolean) {
        if (source !is CommandSource) {
            throw IllegalArgumentException("Source has to be a sender!")
        }

        if (prefix) {
            source.sendMessage((Messages.prefix + message).toText())
        } else {
            source.sendMessage(message.toText())
        }
    }

    /**
     * Sends the given [chatMessage] to the given [player].
     * @param P the type of the player.
     */
    override fun <P> sendPlayerMessage(player: P, chatMessage: ChatMessage) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        val components = chatMessage.components

        if (components.isEmpty()) {
            throw IllegalArgumentException("Components amount of message is 0. Are you not using the parent component?")
        }

        val finalMessage = StringBuilder()
        val cache = StringBuilder()
        finalMessage.append("{\"text\": \"\"")
        finalMessage.append(", \"extra\" : [")
        var firstExtra = false

        for (component in components) {
            if (component !is ChatColor && firstExtra) {
                finalMessage.append(", ")
            }

            when (component) {
                is ChatColor -> cache.append(component)

                is String -> {
                    finalMessage.append("{\"text\": \"")
                    finalMessage.append(ChatColor.translateChatColorCodes('&', cache.toString() + component))
                    finalMessage.append("\"}")
                    cache.setLength(0)
                    firstExtra = true
                }

                else -> {
                    finalMessage.append(component)
                    firstExtra = true
                }
            }
        }
        finalMessage.append("]}")

        val playerResult = arrayOfNulls<Player>(1)
        playerResult[0] = player

        val component = ITextComponent.Serializer.jsonToComponent(finalMessage.toString())
        val packetChatMessage = SPacketChat(component!!, ChatType.CHAT)

        (player as EntityPlayerMP).connection.sendPacket(packetChatMessage)
    }
}