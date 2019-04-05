package com.github.shynixn.petblocks.sponge.logic.business.service

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.SoundService
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.google.inject.Inject
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player

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
class SoundServiceImpl @Inject constructor(private val configurationService: ConfigurationService, private val loggingService: LoggingService) : SoundService {
    private val mappings = hashMapOf("EAT" to "ENTITY_GENERIC_EAT", "EXPLODE" to "ENTITY_GENERIC_EXPLODE")

    /**
     * Plays the given [sound] at the given [location] for the given [player] or
     * all players in the world if the config option is enabled.
     * @param P the type of the player.
     * @param L the type of the location.
     */
    override fun <L, P> playSound(location: L, sound: Sound, player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (location !is Transform<*>) {
            throw IllegalArgumentException("Location has to be a SpongeTransform!")
        }

        val canOtherPlayersHearSound = configurationService.findValue<Boolean>("global-configuration.sounds-other-players")

        if (canOtherPlayersHearSound) {
            playSoundToPlayers(location.position, sound, player.world.players)
        } else {
            playSoundToPlayers(location.position, sound, listOf(player))
        }
    }

    /**
     * Plays the given [sound] at the given [location] for the given [players].
     */
    private fun playSoundToPlayers(location: Vector3d, sound: Sound, players: Collection<Player>) {
        if (sound.name.equals("none", true)) {
            return
        }

        val name = when {
            mappings.contains(sound.name) -> mappings[sound.name]!!
            sound.name.contains("WALK") -> "ENTITY_" + sound.name.split("_")[0] + "_STEP"
            sound.name.contains("IDLE") -> "ENTITY_" + sound.name.split("_")[0] + "_AMBIENT"
            else -> sound.name
        }

        try {
            for (player in players) {
                player.playSound(this.getSoundTypeFromName(name), location, sound.volume, sound.pitch)
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to send sound. Is the sound '" + sound.name + "' supported by this server version?", e)
        }
    }

    /**
     * Gets the soundType from the sound name.
     */
    private fun getSoundTypeFromName(name: String): SoundType {
        SoundTypes::class.java.declaredFields.filter { it.name.equals(name, ignoreCase = true) }.forEach {
            try {
                return it.get(null) as SoundType
            } catch (e: IllegalAccessException) {
            }
        }

        throw RuntimeException("The sound '$name' is not known to Sponge!")
    }
}