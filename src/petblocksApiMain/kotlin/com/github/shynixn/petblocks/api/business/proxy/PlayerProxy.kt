package com.github.shynixn.petblocks.api.business.proxy

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.persistence.entity.Position

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
interface PlayerProxy {
    /**
     * Gets the unique id of the player.
     */
    val uniqueId: String

    /**
     * Gets the framework handle.
     */
    val handle: Any

    /**
     * Gets the name of the player.
     */
    val name : String

    /**
     * Gets the position of the player.
     */
    val position : Position

    /**
     * Sends a message to the player.
     */
    fun sendMessage(text: String)

    /**
     * Sets the item at the given index in the inventory.
     */
    fun <I> setInventoryItem(index: Int, itemstack: I)

    /**
     * Sets the item in the players hand.
     */
    fun <I> setItemInHand(itemStack: I, offHand: Boolean = false)

    /**
     * Gets the item in the players hand.
     */
    fun <I> getItemInHand(offHand: Boolean = false): I?

    /**
     * Gets the location of the player.
     */
    fun <L> getLocation(): L

    /**
     * Gets if this player has got permissions.
     */
    fun hasPermission(permission: Permission): Boolean

    /**
     * Updates the player inventory.
     */
    fun updateInventory()

    /**
     * Generates a vector for the launching direction.
     */
    fun <V> getDirectionLaunchVector(): V
}