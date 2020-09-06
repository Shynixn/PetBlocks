package com.github.shynixn.petblocks.core.logic.persistence.entity

/**
 * Position packet which gets sent by the client to the server
 * every 20 ticks.
 */
data class PacketPlayInPosition(
    /**
     * Player.
     */
    val player: Any,
    /**
     * X coordinate.
     */
    val x: Double,
    /**
     * Y coordinate.
     */
    val y: Double,
    /**
     * Z coordinate.
     */
    val z: Double
)
