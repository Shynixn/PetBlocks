package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemContainer
import com.github.shynixn.petblocks.sponge.logic.business.helper.*
import org.spongepowered.api.Sponge
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
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
class SpongeItemContainer : ItemContainer<Player> {
    constructor(enabled: Boolean, position: Int, page: GUIPage?, id: Int, damage: Int, skin: String?, unbreakable: Boolean, name: String?, lore: Array<out String>?) : super(enabled, position, page, id, damage, skin, unbreakable, name, lore)
    constructor(orderNumber: Int, data: Map<String, Any>?) : super(orderNumber, data)

    /**
     * Generates a new itemStack for the player and his permissions
     *
     * @param player      player
     * @param permissions permission
     * @return itemStack
     */
    override fun generate(player: Player, permissions: Array<String?>): Any {
        try {
            if (this.isEnabled) {
                val itemType = CompatibilityItemType.getFromId(this.itemId)
                val itemStack = ItemStack.builder().quantity(1)
                        .itemType(itemType!!.itemType)
                        .build()
                itemStack.setDamage(this.itemDamage)
                if (itemType == CompatibilityItemType.SKULL_ITEM && this.skin != null) {
                    itemStack.setSkin(this.skin)
                }
                itemStack.offer(Keys.UNBREAKABLE, this.isItemUnbreakable)
                if (this.displayName.isPresent) {
                    itemStack.offer(Keys.DISPLAY_NAME, this.displayName.get().translateToText())
                }
                this.updateLore(itemStack, player, permissions)
                return itemStack
            }
        } catch (ex: Exception) {
            Sponge.getGame().sendMessage("Invalid config file. Fix the following error or recreate it!")
            Sponge.getGame().sendMessage("Failed to generate itemStack.")
            ex.printStackTrace()
        }

        return ItemStack.builder().itemType(ItemTypes.AIR).build()
    }

    private fun updateLore(itemStack: ItemStack, player: Player, permissions: Array<String?>?) {
        val lore = this.provideLore(player, permissions)
        if (lore != null) {
            val data = lore as Array<String>
            itemStack.setLore(data)
        }
    }

    private fun provideLore(player: Player, permissions: Array<String?>?): Array<String?>? {
        if (permissions != null && permissions.size == 1 && permissions[0] != null) {
            if (permissions.size == 1 && permissions[0] == "minecraft-heads") {
                return arrayOf(ChatColor.GRAY.toString() + "Use exclusive pet heads as costume.", ChatColor.YELLOW.toString() + "Sponsored by Minecraft-Heads.com")
            }
            if (permissions.size == 1 && permissions[0] == "head-database") {
                return arrayOf(ChatColor.DARK_RED.toString() + "" + ChatColor.ITALIC + "Plugin is not available for Sponge. - " + ChatColor.YELLOW + "Click me!")
            }
        }
        val modifiedLore = arrayOfNulls<String>(this.lore.get().size)
        for (i in modifiedLore.indices) {
            modifiedLore[i] = this.lore.get()[i]
            if (this.lore.get()[i].contains("<permission>")) {
                if (permissions != null && (permissions.isEmpty() || this.hasPermission(player, permissions))) {
                    modifiedLore[i] = this.lore.get()[i].replace("<permission>", Config.getInstance<Any>().permissionIconYes)
                } else {
                    modifiedLore[i] = this.lore.get()[i].replace("<permission>", Config.getInstance<Any>().permissionIconNo)
                }
            }
        }
        return modifiedLore
    }

    private fun hasPermission(player: Player, permissions: Array<String?>): Boolean {
        for (permission in permissions) {
            if (permission!!.endsWith(".all")) {
                val subPermission = permission.substring(0, permission.indexOf("all")) + this.position
                if (player.hasPermission(subPermission)) {
                    return true
                }
            }
            if (player.hasPermission(permission))
                return true
        }
        return false
    }
}