package com.github.shynixn.petblocks.bukkit.impl.ai

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResult
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResultType
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshot
import com.github.shynixn.mcutils.physicobject.api.AIAction
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.Plugin
import java.util.Date

class MoveToOwnerAction(private val pathfinderService: PathfinderService, private val plugin: Plugin) :
    AIAction<PetEntityImpl> {
    private var playerLocation: Vector3d = Vector3d()
    private var worldSnapshot: WorldSnapshot? = null
    private var rescueCounter = 0

    /**
     * Is called when the action is started.
     */
    override fun start(actor: PetEntityImpl) {
        val location = actor.physicsComponent.position.toLocation()
        worldSnapshot = pathfinderService.calculateFastPathfinderSnapshot(location, 30, 12, 30)
    }

    /**
     * Is called when the action is continued to execute.
     */
    override fun execute(actor: PetEntityImpl) {
        val result =
            pathfinderService.findPath(
                worldSnapshot!!,
                actor.physicsComponent.position.toLocation(),
                playerLocation.toLocation()
            )

        println(result.resultType)
        if (result.resultType == PathfinderResultType.FOUND) {
            //  visualizePath(result)
            actor.moveToTargetComponent.walkToTarget(result.steps)
            rescueCounter = 0
        } else if (result.resultType == PathfinderResultType.INVALID_START) {
            rescueCounter++

            if (rescueCounter > 20) {
                println("Rescue")
                actor.teleportInWorld(playerLocation)
                // Gravity after teleport.
                actor.physicsComponent.setVelocity(Vector3d(0.0, 0.1, 0.0))
                rescueCounter = 0
            }
        } else if (result.resultType == PathfinderResultType.INVALID_END) {
            rescueCounter++

            if (rescueCounter > 20) {
                println("Recalculate Path.")
                start(actor)
                rescueCounter = 0
            }
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
        // Minus 2 minimum distance.
        return playerLocation.distance(actor.physicsComponent.position).toInt() - 2
    }

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
