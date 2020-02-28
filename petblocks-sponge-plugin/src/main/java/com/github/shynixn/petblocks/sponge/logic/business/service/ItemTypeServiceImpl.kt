@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.service.ItemTypeService
import com.github.shynixn.petblocks.api.persistence.entity.Item
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.toText
import com.github.shynixn.petblocks.sponge.logic.business.extension.toTextString
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.item.ItemType
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.text.Text
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.util.*
import kotlin.collections.HashMap

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
class ItemTypeServiceImpl : ItemTypeService {
    private val cache = HashMap<Any, Any>()

    /**
     * Tries to find a matching itemType matching the given hint.
     */
    override fun <I> findItemType(sourceHint: Any): I {
        if (cache.containsKey(sourceHint)) {
            return cache[sourceHint]!! as I
        }

        var descHint = sourceHint

        if (descHint is ItemStack) {
            return descHint.type as I
        }

        if (sourceHint is MaterialType) {
            descHint = sourceHint.name
        }

        val intHint: Int? = if (descHint is Int) {
            descHint
        } else if (descHint is String && descHint.toIntOrNull() != null) {
            descHint.toInt()
        } else {
            null
        }

        if (intHint != null) {
            // It is a number.
            for (material in MaterialType.values()) {
                if (material.numericId == intHint) {
                    cache[sourceHint] = Sponge.getGame().registry.getType(ItemType::class.java, material.minecraftName).get()
                    return cache[sourceHint]!! as I
                }
            }
        }

        if (descHint is BlockType) {
            descHint = descHint.name
        }

        if (descHint is ItemType) {
            cache[sourceHint] = descHint
            return cache[sourceHint]!! as I
        }

        if (descHint is String) {
            cache[sourceHint] = try {
                Sponge.getGame().registry.getType(ItemType::class.java, MaterialType.valueOf(descHint).minecraftName).get()
            } catch (e: Exception) {
                try {
                    Sponge.getGame().registry.getType(ItemType::class.java, descHint).get()
                } catch (e: Exception) {
                    Sponge.getGame().registry.getType(BlockType::class.java, descHint).get()
                }
            }

            return cache[sourceHint]!! as I
        }

        throw IllegalArgumentException("Hint $sourceHint does not exist!")
    }

    /**
     * Tries to find the data value of the given hint.
     */
    override fun findItemDataValue(sourceHint: Any): Int {
        if (sourceHint is ItemStack) {
            return sourceHint.cast<net.minecraft.item.ItemStack>().itemDamage
        }

        if (sourceHint is Int) {
            return sourceHint
        }

        throw IllegalArgumentException("Hint $sourceHint does not exist!")
    }

    /**
     * Converts the given itemStack ot an item.
     */
    override fun <I> toItem(itemStack: I): Item {
        require(itemStack is ItemStack)

        val optDisplay = itemStack.get(Keys.DISPLAY_NAME)
        val displayName = if (optDisplay.isPresent) {
            optDisplay.get().toTextString()
        } else {
            null
        }

        val optLore = itemStack.get(Keys.ITEM_LORE)

        val lore = if (optLore.isPresent) {
            val items = ArrayList<String>()

            for (line in optLore.get()) {
                items.add(line.toTextString())
            }

            items
        } else {
            null
        }

        return ItemEntity(
            findItemType<ItemType>(itemStack).name,
            findItemDataValue(itemStack),
            false,
            displayName,
            lore,
            null
        )
    }

    /**
     * Converts the given item to an ItemStack.
     */
    override fun <I> toItemStack(item: Item): I {
        val itemstack = ItemStack.builder()
            .itemType(findItemType(item.type)).build()

        itemstack.cast<net.minecraft.item.ItemStack>().itemDamage = item.dataValue

        if (item.displayName != null) {
            itemstack.offer(Keys.DISPLAY_NAME, item.displayName!!.toText())
        }

        if (item.lore != null) {
            val items = ArrayList<Text>()

            for (line in item.lore!!) {
                items.add(line.toText())
            }

            itemstack.offer(Keys.ITEM_LORE, items)
        }

        if (item.skin != null) {
            val nmsItemStack = itemstack.cast<net.minecraft.item.ItemStack>()
            var newSkin = item.skin!!

            val nbtTagCompound = if (nmsItemStack.tagCompound != null) {
                nmsItemStack.tagCompound!!
            } else {
                NBTTagCompound()
            }

            if (newSkin.length > 32) {
                if (newSkin.contains("textures.minecraft.net")) {
                    if (!newSkin.startsWith("http://")) {
                        newSkin = "http://$newSkin"
                    }

                    newSkin = Base64Coder.encodeString("{textures:{SKIN:{url:\"$newSkin\"}}}")
                }

                val skinProfile = Sponge.getServer().gameProfileManager.createProfile(UUID.randomUUID(), null)
                val profileProperty =
                    Sponge.getServer().gameProfileManager.createProfileProperty("textures", newSkin, null)
                skinProfile.propertyMap.put("textures", profileProperty)

                val internalTag = NBTTagCompound()
                internalTag.setString("Id", skinProfile.uniqueId.toString())

                val propertiesTag = NBTTagCompound()

                for (content in skinProfile.propertyMap.keySet()) {
                    val nbtTagList = NBTTagList()

                    for (itemSkin in skinProfile.propertyMap.get(content)) {
                        val nbtItem = NBTTagCompound()
                        nbtItem.setString("Value", itemSkin.value)

                        if (itemSkin.hasSignature()) {
                            nbtItem.setString("Signature", itemSkin.signature.get())
                        }

                        nbtTagList.appendTag(nbtItem)
                    }

                    propertiesTag.setTag(content, nbtTagList)
                }

                internalTag.setTag("Properties", propertiesTag)
                nbtTagCompound.setTag("SkullOwner", internalTag)
            } else if (newSkin.isNotEmpty()) {
                nbtTagCompound.setString("SkullOwner", newSkin)
            }

            nmsItemStack.tagCompound = nbtTagCompound
        }

        if (item.unbreakable) {
            val nmsItemStack = itemstack.cast<net.minecraft.item.ItemStack>()

            val nbtTagCompound = if (nmsItemStack.tagCompound != null) {
                nmsItemStack.tagCompound!!
            } else {
                NBTTagCompound()
            }

            nbtTagCompound.setBoolean("Unbreakable", true)

            nmsItemStack.tagCompound = nbtTagCompound
        }

        return itemstack as I
    }
}