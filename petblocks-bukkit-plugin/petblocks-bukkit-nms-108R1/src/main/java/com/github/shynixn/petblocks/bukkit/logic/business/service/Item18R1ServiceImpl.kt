@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.ItemStackProxy
import com.github.shynixn.petblocks.api.business.service.ItemService
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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
class Item18R1ServiceImpl : ItemService {
    /**
     * Converts the given type to an id.
     */
    override fun convertTypeToId(type: Any): Int {
        if (type !is Material) {
            throw IllegalArgumentException("Material has to be a BukkitMaterial!")
        }

        @Suppress("DEPRECATION")
        return type.id
    }

    /**
     * Creates a new itemstack from the given parameters.
     */
    override fun createItemStack(type: Any, dataValue: Int): ItemStackProxy {
        return Class.forName("com.github.shynixn.petblocks.bukkit.logic.business.proxy.ItemStackProxyImpl")
            .getDeclaredConstructor(String::class.java, Int::class.java)
            .newInstance(getMaterialValue(type).name, dataValue) as ItemStackProxy
    }

    /**
     * Gets if the given [itemStack] has got the given [type] and [dataValue].
     */
    override fun <I> hasItemStackProperties(itemStack: I, type: Any, dataValue: Int): Boolean {
        if (itemStack !is ItemStack) {
            throw IllegalArgumentException("ItemStack has to be a BukkitItemStack!")
        }

        val material = getMaterialValue(type)
        return material == itemStack.type && dataValue == itemStack.durability.toInt()
    }

    /**
     * Processing if there is any way the given [value] can be mapped to an material.
     */
    private fun getMaterialValue(value: Any): Material {
        if (value is Int) {
            return Material::class.java.getDeclaredMethod("getMaterial", Int::class.java).invoke(null, value) as Material
        } else if (value is String && value.toIntOrNull() != null) {
            return Material::class.java.getDeclaredMethod("getMaterial", Int::class.java).invoke(null, value.toInt()) as Material
        } else if (value is String) {
            return Material.getMaterial(value) ?: throw IllegalArgumentException("Material $value does not exist!")
        } else if (value is MaterialType) {
            return getMaterialValue(value.numericId)
        }

        throw  throw IllegalArgumentException("Material $value is not a string or int.")
    }
}