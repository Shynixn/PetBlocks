package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import java.util.*

class MoveToTargetComponent(private val mathComponent: MathComponent, private val speed: Double) : PhysicComponent {
    private var currentPath: Queue<Vector3d>? = null
    private var currentTargetPosition: Vector3d? = null
    private var vectorPerTick: Vector3d? = null
    private var lastDistance: Double = Double.MAX_VALUE

    init {
        mathComponent.onPostPositionChange.add { position, _, rayTrace ->
            onCheckPositionReached(position)
        }
    }

    /**
     * Walks to the given target using the given path.
     */
    fun walkToTarget(path: List<Vector3d>) {
        currentPath = LinkedList(path)
        currentPath!!.poll()
        currentTargetPosition = null
        vectorPerTick = null
        lastDistance = Double.MAX_VALUE
    }

    /**
     * Tick on async thread.
     */
    override fun tickPhysic() {
        if (currentPath == null) {
            return
        }

        if (currentTargetPosition == null) {
            currentTargetPosition = currentPath!!.poll()

            if (currentTargetPosition == null) {
                currentPath = null
                return
            }

            // Move the vector slightly up, so he does not fall through the ground.
            val vector = currentTargetPosition!!.clone().add(0.0, 0.001, 0.0).subtract(mathComponent.position)
            val normalizedVector = vector.clone().normalize()
            vectorPerTick = normalizedVector.multiply(speed)
            mathComponent.setVelocity(vectorPerTick!!)
            lastDistance = Double.MAX_VALUE
            mathComponent.fixYawMotion()
            mathComponent.position.yaw += 180 // Rotation is weird.
            return
        }

        if (vectorPerTick != null) {
            mathComponent.setVelocity(vectorPerTick!!)
            mathComponent.position.yaw += 180 // Rotation is weird.
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
                mathComponent.setVelocity(Vector3d(0.0, 0.0, 0.0))
            }
        } else {
            lastDistance = distanceNow
        }
    }
}
