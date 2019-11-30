@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.PotionEffect
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toPosition
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffectType
import org.bukkit.util.Vector
import java.util.*
import kotlin.collections.ArrayList
import kotlin.math.cos
import kotlin.math.sin

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
class ProxyServiceImpl @Inject constructor(private val version: Version, private val loggingService: LoggingService) : ProxyService {
    /**
     * Gets a list of points between 2 locations.
     */
    override fun <L> getPointsBetweenLocations(location1: L, location2: L, amount: Int): List<L> {
        require(location1 is Location)
        require(location2 is Location)

        if (location1.world != location2.world) {
            return ArrayList()
        }

        val locations = ArrayList<Location>()
        val vectorBetween = location1.subtract(location2)
        val onePointLength = vectorBetween.length() / amount

        for (i in 0 until amount) {
            val location = location2.clone().add(0.0, 0.7, 0.0).add(vectorBetween.toVector().normalize().multiply(i).multiply(onePointLength))
            locations.add(location)
        }

        return locations as List<L>
    }

    /**
     * Applies the given [potionEffect] to the given [player].
     */
    override fun <P> applyPotionEffect(player: P, potionEffect: PotionEffect) {
        require(player is Player)

        // PotionEffectType.values() can return null values in some minecraft versions.
        val foundPotionType =
            PotionEffectType.values().cast<Array<PotionEffectType?>>().firstOrNull { t -> t != null && t.name.equals(potionEffect.potionType, true) }

        if (foundPotionType == null) {
            loggingService.warn("PotionEffectType: ${potionEffect.potionType} does not exist!")
            return
        }

        val potionEffectBukkit = org.bukkit.potion.PotionEffect(
            foundPotionType, potionEffect.duration * 20, potionEffect.amplifier, potionEffect.ambient, potionEffect.particles
        )

        if (player.hasPotionEffect(foundPotionType)) {
            player.removePotionEffect(foundPotionType)
        }

        player.addPotionEffect(potionEffectBukkit)
    }

    /**
     * Drops the given item at the given position.
     */
    override fun <L, I> dropInventoryItem(location: L, item: I) {
        require(location is Location)
        require(item is ItemStack)

        try {
            location.world!!.dropItem(location, item)
        } catch (e: Exception) {
            // Cannot drop air.
        }
    }

    /**
     * Gets the inventory item at the given index.
     */
    override fun <I, IT> getInventoryItem(inventory: I, index: Int): IT? {
        require(inventory is Inventory)
        return inventory.getItem(index) as IT?
    }

    /**
     * Gets if the given player has got the given permission.
     */
    override fun <P> hasPermission(player: P, permission: String): Boolean {
        require(player is Player)
        return player.hasPermission(permission)
    }

    /**
     * Clears the given inventory.
     */
    override fun <I> clearInventory(inventory: I) {
        require(inventory is Inventory)
        inventory.clear()
    }

    /**
     * Gets the lower inventory of an inventory.
     */
    override fun <I> getLowerInventory(inventory: I): I {
        // Only necessary for sponge.
        return inventory
    }

    /**
     * Gets if the given inventory belongs to a player. Returns null if not.
     */
    override fun <P, I> getPlayerFromInventory(inventory: I): P? {
        require(inventory is Inventory)

        if (inventory.holder is Player) {
            return inventory.holder as P
        }

        return null
    }

    /**
     * Updates the inventory.
     */
    override fun <I, IT> setInventoryItem(inventory: I, index: Int, item: IT) {
        require(inventory is Inventory)
        require(item is ItemStack?)

        inventory.setItem(index, item)
    }

    /**
     * Updates the given player inventory.
     */
    override fun <P> updateInventory(player: P) {
        require(player is Player)
        player.updateInventory()
    }

    /**
     * Opens a new inventory for the given player.
     */
    override fun <P, I> openInventory(player: P, title: String, size: Int): I {
        require(player is Player)
        val inventory = Bukkit.getServer().createInventory(player, size, title)
        player.openInventory(inventory)
        return inventory as I
    }

    /**
     * Closes the inventory of the given player.
     */
    override fun <P> closeInventory(player: P) {
        require(player is Player)
        player.closeInventory()
    }

    /**
     * Gets the name of a player.
     */
    override fun <P> getPlayerName(player: P): String {
        require(player is Player)
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
        require(player is Player)
        return player.location as L
    }

    /**
     * Converts the given [location] to a [Position].
     */
    override fun <L> toPosition(location: L): Position {
        require(location is Location)
        return location.toPosition()
    }

    /**
     * Gets the looking direction of the player.
     */
    override fun <P> getDirectionVector(player: P): Position {
        require(player is Player)
        val vector = Vector()
        val rotX = player.location.yaw.toDouble()
        val rotY = player.location.pitch.toDouble()
        vector.y = -sin(Math.toRadians(rotY))
        val h = cos(Math.toRadians(rotY))
        vector.x = -h * sin(Math.toRadians(rotX))
        vector.z = h * cos(Math.toRadians(rotX))
        return vector.toPosition()
    }

    /**
     * Gets the item in the player hand.
     */
    override fun <P, I> getPlayerItemInHand(player: P, offhand: Boolean): I? {
        require(player is Player)

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
        require(player is Player)

        if (version.isVersionSameOrGreaterThan(Version.VERSION_1_9_R1)) {
            val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

            if (offhand) {
                inventoryClazz.getDeclaredMethod("setItemInOffHand", ItemStack::class.java)
                    .invoke(player.inventory, itemStack)
            } else {
                inventoryClazz.getDeclaredMethod("setItemInMainHand", ItemStack::class.java)
                    .invoke(player.inventory, itemStack)
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
        require(player is Player)
        return player.hasPermission(permission.permission)
    }

    /**
     * Gets the player uuid.
     */
    override fun <P> getPlayerUUID(player: P): String {
        require(player is Player)
        return player.uniqueId.toString()
    }

    /**
     * Sends a message to the [sender].
     */
    override fun <S> sendMessage(sender: S, message: String) {
        require(sender is CommandSender)
        sender.sendMessage(message)
    }

    /**
     * Gets if the given instance can be converted to a player.
     */
    override fun <P> isPlayer(instance: P): Boolean {
        return instance is Player
    }
}