package com.github.shynixn.petblocks.bukkit.impl.ai

import com.github.shynixn.mcutils.physicobject.api.AIAction
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl

class IdleAction : AIAction<PetEntityImpl> {
    /**
     * Is called when the action is continued to execute.
     */
    override fun execute(actor: PetEntityImpl) {
        // Make look at owner.
        val ownerPosition = actor.ownerLocation.clone()
        ownerPosition.y += 2
        val directionVector = ownerPosition.subtract(actor.physicsComponent.position)
        actor.physicsComponent.position.setDirection(directionVector)
        actor.teleportInWorld(actor.physicsComponent.position)
    }

    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        return 1
    }
}
