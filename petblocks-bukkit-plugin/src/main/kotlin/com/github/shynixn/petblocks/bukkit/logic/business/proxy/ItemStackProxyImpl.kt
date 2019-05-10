@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.proxy.ItemStackProxy
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
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
class ItemStackProxyImpl(
    /**
     * TypeName.
     */
    override var typeName: String,
    /**
     * DataValue.
     */
    override var data: Int = 0
) : ItemStackProxy {

    private var internalDisplayName: String? = null

    /**
     *  ItemStack lore.
     */
    override var lore: List<String> = ArrayList()

    /**
     * ItemStack displayName.
     */
    override var displayName: String
        get() {
            if (internalDisplayName == null) {
                return ""
            }

            return internalDisplayName!!
        }
        set(value) {
            internalDisplayName = value
        }

    /**
     * Amount.
     */
    override var amount: Int = 1

    /**
     * Creates a new itemstack from this proxy.
     */
    override fun <I> build(): I {
        val itemstack = ItemStack::class.java.getDeclaredConstructor(Material::class.java, Int::class.java, Short::class.java)
            .newInstance(Material.getMaterial(typeName)!!, amount, data.toShort())

        val meta = itemstack.itemMeta!!

        if (this.internalDisplayName != null) {
            meta.setDisplayName(this.internalDisplayName!!.translateChatColors())
        }

        meta.lore = this.lore

        itemstack.itemMeta = meta

        return itemstack as I
    }
}