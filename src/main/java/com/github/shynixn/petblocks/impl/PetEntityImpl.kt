package com.github.shynixn.petblocks.impl

import com.github.shynixn.mccoroutine.bukkit.*
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicObject
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityEquipment
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMetadata
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMount
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResult
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResultType
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshot
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.Date
import java.util.logging.Level

class PetEntityImpl(
    private val physicsComponent: MathComponent,
    private val moveToTargetComponent: MoveToTargetComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: ArmorstandEntityComponent,
    private val plugin: Plugin,
    private val pet: Pet,
    private val petMeta: PetMeta,
    private val placeHolderService: PlaceHolderService,
    private val packetService: PacketService,
    private val physicObjectDispatcher: PhysicObjectDispatcher,
    private val pathfinderService: PathfinderService,
    private val petActionExecutionService: PetActionExecutionService,
    private val clickCoolDownMs: Long,
    private val pathFinderCube: Vector3d,
    private val visualizePath: Boolean
) : PhysicObject {
    private var positionUpdateCounter = 0
    private var velocity = Vector3d(0.0, 0.0, 0.0)
    private var lastClickTimeStamp = 0L

    init {
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            while (!isDead) {
                try {
                    val template = pet.template
                    val loop = template.loops[pet.loop]

                    if (loop == null) {
                        plugin.logger.log(
                            Level.SEVERE,
                            "Pet loop is set to '${pet.loop}' but it does not exist. Change the pet loop and respawn the pet."
                        )
                        break
                    }

                    petActionExecutionService.executeAction(pet.player, pet, loop)
                    delay(1.ticks)
                } catch (e: Exception) {
                    plugin.logger.log(Level.SEVERE, "Cannot execute pet loop '${pet.loop}'.", e)
                    break
                }
            }
        }
    }

    /**
     * Gets all entity ids.
     */
    override val entityIds: List<Int> by lazy {
        arrayListOf(entityComponent.entityId)
    }

    /**
     * Is the physicObject dead.
     */
    override var isDead: Boolean = false
        private set

    /**
     * Gets the location of the pet.
     */
    fun getLocation(): Vector3d {
        return petMeta.lastStoredLocation
    }

    fun getVelocity(): Vector3d {
        return velocity
    }

    fun setVelocity(vector3d: Vector3d) {
        this.physicsComponent.setVelocity(vector3d)
    }

    /**
     * Teleports the pet in world.
     */
    fun teleportInWorld(vector3d: Vector3d) {
        this.physicsComponent.teleport(vector3d)
    }

    /**
     * RightClicks the pet.
     */
    fun rightClick(player : Player) {
        val currentDateTime = Date().time

        if (currentDateTime - lastClickTimeStamp < clickCoolDownMs) {
            return
        }

        lastClickTimeStamp = currentDateTime

        val rightClickEvent = pet.template.events["rightClick"]
        if (rightClickEvent != null) {
            plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
                petActionExecutionService.executeAction(player, pet, rightClickEvent)
            }
        }
    }

    /**
     * LeftClicks the pet.
     */
    fun leftClick(player : Player) {
        val currentDateTime = Date().time

        if (currentDateTime - lastClickTimeStamp < clickCoolDownMs) {
            return
        }

        lastClickTimeStamp = currentDateTime

        val leftClickEvent = pet.template.events["leftClick"]
        if (leftClickEvent != null) {
            plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
                petActionExecutionService.executeAction(player, pet, leftClickEvent)
            }
        }
    }

    /**
     * Moves to the given location.
     */
    fun moveToLocation(location: Location, speed: Double) {
        val snapshot = pathfinderService.calculateFastPathfinderSnapshot(
            location,
            pathFinderCube.x.toInt(),
            pathFinderCube.y.toInt(),
            pathFinderCube.z.toInt()
        )

        plugin.launch(physicObjectDispatcher) {
            val sourceLocation = physicsComponent.position.toLocation()

            for (i in 0 until 3) {
                val targetLocation = location.toVector3d().addRelativeFront(-1.0 * i).toLocation()

                if (!attemptSolutions(snapshot, sourceLocation, targetLocation, speed)) {
                    if (!attemptSolutions(snapshot, sourceLocation.clone().add(0.0, 1.0, 0.0), targetLocation, speed)) {
                        if (!attemptSolutions(
                                snapshot,
                                sourceLocation.clone().add(0.0, -1.0, 0.0),
                                targetLocation,
                                speed
                            )
                        ) {
                            if (!attemptSolutions(
                                    snapshot,
                                    sourceLocation,
                                    targetLocation.clone().add(0.0, 1.0, 0.0),
                                    speed
                                )
                            ) {
                                if (!attemptSolutions(
                                        snapshot,
                                        sourceLocation,
                                        targetLocation.clone().add(0.0, -1.0, 0.0),
                                        speed
                                    )
                                ) {
                                    continue
                                }
                            }
                        }
                    }
                }

                break
            }
        }
    }

    private fun attemptSolutions(
        snapshot: WorldSnapshot, sourceLocation: Location, targetLocation: Location, speed: Double
    ): Boolean {
        val result = pathfinderService.findPath(snapshot, sourceLocation, targetLocation)

        if (result.resultType == PathfinderResultType.FOUND) {
            if (visualizePath) {
                visualizePath(result)
            }

            moveToTargetComponent.walkToTarget(result.steps, speed)
            return true
        }

        return false
    }

    /**
     * Gets called when the player is riding the entity.
     */
    fun ride(player: Player, forward: Double, isJumping: Boolean) {
        positionUpdateCounter++
        if (positionUpdateCounter > 10) {
            // Required so the position of the player stays in sync while packet riding.
            packetService.setServerPlayerPosition(player, physicsComponent.position.toLocation())
            positionUpdateCounter = 0
        }

        val isOnGround = if (isJumping) {
            isOnGround(getLocation().toLocation())
        } else {
            false
        }

        plugin.launch(physicObjectDispatcher) {
            if (forward != 0.0) {
                val movementVector = if (forward > 0.0) {
                    player.location.direction.normalize().multiply(0.5).toVector3d()
                } else {
                    player.location.direction.normalize().multiply(-0.5).toVector3d()
                }

                physicsComponent.motion.x = movementVector.x
                physicsComponent.motion.z = movementVector.z
            }

            if (isJumping && isOnGround) {
                physicsComponent.motion.y = 1.0
            }

            physicsComponent.position.pitch = 0.0
            physicsComponent.position.yaw = player.location.yaw.toDouble()
        }
    }

    private fun isOnGround(location: Location): Boolean {
        val movingObjectPosition =
            location.world!!.rayTraceBlocks(location, Vector(0, -1, 0), 1.0, FluidCollisionMode.NEVER, true)
        return movingObjectPosition != null && movingObjectPosition.hitBlock != null
    }

    /**
     * If owner parameter is not null, only the owner receives packets.
     */
    fun updateVisibility(visibility: PetVisibility, owner: Player, location: Location) {
        for (player in playerComponent.visiblePlayers) {
            if (visibility == PetVisibility.OWNER && player != owner) {
                playerComponent.onRemoveMinecraft.forEach { e -> e.invoke(player, location) }
            } else if (visibility == PetVisibility.ALL && player != owner) {
                playerComponent.onSpawnMinecraft.forEach { e -> e.invoke(player, location) }
            }
        }
    }

    /**
     * Updates the displayName in the world.
     */
    fun updateDisplayName(name: String) {
        for (player in playerComponent.visiblePlayers) {
            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = entityComponent.entityId
                it.customNameVisible = true
                it.customname = placeHolderService.replacePlaceHolders(player, name, pet)
            })
        }
    }

    /**
     * Updates the head Itemstack.
     */
    fun updateHeadItemStack(itemStack: ItemStack) {
        for (player in playerComponent.visiblePlayers) {
            packetService.sendPacketOutEntityEquipment(player, PacketOutEntityEquipment().also {
                it.entityId = entityComponent.entityId
                it.items = listOf(Pair(ArmorSlotType.HELMET, itemStack))
            })
        }
    }

    /**
     * Updates the riding state of the player.
     */
    fun updateRidingState(owner: Player) {
        for (player in playerComponent.visiblePlayers) {
            if (petMeta.visibility == PetVisibility.OWNER && player != owner) {
                continue
            }

            val ridingState = petMeta.ridingState

            if (ridingState == PetRidingState.NO) {
                // Remove ground and fly
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = entityComponent.entityId
                })
                // Remove hat
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = player.entityId
                })
            }

            if (ridingState == PetRidingState.HAT) {
                // Remove ground and fly
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = entityComponent.entityId
                })
                // Set pet as passenger of player
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = player.entityId
                    it.passengers = listOf(entityComponent.entityId)
                })
            }

            if (ridingState == PetRidingState.GROUND) {
                // Remove hat
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = player.entityId
                })
                // Set pet as passenger of player
                packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                    it.entityId = entityComponent.entityId
                    it.passengers = listOf(player.entityId)
                })
            }
        }
    }

    /**
     * Tick on async thread.
     */
    override fun tickPhysic() {
        physicsComponent.tickPhysic()
        playerComponent.tickPhysic()
        entityComponent.tickPhysic()
        moveToTargetComponent.tickPhysic()
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        if (this.pet.isDisposed) {
            this.remove()
            return
        }

        this.petMeta.lastStoredLocation = physicsComponent.position.clone()
        this.velocity = physicsComponent.motion.clone()
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent.tickMinecraft()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        // Entity needs to be closed first.
        entityComponent.close()
        physicsComponent.close()
        playerComponent.close()
        isDead = true
    }

    /**
     * For Debugging purposes.
     */
    private var copy: List<Pair<Vector3d, Pair<Material, Byte>>> = emptyList()

    /**
     * For Debugging purposes.
     */
    private fun visualizePath(pathfinderResult: PathfinderResult) {
        val worldName = pet.location.world!!.name
        pathfinderResult.steps.forEach { e -> e.world = worldName }

        for (player in Bukkit.getOnlinePlayers()) {
            for (item in copy) {
                player.sendBlockChange(item.first.toLocation(), item.second.first, item.second.second)
            }

            for (item in pathfinderResult.steps) {
                player.sendBlockChange(item.toLocation().add(0.0, -1.0, 0.0), Material.GOLD_BLOCK, 0)
            }
        }

        copy =
            pathfinderResult.steps.map { e -> Pair(e, Pair(e.toLocation().block.type, e.toLocation().block.data)) }
    }
}
