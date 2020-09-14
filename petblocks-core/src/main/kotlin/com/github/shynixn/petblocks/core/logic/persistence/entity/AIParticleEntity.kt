package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.annotation.YamlSerialize
import com.github.shynixn.petblocks.api.persistence.entity.AIParticle
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.Position

class AIParticleEntity : AIBaseEntity(), AIParticle {
    /**
     * Name of the type.
     */
    override var type: String = "particle"

    /**
     * Amount of seconds until the next particle is played.
     */
    @YamlSerialize(value = "delay-between", orderNumber = 1)
    override var delayBetweenPlaying: Double = 1.0

    /**
     * Offset from the pet source position.
     */
    @YamlSerialize(value = "offset", orderNumber = 2, implementation = PositionEntity::class)
    override var offset: Position = PositionEntity()

    /**
     * Particle.
     */
    @YamlSerialize(value = "particle", orderNumber = 3, implementation = ParticleEntity::class)
    override var particle: Particle = ParticleEntity()

}
