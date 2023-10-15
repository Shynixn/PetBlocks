package com.github.shynixn.petblocks.entity

import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.enumeration.PetSpawnResultType

class PetSpawnResult(
    /**
     * Resulting type.
     * Only on success, the pet is not null.
     */
    val type: PetSpawnResultType,
    /**
     * The newly spawned pet.
     */
    val pet: Pet?
)
