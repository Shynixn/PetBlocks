package com.github.shynixn.petblocks.bukkit.contract

import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl
import org.bukkit.Location
import org.bukkit.entity.Player

interface PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    fun createPetEntity(location: Location, petMeta: PetMeta, owner : Player): PetEntityImpl
}
