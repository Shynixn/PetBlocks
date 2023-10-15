package com.github.shynixn.petblocks.impl.ai

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.AIAction
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResult
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResultType
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshot
import com.github.shynixn.petblocks.impl.PetEntityImpl
import org.bukkit.Bukkit
import org.bukkit.Material

class MoveToOwnerAction(private val pathfinderService: PathfinderService) : AIAction<PetEntityImpl> {
    private var playerLocation: Vector3d = Vector3d()
    private var worldSnapshot: WorldSnapshot? = null
    private var teleportInWorldRescue = 0
    private var recalculatePathRescue = 0

    /**
     * Is called when the action is started.
     */
    override fun start(actor: PetEntityImpl) {
        val location = actor.getLocation().toLocation()
        worldSnapshot = pathfinderService.calculateFastPathfinderSnapshot(location, 30, 12, 30)
    }

    /**
     * Is called when the action is continued to execute.
     */
    override fun execute(actor: PetEntityImpl) {
        val result = pathfinderService.findPath(
            worldSnapshot!!, actor.physicsComponent.position.toLocation(), playerLocation.toLocation()
        )

        if (result.resultType == PathfinderResultType.FOUND) {
            //  visualizePath(result)
            actor.moveToTargetComponent.walkToTarget(result.steps)
            teleportInWorldRescue = 0
            recalculatePathRescue = 0
        } else if (result.resultType == PathfinderResultType.INVALID_START) {
            teleportInWorldRescue++
        } else if (result.resultType == PathfinderResultType.INVALID_END) {
            recalculatePathRescue++
        }

        if (teleportInWorldRescue > 20) {
            actor.teleportInWorld(playerLocation)
            // Gravity after teleport.
            actor.physicsComponent.setVelocity(Vector3d(0.0, 0.1, 0.0))
            teleportInWorldRescue = 0
            recalculatePathRescue = 0
        }

        if (recalculatePathRescue > 20) {
            start(actor)
            teleportInWorldRescue += 5
            recalculatePathRescue = 0
        }
    }

    /**
     * Is called when the action is stopped.
     */
    override fun stop(actor: PetEntityImpl) {
        actor.moveToTargetComponent.walkToTarget(emptyList())
    }

    /**
     * Calculates the current score of this action.
     */
    override fun score(actor: PetEntityImpl): Int {
        playerLocation = actor.ownerLocation.clone()

        if (playerLocation.world != actor.physicsComponent.position.world) {
            // Do not perform any calculations if not in same world.
            return Int.MIN_VALUE
        }

        // Minus 2 minimum distance.
        return playerLocation.distance(actor.physicsComponent.position).toInt() - 2
    }

    /**
     * For Debugging purposes.
     */
    private var copy: List<Pair<Vector3d, Pair<Material, Byte>>> = emptyList()

    /**
     * For Debugging purposes.
     */
    private fun visualizePath(pathfinderResult: PathfinderResult) {
        pathfinderResult.steps.forEach { e -> e.world = playerLocation.world }

        for (player in Bukkit.getOnlinePlayers()) {
            for (item in copy) {
                player.sendBlockChange(item.first.toLocation(), item.second.first, item.second.second)
            }

            for (item in pathfinderResult.steps.take(pathfinderResult.steps.size - 2)) {
                player.sendBlockChange(item.toLocation(), Material.GOLD_BLOCK, 0)
            }
        }

        copy = pathfinderResult.steps.map { e -> Pair(e, Pair(e.toLocation().block.type, e.toLocation().block.data)) }
    }
}
