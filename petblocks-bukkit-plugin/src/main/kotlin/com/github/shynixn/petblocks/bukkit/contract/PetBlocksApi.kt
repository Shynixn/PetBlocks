package com.github.shynixn.petblocks.bukkit.contract

import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin

object PetBlocksApi {
    private var plugin: PetBlocksPlugin? = null

    /**
     * Initializes the [petblocksPlugin] proxy.
     */
    private fun initializePetBlocks(petblocksPlugin: PetBlocksPlugin) {
        plugin = petblocksPlugin
    }

    /**
     * Gets a specific logic implementation from PetBlocks.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    fun <S> resolve(service: Class<S>): S {
        return plugin!!.resolve(service)
    }
}
