@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.service.ItemService
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.lang.reflect.Method

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
class Item18R1ServiceImpl : ItemService {
    private val getMaterialFromIdMethod: Method = Material::class.java.getDeclaredMethod("getMaterial", Int::class.javaPrimitiveType)

    /**
     * Creates a new itemstack from the given parameters.
     */
    override fun <I> createItemStack(typeName: String, dataValue: Int, amount: Int): I {
        val materialType = getMaterialValue<Material>(typeName)

        return ItemStack(materialType, amount, dataValue.toShort()) as I
    }

    /**
     * Gets if the given itemstack is the given materialType.
     */
    override fun <I> isItemStackMaterialType(itemStack: I, materialType: MaterialType): Boolean {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        val id = Material::class.java.getDeclaredMethod("getId").invoke(itemStack.type)

        for (value in MaterialType.values()) {
            if (value.MinecraftNumericId == id) {
                return true
            }
        }

        return false
    }

    /**
     * Creates a new itemstack from the given materialType.
     */
    override fun <I> createItemStack(materialType: MaterialType, dataValue: Int, amount: Int): I {
        return ItemStack(getMaterialValue<Material>(materialType.MinecraftNumericId), amount, dataValue.toShort()) as I
    }

    /**
     * Sets the amount of items on the given stack.
     */
    override fun <I> setAmountOfItemStack(itemStack: I, amount: Int) {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        itemStack.amount = amount
    }

    /**
     * Gets the amount of items on the given stack.
     */
    override fun getAmountOfItemStack(itemStack: Any): Int {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        return itemStack.amount
    }

    /**
     * Sets the displayName of an itemstack.
     */
    override fun <I> setDisplayNameOfItemStack(itemstack: I, name: String) {
        if (itemstack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        val meta = itemstack.itemMeta
        meta.displayName = name.translateChatColors()
        itemstack.itemMeta = meta
    }

    /**
     * Sets the lore of an itemstack.
     */
    override fun <I> setLoreOfItemStack(itemstack: I, index: Int, text: String) {
        if (itemstack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        val meta = itemstack.itemMeta
        var lore = meta.lore

        if (lore == null) {
            lore = ArrayList<String>()
        }

        if (index >= lore.size) {
            val newLore = arrayOfNulls<String>(index + 1)

            for (i in 0 until newLore.size) {
                if (i < lore.size) {
                    newLore[i] = lore[i]
                } else {
                    newLore[i] = ""
                }
            }

            lore = newLore.toList()
        }

        lore[index] = text.translateChatColors()

        meta.lore = lore
        itemstack.itemMeta = meta
    }

    /**
     * Gets the material from the numeric value or string value.
     */
    override fun <M> getMaterialValue(value: Any): M {
        if (value is Int) {
            return getMaterialFromIdMethod.invoke(null, value) as M
        } else if (value is String && value.toIntOrNull() != null) {
            return getMaterialFromIdMethod.invoke(null, value.toInt()) as M
        } else if (value is String) {
            return Material.getMaterial(value) as M ?: throw IllegalArgumentException("Material $value does not exist!")
        }

        throw  throw IllegalArgumentException("Material $value is not a string or int.")
    }
}