package com.github.shynixn.petblocks.bukkit

interface PetBlocksResolvePlugin {
    /**
     * Gets a business logic from the plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    fun <S> resolve(service: Class<S>): S
}
