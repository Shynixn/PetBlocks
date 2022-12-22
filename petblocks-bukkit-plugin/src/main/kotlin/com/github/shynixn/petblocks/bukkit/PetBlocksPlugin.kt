package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mcutils.common.CommandServiceImpl
import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PetService
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import org.bukkit.entity.Player

class PetBlocksPlugin : SuspendingJavaPlugin() {
    /**
     * Called when this plugin is enabled
     */
    override suspend fun onEnableAsync() {
        val petService: PetService

        val pet = petService.getPetsFromPlayer()[0]

        pet.visibility = PetVisibility.ALL


    }


    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        super.onDisableAsync()
    }
}
