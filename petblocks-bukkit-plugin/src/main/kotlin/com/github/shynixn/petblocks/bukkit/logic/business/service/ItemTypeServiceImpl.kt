@file:Suppress("UNCHECKED_CAST", "DEPRECATION")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.ItemTypeService
import com.github.shynixn.petblocks.api.persistence.entity.Item
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import com.google.inject.Inject
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.util.*
import kotlin.collections.HashMap

class ItemTypeServiceImpl @Inject constructor(private val version: Version) : ItemTypeService {
    private val cache = HashMap<Any, Any>()

    /**
     * Tries to find the data value of the given hint.
     */
    override fun findItemDataValue(sourceHint: Any): Int {
        if (sourceHint is ItemStack) {
            return sourceHint.durability.toInt()
        }

        if (sourceHint is Int) {
            return sourceHint
        }

        throw IllegalArgumentException("Hint $sourceHint does not exist!")
    }

    /**
     * Converts the given item to an ItemStack.
     */
    override fun <I> toItemStack(item: Item): I {
        val itemStack = ItemStack(findItemType<Material>(item.type), 1, item.dataValue.toShort())

        if (itemStack.itemMeta != null) {
            var currentMeta = itemStack.itemMeta

            if (!item.skin.isNullOrEmpty() && currentMeta is SkullMeta) {
                var newSkin = item.skin!!

                if (newSkin.length > 32) {
                    val cls = Class.forName(
                        "org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull".replace(
                            "VERSION",
                            version.bukkitId
                        )
                    )
                    val real = cls.cast(currentMeta)
                    val field = real.javaClass.getDeclaredField("profile")
                    val newSkinProfile = GameProfile(UUID.randomUUID(), null)

                    if (newSkin.contains("textures.minecraft.net")) {
                        if (!newSkin.startsWith("http://")) {
                            newSkin = "http://$newSkin"
                        }

                        newSkin = Base64Coder.encodeString("{textures:{SKIN:{url:\"$newSkin\"}}}")
                    }

                    newSkinProfile.properties.put("textures", Property("textures", newSkin))
                    field.isAccessible = true
                    field.set(real, newSkinProfile)
                    currentMeta = SkullMeta::class.java.cast(real)
                } else {
                    currentMeta.owner = newSkin
                }
            }

            if (item.displayName != null) {
                currentMeta!!.setDisplayName(item.displayName!!.translateChatColors())
            }

            if (item.lore != null) {
                currentMeta!!.lore = item.lore!!.map { l -> l.translateChatColors() }.toMutableList()
            }

            itemStack.itemMeta = currentMeta
        }

        return if (item.nbtTag != "") {
            if (version.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1)) {
                val nmsItemStackClass = findClazz("net.minecraft.world.item.ItemStack")
                val craftItemStackClass = findClazz("org.bukkit.craftbukkit.VERSION.inventory.CraftItemStack")
                val nmsCopyMethod = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack::class.java)
                val nmsToBukkitMethod = craftItemStackClass.getDeclaredMethod("asBukkitCopy", nmsItemStackClass)

                val nbtTagClass = findClazz("net.minecraft.nbt.NBTTagCompound")
                val getNBTTag =
                    if (version.isVersionSameOrGreaterThan(Version.VERSION_1_19_R1)) {
                        nmsItemStackClass.getDeclaredMethod("u")
                    } else if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R2)) {
                        nmsItemStackClass.getDeclaredMethod("t")
                    } else if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R1)) {
                        nmsItemStackClass.getDeclaredMethod("s")
                    } else {
                        nmsItemStackClass.getDeclaredMethod("getTag")
                    }

                val setNBTTag = if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R1)) {
                    nmsItemStackClass.getDeclaredMethod("c", nbtTagClass)
                } else {
                    nmsItemStackClass.getDeclaredMethod("setTag", nbtTagClass)
                }

                val nmsItemStack = nmsCopyMethod.invoke(null, itemStack)
                var targetNbtTag = getNBTTag.invoke(nmsItemStack)

                if (targetNbtTag == null) {
                    targetNbtTag = nbtTagClass.newInstance()
                }

                val compoundMapField = nbtTagClass.getDeclaredField("x")
                compoundMapField.isAccessible = true
                val targetNbtMap = compoundMapField.get(targetNbtTag) as MutableMap<Any?, Any?>

                try {
                    val mojangsonParser = findClazz(
                        "net.minecraft.nbt.MojangsonParser"
                    )
                    val sourceNbtTag = if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R1)) {
                        mojangsonParser.getDeclaredMethod("a", String::class.java).invoke(null, item.nbtTag)
                    } else {
                        mojangsonParser.getDeclaredMethod("parse", String::class.java).invoke(null, item.nbtTag)
                    }

                    val sourceNbtMap = compoundMapField.get(sourceNbtTag) as MutableMap<Any?, Any?>

                    for (key in sourceNbtMap.keys) {
                        targetNbtMap[key] = sourceNbtMap[key]
                    }

                    setNBTTag.invoke(nmsItemStack, targetNbtTag)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return nmsToBukkitMethod.invoke(null, nmsItemStack) as I
            } else {
                val nmsItemStackClass =
                    Class.forName("net.minecraft.server.VERSION.ItemStack".replace("VERSION", version.bukkitId))
                val craftItemStackClass =
                    Class.forName(
                        "org.bukkit.craftbukkit.VERSION.inventory.CraftItemStack".replace(
                            "VERSION",
                            version.bukkitId
                        )
                    )
                val nmsCopyMethod = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack::class.java)
                val nmsToBukkitMethod = craftItemStackClass.getDeclaredMethod("asBukkitCopy", nmsItemStackClass)

                val nbtTagClass =
                    Class.forName("net.minecraft.server.VERSION.NBTTagCompound".replace("VERSION", version.bukkitId))
                val getNBTTag = nmsItemStackClass.getDeclaredMethod("getTag")
                val setNBTTag = nmsItemStackClass.getDeclaredMethod("setTag", nbtTagClass)

                val nmsItemStack = nmsCopyMethod.invoke(null, itemStack)
                var targetNbtTag = getNBTTag.invoke(nmsItemStack)

                if (targetNbtTag == null) {
                    targetNbtTag = nbtTagClass.newInstance()
                }

                val compoundMapField = nbtTagClass.getDeclaredField("map")
                compoundMapField.isAccessible = true
                val targetNbtMap = compoundMapField.get(targetNbtTag) as MutableMap<Any?, Any?>

                try {
                    val sourceNbtTag = Class.forName(
                        "net.minecraft.server.VERSION.MojangsonParser".replace(
                            "VERSION",
                            version.bukkitId
                        )
                    )
                        .getDeclaredMethod("parse", String::class.java).invoke(null, item.nbtTag)
                    val sourceNbtMap = compoundMapField.get(sourceNbtTag) as MutableMap<Any?, Any?>

                    for (key in sourceNbtMap.keys) {
                        targetNbtMap[key] = sourceNbtMap[key]
                    }

                    setNBTTag.invoke(nmsItemStack, targetNbtTag)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                return nmsToBukkitMethod.invoke(null, nmsItemStack) as I
            }
        } else {
            itemStack as I
        }
    }

    /**
     * Converts the given itemStack ot an item.
     */
    override fun <I> toItem(itemStack: I): Item {
        require(itemStack is ItemStack)

        val displayName = if (itemStack.itemMeta != null) {
            itemStack.itemMeta!!.displayName
        } else {
            null
        }

        val lore = if (itemStack.itemMeta != null) {
            itemStack.itemMeta!!.lore
        } else {
            null
        }

        val skin = if (itemStack.itemMeta != null && itemStack.itemMeta is SkullMeta) {
            val currentMeta = itemStack.itemMeta as SkullMeta
            val owner = currentMeta.owner

            if (!owner.isNullOrEmpty()) {
                owner
            } else {
                val cls = Class.forName(
                    "org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull".replace(
                        "VERSION",
                        version.bukkitId
                    )
                )
                val real = cls.cast(currentMeta)
                val field = real.javaClass.getDeclaredField("profile")
                field.isAccessible = true
                val profile = field.get(real) as GameProfile?

                if (profile == null) {
                    null
                } else {
                    profile.properties.get("textures").toTypedArray()[0].value
                }
            }
        } else {
            null
        }

        return ItemEntity(
            findItemType<Material>(itemStack).name,
            itemStack.durability.toInt(),
            "",
            displayName,
            lore,
            skin
        )
    }

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
            val idField = Material::class.java.getDeclaredField("id")
            idField.isAccessible = true

            for (material in Material::class.java.enumConstants) {
                if (idField.get(material) as Int == intHint) {
                    cache[sourceHint] = material
                    return cache[sourceHint]!! as I
                }
            }
        }

        if (descHint is Material) {
            cache[sourceHint] = descHint
            return cache[sourceHint]!! as I
        }

        if (descHint is String) {
            for (material in Material::class.java.enumConstants) {
                try {
                    // "${material} also delivers interesting hints.
                    if (material.name.equals(descHint, true) || ("LEGACY_$descHint" == material.name)) {
                        cache[sourceHint] = material
                        return cache[sourceHint]!! as I
                    }
                } catch (e: Exception) {
                }
            }
        }

        throw IllegalArgumentException("Hint $sourceHint does not exist!")
    }
}
