package com.github.shynixn.petblocks.contract

import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.impl.PetEntityImpl

interface PetEntityFactory : AutoCloseable{
    /**
     * Tries to locate a pet entity by ids.
     */
    fun findPetEntityById(id: Int): PetEntityImpl?

    /**
     * Removes pet entities.
     */
    fun removePetEntityById(id: Int)

    /**
     * Creates a new pet entity.
     */
    fun createPetEntity(pet: Pet, meta: PetMeta): PetEntityImpl
}

