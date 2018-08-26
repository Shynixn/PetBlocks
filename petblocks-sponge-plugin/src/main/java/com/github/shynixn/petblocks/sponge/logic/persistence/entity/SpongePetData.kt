package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetData
import com.github.shynixn.petblocks.sponge.logic.business.helper.*
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack

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
class SpongePetData : PetData {
    /**
     * Sets the itemStack.
     *
     * @param name name
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakable
     */
    override fun setSkin(name: String, damage: Int, skin: String?, unbreakable: Boolean) {
        this.setSkin(CompatibilityItemType.getFromName(name).id, damage, skin, unbreakable)
    }

    /**
     * Returns the material name of the item id.
     * @return name
     */
    override fun getItemMaterialName(): String {
        return CompatibilityItemType.getFromId(this.itemId).name
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
        this.playerInfo = SpongePlayerData()
        this.playerInfo.name = player.name
        this.playerInfo.setUuid(player.uniqueId)
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
        val itemType = CompatibilityItemType.getFromId(this.itemId)
        val itemStack = ItemStack.builder().quantity(1)
                .itemType(itemType!!.itemType)
                .build()
        itemStack.setDamage(this.itemDamage)
        if (this.getSkin() != null) {
            itemStack.setSkin(this.getSkin())
        }
        itemStack.setUnbreakable(this.isItemUnbreakable)
        if (this.getPetDisplayName() != null) {
            itemStack.offer(Keys.DISPLAY_NAME, this.getPetDisplayName().translateToText())
        }
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
        if (Config.petNameBlackList != null) {
            Config.petNameBlackList
                    .asSequence()
                    .filter { name.toUpperCase().contains(it.toUpperCase()) }
                    .forEach { throw RuntimeException("Name is not valid!") }
        }
        this.petDisplayName = name.translateChatColors()
    }
}