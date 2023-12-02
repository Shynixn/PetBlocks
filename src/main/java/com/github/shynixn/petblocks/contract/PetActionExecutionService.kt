package com.github.shynixn.petblocks.contract

import com.github.shynixn.mcutils.common.CancellationToken
import com.github.shynixn.petblocks.entity.PetActionDefinition
import org.bukkit.entity.Player

interface PetActionExecutionService {
    /**
     * Executes an pet action.
     * @param eventPlayer Related to the event. May not be the owner.
     */
    suspend fun executeAction(eventPlayer : Player, pet: Pet, petActionDefinition: PetActionDefinition, cancellationToken: CancellationToken)
}
