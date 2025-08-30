package com.github.shynixn.petblocks.impl.physic

import checkForPluginMainThread
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.common.toVector3d
import kotlinx.coroutines.Job
import java.util.*

class MoveToTargetComponent(private val mathComponent: MathComponent)  {
    private var currentPath: Queue<Vector3d>? = null
    private var currentTargetPosition: Vector3d? = null
    private var vectorPerTick: Vector3d? = null
    private var lastDistance: Double = Double.MAX_VALUE
    private var speed = 0.0
    private var lastJob = Job().also {
        it.complete()
    }

    init {
        mathComponent.onPostPositionChange.add { position, _ ->
            onCheckPositionReached(position)
        }
    }

    /**
     * Walks to the given target using the given path.
     */
    fun walkToTarget(path: List<Vector3d>, speed: Double): Job {
        checkForPluginMainThread()

        lastJob.complete()
        currentPath = LinkedList(path)
        currentPath!!.poll()
        currentTargetPosition = null
        vectorPerTick = null
        this.speed = speed
        lastDistance = Double.MAX_VALUE

        lastJob = Job()
        return lastJob
    }

    /**
     * Tick on async thread.
     */
    fun tickPhysic() {
        checkForPluginMainThread()

        if (currentPath == null) {
            return
        }

        if (currentTargetPosition == null) {
            currentTargetPosition = currentPath!!.poll()

            if (currentTargetPosition == null) {
                currentPath = null
                lastJob.complete()
                return
            }

            // Move the vector slightly up, so he does not fall through the ground.
            val vector = currentTargetPosition!!.copy().subtract(mathComponent.position)
            val normalizedVector = vector.copy().normalize()
            vectorPerTick = normalizedVector.multiply(speed)
            mathComponent.setVelocity(vectorPerTick!!)
            lastDistance = Double.MAX_VALUE
            correctLookingDirection()
            return
        }

        if (vectorPerTick != null) {
            mathComponent.setVelocity(vectorPerTick!!)
            return
        }
    }

    private fun onCheckPositionReached(position: Vector3d) {
        if (currentTargetPosition == null) {
            return
        }

        val distanceNow = position.distance(currentTargetPosition!!)

        if (distanceNow > lastDistance) {
            vectorPerTick = null
            currentTargetPosition = null

            if (currentPath!!.isEmpty()) {
                currentPath = null
                // Remove vector when finished.
                mathComponent.setVelocity(Vector3d(0.0, -1.0, 0.0))
                lastJob.complete()
            }
        } else {
            lastDistance = distanceNow
        }
    }

    private fun correctLookingDirection() {
        val targetLocation = currentTargetPosition!!.toVector()
        val directionVector = targetLocation.subtract(mathComponent.position.toVector())
        mathComponent.position.setDirection(directionVector.toVector3d())
        mathComponent.position.pitch = 0.0
    }
}
