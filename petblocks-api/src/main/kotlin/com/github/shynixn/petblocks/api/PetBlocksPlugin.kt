package com.github.shynixn.petblocks.api

interface PetBlocksPlugin {
    /**
     * Gets a specific logic implementation from PetBlocks.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    fun <S> resolve(service: Class<S>): S
}
