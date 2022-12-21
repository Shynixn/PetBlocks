package com.github.shynixn.petblocks.bukkit.entity

import com.github.shynixn.petblocks.bukkit.contract.Pet

class PetSpawnResult(
    /**
     * Resulting type.
     * Only on success, the pet is not null.
     */
    val type: PetSpawnResultType,
    /**
     * The newly spawned pet.
     */
    val pet: Pet
)
