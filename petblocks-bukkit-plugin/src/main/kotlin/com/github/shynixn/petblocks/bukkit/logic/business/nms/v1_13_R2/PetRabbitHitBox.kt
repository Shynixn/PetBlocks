package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_13_R2

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.bukkit.logic.business.extension.removeFinalModifier
import com.google.common.collect.Sets
import net.minecraft.server.v1_13_R2.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class PetRabbitHitBox(world: World) : EntityRabbit(world) {
    private var petDesign: PetDesign? = null
    private var pathfinderCounter = 0

    /**
     * Additional override constructor.
     */
    constructor(player: Player, petDesign: PetDesign, location: Location) : this((location.world as CraftWorld).handle) {
        this.petDesign = petDesign
        this.isSilent = true

        val bField = PathfinderGoalSelector::class.java.getDeclaredField("b")
        val cField = PathfinderGoalSelector::class.java.getDeclaredField("c")

        bField.removeFinalModifier()
        cField.removeFinalModifier()

        bField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        bField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())

        val aiGoal = petDesign.petMeta.aiGoals.firstOrNull { p -> p is AIMovement }
        val speed = if (aiGoal != null) {
            (aiGoal as AIMovement).movementSpeed
        } else {
            0.75
        }

        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * speed

        this.Q = if (aiGoal != null) {
            (aiGoal as AIMovement).climbingHeight.toFloat()
        } else {
            1.0F
        }

        val mcWorld = (location.world as CraftWorld).handle
        this.setPosition(location.x, location.y + 1, location.z)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    /**
     * Applies pathfinder to the entity.
     */
    fun applyPathfinder(pathfinder: Any) {
        if (pathfinder is PathfinderProxy) {
            this.goalSelector.a(pathfinderCounter++, Pathfinder(pathfinder))
        } else {
            this.goalSelector.a(pathfinderCounter++, pathfinder as PathfinderGoal)
        }
    }

    /**
     * Overrides the moving of the pet design.
     */
    override fun move(enummovetype: EnumMoveType?, d0: Double, d1: Double, d2: Double) {
        super.move(enummovetype, d0, d1, d2)

        if (petDesign == null) {
            return
        }

        val aiGoal = petDesign!!.petMeta.aiGoals.firstOrNull { p -> p is AIMovement } ?: return
        val axisBoundingBox = this.boundingBox

        val minXA = axisBoundingBox.minX
        val minXB = axisBoundingBox.minY
        val minXC = axisBoundingBox.minZ
        val maxXD = axisBoundingBox.maxX
        val maxXF = axisBoundingBox.maxZ

        this.locX = (minXA + maxXD) / 2.0
        this.locY = minXB - 2.0 + (aiGoal as AIMovement).movementYOffSet
        this.locZ = (minXC + maxXF) / 2.0
    }

    /**
     * Riding function.
     */
    override fun a(sidemot: Float, f2: Float, formot: Float) {
        if (petDesign == null) {
            super.a(sidemot, f2, formot)
            return
        }

        if (this.passengers == null) {
            super.a(sidemot, f2, formot)
            return
        }

        val passenger = this.passengers.first() as EntityArmorStand
        passenger.yaw = this.yaw
        passenger.pitch = this.pitch * 0.5f
        passenger.lastYaw = this.yaw
        passenger.aQ = this.yaw
        passenger.aS = this.yaw

        super.a(sidemot, f2, formot)
    }

    /**
     * Overrides the default moving sound.
     */
    override fun dz(): SoundEffect {
        if (petDesign == null) {
            return super.dz()
        }

        if (!this.isInWater) {
            petDesign!!.proxy.playMovementEffects()
        }

        return super.dz()
    }
}