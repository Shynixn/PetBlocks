@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toPosition
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
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
class ProxyServiceImpl @Inject constructor(private val version: Version) : ProxyService {
    /**
     * Gets the name of a player.
     */
    override fun <P> getPlayerName(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.name
    }

    /**
     * Gets the player from the given UUID.
     */
    override fun <P> getPlayerFromUUID(uuid: String): P {
        val player = Bukkit.getPlayer(UUID.fromString(uuid))

        if (player != null && player.isOnline) {
            return player as P
        }

        throw IllegalArgumentException("Player is no longer online!")
    }

    /**
     * Gets the location of the player.
     */
    override fun <L, P> getPlayerLocation(player: P): L {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.location as L
    }

    /**
     * Converts the given [location] to a [Position].
     */
    override fun <L> toPosition(location: L): Position {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        return location.toPosition()
    }

    /**
     * Gets the looking direction of the player.
     */
    override fun <P> getDirectionVector(player: P): Position {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val vector = Vector()
        val rotX = player.location.yaw.toDouble()
        val rotY = player.location.pitch.toDouble()
        vector.y = -Math.sin(Math.toRadians(rotY))
        val h = Math.cos(Math.toRadians(rotY))
        vector.x = -h * Math.sin(Math.toRadians(rotX))
        vector.z = h * Math.cos(Math.toRadians(rotX))
        return vector.toPosition()
    }

    /**
     * Gets the item in the player hand.
     */
    override fun <P, I> getPlayerItemInHand(player: P, offhand: Boolean): I? {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return if (version.isVersionSameOrGreaterThan(Version.VERSION_1_9_R1)) {
            val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

            if (offhand) {
                inventoryClazz.getDeclaredMethod("getItemInOffHand").invoke(player.inventory) as I
            } else {
                inventoryClazz.getDeclaredMethod("getItemInMainHand").invoke(player.inventory) as I
            }
        } else {
            Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("getItemInHand")
                .invoke(player) as I
        }
    }

    /**
     * Sets the item in the player hand.
     */
    override fun <P, I> setPlayerItemInHand(player: P, itemStack: I, offhand: Boolean) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (version.isVersionSameOrGreaterThan(Version.VERSION_1_9_R1)) {
            val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

            if (offhand) {
                inventoryClazz.getDeclaredMethod("setItemInOffHand", ItemStack::class.java).invoke(player.inventory, itemStack)
            } else {
                inventoryClazz.getDeclaredMethod("setItemInMainHand", ItemStack::class.java).invoke(player.inventory, itemStack)
            }
        } else {
            Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("setItemInHand", ItemStack::class.java)
                .invoke(player, itemStack)
        }

        player.updateInventory()
    }

    /**
     * Gets if the given player has got the given permission.
     */
    override fun <P> hasPermission(player: P, permission: Permission): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.hasPermission(permission.permission)
    }

    /**
     * Gets the player uuid.
     */
    override fun <P> getPlayerUUID(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return player.uniqueId.toString()
    }

    /**
     * Sends a message to the [sender].
     */
    override fun <S> sendMessage(sender: S, message: String) {
        if (sender !is CommandSender) {
            throw IllegalArgumentException("Sender has to be a BukkitCommandSender!")
        }

        sender.sendMessage(message)
    }

    /**
     * Gets if the given instance can be converted to a player.
     */
    override fun <P> isPlayer(instance: P): Boolean {
        return instance is Player
    }

}