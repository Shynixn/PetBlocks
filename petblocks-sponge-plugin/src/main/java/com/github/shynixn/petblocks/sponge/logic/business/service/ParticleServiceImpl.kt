package com.github.shynixn.petblocks.sponge.logic.business.service

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ParticleService
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.block.BlockState
import org.spongepowered.api.block.BlockType
import org.spongepowered.api.data.key.Keys
import org.spongepowered.api.effect.particle.ParticleEffect
import org.spongepowered.api.effect.particle.ParticleOptions
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.util.Color

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
class ParticleServiceImpl @Inject constructor(private val configurationService: ConfigurationService, private val loggingService: LoggingService) : ParticleService {
    /**
     * Plays the given [particle] at the given [location] for the given [player] or
     * all players in the world if the config option all alwaysVisible is enabled.
     * @param P the type of the player.
     * @param L the type of the location.
     */
    override fun <L, P> playParticle(location: L, particle: Particle, player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (location !is Transform<*>) {
            throw IllegalArgumentException("Location has to be a SpongeTransform!")
        }

        val canOtherPlayersSeeParticles = configurationService.findValue<Boolean>("global-configuration.particles-other-players")

        if (canOtherPlayersSeeParticles) {
            playParticleToPlayers(location.position, particle, player.world.players)
        } else {
            playParticleToPlayers(location.position, particle, listOf(player))
        }
    }

    /**
     * Plays the given [particle] at the given [location] for the given [players].
     */
    private fun playParticleToPlayers(location: Vector3d, particle: Particle, players: Collection<Player>) {
        if (particle.type == ParticleType.NONE) {
            return
        }

        if (particle.type == ParticleType.REDSTONE || particle.type == ParticleType.NOTE) {
            particle.amount = 0
            particle.speed = 1.0f.toDouble()
        }

        val type = Sponge.getGame().registry.getType(org.spongepowered.api.effect.particle.ParticleType::class.java, "minecraft:" + particle.type.minecraftId_112).get()

        val builder = if (particle.type == ParticleType.REDSTONE) {
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
            builder.option(ParticleOptions.BLOCK_STATE, BlockState.builder().blockType(particle.materialName as BlockType)
                .add(Keys.ITEM_DURABILITY, particle.data).build())
        }

        try {
            val effect = builder.build()

            for (player in players) {
                player.spawnParticles(effect, location)
            }
        } catch (e: Exception) {
            loggingService.warn("Failed to send particle. Is the particle '" + particle.type.name + "' supported by this server version?", e)
        }
    }
}