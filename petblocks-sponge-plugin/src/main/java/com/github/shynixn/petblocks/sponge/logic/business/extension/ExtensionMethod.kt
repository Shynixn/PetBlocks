@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.extension

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import net.minecraft.entity.player.EntityPlayerMP
import org.spongepowered.api.Game
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.type.CarriedInventory
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.world.World


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
 * Unloads the given plugin.
 */
fun Game.disablePlugin(plugin: Any) {
    Sponge.getGame().eventManager.unregisterPluginListeners(plugin)
    Sponge.getGame().commandManager.getOwnedBy(plugin).forEach { Sponge.getGame().commandManager.removeMapping(it) }
    Sponge.getGame().scheduler.getScheduledTasks(plugin).forEach { it.cancel() }
}

/**
 * Sends the [message] to the console source.
 */
fun ConsoleSource.sendMessage(message: String) {
    this.sendMessage(message.toText())
}

/**
 * Converts the given string to a text.
 */
fun String.toText(): Text {
    return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(this.translateChatColors())
}

/**
 * Converts the given text to a string.
 */
fun Text.toTextString(): String {
    return TextSerializers.LEGACY_FORMATTING_CODE.serialize(this)
}

/**
 * Updates the inventory of the player.
 */
fun CarriedInventory<*>.updateInventory() {
    val player = this.carrier.get()
    (player as EntityPlayerMP).sendContainerToPlayer((player as EntityPlayerMP).openContainer)
}

/**
 * Gets the x coordinate.
 */
val Transform<World>.x
    get() = this.position.x

/**
 * Gets the y coordinate.
 */
val Transform<World>.y
    get() = this.position.y

/**
 * Gets the z coordinate.
 */
val Transform<World>.z
    get() = this.position.z

/**
 * Gets the yaw.
 */
val Transform<World>.yaw
    get() = this.rotation.y

/**
 * Gets the pitch.
 */
val Transform<World>.pitch
    get() = this.rotation.x

/**
 * Converts the [Transform] to a Position.
 */
fun Transform<World>.toPosition(): Position {
    return PositionEntity(this.x, this.y, this.z, this.yaw, this.pitch, this.extent.name)
}


/**
 * Converts the [Transform] to a Position.
 */
fun Position.toTransform(): Transform<World> {
    return Transform(Sponge.getServer().getWorld(worldName!!).get(), Vector3d(this.x, this.y, this.z), Vector3d(0.0, this.yaw, this.pitch))
}

/**
 * Converts the [Vector3d] to a Position.
 */
fun Vector3d.toPosition(): Position {
    return PositionEntity(this.x, this.y, this.z)
}

/**
 * Converts the [Position] to a Vector.
 */
fun Position.toVector(): Vector3d {
    return Vector3d(this.x, this.y, this.z)
}

/**
 * Sets the itemstack lore.
 */
fun ItemStack.setLore(lore: List<String>): ItemStack {
    val items = ArrayList<Text>()

    for (line in lore) {
        items.add(line.toText())
    }

    this.offer(org.spongepowered.api.data.key.Keys.ITEM_LORE, items)

    return this
}

/**
 * Changes the displayname of the itemstack.
 * Gets an empty string if the displayName is not present.
 */
var ItemStack.displayName: String
    get() {
        val optDisplay = this.get(org.spongepowered.api.data.key.Keys.DISPLAY_NAME)

        if(optDisplay.isPresent){
            return optDisplay.get().toTextString()
        }

        return ""
    }
    set(value) {
        this.offer(org.spongepowered.api.data.key.Keys.DISPLAY_NAME, value.toText())
    }

/**
 * Gets the skin of an itemstack.
 */
var ItemStack.skin: String?
    get() {
        return null
    }
    set(value) {
        if (value == null) {
            return
        }

  /*      var newSkin = value

        if (newSkin.length > 32) {

        } else if (value.isNotEmpty()) {

        }


        val item = itemStack as Any as net.minecraft.anchor.v1_12_mcpR1.item.ItemStack

        var compound = item.getTagCompound()
        if (compound == null) {
            compound = NBTTagCompound()
        }

        val newSkinProfile = Sponge.getServer().gameProfileManager.createProfile(UUID.randomUUID(), null)
        val profileProperty = Sponge.getServer().gameProfileManager.createProfileProperty(
            "textures", Base64.getEncoder().encodeToString(
                "{textures:{SKIN:{url:\"$skinUrl\"}}}".getBytes()
            ), null
        )
        newSkinProfile.propertyMap.put("textures", profileProperty)

        var nbttagcompound = NBTTagCompound()
        nbttagcompound = writeGameProfile(nbttagcompound, newSkinProfile)
        compound!!.setTag("SkullOwner", nbttagcompound)
        item.setTagCompound(compound)





*/


    }


/**
 * Converts the current itemstack to an unbreakable itemstack.
 */
fun ItemStack.createUnbreakableCopy(): ItemStack {
   throw RuntimeException()
}


/**
 * Gets the server version the plugin is running on.
 */
fun getServerVersion(): Version {
    try {
        val version =
            Sponge.getPluginManager().getPlugin("sponge").get().version.get().split("-").dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        for (versionSupport in Version.values()) {
            if (versionSupport.bukkitId == version) {
                return versionSupport
            }
        }

    } catch (e: Exception) {
    }

    return Version.VERSION_UNKNOWN
}