package com.github.shynixn.petblocks.bukkit

import org.bukkit.plugin.java.JavaPlugin

object PetBlocksApi {
    private val plugin: PetBlocksResolvePlugin by lazy {
        val clazz = Class.forName("com.github.shynixn.petblocks.bukkit.PetBlocksPlugin")
        JavaPlugin.getPlugin(clazz as Class<JavaPlugin>) as PetBlocksResolvePlugin
    }

    /**
     * Gets a specific logic implementation from PetBlocks.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    fun <S> resolve(service: Class<S>): S {
        return plugin.resolve(service)
    }
}
