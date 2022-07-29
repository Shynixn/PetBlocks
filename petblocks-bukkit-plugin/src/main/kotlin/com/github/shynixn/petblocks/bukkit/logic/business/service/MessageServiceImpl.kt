package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.MessageService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.extension.sendPacket
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
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
class MessageServiceImpl @Inject constructor(private val version: Version) : MessageService {
    /**
     * Sends a message to the given source.
     */
    override fun <S> sendSourceMessage(source: S, message: String, prefix: Boolean) {
        if (source !is CommandSender) {
            throw IllegalArgumentException("Source has to be a sender!")
        }

        if (prefix) {
            source.sendMessage(Messages.prefix + message)
        } else {
            source.sendMessage(message)
        }
    }

    /**
     * Sends a colored console message.
     */
    override fun sendConsoleMessage(message: String) {
        Bukkit.getServer().consoleSender.sendMessage(message)
    }

    /**
     * Sends the given [chatMessage] to the given [player].
     */
    override fun <P> sendPlayerMessage(player: P, chatMessage: ChatMessage) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val components = chatMessage.components
        var firstExtra = false

        if (components.isEmpty()) {
            throw IllegalArgumentException("Components amount of message is 0. Are you not using the parent component?")
        }

        val finalMessage = StringBuilder()
        val cache = StringBuilder()
        finalMessage.append("{\"text\": \"\"")
        finalMessage.append(", \"extra\" : [")

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

        val packet =
            if (version.isVersionSameOrGreaterThan(Version.VERSION_1_19_R1)) {
                val clazz = Class.forName("net.minecraft.network.chat.IChatBaseComponent\$ChatSerializer")
                val packetClazz = findClazz("net.minecraft.network.protocol.game.ClientboundSystemChatPacket")
                val chatBaseComponentClazz = findClazz("net.minecraft.network.chat.IChatBaseComponent")
                val chatComponent =
                    clazz.getDeclaredMethod("a", String::class.java).invoke(null, finalMessage.toString())
                packetClazz.getDeclaredConstructor(chatBaseComponentClazz, Boolean::class.java)
                    .newInstance(chatComponent,false)
            } else if (version.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1)) {
                val clazz = Class.forName("net.minecraft.network.chat.IChatBaseComponent\$ChatSerializer")
                val packetClazz = findClazz("net.minecraft.network.protocol.game.PacketPlayOutChat")
                val chatBaseComponentClazz = findClazz("net.minecraft.network.chat.IChatBaseComponent")
                val chatComponent =
                    clazz.getDeclaredMethod("a", String::class.java).invoke(null, finalMessage.toString())
                val systemUtilsClazz = findClazz("net.minecraft.SystemUtils")
                val defaultUUID = if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R2)) {
                    systemUtilsClazz.getDeclaredField("c").get(null) as UUID
                } else {
                    systemUtilsClazz.getDeclaredField("b").get(null) as UUID
                }
                val chatEnumMessage = findClazz("net.minecraft.network.chat.ChatMessageType")
                packetClazz.getDeclaredConstructor(chatBaseComponentClazz, chatEnumMessage, UUID::class.java)
                    .newInstance(chatComponent, chatEnumMessage.enumConstants[0], defaultUUID)
            } else {
                val clazz: Class<*> = if (version == Version.VERSION_1_8_R1) {
                    findClazz("net.minecraft.server.VERSION.ChatSerializer")
                } else {
                    findClazz("net.minecraft.server.VERSION.IChatBaseComponent\$ChatSerializer")
                }

                val packetClazz = findClazz("net.minecraft.server.VERSION.PacketPlayOutChat")
                val chatBaseComponentClazz = findClazz("net.minecraft.server.VERSION.IChatBaseComponent")

                val method = clazz.getDeclaredMethod("a", String::class.java)
                method.isAccessible = true
                val chatComponent = method.invoke(null, finalMessage.toString())
                when {
                    version.isVersionSameOrGreaterThan(Version.VERSION_1_16_R1) -> {
                        val systemUtilsClazz = findClazz("net.minecraft.server.VERSION.SystemUtils")
                        val defaultUUID = systemUtilsClazz.getDeclaredField("b").get(null) as UUID
                        val chatEnumMessage = findClazz("net.minecraft.server.VERSION.ChatMessageType")
                        packetClazz.getDeclaredConstructor(chatBaseComponentClazz, chatEnumMessage, UUID::class.java)
                            .newInstance(chatComponent, chatEnumMessage.enumConstants[0], defaultUUID)
                    }
                    version.isVersionSameOrGreaterThan(Version.VERSION_1_12_R1) -> {
                        val chatEnumMessage = findClazz("net.minecraft.server.VERSION.ChatMessageType")
                        packetClazz.getDeclaredConstructor(chatBaseComponentClazz, chatEnumMessage)
                            .newInstance(chatComponent, chatEnumMessage.enumConstants[0])
                    }
                    else -> {
                        packetClazz.getDeclaredConstructor(chatBaseComponentClazz, Byte::class.javaPrimitiveType!!)
                            .newInstance(chatComponent, 0.toByte())
                    }
                }
            }

        player.sendPacket(packet)
    }
}
