@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.GuiItem
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toParticleType
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiIconEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiItemEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity
import com.google.inject.Inject
import org.bukkit.ChatColor
import org.bukkit.configuration.MemorySection
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level
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
class ConfigurationServiceImpl @Inject constructor(private val plugin: Plugin) : ConfigurationService {
    private val cache = HashMap<String, List<GuiItem>>()
    private var namingMessage: ChatMessage? = null
    private var skullNamingMessage: ChatMessage? = null

    /**
     * Tries to load the config value from the given [path].
     * Throws a [IllegalArgumentException] if the path could not be correctly
     * loaded.
     */
    override fun <C> findValue(path: String): C {
        if (path == "messages.naming-suggest") {
            if (namingMessage == null) {
                namingMessage = chatMessage {
                    text {
                        findValue<String>("messages.prefix") + findValue("messages.naming-suggest-prefix")
                    }
                    component {
                        text {
                            findValue("messages.naming-suggest-clickable")
                        }
                        clickAction {
                            ChatClickAction.SUGGEST_COMMAND to "/" + findValue("petblocks-gui.command") + " rename "
                        }
                        hover {
                            text {
                                findValue("messages.naming-suggest-hover")
                            }
                        }
                    }
                    text {
                        findValue("messages.naming-suggest-suffix")
                    }
                }
            }

            return namingMessage as C
        }

        if (path == "messages.skullnaming-suggest") {
            if (skullNamingMessage == null) {
                skullNamingMessage = chatMessage {
                    text {
                        findValue<String>("messages.prefix") + findValue("messages.skullnaming-suggest-prefix")
                    }
                    component {
                        text {
                            findValue("messages.skullnaming-suggest-clickable")
                        }
                        clickAction {
                            ChatClickAction.SUGGEST_COMMAND to "/" + findValue("petblocks-gui.command") + " skin "
                        }
                        hover {
                            text {
                                findValue("messages.skullnaming-suggest-hover")
                            }
                        }
                    }
                    text {
                        findValue("messages.skullnaming-suggest-suffix")
                    }
                }
            }

            return skullNamingMessage as C
        }

        if (path == "plugin.version") {
            return plugin.description.version as C
        }

        if (!plugin.config.contains(path)) {
            throw IllegalArgumentException("Path '$path' could not be found!")
        }

        var data = this.plugin.config.get(path)

        if (data is String) {
            data = ChatColor.translateAlternateColorCodes('&', data)
            return data as C
        }

        if (data is MemorySection) {
            if (data.contains("name") && data.contains("speed") && data.contains("amount")) {
                val particle = ParticleEntity()
                val values = data.getValues(false)

                with(particle) {
                    type = (values["name"] as String).toParticleType()
                    amount = values["amount"] as Int
                    speed = values["speed"] as Double
                }

                if (values.containsKey("offx")) {
                    with(particle) {
                        offSetX = values["offx"] as Double
                        offSetY = values["offy"] as Double
                        offSetZ = values["offz"] as Double
                    }
                }

                if (values.containsKey("red")) {
                    with(particle) {
                        colorRed = values["red"] as Int
                        colorGreen = values["green"] as Int
                        colorBlue = values["blue"] as Int
                    }
                }


                if (values.containsKey("id")) {
                    if (values["id"] is String) {
                        particle.materialName = values["id"] as String
                    } else {
                        throw IllegalArgumentException("WAT")
                        //  particle.materialName = MaterialCompatibility13.getMaterialFromId(values["id"] as Int).name
                    }
                }

                return particle as C
            } else if (data.contains("name") && data.contains("volume")) {
                val sound = SoundEntity()
                val values = data.getValues(false)

                with(sound) {
                    name = values["name"] as String
                    volume = values["volume"] as Double
                    pitch = values["pitch"] as Double
                }

                return sound as C
            }
        }

        return data as C
    }

