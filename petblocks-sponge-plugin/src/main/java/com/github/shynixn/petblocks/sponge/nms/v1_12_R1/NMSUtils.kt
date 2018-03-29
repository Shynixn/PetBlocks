package com.github.shynixn.petblocks.sponge.nms.v1_12_R1

import net.minecraft.anchor.v1_12_mcpR1.entity.player.EntityPlayerMP
import net.minecraft.anchor.v1_12_mcpR1.nbt.NBTTagCompound
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack

@Suppress("CAST_NEVER_SUCCEEDS")
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
class NMSUtils {

    fun setItemDamage(itemStack: ItemStack, damage: Int) {
        val itemStack1 = itemStack as net.minecraft.anchor.v1_12_mcpR1.item.ItemStack
        itemStack1.itemDamage = damage
    }

    fun updateInventoryFor(player: Player) {
        (player as EntityPlayerMP).sendContainerToPlayer((player as EntityPlayerMP).openContainer)
    }

    fun setItemOwner(itemStack: ItemStack, owner: String) {
        val itemStack1 = itemStack as net.minecraft.anchor.v1_12_mcpR1.item.ItemStack
        var compound = itemStack1.tagCompound
        if (compound == null) {
            compound = NBTTagCompound()
        }
        compound.setString("SkullOwner", owner)
        itemStack1.tagCompound = compound
    }
}