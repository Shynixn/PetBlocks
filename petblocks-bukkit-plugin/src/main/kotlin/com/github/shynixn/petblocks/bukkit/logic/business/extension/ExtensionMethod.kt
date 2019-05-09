@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.extension

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.MemorySection
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.util.Vector
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.lang.reflect.InvocationTargetException
import java.util.*

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

/**
 * Deserializes the configuraiton section path to a map.
 */
fun FileConfiguration.deserializeToMap(path: String): Map<String, Any?> {
    val section = getConfigurationSection(path)!!.getValues(false)
    deserialize(section)
    return section
}

/**
 * Deserializes the given section.
 */
fun deserialize(section: MutableMap<String, Any?>) {
    section.keys.forEach { key ->
        if (section[key] is MemorySection) {
            val map = (section[key] as MemorySection).getValues(false)
            deserialize(map)
            section[key] = map
        }
    }
}

/**
 * Calls the distance method safely.
 */
fun Location.distanceSafely(target: Location): Double {
    if (this.world == null || target.world == null || this.world!!.name != target.world!!.name) {
        return Double.MAX_VALUE
    }

    return this.distance(target)
}

/**
 * Teleports the player via packets to keep his state in the world.
 */
fun Player.teleportUnsafe(location: Location) {
    val version = getServerVersion()
    val craftPlayer =
        Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".replace("VERSION", version.bukkitId))
            .cast(player)
    val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
    val entityPlayer = methodHandle.invoke(craftPlayer)
    val entityClazz = Class.forName("net.minecraft.server.VERSION.Entity".replace("VERSION", version.bukkitId))

    val setPositionMethod = entityClazz
        .getDeclaredMethod(
            "setPositionRotation",
            Double::class.java,
            Double::class.java,
            Double::class.java,
            Float::class.java,
            Float::class.java
        )

    setPositionMethod.invoke(entityPlayer, location.x, location.y, location.z, location.yaw, location.pitch)

    val packetTeleport =
        Class.forName("net.minecraft.server.VERSION.PacketPlayOutEntityTeleport".replace("VERSION", version.bukkitId))
            .getDeclaredConstructor(entityClazz).newInstance(entityPlayer)

    location.world!!.players.forEach { worldPlayer ->
        worldPlayer.sendPacket(packetTeleport)
    }
}

/**
 * Converts the [Position] to a BukkitVector.
 */
fun Position.toVector(): Vector {
    return Vector(this.x, this.y, this.z)
}

/**
 * Converts the [Position] to a BukkitLocation.
 */
fun Position.toLocation(): Location {
    return Location(Bukkit.getWorld(this.worldName!!), this.x, this.y, this.z, this.yaw.toFloat(), this.pitch.toFloat())
}

/**
 * Converts the [Vector] to a position
 */
fun Vector.toPosition(): Position {
    return PositionEntity(this.x, this.y, this.z)
}

/**
 * Converts the [Location] to a Position.
 */
fun Location.toPosition(): Position {
    return PositionEntity(this.x, this.y, this.z, this.yaw.toDouble(), this.pitch.toDouble(), this.world!!.name)
}

/**
 * Updates this inventory.
 */
fun PlayerInventory.updateInventory() {
    (this.holder as Player).updateInventory()
}

/**
 * Finds a class for the current version.
 */
fun Version.findClazz(name: String): Class<*> {
    return Class.forName(name.replace("VERSION", this.bukkitId))
}

/**
 * Transforms the given object to a memory section map.
 */
fun Any.yamlMap(deep: Boolean = false): Map<String, Any> {
    if (this !is MemorySection) {
        throw IllegalArgumentException("This object is not a MemorySection!")
    }

    return this.getValues(deep)
}

/**
 * Sends the given [packet] to this player.
 */
@Throws(
    ClassNotFoundException::class,
    IllegalAccessException::class,
    NoSuchMethodException::class,
    InvocationTargetException::class,
    NoSuchFieldException::class
)
fun Player.sendPacket(packet: Any) {
    val version = getServerVersion()
    val craftPlayer =
        Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".replace("VERSION", version.bukkitId))
            .cast(player)
    val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
    val entityPlayer = methodHandle.invoke(craftPlayer)

    val field = Class.forName("net.minecraft.server.VERSION.EntityPlayer".replace("VERSION", version.bukkitId))
        .getDeclaredField("playerConnection")
    field.isAccessible = true
    val connection = field.get(entityPlayer)

    val sendMethod = connection.javaClass.getDeclaredMethod("sendPacket", packet.javaClass.interfaces[0])
    sendMethod.invoke(connection, packet)
}

/**
 * Converts the current itemstack to an unbreakable itemstack.
 */
