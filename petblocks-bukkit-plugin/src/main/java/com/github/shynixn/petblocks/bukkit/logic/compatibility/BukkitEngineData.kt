package com.github.shynixn.petblocks.bukkit.logic.compatibility

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.enumeration.RideType
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toParticleType
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.compatibility.EngineData
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SoundEntity
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player

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
class BukkitEngineData(id: Long, data: Map<String, Any>) : EngineData<Player>(id) {
    init {
        this.itemContainer = BukkitItemContainer(id.toInt(), (data["gui"] as MemorySection).getValues(false))
        this.entity = data["behaviour.entity"] as String
        this.rideType = RideType.valueOf(data["behaviour.riding"] as String)

        this.ambientSound = SoundEntity(data["sound.ambient.name"] as String)
        this.ambientSound.volume = data["sound.ambient.volume"] as Double
        this.ambientSound.pitch = data["sound.ambient.pitch"] as Double

        this.walkingSound = SoundEntity(data["sound.walking.name"] as String)
        this.walkingSound.volume = data["sound.walking.volume"] as Double
        this.walkingSound.pitch = data["sound.walking.pitch"] as Double

        if (data.containsKey("petname")) {
            this.petName = data["petname"] as String
        }

        if (data.containsKey("particle")) {
            val entityParticle = ParticleEntity(ParticleType.NONE)
            val values = (data["particle"] as MemorySection).getValues(false)

            with(entityParticle) {
                type = (values["name"] as String).toParticleType()
                amount = values["amount"] as Int
                speed = values["speed"] as Double
            }

            if (values.containsKey("offx")) {
                with(entityParticle) {
                    offSetX = values["offx"] as Double
                    offSetY = values["offy"] as Double
                    offSetZ = values["offz"] as Double
                }
            }

            if (values.containsKey("red")) {
                with(entityParticle) {
                    colorRed = values["red"] as Int
                    colorGreen = values["green"] as Int
                    colorBlue = values["blue"] as Int
                }
            }


            if (values.containsKey("id")) {
                if (values["id"] is String) {
                    entityParticle.materialName = values["id"] as String
                } else {
                    entityParticle.materialName = MaterialCompatibility13.getMaterialFromId(values["id"] as Int).name
                }
            }

            this.particleEffectMeta = entityParticle
        }
    }
}