package com.github.shynixn.petblocks.impl.ai

import com.github.shynixn.mcutils.common.physic.AIAction
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.impl.PetEntityImpl

class DoNothingAction(private val petMeta: PetMeta) : AIAction<PetEntityImpl> {
    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        val ownerPosition = actor.ownerLocation.clone()

        if (ownerPosition.world != actor.getLocation().world) {
            return Int.MAX_VALUE
        }

        if (petMeta.ridingState != PetRidingState.NO) {
            return Int.MAX_VALUE
        }

        return Int.MIN_VALUE
    }
}
