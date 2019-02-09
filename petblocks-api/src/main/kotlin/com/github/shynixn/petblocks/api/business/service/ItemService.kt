package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
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
interface ItemService {
    /**
     * Creates a new itemstack from the given materialType.
     */
    fun <I> createItemStack(materialType: MaterialType, dataValue: Int = 0, amount: Int = 0): I

    /**
     * Creates a new itemstack from the given parameters.
     */
    fun <I> createItemStack(typeName: String, dataValue: Int = 0, amount: Int = 1): I

    /**
     * Sets the amount of items on the given stack.
     */
    fun <I> setAmountOfItemStack(itemStack: I, amount: Int)

    /**
     * Gets the amount of items on the given stack.
     */
    fun getAmountOfItemStack(itemStack: Any): Int

    /**
     * Gets the material from the numeric value or string value.
     */
    fun <M> getMaterialValue(value: Any): M

    /**
     * Sets the displayName of an itemstack.
     */
    fun <I> setDisplayNameOfItemStack(itemstack: I, name: String)

    /**
     * Sets the lore of an itemstack.
     */
    fun <I> setLoreOfItemStack(itemstack: I, index: Int, text: String)

    /**
     * Gets if the given [itemStack] has got the given [type] and [dataValue].
     */
    fun <I> hasItemStackProperties(itemStack: I, type: Any, dataValue: Int = 0): Boolean

    /**
     * Gets the itemstack in the hand of the player with optional offHand flag.
     */
    fun <P, I> getItemInHand(player: P, offHand: Boolean = false): Optional<I>

    /**
     * Gets if the given itemstack is the given materialType.
     */
    fun <I> isItemStackMaterialType(itemStack: I, materialType: MaterialType): Boolean
}