package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_14_R1

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import net.minecraft.server.v1_14_R1.*
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_14_R1.CraftWorld
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
class NMSPetRabbit(petDesign: NMSPetArmorstand, location: Location) : EntityRabbit(EntityTypes.RABBIT, (location.world as CraftWorld).handle) {
    private var petDesign: NMSPetArmorstand? = null
    private var pathfinderCounter = 0
    // BukkitEntity has to be self cached since 1.14.
    private var entityBukkit: Any? = null

    init {
        this.petDesign = petDesign
        this.isSilent = true

        clearAIGoals()
        this.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).value = 0.30000001192092896 * 0.75
        this.K = 1.0F

        val mcWorld = (location.world as CraftWorld).handle
        this.setPosition(location.x, location.y - 200, location.z)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        val targetLocation = location.clone()
        PetBlocksApi.resolve(ConcurrencyService::class.java).runTaskSync(20L) {
            // Only fix location if it is not already fixed.
            if (this.bukkitEntity.location.distance(targetLocation) > 20) {
                this.setPosition(targetLocation.x, targetLocation.y + 1.0, targetLocation.z)
            }
        }
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
                    this.K = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.goalSelector.a(pathfinderCounter++, pathfinder as PathfinderGoal)
            }
        }
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
     * Disable health.
     */
    override fun setHealth(f: Float) {
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPet {
        if (this.entityBukkit == null) {
            entityBukkit = CraftPet(this.world.server, this)

            val field = Entity::class.java.getDeclaredField("bukkitEntity")
            field.isAccessible = true
            field.set(this, entityBukkit)
        }

        return this.entityBukkit as CraftPet
    }

    /**
     * Clears all entity aiGoals.
     */
    private fun clearAIGoals() {
        val dField = PathfinderGoalSelector::class.java.getDeclaredField("d")
        dField.isAccessible = true
        (dField.get(this.goalSelector) as MutableSet<*>).clear()
        (dField.get(this.targetSelector) as MutableSet<*>).clear()
    }
}