    /**
     * Tries to return a list of [GuiItem] matching the given path from the config.
     * Can be called asynchronly.
     */
    override fun findGUIItemCollection(path: String): Optional<List<GuiItem>> {
        if (cache.containsKey(path)) {
            return Optional.of(cache[path]!!)
        }

        val items = ArrayList<GuiItem>()
        val section = (plugin.config.get(path) as MemorySection).getValues(false)

        section.keys.forEach { key ->
            val guiItem = GuiItemEntity()
            val guiIcon = guiItem.icon
            val description = (section[key] as MemorySection).getValues(true)

            this.setItem<Boolean>("visible", description) { value -> guiItem.visible = value }
            this.setItem<Int>("position", description) { value -> guiItem.position = value }
            this.setItem<String>("script", description) { value -> guiItem.script = value }

            this.setItem<Int>("icon.id", description) { value -> guiIcon.type = value }
            this.setItem<Int>("icon.damage", description) { value -> guiIcon.data = value }
            this.setItem<String>("icon.name", description) { value -> guiIcon.displayName = value }
            this.setItem<Boolean>("icon.unbreakable", description) { value -> guiIcon.unbreakable = value }
            this.setItem<String>("icon.skin", description) { value -> guiIcon.skin = value }
            this.setItem<String>("icon.script", description) { value -> guiIcon.script = value }
            this.setItem<List<String>>("icon.lore", description) { value -> guiIcon.lore = value }

            items.add(guiItem)
        }

        cache[path] = items
        return Optional.of(items)
    }

    /**
     * Sets optional gui items to a instance.
     */
    private fun <T> setItem(key: String, map: Map<String, Any>, f: (T) -> Unit) {
        if (map.containsKey(key)) {
            f.invoke(map[key]!! as T)
        }
    }

    /**
     * Tries to return a [GuiItem] matching the displayName and the lore of the given [item].
     * Can be called from Asynchronly.
     */
    override fun <I> findClickedGUIItem(item: I): Optional<GuiItem> {
        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be an BukkitItemStack")
        }
        /**
         *   if (path.startsWith("minecraft-heads-com.")) {
        val category = path.split(".")[1]
        val items = getItemsFromMinecraftHeadsDatabase(category)
        cache[path] = items
        return Optional.of(items)
        }

         */


        /*guiItemsController.all.forEach { i ->
            try {

                if ((i as GUIItemContainer<*>).displayName.isPresent && !(i as GUIItemContainer<*>).displayName.get().trim().isEmpty()) {
                    if (item.itemMeta.displayName == (i as GUIItemContainer<*>).displayName.get()) {
                        val lore = i.lore.get()
                        val meta = item.itemMeta
                        if (meta.lore == null) {
                            meta.lore = ArrayList()
                        }

                        if (meta.lore.size == lore.size) {
                            return Optional.of(BukkitGUIItem(i))
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                // Ignored
            }
        }*/

        return Optional.empty()
    }

    /**
     * Clears cached resources and refreshes the used configuration.
     */
    override fun refresh() {
        cache.clear()
        namingMessage = null
        skullNamingMessage = null
    }

    /**
     * Returns the minecraft-heads.com category heads.
     */
    private fun getItemsFromMinecraftHeadsDatabase(category: String): List<GuiItem> {
        val items = ArrayList<GuiItem>()
        /*  try {
              val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
              decipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(Base64Coder.decode("Ydy3wN+SnAgC/sYQZ72yEg=="), "AES"), IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8"))))
              BufferedReader(InputStreamReader(CipherInputStream(JavaPlugin.getPlugin(PetBlocksPlugin::class.java).getResource("assets/petblocks/minecraftheads.db"), decipher))).use { reader ->
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
                              val container = BukkitGUIItem(BukkitItemContainer(true, i, GUIPage.MINECRAFTHEADS_COSTUMES, 397, 3, texture, false, tags[1].replace("\"", ""), emptyArray()))
                              items.add(container)
                          } catch (ignored: Exception) {
                              PetBlocksPlugin.logger().log(Level.WARNING, "Failed parsing minecraftheads.com head.", ignored)
                          }
                      }
                  }
              }
          } catch (e: Exception) {
              PetBlocksPlugin.logger().log(Level.WARNING, "Failed to read minecraft-heads.com skins.", e)
          }*/

        return items
    }
}