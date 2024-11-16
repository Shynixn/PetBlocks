package com.github.shynixn.petblocks.entity

class PhysicSettings {
    /**
     * The amount of acceleration in minus y axe direction which is applied after each tick.
     */
    var gravity: Double = 0.0

    /**
     * If a velocity is applied to the pet, this is the percentage of the speed the pet preserves after each tick.
    e.g. 1.0 = Pet continuous to fly with the same speed into infinity.
    e.g. 0.8 = Pet speed is reduced to 80% of its previous value after each tick.
     */
    var relativeVelocityReduce = 0.0

    /*If a velocity is applied to the pet, this is the absolute value which is taken from the velocity speed vector after each tick.
     e.g. 0.005 means that the speed is reduced by 0.005 after each tick*/
    var absoluteVelocityReduce = 0.0

    /**
     * Offset from the ground.
     */
    var groundOffset = 0.0

    /**
     * Collide With water.
     */
    var collideWithWater : Boolean = false

    /**
     * Collide With passable Blocks.
     */
    var collideWithPassableBlocks : Boolean = false
}
