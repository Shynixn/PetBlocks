package com.github.shynixn.petblocks.bukkit.contract

import com.github.shynixn.petblocks.bukkit.entity.PetActionDefinition

interface PetActionExecutionService {
    /**
     * Executes an pet action.
     */
    fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition)
}
