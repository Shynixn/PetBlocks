@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.proxy

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.sponge.logic.business.extension.hasPermissions
import com.github.shynixn.petblocks.sponge.logic.business.extension.sendMessage
import com.github.shynixn.petblocks.sponge.logic.business.extension.updateInventory
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.SlotIndex
import org.spongepowered.api.item.inventory.property.SlotPos
import org.spongepowered.api.item.inventory.type.GridInventory
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
     * Gets the unique id of the player.
     */
    override val uniqueId: UUID
        get() = player.uniqueId
    /**
     * Gets the framework handle.
     */
    override val handle: Any
        get() = player

    /**
     * Sends a message to the player.
     */
    override fun sendMessage(text: String) {
        player.sendMessage(text)
    }

    /**
     * Sets the item at the given index in the inventory.
     */
    override fun <I> setInventoryItem(index: Int, itemstack: I) {
        if (itemstack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a SpongeItemStack!")
        }

        if (index == 0) {
            this.player.inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(SlotPos.of(0, 0)).set(itemstack)
        } else {
            this.player.inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(SlotIndex.of(index)).set(itemstack)
        }
    }

    /**
     * Sets the item in the players hand.
     */
    override fun <I> setItemInHand(itemStack: I, offHand: Boolean) {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a SpongeItemStack!")
        }

        if (offHand) {
            this.player.setItemInHand(HandTypes.OFF_HAND, itemStack)
        } else {
            this.player.setItemInHand(HandTypes.MAIN_HAND, itemStack)
        }
    }

    /**
     * Gets the item in the players hand.
     */
    override fun <I> getItemInHand(offHand: Boolean): Optional<I> {
        return if (offHand) {
            this.player.getItemInHand(HandTypes.OFF_HAND) as Optional<I>
        } else {
            this.player.getItemInHand(HandTypes.MAIN_HAND) as Optional<I>
        }
    }

    /**
     * Gets the location of the player.
     */
    override fun <L> getLocation(): L {
        return player.location as L
    }

    /**
     * Gets if this player has got permissions.
     */
    override fun hasPermission(permission: Permission): Boolean {
        return player.hasPermissions(permission)
    }

    /**
     * Updates the player inventory.
     */
    override fun updateInventory() {
        player.updateInventory()
    }

    /**
     * Generates a vector for the launching direction.
     */
    override fun <V> getDirectionLaunchVector(): V {
        val rotX = player.headRotation.y
        val rotY = player.headRotation.x
        val h = Math.cos(Math.toRadians(rotY))
        return Vector3d(-h * Math.sin(Math.toRadians(rotX)), 0.5, h * Math.cos(Math.toRadians(rotX)))
                .mul(1.2) as V
    }
}