package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_13_R2

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.bukkit.logic.business.extension.removeFinalModifier
import com.google.common.collect.Sets
import net.minecraft.server.v1_13_R2.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_13_R2.CraftWorld
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
class NMSPet(petDesign: NMSPetArmorstand, location: Location) : EntityCreature(EntityTypes.RABBIT, (location.world as CraftWorld).handle) {
    private var petDesign: NMSPetArmorstand? = null
    private var pathfinderCounter = 0

    init {
        this.petDesign = petDesign
        this.isSilent = true

        clearAIGoals()
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * 0.75
        this.Q = 1.0F

        val mcWorld = (location.world as CraftWorld).handle
        this.setPosition(location.x, location.y + 1, location.z)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)
    }

    /**
     * Applies pathfinders to the entity.
     */
    fun applyPathfinders(pathfinders: List<Any>) {
        clearAIGoals()

        for (pathfinder in pathfinders) {
            if (pathfinder is PathfinderProxy) {
                this.goalSelector.a(pathfinderCounter++, Pathfinder(pathfinder))

                val aiBase = pathfinder.aiBase

                if (aiBase is AIMovement) {
                    this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * aiBase.movementSpeed
                    this.Q = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.goalSelector.a(pathfinderCounter++, pathfinder as PathfinderGoal)
            }
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

        val aiGoal = petDesign!!.petMeta.aiGoals.lastOrNull { p -> p is AIMovement } ?: return
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
     * Gets called on move to play sounds.
     */
    override fun a(blockposition: BlockPosition, iblockdata: IBlockData) {
        if (petDesign == null) {
            return
        }

        if (!this.isInWater) {
            petDesign!!.proxy.playMovementEffects()
        }
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPet {
        if (this.bukkitEntity == null) {
            this.bukkitEntity = CraftPet(this.world.server, this)
        }

        return this.bukkitEntity as CraftPet
    }

    /**
     * Clears all entity aiGoals.
     */
    private fun clearAIGoals() {
        val bField = PathfinderGoalSelector::class.java.getDeclaredField("b")
        val cField = PathfinderGoalSelector::class.java.getDeclaredField("c")

        bField.removeFinalModifier()
        cField.removeFinalModifier()

        bField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        bField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.goalSelector, Sets.newLinkedHashSet<Any>())
        cField.set(this.targetSelector, Sets.newLinkedHashSet<Any>())
    }
}