package com.github.shynixn.petblocks.bukkit.logic.persistence.configuration

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.business.helper.toParticleType
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.BukkitItemContainer
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.persistence.configuration.ParticleConfiguration
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
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
class BukkitParticleConfiguration : ParticleConfiguration<Player>() {

    private val plugin = JavaPlugin.getPlugin(PetBlocksPlugin::class.java)

    /**
     * Reloads the content from the fileSystem
     */
    override fun reload() {
        this.particleCache.clear()
        this.plugin.reloadConfig()

        val data = (this.plugin.config.get("particles") as MemorySection).getValues(false)

        for (key in data.keys) {
            try {
                val container = BukkitItemContainer(Integer.parseInt(key), (data[key] as MemorySection).getValues(false))

                val entityParticle = ParticleEntity(ParticleType.NONE)
                val values = ((data[key] as MemorySection).getValues(false)["effect"] as MemorySection).getValues(true)

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

                this.particleCache[container] = entityParticle
            } catch (e: Exception) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load particle " + key + '.'.toString(), e)
            }
        }
    }
}