package com.github.shynixn.petblocks.entity

class MathSettings {
    // The object keeps 99% of its current speed after each tick.
    var groundResistanceRelative = 0.99

    // Amount of negative acceleration is applied after each tick.
    // e.g. Reverse vector is created, normalized and multiplied by it.
    var groundResistanceAbsolute = 0.0001

    // Air
    /**
     * Amount of positive acceleration in y direction is applied after each tick.
     */
    var gravityAbsolute: Double = 0.03

    // The object keeps 99% of its current speed after each tick.
    var airResistanceRelative = 0.99

    // Amount of negative acceleration is applied after each tick.
    // e.g. Reverse vector is created, normalized and multiplied by it.
    var airResistanceAbsolute = 0.0001

    /**
     * Sometimes the object needs a higher raytracing origin to be able to pass through objects better.
     * e.g. 1.0 for player npcs.
     */
    var rayTraceYOffset: Double = 0.0
}
