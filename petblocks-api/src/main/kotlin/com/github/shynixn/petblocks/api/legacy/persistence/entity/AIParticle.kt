package com.github.shynixn.petblocks.api.legacy.persistence.entity

interface AIParticle : AIBase {
    /**
     * Particle.
     */
    var particle: Particle

    /**
     * Offset from the pet source position.
     */
    var offset: Position

    /**
     * Amount of seconds until the next particle is played.
     */
    var delayBetweenPlaying: Double
}
