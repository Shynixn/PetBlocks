package com.github.shynixn.petblocks.sponge.logic.business.service

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.sponge.logic.business.helper.CompatibilityItemType
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleOptions
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Color
import org.spongepowered.api.world.Location

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
class ParticleServiceImpl @Inject constructor(private val logger: Logger, private val configurationService: ConfigurationService) : ParticleService {
    /**
     * Plays the given [particle] at the given [location] for the given [player] or
     * all players in the world if the config option all visible is enabled.
     */
    override fun <L, P> playParticle(location: L, particle: Particle, player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        val canOtherPlayersSeeParticles = configurationService.findValue<Boolean>("pet.design.particles-other-players")

        if (canOtherPlayersSeeParticles) {
            playParticle(location, particle, player.world.players)
        } else {
            playParticle(location, particle, listOf(player))
        }
    }

    /**
     * Plays the given [particle] at the given [location] for the given [players].
     */
    override fun <L, P> playParticle(location: L, particle: Particle, players: Collection<P>) {
        if (location !is Location<*>) {
            throw IllegalArgumentException("Location has to be a SpongeLocation!")
        }

        if (particle.type == ParticleType.NONE) {
            return
        }

        val targets = (players as Collection<Player>).toTypedArray()

        if (particle.type == ParticleType.REDSTONE || particle.type == ParticleType.NOTE) {
            particle.amount = 0
            particle.speed = 1.0f.toDouble()
        }

        val type = Sponge.getGame().registry.getType(org.spongepowered.api.effect.particle.ParticleType::class.java, "minecraft:" + particle.type.gameId_113).get()

        val builder: ParticleEffect.Builder

        builder = if (particle.type == ParticleType.REDSTONE) {
            ParticleEffect.builder()
                    .type(type).option(ParticleOptions.COLOR, Color.ofRgb(particle.colorRed, particle.colorGreen, particle.colorBlue))
        } else {
            ParticleEffect.builder()
                    .type(type)
                    .quantity(particle.amount)
                    .offset(Vector3d(particle.offSetX, particle.offSetY, particle.offSetZ))
                    .velocity(Vector3d(particle.speed, particle.speed, particle.speed))
        }
        if (particle.materialName != null) {
            builder.option(ParticleOptions.BLOCK_STATE, BlockState.builder().blockType(CompatibilityItemType.getFromName(particle.materialName)!!.blockType)
                    .add(Keys.ITEM_DURABILITY, particle.data).build())
        }

        try {
            val effect = builder.build()
            targets.forEach { player ->
                player.spawnParticles(effect, Vector3d(location.position.x, location.position.y, location.position.z))
            }
        } catch (e: Exception) {
            logger.warn("Failed to send particle.", e)
        }
    }
}