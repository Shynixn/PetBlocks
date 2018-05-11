@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.bukkit.logic.business.helper.setDisplayName
import com.github.shynixn.petblocks.bukkit.logic.business.helper.setLore
import com.github.shynixn.petblocks.bukkit.logic.business.helper.setSkin
import com.github.shynixn.petblocks.bukkit.logic.business.helper.setUnbreakable
import com.github.shynixn.petblocks.bukkit.nms.v1_12_R1.MaterialCompatibility12
import com.github.shynixn.petblocks.core.logic.persistence.entity.CustomGUIItem
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

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
class BukkitGUIItem : CustomGUIItem {
    private var cachedItemStack: ItemStack? = null

    /** Returns an itemStack from the given item */
    override fun <I> toItemStack(): I {
        if (cachedItemStack != null) {
            return cachedItemStack!!.clone() as I
        }

        val itemStack = ItemStack(MaterialCompatibility12.getMaterialFromId(this.type), 1, this.data.toShort())
        if (itemStack.type == Material.SKULL_ITEM && this.skin != "") {
            itemStack.setSkin(this.skin)
        }

        cachedItemStack = itemStack.setUnbreakable(this.unbreakable).setDisplayName(displayName)
                .setLore(this.lore)

        return cachedItemStack as I
    }

    constructor(id: Int, values: Map<String, Any>) : super(id, values)

    constructor(container: GUIItemContainer<*>) : super (container)
}