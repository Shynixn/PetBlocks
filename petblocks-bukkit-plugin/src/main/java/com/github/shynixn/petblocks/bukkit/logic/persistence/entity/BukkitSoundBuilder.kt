package com.github.shynixn.petblocks.bukkit.logic.persistence.entity

import com.github.shynixn.petblocks.bukkit.nms.VersionSupport
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundBuilder
import org.bukkit.Sound

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
class BukkitSoundBuilder(text: String?, volume: Double, pitch: Double) : SoundBuilder(text, volume, pitch) {

    /**
     * Plays the sound to the given players at the given location. Given players to far away cannot hear the sound.
     *
     * @param location location
     * @param players  players
     * @throws Exception exception
     */
    override fun <Location : Any?, Player : Any?> apply(location: Location, players: Array<out Player>?) {
        val inputPlayers = players as (Array<org.bukkit.entity.Player>)
        val inputLocation = location as org.bukkit.Location

        for (player in inputPlayers) {
            if (this.text == "none")
                return
            player.playSound(inputLocation, Sound.valueOf(this.text), this.volume, this.pitch)
        }
    }

    /**
     * Converts the sounds to 1.9 sounds
     */
    override fun convertSounds() {
        if (VersionSupport.getServerVersion() != null && VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
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
}