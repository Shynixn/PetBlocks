package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.persistence.entity.ParticleEffectMeta
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEffectData
import com.github.shynixn.petblocks.sponge.logic.business.helper.CompatibilityItemType
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleOptions
import org.spongepowered.api.effect.particle.ParticleType
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Color
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.lang.reflect.InvocationTargetException
import java.util.*
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
class SpongeParticleEffect : ParticleEffectData {
    /**
     * Sets the material of the particleEffect.
     *
     * @param material material
     * @return builder
     */
    override fun setMaterial(material: Any?): ParticleEffectMeta {
        throw RuntimeException("Not implemented!")
        return this
    }

    /**
     * Returns the material of the particleEffect.
     *
     * @return material
     */
    override fun getMaterial(): Any? {
        if (materialId == null) {
            return null
        }

        return CompatibilityItemType.getFromId(this.materialId)
    }

    constructor(effectName: String?, amount: Int, speed: Double, offsetX: Double, offsetY: Double, offsetZ: Double) : super(effectName, amount, speed, offsetX, offsetY, offsetZ)
    constructor() : super()
    constructor(items: Map<String, Any>?) : super(items)

    /**
     * Sets the name of the material
     * @param name name
     * @return builder
     */
    override fun setMaterialName(name: String?): ParticleEffectMeta {
        this.materialId = CompatibilityItemType.getFromName(name).id
        return this
    }

    /**
     * Returns the name of the material.
     * @return material
     */
    override fun getMaterialName(): String {
        return CompatibilityItemType.getFromId(this.materialId).name
    }

    /**
     * Plays the effect at the given location to the given players.
     *
     * @param location location
     * @param players  players
     */
    override fun <Location, Player> applyTo(locationtemp: Location?, vararg playerstemp: Player) {
        try {
            if (this.effect == null || this.effect.equals("none", ignoreCase = true))
                return

            val players = playerstemp as Array<org.spongepowered.api.entity.living.player.Player>
            val location = locationtemp as org.spongepowered.api.world.Location<World>

            val type = Sponge.getGame().registry.getType(ParticleType::class.java, "minecraft:" + this.effectType.minecraftId).get()

            val builder: ParticleEffect.Builder
            if (this.effectType == ParticleEffectMeta.ParticleEffectType.REDSTONE || this.effectType == ParticleEffectMeta.ParticleEffectType.NOTE) {
                builder = ParticleEffect.builder()
                        .type(type).option(ParticleOptions.COLOR, Color.ofRgb(this.red, this.green, this.blue))
            } else {
                builder = ParticleEffect.builder()
                        .type(type)
                        .quantity(this.amount)
                        .offset(Vector3d(this.offsetX, this.offsetY, this.offsetZ))
                        .velocity(Vector3d(this.speed, this.speed, this.speed))
            }
            if (this.material != null) {
                builder.option(ParticleOptions.BLOCK_STATE, BlockState.builder().blockType(CompatibilityItemType.getFromId(this.materialId)!!.blockType)
                        .add(Keys.ITEM_DURABILITY, this.data as Int).build())
            }

            val effect = builder.build()
            for (player in players) {
                player.spawnParticles(effect, Vector3d(location.x, location.y, location.z))
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    /**
     * Copies the current object.
     *
     * @return copy.
     */
    override fun copy(): ParticleEffectMeta {
        val particle = SpongeParticleEffect()
        particle.effect = this.effect
        particle.amount = this.amount
        particle.offsetX = this.offsetX
        particle.offsetY = this.offsetY
        particle.offsetZ = this.offsetZ
        particle.speed = this.speed
        particle.materialId = this.materialId
        particle.data = this.data
        return particle
    }
}