@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.extension

import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import net.minecraft.entity.player.EntityPlayerMP
import org.spongepowered.api.Game
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.source.ConsoleSource
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.item.inventory.type.CarriedInventory
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
import org.spongepowered.api.world.World
import java.lang.reflect.Field
import java.lang.reflect.Modifier

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
 * Converts the [Transform] to a Position.
 */
fun Transform<World>.toPosition(): Position {
    return PositionEntity(this.position.x, this.position.y, this.position.z, this.yaw.toDouble(), this.pitch.toDouble(), this.extent.name)
}

/**
 * Gets the server version the plugin is running on.
 */
fun getServerVersion(): Version {
    try {
        val version = Sponge.getPluginManager().getPlugin("sponge").get().version.get().split("-").dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        for (versionSupport in Version.values()) {
            if (versionSupport.bukkitId == version) {
                return versionSupport
            }
        }

    } catch (e: Exception) {
    }

    return Version.VERSION_UNKNOWN
}