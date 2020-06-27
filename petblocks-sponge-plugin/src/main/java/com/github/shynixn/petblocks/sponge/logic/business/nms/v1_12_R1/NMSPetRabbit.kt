package com.github.shynixn.petblocks.sponge.logic.business.nms.v1_12_R1

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.HiddenProxy
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.x
import com.github.shynixn.petblocks.sponge.logic.business.extension.y
import com.github.shynixn.petblocks.sponge.logic.business.extension.z
import com.google.common.collect.Sets
import net.minecraft.block.Block
import net.minecraft.entity.SharedMonsterAttributes
import net.minecraft.entity.ai.EntityAIBase
import net.minecraft.entity.ai.EntityAITasks
import net.minecraft.entity.passive.EntityRabbit
import net.minecraft.inventory.EntityEquipmentSlot
import net.minecraft.util.math.BlockPos
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.world.World

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
class NMSPetRabbit(petDesign: NMSPetArmorstand, location: Transform<World>) :
    EntityRabbit(location.extent as net.minecraft.world.World), EntityPetProxy, HiddenProxy {
    private var petDesign: NMSPetArmorstand? = null
    private var pathfinderCounter = 0

    init {
        this.petDesign = petDesign
        this.isSilent = true

        clearAIGoals()
        this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.30000001192092896 * 0.75
        this.stepHeight = 1.0F

        val mcWorld = location.extent as net.minecraft.world.World
        this.setPosition(location.x, location.y - 200, location.z)
        mcWorld.spawnEntity(this)

        val targetLocation = location.toPosition()
        PetBlocksApi.resolve(ConcurrencyService::class.java).runTaskSync(20L) {
            // Only fix location if it is not already fixed.
            if (petDesign.proxy.getLocation<Transform<World>>().toPosition().distance(targetLocation) > 20) {
                this.setPosition(targetLocation.x, targetLocation.y + 1.0, targetLocation.z)
            }
        }
    }

    /**
     * Removes this entity.
     */
    override fun deleteFromWorld() {
        this.setDead()
    }

    /**
     * Boots marker.
     */
    override var bootsItemStack: Any?
        get() {
            return this.getItemStackFromSlot(EntityEquipmentSlot.FEET)
        }
        set(value) {
            this.setItemStackToSlot(EntityEquipmentSlot.FEET, value as net.minecraft.item.ItemStack)
        }

    /**
     * Applies pathfinders to the entity.
     */
    fun applyPathfinders(pathfinders: List<Any>) {
        clearAIGoals()

        for (pathfinder in pathfinders) {
            if (pathfinder is PathfinderProxy) {
                this.tasks.addTask(pathfinderCounter++, Pathfinder(pathfinder))

                val aiBase = pathfinder.aiBase

                if (aiBase is AIMovement) {
                    this.getEntityAttribute(SharedMonsterAttributes.MOVEMENT_SPEED).baseValue = 0.30000001192092896 *
                            aiBase.movementSpeed
                    this.stepHeight = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.tasks.addTask(pathfinderCounter++, pathfinder as EntityAIBase)
            }
        }
    }

    /**
     * Gets called on move to play sounds.
     */
    override fun playStepSound(pos: BlockPos, blockIn: Block) {
        if (petDesign == null) {
            return
        }

        if (!this.isInWater) {
            petDesign!!.proxy.playMovementEffects()
        }
    }

    /**
     * Disable health.
     */
    override fun setHealth(f: Float) {
    }

    /**
     * Clears all entity aiGoals.
     */
    private fun clearAIGoals() {
        val taskEntriesField = EntityAITasks::class.java.getDeclaredField("field_75782_a")
        taskEntriesField.isAccessible = true
        taskEntriesField.set(this.tasks, Sets.newLinkedHashSet<Any>())
        taskEntriesField.set(this.targetTasks, Sets.newLinkedHashSet<Any>())

        val executingTaskEntries = EntityAITasks::class.java.getDeclaredField("field_75780_b")
        executingTaskEntries.isAccessible = true
        executingTaskEntries.set(this.tasks, Sets.newLinkedHashSet<Any>())
        executingTaskEntries.set(this.targetTasks, Sets.newLinkedHashSet<Any>())
    }
}