package com.github.shynixn.petblocks.impl.ai

import com.github.shynixn.mcutils.common.physic.AIAction
import com.github.shynixn.petblocks.impl.PetEntityImpl

class IdleAction : AIAction<PetEntityImpl> {
    /**
     * Is called when the action is continued to execute.
     */
    override fun execute(actor: PetEntityImpl) {
        // Make look at owner.
        val ownerPosition = actor.ownerLocation.clone()

        if (ownerPosition.world == actor.physicsComponent.position.world) {
            val directionVector = ownerPosition.subtract(actor.physicsComponent.position)
            actor.physicsComponent.position.setDirection(directionVector)
            actor.teleportInWorld(actor.physicsComponent.position.clone())
        }
    }


    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        return 1
    }
}
