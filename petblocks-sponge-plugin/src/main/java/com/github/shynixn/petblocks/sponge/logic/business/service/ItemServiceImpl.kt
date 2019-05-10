@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.ItemStackProxy
import com.github.shynixn.petblocks.api.business.service.ItemService
import com.github.shynixn.petblocks.sponge.logic.business.extension.dataValue
import com.github.shynixn.petblocks.sponge.logic.business.proxy.ItemStackProxyImpl
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.inventory.ItemStack

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
class ItemServiceImpl : ItemService {
    /**
     * Converts the given type to an id.
     */
    override fun convertTypeToId(type: Any): Int {
        if (type is BlockType) {
            for (material in MaterialType.values()) {
                if (material.minecraftName == type.name) {
                    return material.numericId
                }
            }
        }

        throw IllegalArgumentException("Cannot convert type $type.")
    }

    /**
     * Creates a new itemstack from the given parameters.
     */
    override fun createItemStack(type: Any, dataValue: Int): ItemStackProxy {
        return ItemStackProxyImpl(getItemTypeValue(type).name, dataValue)
    }

    /**
     * Gets if the given [itemStack] has got the given [type] and [dataValue].
     */
    override fun <I> hasItemStackProperties(itemStack: I, type: Any, dataValue: Int): Boolean {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a SpongeItemStack!")
        }

        val material = getItemTypeValue(type)
        return material == itemStack.type && dataValue == itemStack.dataValue
    }

    /**
     * Processing if there is any way the given [value] can be mapped to an itemType.
     */
    private fun getItemTypeValue(value: Any): ItemType {
        if (value is Int) {
            for (material in MaterialType.values()) {
                if (material.numericId == value) {
                    return Sponge.getGame().registry.getType(ItemType::class.java, material.minecraftName).get()
                }
            }
        } else if (value is String && value.toIntOrNull() != null) {
            for (material in MaterialType.values()) {
                if (material.numericId == value.toInt()) {
                    return Sponge.getGame().registry.getType(ItemType::class.java, material.minecraftName).get()
                }
            }
        } else if (value is String) {
            return try {
                Sponge.getGame().registry.getType(ItemType::class.java, MaterialType.valueOf(value).minecraftName).get()
            } catch (e: Exception) {
                Sponge.getGame().registry.getType(ItemType::class.java, value).get()
            }
        } else if (value is MaterialType) {
            return Sponge.getGame().registry.getType(ItemType::class.java, value.minecraftName).get()
        }

        throw  throw IllegalArgumentException("Material $value is not a string or int.")
    }
}