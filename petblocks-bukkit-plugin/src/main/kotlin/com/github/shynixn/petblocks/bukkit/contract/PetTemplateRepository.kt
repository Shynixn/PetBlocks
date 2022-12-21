package com.github.shynixn.petblocks.bukkit.contract

import com.github.shynixn.petblocks.bukkit.entity.PetTemplate

interface PetTemplateRepository {
    /**
     * Gets all templates from the repository.
     */
    suspend fun getAll(): List<PetTemplate>
}
