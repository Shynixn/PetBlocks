package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.PlaceHolder
import com.github.shynixn.petblocks.api.business.service.DependencyPlaceholderApiService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.google.inject.Inject
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

/**
 * Created by Shynixn 2020.
 * <p>
 * Version 1.5
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2020 by Shynixn
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
class DependencyPlaceholderApiServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val persistencePetMetaService: PersistencePetMetaService
) : PlaceholderExpansion(),
    DependencyPlaceholderApiService {
    private var registerd: Boolean = false

    /**
     * Registers the placeholder hook if it is not already registered.
     */
    override fun registerListener() {
        if (!registerd) {
            this.register()
            registerd = true
        }
    }

    /**
     * Applies external placeHolders to the given text and returns the new text.
     */
    override fun <P> applyPlaceHolders(player: P, text: String): String {
        require(player is Player)

        return try {
            PlaceholderAPI.setPlaceholders(player, text)
        } catch (e: Throwable) {
            text
        }
    }

    /**
     * Gets the expansion version which is the same of the plugin version.
     */
    override fun getVersion(): String {
        return plugin.description.version
    }

    /**
     * Gets the expansion author for placeholderapi.
     */
    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    /**
     * Gets the identifier which is required by placeholderapi to match the placeholder against this plugin.
     */
    override fun getIdentifier(): String {
        return "petblocks"
    }

    /**
     * Gets called when a new value of the given placeholder is requested.
     */
    override fun onPlaceholderRequest(player: Player?, placeHolderName: String?): String? {
        if (player == null) {
            return null
        }

        if (!persistencePetMetaService.hasPetMeta(player)) {
            return null
        }

        if (placeHolderName == null) {
            return null
        }

        try {
            val placeHolder = PlaceHolder.values().asSequence().firstOrNull { p -> p.placeHolder == placeHolderName }
                ?: return null

            val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)

            when (placeHolder) {
                PlaceHolder.PET_ENABLED -> {
                    return petMeta.enabled.toString()
                }
                PlaceHolder.PET_NAME -> {
                    return petMeta.displayName
                }
                PlaceHolder.PET_SOUND_ENABLED -> {
                    return petMeta.soundEnabled.toString()
                }
                PlaceHolder.PET_PARTICLE_ENABLED -> {
                    return petMeta.particleEnabled.toString()
                }
                PlaceHolder.PET_SKIN_TYPENAME -> {
                    return petMeta.skin.typeName
                }
                PlaceHolder.PET_SKIN_DATA -> {
                    return petMeta.skin.dataValue.toString()
                }
                PlaceHolder.PET_SKIN_HEAD -> {
                    return petMeta.skin.owner
                }
                PlaceHolder.PET_SKIN_NBT -> {
                    return petMeta.skin.nbtTag
                }
                else -> {
                    return null
                }
            }
        } catch (ignored: Exception) {
            return null
        }
    }
}