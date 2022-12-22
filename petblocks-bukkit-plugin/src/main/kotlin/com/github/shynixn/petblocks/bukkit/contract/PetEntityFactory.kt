package com.github.shynixn.petblocks.bukkit.contract

import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl

interface PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    fun createPetEntity(pet: Pet, meta : PetMeta, template: PetTemplate): PetEntityImpl
}
