package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

import com.github.shynixn.petblocks.api.business.proxy.ArmorstandPetProxy
import net.minecraft.world.entity.EnumItemSlot
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.entity.CraftArmorStand
import org.bukkit.craftbukkit.v1_17_R1.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack

/**
 * CraftBukkit Wrapper of the Armorstand.
 */
class CraftPetArmorstand(server: CraftServer, nmsPet: NMSPetArmorstand) : CraftArmorStand(server, nmsPet),
    ArmorstandPetProxy {
    /**
     * Sets the helmet item stack securely if
     * blocked by the NMS call.
     */
    override fun <I> setHelmetItemStack(item: I) {
        require(item is ItemStack?)
        (handle as NMSPetArmorstand).setSecureSlot(EnumItemSlot.f, CraftItemStack.asNMSCopy(item))
    }

    /**
     * Sets the boots item stack securely if
     * blocked by the NMS call.
     */
    override fun <I> setBootsItemStack(item: I) {
        require(item is ItemStack?)
        (handle as NMSPetArmorstand).setSecureSlot(EnumItemSlot.c, CraftItemStack.asNMSCopy(item))
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
