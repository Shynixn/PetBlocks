package com.github.shynixn.petblocks.bukkit.logic.business.helper

import com.github.shynixn.petblocks.bukkit.nms.VersionSupport
import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils.invokeConstructor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.InvocationTargetException
import java.util.logging.Level

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
/**
 * Sends the built message to the given players
 *
 * @param players players
 */
fun ChatBuilder.sendMessage(vararg players: Player) {
    val finalMessage = StringBuilder()
    val cache = StringBuilder()
    finalMessage.append("{\"text\": \"\"")
    finalMessage.append(", \"extra\" : [")
    var firstExtra = false
    for (component in this.components) {

        if (component !is ChatColor && firstExtra) {
            finalMessage.append(", ")
        }

        when (component) {
            is ChatColor -> cache.append(component)
            is String -> {
                finalMessage.append("{\"text\": \"")
                finalMessage.append(ChatColor.translateAlternateColorCodes('&', cache.toString() + component))
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
    try {
        val clazz: Class<*> = if (Bukkit.getServer()::class.java.`package`.name.replace(".", ",").split(",")[3] == "v1_8_R1") {
            Class.forName("net.minecraft.server.VERSION.ChatSerializer".findServerVersion())
        } else {
            Class.forName("net.minecraft.server.VERSION.IChatBaseComponent\$ChatSerializer".findServerVersion())
        }

        val packetClazz = Class.forName("net.minecraft.server.VERSION.PacketPlayOutChat".findServerVersion())
        val chatBaseComponentClazz =  Class.forName("net.minecraft.server.VERSION.IChatBaseComponent".findServerVersion())

        val method = clazz.getDeclaredMethod("a",String::class.java)
        method.isAccessible = true
        val chatComponent = method.invoke(null, finalMessage.toString())
        val packet: Any
        packet = if (VersionSupport.getServerVersion().versionText == "v1_12_R1") {
            val chatEnumMessage = Class.forName("net.minecraft.server.VERSION.ChatMessageType".findServerVersion())
            invokeConstructor(packetClazz, arrayOf(chatBaseComponentClazz, chatEnumMessage), arrayOf(chatComponent, chatEnumMessage.enumConstants[0]))
        } else {
            invokeConstructor(packetClazz, arrayOf(chatBaseComponentClazz, Byte::class.javaPrimitiveType!!), arrayOf(chatComponent, 0.toByte()))
        }
        for (player in players) {
            sendPacket(player, packet)
        }
    } catch (e: Exception) {
        Bukkit.getLogger().log(Level.WARNING, "Failed to send packet.", e)
    }
}

/**
 * Sends a packet to the client player
 *
 * @param player player
 * @param packet packet
 * @throws ClassNotFoundException    exception
 * @throws IllegalAccessException    exception
 * @throws NoSuchMethodException     exception
 * @throws InvocationTargetException exception
 * @throws NoSuchFieldException      exception
 */

@Throws(ClassNotFoundException::class, IllegalAccessException::class, NoSuchMethodException::class, InvocationTargetException::class, NoSuchFieldException::class)
private fun sendPacket(player: Player, packet: Any) {
    val craftPlayer = Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".findServerVersion()).cast(player)
    val entityPlayer = craftPlayer.javaClass.getDeclaredMethod("getHandle").invoke(craftPlayer)
    val field = entityPlayer.javaClass.getDeclaredField("playerConnection")
    field.isAccessible = true
    val connection = field.get(entityPlayer)
    connection.javaClass.getDeclaredMethod("sendPacket", packet.javaClass.interfaces[0]).invoke(connection,packet)
}