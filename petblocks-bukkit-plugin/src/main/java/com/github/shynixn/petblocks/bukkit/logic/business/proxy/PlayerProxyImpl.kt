@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper
import com.github.shynixn.petblocks.bukkit.nms.VersionSupport
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
class PlayerProxyImpl(private val player: Player) : PlayerProxy {
    /**
     * Gets the framework handle.
     */
    override val handle: Any
        get() = player

    private val version = VersionSupport.getServerVersion()

    /**
     * Sets the item in the players hand.
     */
    override fun <I> setItemInHand(itemStack: I, offHand: Boolean) {
        if (version.isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
            val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

            if (offHand) {
                inventoryClazz.getDeclaredMethod("setItemInOffHand", ItemStack::class.java).invoke(player.inventory, itemStack)
            } else {
                inventoryClazz.getDeclaredMethod("setItemInMainHand", ItemStack::class.java).invoke(player.inventory, itemStack)
            }
        } else {
            Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("setItemInHand", ItemStack::class.java)
                    .invoke(this.player, itemStack)
        }
    }

    /**
     * Gets the item in the players hand.
     */
    override fun <I> getItemInHand(offHand: Boolean): Optional<I> {
        return if (version.isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
            val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

            if (offHand) {
                Optional.ofNullable(inventoryClazz.getDeclaredMethod("getItemInOffHand").invoke(player.inventory)) as Optional<I>
            } else {
                Optional.ofNullable(inventoryClazz.getDeclaredMethod("getItemInMainHand").invoke(player.inventory)) as Optional<I>
            }
        } else {
            Optional.ofNullable(Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("getItemInHand")
                    .invoke(this.player)) as Optional<I>
        }
    }

    /**
     * Gets if this player has got permissions.
     */
    override fun hasPermission(permission: Permission): Boolean {
        return PetBlockModifyHelper.hasPermission(player, permission)
    }

    /**
     * Updates the player inventory.
     */
    override fun updateInventory() {
        player.updateInventory()
    }

    /**
     * Sends a message to the player.
     */
    override fun sendMessage(text: String) {
        this.player.sendMessage(text)
    }

    /**
     * Sets the item at the given index in the inventory.
     */
    override fun <I> setInventoryItem(index: Int, itemstack: I) {
        if (itemstack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        player.inventory.setItem(index, itemstack)
    }

    /**
     * Generates a vector for the launching direction.
     */
    override fun <V> getDirectionLaunchVector(): V {
        val vector = Vector()
        val rotX = player.location.yaw.toDouble()
        val rotY = player.location.pitch.toDouble()
        vector.y = -Math.sin(Math.toRadians(rotY))
        val h = Math.cos(Math.toRadians(rotY))
        vector.x = -h * Math.sin(Math.toRadians(rotX))
        vector.z = h * Math.cos(Math.toRadians(rotX))
        return vector.multiply(1.2) as V
    }

    /**
     * Gets the location of the player.
     */
    override fun <L> getLocation(): L {
        return player.location as L
    }

    /**
     * Gets the unique id of the player.
     */
    override val uniqueId: UUID
        get() = player.uniqueId
}