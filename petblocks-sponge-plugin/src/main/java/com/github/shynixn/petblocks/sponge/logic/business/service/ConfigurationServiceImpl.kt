@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.sponge.logic.business.helper.getDisplayName
import com.github.shynixn.petblocks.sponge.logic.business.helper.getLore
import com.github.shynixn.petblocks.sponge.logic.business.helper.getResource
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.SpongeStaticGUIItems
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeGUIItem
import com.github.shynixn.petblocks.sponge.logic.persistence.entity.SpongeItemContainer
import com.google.inject.Inject
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.plugin.PluginContainer
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.nio.file.Path
import java.util.*
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
class ConfigurationServiceImpl @Inject constructor(private val plugin: PluginContainer, private val guiItemsController: SpongeStaticGUIItems, @ConfigDir(sharedRoot = false) private val privateConfigDir: Path) : ConfigurationService {
    private val cache = HashMap<String, List<GUIItem>>()
    private lateinit var node: ConfigurationNode

    init {
        try {
            val defaultConfig = this.privateConfigDir.resolve("config.yml")
            val loader = YAMLConfigurationLoader.builder().setPath(defaultConfig).build()
            this.node = loader.load()

            guiItemsController.reload()
        } catch (e: IOException) {
            plugin.logger.warn("Failed to reload config.yml.", e)
        }
    }

    /**
     * Tries to load the config value from the given [path].
     * Throws a [IllegalArgumentException] if the path could not be correctly
     * loaded.
     */
    override fun <C> findValue(path: String): C {
        val items = path.split(Pattern.quote(".").toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val targetNode = this.node.getNode(*items as Array<Any>)
        var data = targetNode.value

        if (data is String) {
            data = ChatColor.translateAlternateColorCodes('&', data)
        }

        return data as C
    }

    /**
     * Tries to return a list of [GUIItem] matching the given path from the config.
     * Can be called asynchronly.
     */
    override fun findGUIItemCollection(path: String): Optional<List<GUIItem>> {
        if (cache.containsKey(path)) {
            return Optional.of(cache[path]!!)
        }

        if (path.startsWith("minecraft-heads-com.")) {
            val category = path.split(".")[1]
            val items = getItemsFromMinecraftHeadsDatabase(category)
            cache[path] = items
            return Optional.of(items)
        }

        val items = ArrayList<GUIItem>()
        try {
            val data = Config.getData<Map<Int, Any>>(path)
            for (key in data!!.keys) {
                val container = SpongeGUIItem((key), data[key] as Map<String, Any>)
                items.add(container)
            }

            cache[path] = items
        } catch (e: Exception) {
            plugin.logger.warn("Failed load GUI Item collection called '$path'.", e)
        }

        return Optional.of(items)
    }

    /**
     * Tries to return a [GUIItem] matching the displayName and the lore of the given [item].
     * Can be called from Asynchronly.
     */
    override fun <I> findClickedGUIItem(item: I): Optional<GUIItem> {
        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be an BukkitItemStack")
        }

        guiItemsController.all.forEach { i ->
            try {

                if ((i as GUIItemContainer<*>).displayName.isPresent && !(i as GUIItemContainer<*>).displayName.get().trim().isEmpty()) {
                    if (item.getDisplayName() == (i as GUIItemContainer<*>).displayName.get()) {
                        val lore = i.lore.get()

                        if (item.getLore().size == lore.size) {
                            return Optional.of(SpongeGUIItem(i))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Ignored
            }
        }

        return Optional.empty()
    }

    /**
     * Returns the minecraft-heads.com category heads.
     */
    private fun getItemsFromMinecraftHeadsDatabase(category: String): List<GUIItem> {
        val items = ArrayList<GUIItem>()
        try {
            val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            decipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(Base64Coder.decode("Ydy3wN+SnAgC/sYQZ72yEg=="), "AES"), IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8"))))
            BufferedReader(InputStreamReader(CipherInputStream(plugin.getResource("minecraftheads.db"), decipher))).use { reader ->
                var s: String?
                val splitter = Pattern.quote(",")
                var i = 0
                while (true) {
                    s = reader.readLine()
                    if (s == null) {
                        break
                    }
                    val tags = s.split(splitter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (tags[0].equals(category, true) && tags.size == 3 && tags[2].length % 4 == 0) {
                        i++
                        try {
                            val line = Base64Coder.decodeString(tags[2]).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "")
                            val url = line.substring(0, line.indexOf("\""))
                            val texture = url.substring(7, url.length)
                            val container = SpongeGUIItem(SpongeItemContainer(true, i, GUIPage.MINECRAFTHEADS_COSTUMES, 397, 3, texture, false, tags[1].replace("\"", ""), emptyArray()))
                            items.add(container)
                        } catch (ignored: Exception) {
                            plugin.logger.warn("Failed parsing minecraftheads.com head.", ignored)
                        }
                    }
                }
            }
        } catch (e: Exception) {
            plugin.logger.warn("Failed to read minecraft-heads.com skins.", e)
        }

        return items
    }
}