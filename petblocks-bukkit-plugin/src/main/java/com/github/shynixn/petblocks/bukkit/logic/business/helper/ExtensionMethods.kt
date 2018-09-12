@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.helper

import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.PersistenceService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager
import com.github.shynixn.petblocks.bukkit.nms.VersionSupport
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import org.bukkit.entity.Entity
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory
import org.bukkit.inventory.meta.SkullMeta
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.InvocationTargetException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

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

fun String.findServerVersion(): String {
    return this.replace("VERSION", VersionSupport.getServerVersion().versionText)
}

/**
 * Sets the item in the players arm.
 */
fun PlayerInventory.setItemStackInHand(itemStack: ItemStack?, offHand: Boolean = false) {
    val version = VersionSupport.getServerVersion()

    if (version.isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
        val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

        if (offHand) {
            inventoryClazz.getDeclaredMethod("setItemInOffHand", ItemStack::class.java).invoke(this, itemStack)
        } else {
            inventoryClazz.getDeclaredMethod("setItemInMainHand", ItemStack::class.java).invoke(this, itemStack)
        }
    } else {
        Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("setItemInHand", ItemStack::class.java)
                .invoke(this.holder, itemStack)
    }
}

/**
 * Is this the pet of this player entity?
 */
fun Entity.isPetOfPlayer(player: Player): Boolean {
    try {
        val petblock = PetBlockManager.instance.petBlockController.getFromPlayer(player)
        if (petblock.isPresent) {
            val block = petblock.get()
            if (block.armorStand != null && block.engineEntity != null && (block.armorStand == this || block.engineEntity == this)) {
                return true
            }
        }
    } catch (ignored: Exception) {
    }

    return false
}

/**
 * Returns if this entity is a pet.
 */
fun Entity.isPet(): Boolean {
    try {
        for (block in PetBlockManager.instance.petBlockController.all) {
            if (block != null && block.armorStand != null && block.engineEntity != null && (block.armorStand == this || block.engineEntity == this)) {
                return true
            }
        }
    } catch (ignored: Exception) {
    }

    return false
}

/**
 * Gets the item in the players arm.
 */
fun PlayerInventory.getItemStackInHand(offHand: Boolean = false): Optional<ItemStack> {
    val version = VersionSupport.getServerVersion()

    return if (version.isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
        val inventoryClazz = Class.forName("org.bukkit.inventory.PlayerInventory")

        if (offHand) {
            Optional.ofNullable(inventoryClazz.getDeclaredMethod("getItemInOffHand").invoke(this)) as Optional<ItemStack>
        } else {
            Optional.ofNullable(inventoryClazz.getDeclaredMethod("getItemInMainHand").invoke(this)) as Optional<ItemStack>
        }
    } else {
        Optional.ofNullable(Class.forName("org.bukkit.entity.HumanEntity").getDeclaredMethod("getItemInHand")
                .invoke(this.holder)) as Optional<ItemStack>
    }
}

/**
 * Updates this inventory.
 */
fun PlayerInventory.updateInventory() {
    Player::class.java.getDeclaredMethod("updateInventory").invoke(this.holder as Player)
}

/**
 * Executes the given [f] for the given [plugin] on main thread.
 */
inline fun Any.sync(plugin: Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, crossinline f: () -> Unit) {
    if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimer(plugin, {
            f.invoke()
        }, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLater(plugin, {
            f.invoke()
        }, delayTicks)
    }
}

/**
 * Executes the given [f] for the given [plugin] asynchronly.
 */
inline fun Any.async(plugin: Plugin, delayTicks: Long = 0L, repeatingTicks: Long = 0L, crossinline f: () -> Unit) {
    if (repeatingTicks > 0) {
        plugin.server.scheduler.runTaskTimerAsynchronously(plugin, {
            f.invoke()
        }, delayTicks, repeatingTicks)
    } else {
        plugin.server.scheduler.runTaskLaterAsynchronously(plugin, {
            f.invoke()
        }, delayTicks)
    }
}

fun Inventory.clearCompletely() {
    for (i in 0 until contents.size) {
        setItem(i, null)
    }
}

/**
 * Sends the given [packet] to this player.
 */
@Throws(ClassNotFoundException::class, IllegalAccessException::class, NoSuchMethodException::class, InvocationTargetException::class, NoSuchFieldException::class)
fun Player.sendPacket(packet: Any) {
    val version = VersionSupport.getServerVersion()
    val craftPlayer = Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".replace("VERSION", version.versionText)).cast(player)
    val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
    val entityPlayer = methodHandle.invoke(craftPlayer)

    val field = Class.forName("net.minecraft.server.VERSION.EntityPlayer".replace("VERSION", version.versionText)).getDeclaredField("playerConnection")
    field.isAccessible = true
    val connection = field.get(entityPlayer)

    val sendMethod = connection.javaClass.getDeclaredMethod("sendPacket", packet.javaClass.interfaces[0])
    sendMethod.invoke(connection, packet)
}

fun ItemStack.setUnbreakable(unbreakable: Boolean): ItemStack {
    val data = HashMap<String, Any>()
    data["Unbreakable"] = unbreakable
    return PetBlockModifyHelper.setItemStackNBTTag(this, data)
}

fun ItemStack.setDisplayName(displayName: String): ItemStack {
    val meta = itemMeta
    meta.displayName = displayName.translateChatColors()
    itemMeta = meta
    return this
}

fun ItemStack.setLore(lore: List<String>): ItemStack {
    val meta = itemMeta
    val tmpLore = ArrayList<String>()

    lore.forEach { l ->
        tmpLore.add(l.translateChatColors())
    }

    meta.lore = tmpLore

    itemMeta = meta
    return this
}

/**
 * Tries to return the [ParticleType] from the given [name].
 */
fun String.toParticleType(): ParticleType {
    val version = VersionSupport.getServerVersion()

    ParticleType.values().forEach { p ->
        if (p.gameId_18.equals(this, true) || p.gameId_113.equals(this, true) || p.name.equals(this, true)) {
            if (version.isVersionSameOrGreaterThan(VersionSupport.fromVersion(p.sinceVersion))) {
                return p
            }
        }
    }

    throw IllegalArgumentException("ParticleType cannot be parsed from '" + this + "'.")
}

fun PersistenceService.runOnMainThread(runnable: Runnable) {
    val plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)
    plugin.server.scheduler.runTask(plugin, runnable)
}

fun <T> CompletableFuture<T>.thenAcceptOnMainThread(action: Consumer<in T>) {
    this.thenAccept { p ->
        val plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)
        plugin.server.scheduler.runTask(plugin, {
            action.accept(p)
        })
    }
}

/**
 * Sets the skin of the itemstack.
 */
fun ItemStack.setSkin(skin: String): ItemStack {
    if (skin.contains("textures.minecraft.net")) {
        if (skin.startsWith("http://")) {
            SkinHelper.setItemStackSkin(this, skin)
        } else {
            SkinHelper.setItemStackSkin(this, "http://$skin")
        }
    } else {
        val meta = itemMeta as SkullMeta
        meta.owner = skin
        itemMeta = meta
    }
    return this
}
