package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_16_R2

import com.github.shynixn.petblocks.api.business.proxy.ArmorstandPetProxy
import net.minecraft.server.v1_16_R2.EnumItemSlot
import org.bukkit.craftbukkit.v1_16_R2.CraftServer
import org.bukkit.craftbukkit.v1_16_R2.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_16_R2.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * CraftBukkit Wrapper of the Armorstand.
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
class CraftPetArmorstand(server: CraftServer, nmsPet: NMSPetArmorstand) : CraftArmorStand(server, nmsPet),
    ArmorstandPetProxy {
    /**
     * Sets the helmet item stack securely if
     * blocked by the NMS call.
     */
    override fun <I> setHelmetItemStack(item: I) {
        require(item is ItemStack?)
        (handle as NMSPetArmorstand).setSecureSlot(EnumItemSlot.HEAD, CraftItemStack.asNMSCopy(item))
    }

    /**
     * Sets the boots item stack securely if
     * blocked by the NMS call.
     */
    override fun <I> setBootsItemStack(item: I) {
        require(item is ItemStack?)
        (handle as NMSPetArmorstand).setSecureSlot(EnumItemSlot.FEET, CraftItemStack.asNMSCopy(item))
    }

    /**
     * Removes this entity.
     */
    override fun deleteFromWorld() {
        super.remove()
    }

    /**
     * Ignore all other plugins trying to remove this entity. This is the entity of PetBlocks,
     * no one else is allowed to modify this!
     */
    override fun remove() {
    }

    /**
     * Pet should never be persistent.
     */
    override fun isPersistent(): Boolean {
        return false
    }

    /**
     * Pet should never be persistent.
     */
    override fun setPersistent(b: Boolean) {}

    /**
     * Custom type.
     */
    override fun toString(): String {
        return "PetBlocks{ArmorstandEntity}"
    }
}