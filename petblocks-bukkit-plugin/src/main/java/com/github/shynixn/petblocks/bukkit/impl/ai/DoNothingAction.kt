package com.github.shynixn.petblocks.bukkit.impl.ai

import com.github.shynixn.mcutils.physicobject.api.AIAction
import com.github.shynixn.mcutils.physicobject.api.MathComponentSettings
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetRidingState
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl

class DoNothingAction(private val petMeta: PetMeta) : AIAction<PetEntityImpl> {
    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        val ownerPosition = actor.ownerLocation.clone()

        if (ownerPosition.world != actor.physicsComponent.position.world) {
            return Int.MAX_VALUE
        }

        if (petMeta.ridingState != PetRidingState.NO) {
            return Int.MAX_VALUE
        }

        return Int.MIN_VALUE
    }
}
