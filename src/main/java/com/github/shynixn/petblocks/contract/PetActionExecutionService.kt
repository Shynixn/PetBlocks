package com.github.shynixn.petblocks.contract

import com.github.shynixn.petblocks.entity.PetActionDefinition

interface PetActionExecutionService {
    /**
     * Executes an pet action.
     */
    suspend fun executeAction(pet: Pet, petActionDefinition: PetActionDefinition)
}
