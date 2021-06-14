package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.AgeableMob
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.ai.attributes.Attributes
import net.minecraft.world.entity.ai.goal.Goal
import net.minecraft.world.entity.ai.goal.GoalSelector
import net.minecraft.world.entity.animal.Pig
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent

/**
 * NMS implementation of the Villager pet backend.
 */
class NMSPetVillager(petDesign: NMSPetArmorstand, location: Location) :
    Pig(EntityType.PIG, (location.world as CraftWorld).handle) {
    private var petDesign: NMSPetArmorstand? = null

    // Pathfinders need to be self cached for Paper.
    private var initialClear = true
    private var pathfinderCounter = 0
    private var cachedPathfinders = HashSet<Goal>()

    // BukkitEntity has to be self cached since 1.14.
    private var entityBukkit: Any? = null

    init {
        this.petDesign = petDesign
        this.isSilent = true

        clearAIGoals()
        this.getAttribute(Attributes.MOVEMENT_SPEED)!!.baseValue = 0.30000001192092896 * 0.75
        this.maxUpStep = 1.0F

        val mcWorld = (location.world as CraftWorld).handle
        this.setPos(location.x, location.y - 200, location.z)
        mcWorld.addEntity(this, CreatureSpawnEvent.SpawnReason.CUSTOM)

        val targetLocation = location.clone()
        PetBlocksApi.resolve(ConcurrencyService::class.java).runTaskSync(20L) {
            // Only fix location if it is not already fixed.
            if (this.bukkitEntity.location.distance(targetLocation) > 20) {
                this.setPos(targetLocation.x, targetLocation.y + 1.0, targetLocation.z)
            }
        }
    }

    /**
     * Applies pathfinders to the entity.
     */
    fun applyPathfinders(pathfinders: List<Any>) {
        clearAIGoals()
        pathfinderCounter = 0

        val proxies = HashMap<PathfinderProxy, CombinedPathfinder.Cache>()
        val hyperPathfinder = CombinedPathfinder(proxies)

        for (pathfinder in pathfinders) {
            if (pathfinder is PathfinderProxy) {
                val wrappedPathfinder = Pathfinder(pathfinder)
                this.cachedPathfinders.add(wrappedPathfinder)
                proxies[pathfinder] = CombinedPathfinder.Cache()

                val aiBase = pathfinder.aiBase

                if (aiBase is AIMovement) {
                    this.getAttribute(Attributes.MOVEMENT_SPEED)!!.baseValue =
                        0.30000001192092896 * aiBase.movementSpeed
                    this.maxUpStep = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.goalSelector.addGoal(pathfinderCounter++, pathfinder as Goal)
                this.cachedPathfinders.add(pathfinder)
            }
        }

        this.goalSelector.addGoal(pathfinderCounter++, hyperPathfinder)
        this.cachedPathfinders.add(hyperPathfinder)
    }

    /**
     * Gets called on move to play sounds.
     */
    override fun playStepSound(blockposition: BlockPos?, iblockdata: BlockState?) {
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
     * Dummy.
     */
    override fun getBreedOffspring(p0: ServerLevel?, p1: AgeableMob?): AgeableMob? {
        return null
    }

    /**
     * Gets the bukkit entity.
     */
    override fun getBukkitEntity(): CraftPet {
        if (this.entityBukkit == null) {
            entityBukkit = CraftPet(this.level.craftServer, this)

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
        if (initialClear) {
            val dField = GoalSelector::class.java.getDeclaredField("d")
            dField.isAccessible = true
            (dField.get(this.goalSelector) as MutableSet<*>).clear()
            (dField.get(this.targetSelector) as MutableSet<*>).clear()
            initialClear = false
        }

        pathfinderCounter = 0
        for (pathfinder in cachedPathfinders) {
            this.goalSelector.addGoal(pathfinderCounter++, pathfinder)
        }

        this.cachedPathfinders.clear()
    }
}
