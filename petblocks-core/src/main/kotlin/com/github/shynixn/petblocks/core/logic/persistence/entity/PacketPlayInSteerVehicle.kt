package com.github.shynixn.petblocks.core.logic.persistence.entity

/**
 * Packet which manages all interactions when a player is mounting
 * another entity.
 */
data class PacketPlayInSteerVehicle(
    /**
     * Player riding the vehicle.
     */
    val player: Any,
    /**
     * Forward movement.
     */
    val forward: Double,
    /**
     * Sideways movement.
     */
    val sideWays: Double,
    /**
     * Jump.
     */
    val jump: Boolean,
    /**
     * Unmount.
     */
    val unMount: Boolean
)
