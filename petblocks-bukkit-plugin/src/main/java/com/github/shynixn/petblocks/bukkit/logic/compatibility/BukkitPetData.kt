package com.github.shynixn.petblocks.bukkit.logic.compatibility

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.compatibility.PetData
import com.github.shynixn.petblocks.core.logic.compatibility.PlayerData
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.HashMap

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
class BukkitPetData : PetData {
    /**
     * Sets the itemStack.
     *
     * @param name name
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakable
     */
    override fun setSkin(name: String, damage: Int, skin: String?, unbreakable: Boolean) {
        this.setSkin(MaterialCompatibility13.getIdFromMaterial(org.bukkit.Material.getMaterial(name)), damage, skin, unbreakable)
    }

    /**
     * Returns the material name of the item id.
     * @return name
     */
    override fun getItemMaterialName(): String {
        return MaterialCompatibility13.getMaterialFromId(this.itemId).name
    }

    /**
     * Initializes a new default petData which is ready to be used.
     *
     * @param player player
     * @param name   nameOfThePet
     */
    constructor(player: Player?, name: String?) {
        if (player == null)
            throw IllegalArgumentException("Player cannot be null!")
        if (name == null)
            throw IllegalArgumentException("Petname cannot be null!")
        this.petDisplayName = name.replace(":player", player.name)
        this.playerInfo = object : PlayerData() {
            override fun <T> getPlayer(): T? {
                return try {
                    Bukkit.getPlayer(player.name) as T
                } catch (ex: Exception) {
                    null
                }
            }
        }
        this.ageTicks = Config.age_smallticks.toLong()
        this.sounds = true
        this.particleEffectBuilder = ParticleEntity(ParticleType.NONE)
        val engineContainer = Config.engineController
                .getContainerFromPosition(Config.defaultEngine)
        if (!engineContainer.isPresent) {
            throw RuntimeException("Default engine could not be loaded correctly!")
        }
        this.engineContainer = engineContainer.get()
    }

    constructor() : super()

    /**
     * Returns the itemStack for the head
     *
     * @return headItemStack
     */
    override fun getHeadItemStack(): Any? {
        var itemStack: ItemStack?
        if (this.getSkin() != null) {
            if (this.getSkin().contains("textures.minecraft")) {
                itemStack = ItemStack(MaterialCompatibility13.getMaterialFromId(this.itemId), 1, this.itemDamage.toShort())
                SkinHelper.setItemStackSkin(itemStack, this.getSkin())
            } else {
                itemStack = ItemStack(MaterialCompatibility13.getMaterialFromId(this.itemId), 1, this.itemDamage.toShort())
                val meta = itemStack.itemMeta
                if (meta is SkullMeta) {
                    meta.owner = this.skin
                }
                itemStack.itemMeta = meta
            }
        } else {
            itemStack = ItemStack(MaterialCompatibility13.getMaterialFromId(this.itemId), 1, this.itemDamage.toShort())
        }
        val meta = itemStack.itemMeta
        meta.displayName = this.petDisplayName
        itemStack.itemMeta = meta
        val data = HashMap<String, Any>()
        data["Unbreakable"] = this.isItemStackUnbreakable
        itemStack = PetBlockModifyHelper.setItemStackNBTTag(itemStack, data)
        return itemStack
    }

    /**
     * Sets the stored display name of the pet which appears above it's head on respawn.
     *
     * @param name name
     */
    override fun setPetDisplayName(name: String?) {
        if (name == null)
            return
        this.petDisplayName = ChatColor.translateAlternateColorCodes('&', name)
    }
}