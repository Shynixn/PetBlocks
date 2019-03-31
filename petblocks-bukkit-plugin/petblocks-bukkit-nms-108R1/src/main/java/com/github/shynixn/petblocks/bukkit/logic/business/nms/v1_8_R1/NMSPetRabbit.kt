package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_8_R1

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.core.logic.business.extension.removeFinalModifier
import net.minecraft.server.v1_8_R1.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_8_R1.CraftWorld
import org.bukkit.craftbukkit.v1_8_R1.util.UnsafeList
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
class NMSPetRabbit(petDesign: NMSPetArmorstand, location: Location) : EntityRabbit((location.world as CraftWorld).handle) {
    private var petDesign: NMSPetArmorstand? = null
    private var pathfinderCounter = 0

    init {
        this.petDesign = petDesign
        this.b(true)

        clearAIGoals()
        this.getAttributeInstance(GenericAttributes.d).value = 0.30000001192092896 * 0.75
        this.S = 1.0F

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
                    this.getAttributeInstance(GenericAttributes.d).value = 0.30000001192092896 * aiBase.movementSpeed
                    this.S = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.goalSelector.a(pathfinderCounter++, pathfinder as PathfinderGoal)
            }
        }
    }

    /**
     * Gets called on move to play sounds.
     */
    override fun a(blockposition: BlockPosition, block: Block) {
        if (petDesign == null) {
            return
        }

        if (!this.inWater) {
            petDesign!!.proxy.playMovementEffects()
        }
    }

    /**
     * Disable health.
     */
    override fun setHealth(f: Float) {
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

        bField.set(this.goalSelector, UnsafeList<PathfinderGoalSelector>())
        bField.set(this.targetSelector, UnsafeList<PathfinderGoalSelector>())
        cField.set(this.goalSelector, UnsafeList<PathfinderGoalSelector>())
        cField.set(this.targetSelector, UnsafeList<PathfinderGoalSelector>())
    }
}