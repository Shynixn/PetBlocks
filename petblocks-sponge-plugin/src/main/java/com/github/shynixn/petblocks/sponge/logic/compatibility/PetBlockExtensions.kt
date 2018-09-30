package com.github.shynixn.petblocks.sponge.logic.compatibility

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.entity.PetBlock
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.sponge.logic.compatibility.Config
import org.spongepowered.api.data.property.block.MatterProperty
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.world.Location
import org.spongepowered.api.world.World
import java.util.*

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

private val random = Random()
private var angryParticle: Particle? = null
private var service: ParticleService? = null


fun PetBlock<Player, Location<World>>.playAfraidOfWaterEffect(counterValue: Int): Int {
    if (angryParticle == null) {
        angryParticle = ParticleEntity(ParticleType.VILLAGER_ANGRY)
        angryParticle!!.offSetX = 2.0
        angryParticle!!.offSetY = 2.0
        angryParticle!!.offSetZ = 2.0
        angryParticle!!.speed = 0.1
        angryParticle!!.amount = 2

        service = PetBlocksApi.INSTANCE.resolve(ParticleService::class.java)
    }

    var counter = counterValue
    val entity = engineEntity as Entity
    if (Config.isAfraidOfwater) {
        val optional = entity.location.blockType.getProperty(MatterProperty::class.java)
        if (optional.get().value === MatterProperty.Matter.LIQUID && counter <= 0) {
            val vec = Vector3d((random.nextInt(3) * isNegative(random)).toFloat(), (random.nextInt(3) * isNegative(random)).toFloat(), (random.nextInt(3) * isNegative(random)).toFloat())
            entity.velocity = vec
            val locationN = entity.location
            if (Config.isAfraidwaterParticles) {
                service!!.playParticle<Location<*>, Player>(locationN, particle = angryParticle!!, player = this.player as Player)
            }
            counter = 20
        }
        counter--
    }
    return counter
}

private fun isNegative(rand: Random): Int {
    return if (rand.nextInt(2) == 0) -1 else 1
}