fun ItemStack.createUnbreakableCopy(): ItemStack {
    val version = getServerVersion()
    val nmsItemStackClass = Class.forName("net.minecraft.server.VERSION.ItemStack".replace("VERSION", version.bukkitId))
    val craftItemStackClass =
        Class.forName("org.bukkit.craftbukkit.VERSION.inventory.CraftItemStack".replace("VERSION", version.bukkitId))
    val nmsCopyMethod = craftItemStackClass.getDeclaredMethod("asNMSCopy", ItemStack::class.java)
    val nmsToBukkitMethod = craftItemStackClass.getDeclaredMethod("asBukkitCopy", nmsItemStackClass)

    val nbtTagClass = Class.forName("net.minecraft.server.VERSION.NBTTagCompound".replace("VERSION", version.bukkitId))
    val getNBTTag = nmsItemStackClass.getDeclaredMethod("getTag")
    val setNBTTag = nmsItemStackClass.getDeclaredMethod("setTag", nbtTagClass)
    val nbtSetBoolean =
        nbtTagClass.getDeclaredMethod("setBoolean", String::class.java, Boolean::class.javaPrimitiveType)

    val nmsItemStack = nmsCopyMethod.invoke(null, this)
    var nbtTag = getNBTTag.invoke(nmsItemStack)

    if (nbtTag == null) {
        nbtTag = nbtTagClass.newInstance()
    }

    nbtSetBoolean.invoke(nbtTag, "Unbreakable", true)
    setNBTTag.invoke(nmsItemStack, nbtTag)

    return nmsToBukkitMethod.invoke(null, nmsItemStack) as ItemStack
}

/**
 * Changes the displayname of the itemstack.
 * Gets an empty string if the displayName is not present.
 */
var ItemStack.displayName: String
    get() {
        if (this.itemMeta != null && (this.itemMeta!!.displayName as String?) != null) {
            return itemMeta!!.displayName
        }

        return ""
    }
    set(value) {
        val meta = itemMeta
        meta!!.setDisplayName(value.translateChatColors())
        itemMeta = meta
    }

/**
 * Sets the itemstack lore.
 */
fun ItemStack.setLore(lore: List<String>): ItemStack {
    val meta = itemMeta
    val tmpLore = ArrayList<String>()

    lore.forEach { l ->
        tmpLore.add(l.translateChatColors())
    }

    meta!!.lore = tmpLore

    itemMeta = meta
    return this
}

/**
 * Tries to return the [ParticleType] from the given [name].
 */
fun String.toParticleType(): ParticleType {
    val version = getServerVersion()

    ParticleType.values().forEach { p ->
        if (p.gameId_18.equals(this, true) || p.gameId_113.equals(this, true) || p.name.equals(this, true)) {
            if (version.isVersionSameOrGreaterThan(p.sinceVersion)) {
                return p
            }
        }
    }

    throw IllegalArgumentException("ParticleType cannot be parsed from '" + this + "'.")
}

/**
 * Gets the skin of an itemstack.
 */
var ItemStack.skin: String?
    get() {
        val currentMeta = this.itemMeta as? SkullMeta ?: return null

        if (!currentMeta.owner.isNullOrEmpty()) {
            return currentMeta.owner
        }

        val cls = Class.forName(
            "org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull".replace(
                "VERSION",
                getServerVersion().bukkitId
            )
        )
        val real = cls.cast(currentMeta)
        val field = real.javaClass.getDeclaredField("profile")
        field.isAccessible = true
        val profile = field.get(real) as GameProfile

        return profile.properties.get("textures").toTypedArray()[0].value
    }
    set(value) {
        val currentMeta = this.itemMeta as? SkullMeta ?: return

        if (value == null) {
            return
        }

        var newSkin = value

        if (newSkin.length > 32) {
            val cls = Class.forName(
                "org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull".replace(
                    "VERSION",
                    getServerVersion().bukkitId
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
            itemMeta = SkullMeta::class.java.cast(real)
        } else if (value.isNotEmpty()) {
            currentMeta.owner = value
            itemMeta = currentMeta
        }
    }

/**
 * Gets the server version the plugin is running on.
 */
fun getServerVersion(): Version {
    try {
        if (Bukkit.getServer() == null || Bukkit.getServer().javaClass.getPackage() == null) {
            return Version.VERSION_UNKNOWN
        }

        val version = Bukkit.getServer().javaClass.getPackage().name.replace(
            ".",
            ","
        ).split(",".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[3]

        for (versionSupport in Version.values()) {
            if (versionSupport.bukkitId == version) {
                return versionSupport
            }
        }

    } catch (e: Exception) {
    }

    return Version.VERSION_UNKNOWN
}