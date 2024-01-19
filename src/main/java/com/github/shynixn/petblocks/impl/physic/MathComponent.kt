package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.packet.api.RayTraceResult
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.api.meta.enumeration.BlockDirection
import com.github.shynixn.petblocks.entity.PhysicSettings
import kotlin.math.abs

class MathComponent(
    var position: Vector3d, private val settings : PhysicSettings,
    private val rayTracingService: RayTracingService
) : PhysicComponent {
    /**
     * Function being called when the position and motion are about to change.
     */
    var onPrePositionChange: MutableList<(Vector3d, Vector3d) -> Unit> = arrayListOf()

    /**
     * Function being called when the position and motion have changed.
     */
    var onPostPositionChange: MutableList<(Vector3d, Vector3d) -> Unit> = arrayListOf()
    var motion: Vector3d = Vector3d(null, 0.0, 0.0, 0.0)

    private var cachedTeleportTarget: Vector3d? = null
    private var movementRayTraceResult: RayTraceResult? = null
    private var gravityRayTraceResult: RayTraceResult? = null

    /**
     * Sets the velocity which is applied per tick to the object.
     */
    fun setVelocity(vector: Vector3d) {
        this.motion = vector.clone()
        this.position.y += 0.25
    }

    /**
     * Teleports the object to the given vector.
     */
    fun teleport(vector: Vector3d) {
        cachedTeleportTarget = vector
    }

    /**
     * Ticks the minecraft thread.
     */
    override fun tickMinecraft() {
        val sourceLocation = position.toLocation()

        if (!sourceLocation.isWorldLoaded || !sourceLocation.chunk.isLoaded) {
            return
        }

        // Handle gravity
        motion.y -= settings.gravity

        // Target location of the object.
        val targetLocation = position.toLocation().add(motion.toVector())

        if (!targetLocation.isWorldLoaded || !targetLocation.chunk.isLoaded) {
            return
        }

        gravityRayTraceResult = rayTracingService.rayTraceMotion(position, Vector3d(0.0, -1.0, 0.0))

        if (gravityRayTraceResult!!.hitBlock && motion.y < 0.0) {
            // Set gravity to zero and correct y axe.
            this.motion.y = 0.0
            this.position.y = gravityRayTraceResult!!.block!!.y + 1.0
        }

        if(motion.x != 0.0 || motion.z != 0.0){
            movementRayTraceResult = rayTracingService.rayTraceMotion(position, motion)
        }
    }

    /**
     * Ticks the async thread.
     */
    override fun tickPhysic() {
        // Handle teleport.
        if (cachedTeleportTarget != null) {
            handleTeleport()
            return
        }

        if(movementRayTraceResult != null){
            if(movementRayTraceResult!!.hitBlock && movementRayTraceResult!!.blockDirection != BlockDirection.UP){
                position.add(motion.x * -1, 0.0, motion.z * -1)
                motion.x = 0.0
                motion.y = 0.0
                movementRayTraceResult = null
            }else{
                if(motion.x != 0.0 || motion.z != 0.0){
                    val targetPosition = movementRayTraceResult!!.targetPosition
                    // Keep yaw and pitch.
                    this.position.x = targetPosition.x
                    this.position.y = targetPosition.y
                    this.position.z = targetPosition.z

                    // Reduces the motion relative to its current speed.
                    this.motion = this.motion.multiply(settings.relativeVelocityReduce)

                    // Reduces the motion absolute by a negative normalized value.
                    val reductionVector = this.motion.clone().normalize().multiply(settings.absoluteVelocityReduce)
                    reduceVectorIfBiggerZero(this.motion, reductionVector)
                    fixMotionFloatingPoints()
                }
            }
        }

        if(gravityRayTraceResult != null){
            if(!gravityRayTraceResult!!.hitBlock || motion.y > 0.0){
                this.position.y += this.motion.y
            }
        }

        // Sends packets to show it.
        onPostPositionChange.forEach { e -> e.invoke(position, motion) }
    }

    private fun handleTeleport() {
        onPrePositionChange.forEach { e ->
            e.invoke(
                position,
                motion
            )
        }
        motion = Vector3d(null, 0.0, 0.0, 0.0)
        position = cachedTeleportTarget!!
        onPostPositionChange.forEach { e ->
            e.invoke(
                position,
                motion
            )
        }
        gravityRayTraceResult = null
        movementRayTraceResult = null
        cachedTeleportTarget = null
    }

    /**
     * Reduce the vector by the reducement vector.
     * It is guaranteed that both x, x, y,y, z, z of both vectors are either both positive or both negative.
     */
    private fun reduceVectorIfBiggerZero(vector: Vector3d, reducement: Vector3d) {
        if (abs(vector.x) - abs(vector.x - reducement.x) > 0) {
            vector.x = vector.x - reducement.x
        } else {
            vector.x = 0.0
        }
        if (abs(vector.y) - abs(vector.y - reducement.y) > 0) {
            vector.y = vector.y - reducement.y
        } else {
            vector.y = 0.0
        }
        if (abs(vector.z) - abs(vector.z - reducement.z) > 0) {
            vector.z = vector.z - reducement.z
        } else {
            vector.z = 0.0
        }
    }

    /**
     * If the values get too small, set them to zero.
     */
    private fun fixMotionFloatingPoints() {
        if (abs(this.motion.x) < 0.0001) {
            this.motion.x = 0.0
        }
        if (abs(this.motion.y) < 0.0001) {
            this.motion.y = 0.0
        }
        if (abs(this.motion.z) < 0.0001) {
            this.motion.z = 0.0
        }
    }
}
