package com.github.shynixn.petblocks.bukkit.service

import com.github.shynixn.petblocks.bukkit.Pet
import com.github.shynixn.petblocks.bukkit.PetEntity
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate

interface PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    fun createPetEntity(pet: Pet, meta: PetMeta, template: PetTemplate): PetEntity
}
