@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.GuiItem
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiItemEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import com.google.inject.Inject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.*
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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
class GUIItemLoadServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val itemTypeService: ItemTypeService,
    private val aiService: AIService,
    private val localizationService: LocalizationService
) : GUIItemLoadService {
    private val cache = HashMap<String, List<GuiItem>>()

    /**
     * Clears cached resources and refreshes the used configuration.
     */
    override fun reload() {
        cache.clear()
    }

    /**
     * Tries to return a [GuiItem] matching the displayName and the lore of the given [item].
     * Can be called asynchronly. Uses the [path] parameter for faster fetching.
     * @param I the type of the itemstack.
     */
    override fun <I> findClickedGUIItem(path: String, item: I): GuiItem? {
        if (!this.cache.containsKey(path)) {
            return null
        }

        val convertedItem = itemTypeService.toItem(item)

        for (guiItem in this.cache[path]!!) {
            try {
                if (convertedItem.displayName != null && convertedItem.displayName!!.equals(
                        guiItem.icon.displayName.translateChatColors(),
                        true
                    )
                ) {
                    if ((convertedItem.lore == null && guiItem.icon.lore.isEmpty()) || (convertedItem.lore!!.size == guiItem.icon.lore.size)) {
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
     * Tries to return a list of [GuiItem] matching the given path from the config.
     * Can be called asynchronly.
     */
    override fun findGUIItemCollection(path: String): List<GuiItem>? {
        if (cache.containsKey(path)) {
            return cache[path]!!
        }

        val items = ArrayList<GuiItem>()
        val section = configurationService.findValue<Map<String, Any>>(path)

        for (key in section.keys) {
            val guiItem = GuiItemEntity()
            val guiIcon = guiItem.icon
            val description = section[key] as Map<String, Any>

            if (description.containsKey("row") && description.containsKey("col")) {
                var column = (description["col"] as Int - 1)
                column += ((column / 9) * 45)
                guiItem.position = (description["row"] as Int - 1) * 9 + column
            }

            if (hasConfiguration(description, "permission")) {
                guiItem.permission = description["permission"] as String
            }

            if (hasConfiguration(description, "hidden")) {
                guiItem.hidden = description["hidden"] as Boolean
            }

            if (hasConfiguration(description, "position")) {
                guiItem.position = description["position"] as Int - 1
            }

            if (hasConfiguration(description, "fixed")) {
                guiItem.fixed = description["fixed"] as Boolean
            }

            if (hasConfiguration(description, "script")) {
                guiItem.scripts = if (description["script"] is List<*>) {
                    description["script"] as List<String>
                } else {
                    listOf(description["script"] as String)
                }
            }

            if (hasConfiguration(description, "petname")) {
                guiItem.targetPetName = description["petname"] as String
                guiItem.targetPetName = localizationService.translate(guiItem.targetPetName!!)
            }

            val iconDescription = description["icon"] as Map<String, Any>

            if (hasConfiguration(iconDescription, "id")) {
                guiIcon.skin.typeName = if (iconDescription["id"] is Int) {
                    (iconDescription["id"] as Int).toString()
                } else {
                    iconDescription["id"] as String
                }
            }

            if (hasConfiguration(iconDescription, "damage")) {
                guiIcon.skin.dataValue = iconDescription["damage"] as Int
            }

            if (hasConfiguration(iconDescription, "name")) {
                guiIcon.displayName = iconDescription["name"] as String
                guiIcon.displayName = localizationService.translate(guiIcon.displayName)
            }

            if (hasConfiguration(iconDescription, "nbt")) {
                guiIcon.skin.nbtTag = iconDescription["nbt"] as String
            }

            if (hasConfiguration(iconDescription, "skin")) {
                guiIcon.skin.owner = iconDescription["skin"] as String
            }

            if (hasConfiguration(iconDescription, "script")) {
                guiIcon.script = iconDescription["script"] as String
            }

            if (hasConfiguration(iconDescription, "lore")) {
                guiIcon.lore = iconDescription["lore"] as List<String>

                val copiedLore = guiIcon.lore.toTypedArray()
                for (i in copiedLore.indices) {
                    copiedLore[i] = localizationService.translate(copiedLore[i])
                }

                guiIcon.lore = copiedLore.toList()
            }

            val skinDescription = if (description.containsKey("set-skin")) {
                guiItem.targetSkin = SkinEntity()
                description["set-skin"] as Map<String, Any>
            } else {
                null
            }

            if (hasConfiguration(skinDescription, "id")) {
                guiItem.targetSkin!!.typeName = (skinDescription!!["id"] as Int).toString()
            }

            if (hasConfiguration(skinDescription, "damage")) {
                guiItem.targetSkin!!.dataValue = skinDescription!!["damage"] as Int
            }

            if (hasConfiguration(skinDescription, "nbt")) {
                guiItem.targetSkin!!.nbtTag = skinDescription!!["nbt"] as String
            }

            if (hasConfiguration(skinDescription, "skin")) {
                val value = skinDescription!!["skin"] as String

                if (value.startsWith("minecraft-heads.com/")) {
                    guiItem.icon.skin.sponsored = true
                    guiItem.targetSkin!!.owner = findMinecraftHeadsItem(value.split("/")[1].toInt()).second
                } else {
                    guiItem.targetSkin!!.owner = value
                }
            }

            val aiSet = aiService.loadAisFromConfig<AIBase>("$path.$key")
            guiItem.addAIs.addAll(aiSet.first)
            guiItem.removeAIs.addAll(aiSet.second)

            if (description.containsKey("blocked-on")) {
                guiItem.blockedConditions = (description["blocked-on"] as List<String>)
            }

            if (description.containsKey("allowed-on")) {
                guiItem.allowedConditions = (description["allowed-on"] as List<String>)
            }

            if (description.containsKey("hidden-on")) {
                guiItem.hiddenConditions = (description["hidden-on"] as List<String>)
            }

            if (description.containsKey("show-on")) {
                guiItem.showConditions = (description["show-on"] as List<String>)
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
        return cache[path]!!
    }

    /**
     * Generates the default pet meta.
     */
    override fun generateDefaultPetMeta(uuid: String, name: String): PetMeta {
        val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, name), SkinEntity())
        val defaultConfig = configurationService.findValue<Map<String, Any>>("pet")

        if (hasConfiguration(defaultConfig, "enabled")) {
            petMeta.enabled = defaultConfig["enabled"] as Boolean
        }

        if (hasConfiguration(defaultConfig, "name")) {
            petMeta.displayName = (defaultConfig["name"] as String)
            petMeta.displayName = localizationService.translate(petMeta.displayName).replace("<player>", name)
        }

        if (hasConfiguration(defaultConfig, "sound-enabled")) {
            petMeta.soundEnabled = defaultConfig["sound-enabled"] as Boolean
        }

        if (hasConfiguration(defaultConfig, "particle-enabled")) {
            petMeta.particleEnabled = defaultConfig["particle-enabled"] as Boolean
        }

        val skin = defaultConfig["skin"] as Map<String, Any>
        val typePayload = skin["id"]

        petMeta.skin.typeName = if (typePayload is Int) {
            typePayload.toString()
        } else {
            typePayload as String
        }

        if (hasConfiguration(skin, "damage")) {
            petMeta.skin.dataValue = skin["damage"] as Int
        }

        if (hasConfiguration(skin, "nbt")) {
            petMeta.skin.nbtTag = skin["nbt"] as String
        }

        if (hasConfiguration(skin, "skin")) {
            petMeta.skin.owner = skin["skin"] as String
        }

        petMeta.aiGoals.clear()

        val aiSet = aiService.loadAisFromConfig<AIBase>("pet")
        petMeta.aiGoals.addAll(aiSet.first)

        petMeta.new = true

        return petMeta
    }

    /**
     * Checks if the given [source] has got the given [name].
     */
    private fun hasConfiguration(source: Map<String, Any?>?, name: String): Boolean {
        return source != null && source.containsKey(name)
    }

    /**
     * Tries to find a minecraft heads.com pair.
     */
    private fun findMinecraftHeadsItem(id: Int): Pair<String, String> {
        val identifier = id.toString()
        val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")

        decipher.init(
            Cipher.DECRYPT_MODE,
            SecretKeySpec(Base64.getDecoder().decode("ZGM2NGIxYjhmZTI4NDZmMA=="), "AES"),
            IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8")))
        )
        BufferedReader(
            InputStreamReader(
                CipherInputStream(
                    configurationService.openResource("assets/petblocks/minecraftheads.db"),
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
