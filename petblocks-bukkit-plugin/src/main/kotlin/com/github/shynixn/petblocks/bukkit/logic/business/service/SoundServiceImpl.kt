@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.SoundType
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.logging.Level

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
class SoundServiceImpl @Inject constructor(private val plugin: Plugin, private val configurationService: ConfigurationService, private val version: Version) : SoundService {
    private val soundCache = HashMap<String, org.bukkit.Sound>()

    /**
     * Plays the given [sound] at the given [location] for the given [player] or
     * all players in the world if the config option is enabled.
     */
    override fun <L, P> playSound(location: L, sound: Sound, player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val canOtherPlayersHearSound = configurationService.findValue<Boolean>("global-configuration.sounds-other-players")

        if (canOtherPlayersHearSound) {
            playSound(location, sound, player.world.players)
        } else {
            playSound(location, sound, listOf(player))
        }
    }

    /**
     * Plays the given [sound] at the given [location] for the given [players].
     */
    private fun <L, P> playSound(location: L, sound: Sound, players: Collection<P>) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        val soundName = sound.name.toUpperCase()

        if (soundName.equals("NONE", true) || soundName.equals("")) {
            return
        }

        try {
            if (!soundCache.containsKey(soundName)) {
                val soundType = SoundType.values().firstOrNull { s -> s.name == soundName }
                if (soundType != null) {
                    for (name in soundType.soundNames) {
                        try {
                            soundCache[soundName] = org.bukkit.Sound.valueOf(name)
                            break
                        } catch (i: IllegalArgumentException) {
                        }
                    }
                } else {
                    soundCache[soundName] = org.bukkit.Sound.valueOf(soundName)
                }
            }

            val playSound = soundCache[soundName] ?: throw IllegalArgumentException("Cannot find sound.")

            for (player in players) {
                (player as Player).playSound(location, playSound, sound.volume.toFloat(), sound.pitch.toFloat())
            }
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed to send sound. Is the sound '" + sound.name + "' supported by this server version?", e)
        }
    }
}
