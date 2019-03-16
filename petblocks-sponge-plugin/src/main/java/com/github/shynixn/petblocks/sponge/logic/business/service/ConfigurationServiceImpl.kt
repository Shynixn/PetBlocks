@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.service.AIService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.ItemService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.GuiItem
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiItemEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.displayName
import com.github.shynixn.petblocks.sponge.logic.business.extension.lore
import com.google.inject.Inject
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.plugin.PluginContainer
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

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
class ConfigurationServiceImpl @Inject constructor(
    @ConfigDir(sharedRoot = false) private val privateConfigDir: Path,
    private val loggingService: LoggingService,
    private val pluginContainer: PluginContainer,
    private val itemService: ItemService,
    private val aiService: AIService
) : ConfigurationService {

    private val cache = HashMap<String, List<GuiItem>>()
    private var disableOnSneak: List<String>? = null
    private var configurationNode: ConfigurationNode? = null

    /**
     * Init.
     */
    init {
        refresh()
    }

    /**
     * Gets the dataFolder.
     */
    override val dataFolder: Path
        get() {
            return privateConfigDir
        }

    /**
     * Tries to load the config value from the given [path].
     * Throws a [IllegalArgumentException] if the path could not be correctly
     * loaded.
     */
    override fun <C> findValue(path: String): C {
        if (path == "plugin.version") {
            return pluginContainer.version.get() as C
        }

        if (!contains(path)) {
            throw IllegalArgumentException("Path '$path' could not be found!")
        }

        if (path == "global-configuration.disable-on-sneak") {
            if (disableOnSneak == null) {
                val items = path.split(".").toTypedArray()
                disableOnSneak = this.configurationNode!!.getNode(*items as Array<Any>).value as List<String>
            }

            return disableOnSneak!! as C
        }

        val items = path.split(".").toTypedArray()
        val data = this.configurationNode!!.getNode(*items as Array<Any>).value

        if (data is String) {
            return data.translateChatColors() as C
        }

        return data as C
    }

    /**
     * Opens a new inputStream to the given [resource].
     */
    override fun openResourceInputStream(resource: String): InputStream {
        val rootAsset = this.pluginContainer.getAsset(resource)

        if (rootAsset.isPresent) {
            return rootAsset.get().url.openStream()
        }

        val assetName = resource.replace("assets/petblocks/", "")

        return this.pluginContainer.getAsset(assetName).get().url.openStream()
    }

    /**
     * Checks if the given path is containing in the config.yml.
     */
    override fun contains(path: String): Boolean {
        try {
            val items = path.split(".").toTypedArray()
            val targetNode = this.configurationNode!!.getNode(*items as Array<Any>)
            targetNode.value ?: return false

            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Tries to return a list of [GuiItem] matching the given path from the config.
     * Can be called asynchronly.
     */
    override fun findGUIItemCollection(path: String): List<GuiItem>? {
        if (cache.containsKey(path)) {
            return cache[path]!!
        }

        val items = ArrayList<GuiItem>()
        val section = findValue<Map<String, Any>>(path)

        section.keys.forEach { key ->
            val guiItem = GuiItemEntity()
            val guiIcon = guiItem.icon
            val description = section[key] as Map<String, Any>

            if (description.containsKey("row") && description.containsKey("col")) {
                var column = (description["col"] as Int - 1)
                column += ((column / 9) * 45)
                guiItem.position = (description["row"] as Int - 1) * 9 + column
            }

            this.setItem<String>("permission", description) { value -> guiItem.permission = value }
            this.setItem<Boolean>("hidden", description) { value -> guiItem.hidden = value }
            this.setItem<Int>("position", description) { value -> guiItem.position = value - 1 }
            this.setItem<Boolean>("fixed", description) { value -> guiItem.fixed = value }
            this.setItem<String>("script", description) { value -> guiItem.script = value }

            val iconDescription = (description["icon"] as Map<String, Any>)

            this.setItem<Int>("id", iconDescription) { value -> guiIcon.skin.typeName = value.toString() }
            this.setItem<Int>("damage", iconDescription) { value -> guiIcon.skin.dataValue = value }
            this.setItem<String>("name", iconDescription) { value -> guiIcon.displayName = value }
            this.setItem<Boolean>("unbreakable", iconDescription) { value -> guiIcon.skin.unbreakable = value }
            this.setItem<String>("skin", iconDescription) { value -> guiIcon.skin.owner = value }
            this.setItem<String>("script", iconDescription) { value -> guiIcon.script = value }
            this.setItem<List<String>>("lore", iconDescription) { value -> guiIcon.lore = value }

            val skinDescription = if (description.containsKey("set-skin")) {
                guiItem.targetSkin = SkinEntity()
                (description["set-skin"] as Map<String, Any>)
            } else {
                null
            }

            this.setItem<Int>("id", skinDescription) { value -> guiItem.targetSkin!!.typeName = value.toString() }
            this.setItem<Int>("damage", skinDescription) { value -> guiItem.targetSkin!!.dataValue = value }
            this.setItem<Boolean>("unbreakable", skinDescription) { value -> guiItem.targetSkin!!.unbreakable = value }
            this.setItem<String>("skin", skinDescription) { value ->
                if (value.startsWith("minecraft-heads.com/")) {
                    guiItem.icon.skin.sponsored = true
                    guiItem.targetSkin!!.owner = findMinecraftHeadsItem(value.split("/")[1].toInt()).second
                } else {
                    guiItem.targetSkin!!.owner = value
                }
            }

            if (description.containsKey("add-ai")) {
                val goalsMap = (description["add-ai"] as Map<String, Any>)

                for (goalKey in goalsMap.keys) {
                    val aiMap = goalsMap[goalKey] as Map<String, Any>
                    val type = aiMap["type"] as String
                    guiItem.addAIs.add(aiService.deserializeAiBase(type, aiMap))
                }
            }

            if (description.containsKey("remove-ai")) {
                val goalsMap = (description["remove-ai"] as Map<String, Any>)

                for (goalKey in goalsMap.keys) {
                    val aiMap = goalsMap[goalKey] as Map<String, Any>
                    val type = aiMap["type"] as String
                    guiItem.removeAIs.add(aiService.deserializeAiBase(type, aiMap))
                }
            }

            if (description.containsKey("replace-ai")) {
                val goalsMap = (description["replace-ai"] as Map<Int, Any>)

                for (goalKey in goalsMap.keys) {
                    val aiMap = goalsMap[goalKey] as Map<String, Any>
                    val type = aiMap["type"] as String
                    guiItem.addAIs.add(aiService.deserializeAiBase(type, aiMap))
                    guiItem.removeAIs.add(aiService.deserializeAiBase(type, aiMap))
                }
            }

            if (description.containsKey("blocked-on")) {
                guiItem.blockedCondition = (description["blocked-on"] as List<String>).toTypedArray()
            }

            if (description.containsKey("hidden-on")) {
                guiItem.hiddenCondition = (description["hidden-on"] as List<String>).toTypedArray()
            }

            if (guiItem.icon.displayName.startsWith("minecraft-heads.com/")) {
                guiItem.icon.displayName = findMinecraftHeadsItem(guiItem.icon.displayName.split("/")[1].toInt()).first
            }

            if (guiItem.icon.skin.owner.startsWith("minecraft-heads.com/")) {
                guiItem.icon.skin.sponsored = true
                guiItem.icon.skin.owner = findMinecraftHeadsItem(guiItem.icon.skin.owner.split("/")[1].toInt()).second
            }

            items.add(guiItem)
        }

        cache[path] = items
        return items
    }

    /**
     * Sets optional gui items to a instance.
     */
    private fun <T> setItem(key: String, map: Map<String, Any>?, f: (T) -> Unit) {
        if (map != null && map.containsKey(key)) {
            f.invoke(map[key] as T)
        }
    }

    /**
     * Tries to return a [GuiItem] matching the displayName and the lore of the given [item].
     * Can be called from Asynchronly.
     */
    override fun <I> findClickedGUIItem(path: String, item: I): GuiItem? {
        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be an SpongeItemStack")
        }

        if (!this.cache.containsKey(path)) {
            return null
        }

        for (guiItem in this.cache[path]!!) {
            try {
                if (item.displayName == guiItem.icon.displayName.translateChatColors()) {
                    if ((item.lore == null && guiItem.icon.lore.isEmpty()) || (item.lore != null && item.lore!!.size == guiItem.icon.lore.size)) {
                        return guiItem
                    }
                }
            } catch (e: Exception) {
                // Ignored
            }
        }

        return null
    }

    /**
     * Clears cached resources and refreshes the used configuration.
     */
    override fun refresh() {
        if (!this.privateConfigDir.toFile().exists()) {
            this.privateConfigDir.toFile().mkdir()
        }

        val configFile = privateConfigDir.resolve("config.yml")

        if (!Files.exists(configFile)) {
            try {
                pluginContainer.getAsset("config.yml").get().copyToFile(configFile)
            } catch (e: IOException) {
                loggingService.warn("Failed to create config.yml.", e)
            }
        }

        disableOnSneak = null
        cache.clear()

        val loader = YAMLConfigurationLoader.builder().setPath(configFile).build()
        this.configurationNode = loader.load()
    }

    /**
     * Generates the default pet meta.
     */
    override fun generateDefaultPetMeta(uuid: String, name: String): PetMeta {
        val playerMeta = PlayerMetaEntity(uuid, name)
        val petMeta = PetMetaEntity(playerMeta, SkinEntity())

        val defaultConfig = findValue<Map<String, Any>>("pet")
        val skin = defaultConfig["skin"] as Map<String, Any>

        setItem<Boolean>("enabled", defaultConfig) { value -> petMeta.enabled = value }
        setItem<String>("name", defaultConfig) { value -> petMeta.displayName = value.replace("<player>", name) }
        setItem<Boolean>("sound-enabled", defaultConfig) { value -> petMeta.soundEnabled = value }
        setItem<Boolean>("particle-enabled", defaultConfig) { value -> petMeta.particleEnabled = value }

        val typePayload = skin["id"]

        val typename = if (typePayload is Int) {
            itemService.createItemStack(typePayload).typeName
        } else {
            typePayload as String
        }

        petMeta.skin.typeName = typename
        setItem<Int>("damage", skin) { value -> petMeta.skin.dataValue = value }
        setItem<Boolean>("unbreakable", skin) { value -> petMeta.skin.unbreakable = value }
        setItem<String>("skin", skin) { value -> petMeta.skin.owner = value }

        petMeta.aiGoals.clear()

        val goalsMap = defaultConfig["add-ai"] as Map<Int, Any?>

        for (goalKey in goalsMap.keys) {
            val aiMap = goalsMap[goalKey] as Map<String, Any>
            val type = aiMap["type"] as String
            petMeta.aiGoals.add(aiService.deserializeAiBase(type, aiMap))
        }

        petMeta.new = true

        return petMeta
    }

    /**
     * Tries to find a minecraft heads.com pair.
     */
    private fun findMinecraftHeadsItem(id: Int): Pair<String, String> {
        val identifier = id.toString()
        val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

        decipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(Base64.getDecoder().decode("YjJjNWIzOTQ0NTY1NDJlNQ=="), "AES"),
            IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8")))
        )
        BufferedReader(
            InputStreamReader(
                CipherInputStream(
                    pluginContainer.getAsset("minecraftheads.db").get().url.openStream(),
                    decipher
                )
            )
        ).use { reader ->
            while (true) {
                val s = reader.readLine() ?: break

                if (s.startsWith(identifier)) {
                    val content = s.split(";")
                    return Pair(content[2], content[3])
                }
            }
        }

        throw RuntimeException("Cannot locate minecraft-heads.com item with id $id.")
    }
}