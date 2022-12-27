package com.github.shynixn.petblocks.bukkit.impl.ai

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResultType
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshot
import com.github.shynixn.mcutils.physicobject.api.AIAction
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl
import kotlinx.coroutines.runBlocking
import org.bukkit.plugin.Plugin

class MoveToOwnerAction(private val pathfinderService: PathfinderService, private val plugin: Plugin) :
    AIAction<PetEntityImpl> {
    private var playerLocation: Vector3d = Vector3d()
    private var worldSnapshot: WorldSnapshot? = null

    /**
     * Is called when the action is started.
     */
    override fun start(actor: PetEntityImpl) {
        // We are on async here, so it is fine.
        runBlocking {
            val location = actor.physicsComponent.position.toLocation()
            worldSnapshot = pathfinderService.calculatePathfinderSnapshot(location, 10, 10, 10)
        }
    }

    /**
     * Is called when the action is continued to execute.
     */
    override fun execute(actor: PetEntityImpl) {
        if (worldSnapshot == null) {
            return
        }

        val result =
            pathfinderService.findPath(
                worldSnapshot!!,
                actor.physicsComponent.position.toLocation(),
                playerLocation.toLocation()
            )

        if (result.resultType == PathfinderResultType.FOUND) {
            actor.moveToTargetComponent.walkToTarget(result.steps)
        }
    }

    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        playerLocation = actor.ownerLocation.clone()
        // Minus 2 minimum distance.
        return playerLocation.distance(actor.physicsComponent.position).toInt() - 2
    }
}
