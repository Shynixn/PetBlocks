package com.github.shynixn.petblocks.bukkit.logic.compatibility

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.compatibility.Config
import com.github.shynixn.petblocks.core.logic.compatibility.ItemContainer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.util.*
import java.util.logging.Level

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
class BukkitItemContainer : ItemContainer<Player> {
    constructor(enabled: Boolean, position: Int, page: GUIPage?, id: Int, damage: Int, skin: String?, unbreakable: Boolean, name: String?, lore: Array<out String>?) : super(enabled, position, page, id, damage, skin, unbreakable, name, lore)
    constructor(orderNumber: Int, data: MutableMap<String, Any>?) : super(orderNumber, data)

    private var cache : ItemStack? = null

    /**
     * Generates a new itemStack for the player and his permissions
     *
     * @param player      player
     * @param permissions permission
     * @return itemStack
     */
    override fun generate(player: Player, vararg permissions: String): Any {
        if (this.cache != null) {
            this.updateLore(player, permissions as  Array<String?>)
            return this.cache!!.clone()
        }
        try {
            if (this.isEnabled) {
                var itemStack: ItemStack? = ItemStack(MaterialCompatibility13.getMaterialFromId(this.itemId), 1, this.itemDamage.toShort())
                if (this.itemId == MaterialCompatibility13.getIdFromMaterial(Material.SKULL_ITEM) && this.skin != null) {
                    if (this.skin.contains("textures.minecraft.net")) {
                        SkinHelper.setItemStackSkin(itemStack, "http://" + this.skin)
                    } else {
                        val meta = itemStack!!.itemMeta as SkullMeta
                        meta.owner = this.skin
                        itemStack.itemMeta = meta
                    }
                }
                val data = HashMap<String, Any>()
                data["Unbreakable"] = this.isItemUnbreakable
                itemStack = PetBlockModifyHelper.setItemStackNBTTag(itemStack, data)
                val itemMeta = itemStack!!.itemMeta
                itemMeta.displayName = this.displayName.get()
                itemStack.itemMeta = itemMeta
                this.cache = itemStack
                this.updateLore(player, permissions as  Array<String?>)
                return itemStack
            }
        } catch (ex: Exception) {
            Bukkit.getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.RED + "Invalid config file. Fix the following error or recreate it!")
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to generate itemStack.", ex)
        }

        return ItemStack(Material.AIR)
    }

    private fun updateLore(player: Player, permissions: Array<String?>?) {
        val lore = this.provideLore(player, permissions)
        if (lore != null) {
            val meta = this.cache!!.itemMeta
            meta.lore = Arrays.asList(*lore)
            this.cache!!.itemMeta = meta
        }
    }

    private fun provideLore(player: Player, permissions: Array<String?>?): Array<String?>? {
        if (permissions != null && permissions.size == 1 && permissions[0] != null) {
            if (permissions.size == 1 && permissions[0] == "minecraft-heads") {
                return arrayOf(ChatColor.GRAY.toString() + "Use exclusive pet heads as costume.", ChatColor.YELLOW.toString() + "Sponsored by Minecraft-Heads.com")
            }
            if (permissions.size == 1 && permissions[0] == "head-database") {
                Bukkit.getPluginManager().getPlugin("HeadDatabase") ?: return arrayOf(ChatColor.DARK_RED.toString() + "" + ChatColor.ITALIC + "Plugin is not installed - " + ChatColor.YELLOW + "Click me!")
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