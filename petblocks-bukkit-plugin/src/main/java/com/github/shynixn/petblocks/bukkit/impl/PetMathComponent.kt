package com.github.shynixn.petblocks.bukkit.impl

import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.physicobject.api.*
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.impl.service.RayTracingServiceImpl
import org.bukkit.FluidCollisionMode
import org.bukkit.util.Vector
import kotlin.math.abs
import kotlin.math.atan2

class PetMathComponent(
    override var position: Vector3d, private val settings: MathComponentSettings
) : MathComponent {
    /**
     * Function being called when the position and motion are about to change.
     */
    override var onPrePositionChange: MutableList<(Vector3d, Vector3d, RayTraceResult) -> Unit> = arrayListOf()

    /**
     * Function being called when the position and motion have changed.
     */
    override var onPostPositionChange: MutableList<(Vector3d, Vector3d, RayTraceResult) -> Unit> = arrayListOf()

    /**
     * Origin coordinate to make relative rotations in the world.
     */
    private val origin = vector {
        x = 0.0
        y = 0.0
        z = -1.0
    }.normalize()
    override var motion: Vector3d = Vector3d(null, 0.0, 0.0, 0.0)

    private var cachedTeleportTarget: Vector3d? = null
    private var lastRayTraceResult: RayTraceResult? = null
    private val rayTracingService: RayTracingServiceImpl =
        RayTracingServiceImpl()

    /**
     * Gets if the object is currrently on ground.
     */
    var isOnGround: Boolean = false
        private set

    /**
     * Sets the velocity which is applied per tick to the object.
     */
    override fun setVelocity(vector: Vector3d) {
        // Motion per step is the new motion.
        this.motion = vector.clone()
        // Move the object a little up otherwise wallcollision of ground immediately cancel movement.
        //  this.position.y += 0.25
        // Correct the yaw of the object after bouncing.
        fixYawMotion()
    }

    /**
     * Teleports the object to the given vector.
     */
    override fun teleport(vector: Vector3d) {
        cachedTeleportTarget = vector
    }

    /**
     * Ticks the minecraft thread.
     */
    override fun tickMinecraft() {
        if (motion.x == 0.0 && motion.y == 0.0 && motion.z == 0.0) {
            lastRayTraceResult = null
            return
        }

        // Current location of the object.
        val sourceLocation = position.toLocation()

        if (!sourceLocation.isWorldLoaded || !sourceLocation.chunk.isLoaded) {
            return
        }

        // Target location of the object.
        val targetLocation = position.toLocation().add(motion.toVector())

        if (!targetLocation.isWorldLoaded || !targetLocation.chunk.isLoaded) {
            return
        }

        // RayTrace Motion in world.
        position.y += settings.rayTraceYOffset
        lastRayTraceResult = rayTracingService.rayTraceMotion(position, motion)
        lastRayTraceResult!!.targetPosition.y -= settings.rayTraceYOffset // this is fine.
        //     position.y -= settings.rayTraceYOffset
    }

    /**
     * Ticks the async thread.
     */
    override fun tickAsync() {
        // Handle teleport.
        if (cachedTeleportTarget != null) {
            onPrePositionChange.forEach { e ->
                e.invoke(
                    position,
                    motion,
                    RayTraceResult(false, position, BlockDirection.SOUTH)
                )
            }
            motion = Vector3d(null, 0.0, 0.0, 0.0)
            position = cachedTeleportTarget!!
            onPostPositionChange.forEach { e ->
                e.invoke(
                    position,
                    motion,
                    RayTraceResult(false, position, BlockDirection.SOUTH)
                )
            }
            cachedTeleportTarget = null
            return
        }

        // Handle motion.
        if (lastRayTraceResult == null) {
            return
        }

        val rayTraceResult = lastRayTraceResult!!

        // Calculate bounce of ground and cache result.
        onPrePositionChange.forEach { e -> e.invoke(this.position, this.motion, rayTraceResult) }

        val movingObjectPosition =
            this.position.toLocation().world!!.rayTraceBlocks(
                this.position.toLocation(),
                Vector(0, -1, 0),
                0.5,
                FluidCollisionMode.NEVER,
                true
            )

        this.isOnGround = movingObjectPosition != null && movingObjectPosition.hitBlock != null

        if (this.isOnGround) {
            if (rayTraceResult.hitBlock && rayTraceResult.blockDirection != BlockDirection.UP) {
                calculateObjectInAir(this.position)
            } else {
                calculateObjectOnGround(rayTraceResult.targetPosition)
            }

            onPostPositionChange.forEach { e -> e.invoke(position, motion, rayTraceResult) }
            return
        }

        calculateObjectInAir(rayTraceResult.targetPosition)
        // Sends packets to show it.
        onPostPositionChange.forEach { e -> e.invoke(position, motion, rayTraceResult) }
    }

    /**
     * Fixes the yaw value after a motion change.
     */
    override fun fixYawMotion() {
        this.position.yaw = getYawFromVector(origin, this.motion.clone().normalize()) * -1
        this.position.pitch = 0.0
    }


    /**
     * Handles movement of the object in air.
     */
    private fun calculateObjectInAir(targetPosition: Vector3d) {
        // Keep yaw and pitch.
        this.position.x = targetPosition.x
        this.position.y = targetPosition.y
        this.position.z = targetPosition.z

        val cacheNegativeYMotion = this.motion.y // The motion should become bigger and not reducement by resistance.

        // Reduces the motion relative to its current speed.
        this.motion = this.motion.multiply(settings.airResistanceRelative)

        // Reduces the motion absolute by a negative normalized value.
        val reductionVector = this.motion.clone().normalize().multiply(settings.airResistanceAbsolute)
        reduceVectorIfBiggerZero(this.motion, reductionVector)
        fixMotionFloatingPoints()

        // Apply negative gravity after wards. (otherwise it is removed by other calculations)
        this.motion.y = cacheNegativeYMotion
        motion.y -= settings.gravityAbsolute
    }

    /**
     * Handles movement of the object on ground.
     */
    private fun calculateObjectOnGround(targetPosition: Vector3d) {
        val difference = abs(this.position.y - targetPosition.y)

        if (difference <= 0.1) {
            // Correct high.
            this.position.y += 1.0
        } else if (difference >= 0.2) {
            this.position.y -= 0.05
        }

        motion.y = 0.0
        // Keep yaw and pitch.
        this.position.x = targetPosition.x
        this.position.z = targetPosition.z

        // Reduces the motion relative to its current speed.
        this.motion = this.motion.multiply(settings.groundResistanceRelative)

        // Reduces the motion absolute by a negative normalized value.
        val reductionVector = this.motion.clone().normalize().multiply(settings.groundResistanceAbsolute)
        reduceVectorIfBiggerZero(this.motion, reductionVector)
        fixMotionFloatingPoints()
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

    /**
     * Gets the angle in degrees from 0 - 360 between the given 2 vectors.
     */
    private fun getYawFromVector(origin: Vector3d, position: Vector3d): Double {
        var angle = atan2(origin.z, origin.x) - atan2(position.z, position.x)
        angle = angle * 360 / (2 * Math.PI)

        if (angle < 0) {
            angle += 360.0
        }

        return angle
    }

    /**
     * Closes the component.
     */
    override fun close() {
        this.onPostPositionChange.clear()
        this.onPrePositionChange.clear()
    }
}
