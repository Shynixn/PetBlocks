package com.github.shynixn.petblocks.core.logic.persistence.entity

/**
 * Packet sent by the server to update the passenger
 * information of an entity on the client.
 */
data class PacketPlayOutMount(
    /**
     * Entity.
     */
    val entity: Any,
    /**
     * Riding entities of the client.
     */
    val passengers: List<Any>
)
