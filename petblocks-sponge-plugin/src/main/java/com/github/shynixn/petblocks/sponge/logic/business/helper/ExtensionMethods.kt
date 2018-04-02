package com.github.shynixn.petblocks.sponge.logic.business.helper

import com.github.shynixn.petblocks.sponge.nms.VersionSupport
import org.spongepowered.api.Game
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.serializer.TextSerializers
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
class ExtensionMethods

fun Game.sendMessage(message: String) {
    Sponge.getServer().console.sendMessage(message.translateToText())
}

fun Game.unloadPlugin(plugin: Any) {
    Sponge.getGame().eventManager.unregisterPluginListeners(this)
    Sponge.getGame().commandManager.getOwnedBy(this).forEach(Consumer { Sponge.getGame().commandManager.removeMapping(it) })
    Sponge.getGame().scheduler.getScheduledTasks(this).forEach(Consumer { it.cancel() })
}

fun Array<String?>.translateToTexts(): Array<Text?> {
    val copy = arrayOfNulls<Text>(this.size)
    this.forEachIndexed { i, p ->
        copy[i] = this[i]!!.translateToText()
    }
    return copy
}

fun Player.updateInventory() {
    ReflectionCache.updateInventoryMethod.invoke(this)
}

fun Player.sendMessage(text: String) {
    sendMessage(text.translateToText())
}

fun String.translateToText(): Text {
    return TextSerializers.LEGACY_FORMATTING_CODE.deserialize(this)
}

fun String.findServerVersion(): String {
    return this.replace("VERSION", VersionSupport.getServerVersion().versionText);
}

fun ItemStack.setSkin(skin: String) {
    if (skin.contains("textures.minecraft.net")) {
        ReflectionCache.setSkinUrlMethod.invoke(this, skin)
    } else {
        ReflectionCache.setSkinOwnerMethod.invoke(this, skin)
    }
}

private object ReflectionCache {
    val utilsClass = Class.forName("com.github.shynixn.petblocks.sponge.nms.VERSION.NMSUtils".findServerVersion())
    val setDamageMethod = utilsClass.getDeclaredMethod("setItemDamage", ItemStack::class.java, Int::class.java)
    val updateInventoryMethod = utilsClass.getDeclaredMethod("updateInventoryFor", Player::class.java)
    val setSkinUrlMethod = utilsClass.getDeclaredMethod("setSkinUrl", ItemStack::class.java, String::class.java)
    val setSkinOwnerMethod = utilsClass.getDeclaredMethod("setSkinOwner", ItemStack::class.java, String::class.java)
}

fun ItemStack.setDamage(damage: Int) {
    ReflectionCache.setDamageMethod!!.invoke(this, damage);
}