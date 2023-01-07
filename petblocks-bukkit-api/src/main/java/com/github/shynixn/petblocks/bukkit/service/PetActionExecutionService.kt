package com.github.shynixn.petblocks.bukkit.service

import com.github.shynixn.petblocks.bukkit.Pet
import com.github.shynixn.petblocks.bukkit.entity.PetActionDefinition

interface PetActionExecutionService {
    /**
     * Executes an pet action.
     */
    suspend fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition)
}
