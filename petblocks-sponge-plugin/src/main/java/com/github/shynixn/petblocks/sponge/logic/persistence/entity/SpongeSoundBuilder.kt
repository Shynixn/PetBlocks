package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundBuilder
import com.github.shynixn.petblocks.sponge.nms.VersionSupport
import org.spongepowered.api.effect.sound.SoundType
import org.spongepowered.api.effect.sound.SoundTypes
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World

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
class SpongeSoundBuilder : SoundBuilder{

    constructor(text: String?, volume: Double, pitch: Double) : super(text, volume, pitch)
    constructor(items: Map<String, Any>?) : super(items)

    /**
     * Plays the sound to the given players at the given location. Given players to far away cannot hear the sound.
     *
     * @param location location
     * @param players  players
     * @throws Exception exception
     */
    override fun <Location : Any?, Player : Any?> apply(location: Location, players: Array<out Player>?) {
        val inputPlayers = players as (Array<org.spongepowered.api.entity.living.player.Player>)
        val inputLocation = location as Transform<World>
        for (player in inputPlayers!!) {
            if (this.text == "none")
                return
            player.playSound(this.getSoundTypeFromName(this.text)!!, inputLocation.position, this.volume.toDouble(), this.pitch.toDouble())
        }
    }

    /**
     * Converts the sounds to 1.9 sounds
     */
    override fun convertSounds() {
        if (VersionSupport.getServerVersion() != null && VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_12_R1)) {
            when (this.text) {
                "ENDERMAN_IDLE" -> {
                    this.text = "ENTITY_ENDERMEN_AMBIENT"
                }
                "MAGMACUBE_WALK" -> {
                    this.text = "ENTITY_MAGMACUBE_JUMP"
                }
                "SLIME_WALK" -> {
                    this.text = "ENTITY_SLIME_JUMP"
                }
                "EXPLODE" -> {
                    this.text = "ENTITY_GENERIC_EXPLODE"
                }

                "EAT" -> {
                    this.text = "ENTITY_GENERIC_EAT"
                }
                "WOLF_GROWL" -> {
                    this.text = "ENTITY_WOLF_GROWL"
                }
                "CAT_MEOW" -> {
                    this.text = "ENTITY_CAT_PURREOW"
                }
                "HORSE_GALLOP" -> {
                    this.text = "ENTITY_GENERIC_EXPLODE"
                }
                "ENTITY_HORSE_GALLOP" -> {
                    this.text = "ENTITY_GENERIC_EXPLODE"
                }
                "BAT_LOOP" -> {
                    this.text = "ENTITY_BAT_LOOP"
                }
                "GHAST_SCREAM" -> {
                    this.text = "ENTITY_GHAST_SCREAM"
                }
                "BLAZE_BREATH" -> {
                    this.text = "ENTITY_BLAZE_AMBIENT"
                }
                "ENDERDRAGON_WINGS" -> {
                    this.text = "ENTITY_ENDERDRAGON_FLAP"
                }
                "ENDERDRAGON_GROWL" -> {
                    this.text = "ENTITY_ENDERDRAGON_GROWL"
                }
                "none" -> {
                    this.text = "none"
                }
                else -> {
                    if (this.text.contains("WALK")) {
                        this.text = "ENTITY_" + this.text.toUpperCase().split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0] + "_STEP"
                    } else if (this.text.contains("IDLE")) {
                        this.text = "ENTITY_" + this.text.toUpperCase().split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0] + "_AMBIENT"
                    }
                }
            }
        }
    }

    private fun getSoundTypeFromName(name: String): SoundType? {
        SoundTypes::class.java.declaredFields
                .filter { it.name.equals(name, ignoreCase = true) }
                .forEach {
                    try {
                        return it.get(null) as SoundType
                    } catch (e: IllegalAccessException) {
                        e.printStackTrace()
                    }
                }
        return null
    }
}