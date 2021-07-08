package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.persistence.entity.AIMovement
import net.minecraft.core.BlockPosition
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityTypes
import net.minecraft.world.entity.ai.attributes.GenericAttributes
import net.minecraft.world.entity.ai.goal.PathfinderGoal
import net.minecraft.world.entity.ai.goal.PathfinderGoalSelector
import net.minecraft.world.entity.animal.EntityRabbit
import net.minecraft.world.level.block.state.IBlockData
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_17_R1.CraftServer
import org.bukkit.craftbukkit.v1_17_R1.CraftWorld
import org.bukkit.event.entity.CreatureSpawnEvent

/**
 * NMS implementation of the Rabbit pet backend.
 */
class NMSPetRabbit(petDesign: NMSPetArmorstand, location: Location) : EntityRabbit(EntityTypes.au, (location.world as CraftWorld).handle) {
    private var petDesign: NMSPetArmorstand? = null

    // Pathfinders need to be self cached for Paper.
    private var initialClear = true
    private var pathfinderCounter = 0
    private var cachedPathfinders = HashSet<PathfinderGoal>()

    // BukkitEntity has to be self cached since 1.14.
    private var entityBukkit: Any? = null

    init {
        this.petDesign = petDesign
        this.isSilent = true

        clearAIGoals()
        this.getAttributeInstance(GenericAttributes.d)!!.value = 0.30000001192092896 * 0.75
        this.O = 1.0F

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
                    this.getAttributeInstance(GenericAttributes.d)!!.value =
                        0.30000001192092896 * aiBase.movementSpeed
                    this.O = aiBase.climbingHeight.toFloat()
                }
            } else {
                this.bP.a(pathfinderCounter++, pathfinder as PathfinderGoal)
                this.cachedPathfinders.add(pathfinder)
            }
        }

        this.bP.a(pathfinderCounter++, hyperPathfinder)
        this.cachedPathfinders.add(hyperPathfinder)
    }

    /**
     * Gets called on move to play sounds.
     */
    override fun b(blockposition: BlockPosition, iblockdata: IBlockData) {
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
            entityBukkit = CraftPet(Bukkit.getServer() as CraftServer, this)

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
            val dField = PathfinderGoalSelector::class.java.getDeclaredField("d")
            dField.isAccessible = true
            (dField.get(this.bP) as MutableSet<*>).clear()
            (dField.get(this.bQ) as MutableSet<*>).clear()
            initialClear = false
        }

        for (pathfinder in cachedPathfinders) {
            this.bP.a(pathfinder)
        }

        this.cachedPathfinders.clear()
    }
}
