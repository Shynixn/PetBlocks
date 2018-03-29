package com.github.shynixn.petblocks.bukkit.logic.business.helper

import com.github.shynixn.petblocks.core.logic.business.helper.ChatBuilder
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
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
        if (component is ChatColor) {
            cache.append(component)
        } else if (component is String) {
            finalMessage.append("{\"text\": \"")
            finalMessage.append(ChatColor.translateAlternateColorCodes('&', cache.toString() + component))
            finalMessage.append("\"}")
            cache.setLength(0)
            firstExtra = true
        } else {
            finalMessage.append(component)
            firstExtra = true
        }
    }
    finalMessage.append("]}")
    try {
        val clazz: Class<*>
        if (Bukkit.getServer()::class.java.`package`.name.replace(".", ",").split(",")[3].equals("v1_8_R1")) {
            clazz = findClass("net.minecraft.server.VERSION.ChatSerializer")
        } else {
            clazz = findClass("net.minecraft.server.VERSION.IChatBaseComponent\$ChatSerializer")
        }
        val packetClazz = findClass("net.minecraft.server.VERSION.PacketPlayOutChat")
        val chatBaseComponentClazz = findClass("net.minecraft.server.VERSION.IChatBaseComponent")
        val chatComponent = invokeMethod(null, clazz, "a", arrayOf(String::class.java), arrayOf(finalMessage.toString()))
        val packet: Any
        if (Bukkit.getServer()::class.java.`package`.name.replace(".", ",").split(",")[3].equals("v1_12_R1")) {
            val chatEnumMessage = findClass("net.minecraft.server.VERSION.ChatMessageType")
            packet = invokeConstructor(packetClazz, arrayOf(chatBaseComponentClazz, chatEnumMessage), arrayOf(chatComponent, chatEnumMessage.enumConstants[0]))
        } else {
            packet = invokeConstructor(packetClazz, arrayOf(chatBaseComponentClazz, Byte::class.javaPrimitiveType!!), arrayOf(chatComponent, 0.toByte()))
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
    val craftPlayer = findClass("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer").cast(player)
    val entityPlayer = invokeMethod(craftPlayer, craftPlayer.javaClass, "getHandle", arrayOf(), arrayOf())
    val field = entityPlayer.javaClass.getDeclaredField("playerConnection")
    field.isAccessible = true
    val connection = field.get(entityPlayer)
    invokeMethod(connection, connection.javaClass, "sendPacket", arrayOf(packet.javaClass.interfaces[0]), arrayOf(packet))
}

/**
 * Invokes a constructor by the given parameters
 *
 * @param clazz      clazz
 * @param paramTypes paramTypes
 * @param params     params
 * @return instance
 * @throws NoSuchMethodException     exception
 * @throws IllegalAccessException    exception
 * @throws InvocationTargetException exception
 * @throws InstantiationException    exception
 */
@Throws(NoSuchMethodException::class, IllegalAccessException::class, InvocationTargetException::class, InstantiationException::class)
private fun invokeConstructor(clazz: Class<*>, paramTypes: Array<Class<*>>, params: Array<Any>): Any {
    val constructor = clazz.getDeclaredConstructor(*paramTypes)
    constructor.isAccessible = true
    return constructor.newInstance(*params)
}

/**
 * Invokes a method by the given parameters
 *
 * @param instance   instance
 * @param clazz      clazz
 * @param name       name
 * @param paramTypes paramTypes
 * @param params     params
 * @return returnedObject
 * @throws InvocationTargetException exception
 * @throws IllegalAccessException    exception
 * @throws NoSuchMethodException     exception
 */
@Throws(InvocationTargetException::class, IllegalAccessException::class, NoSuchMethodException::class)
private fun invokeMethod(instance: Any?, clazz: Class<*>, name: String, paramTypes: Array<Class<*>>, params: Array<Any>): Any {
    val method = clazz.getDeclaredMethod(name, *paramTypes)
    method.isAccessible = true
    return method.invoke(instance, *params)
}

/**
 * Finds a class regarding of the server Version
 *
 * @param name name
 * @return clazz
 * @throws ClassNotFoundException exception
 */
@Throws(ClassNotFoundException::class)
private fun findClass(name: String): Class<*> {
    return Class.forName(name.replace("VERSION", Bukkit.getServer()::class.java.`package`.name.replace(".", ",").split(",")[3]))
}