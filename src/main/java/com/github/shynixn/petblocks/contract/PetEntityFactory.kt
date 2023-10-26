package com.github.shynixn.petblocks.contract

import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.impl.PetEntityImpl

interface PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    fun createPetEntity(pet: Pet, meta: PetMeta): PetEntityImpl
}

