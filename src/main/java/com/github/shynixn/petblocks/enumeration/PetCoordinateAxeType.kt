package com.github.shynixn.petblocks.enumeration

import org.bukkit.Location
import kotlin.math.abs

enum class PetCoordinateAxeType(val yaw: Float) {
    NORTH(0.0F),
    EAST(90.0F),
    SOUTH(180.0F),
    WEST(270.0F);

    companion object {
        fun fromLocation(location: Location): PetCoordinateAxeType {
            val yaw = abs(location.yaw % 360)

            if (yaw >= 315.0F || yaw <= 45.0F) {
                return NORTH
            }

            if (yaw >= 225.0F) {
                return WEST
            }

            if (yaw >= 135.0F) {
                return SOUTH
            }

            return EAST
        }
    }
}
