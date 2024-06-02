package com.github.shynixn.petblocks.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.physic.PhysicObject
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMount
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResult
import com.github.shynixn.mcutils.pathfinder.api.PathfinderResultType
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshot
import com.github.shynixn.petblocks.contract.BreakBlockService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.enumeration.DropType
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.exception.PetBlocksPetDisposedException
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import org.bukkit.*
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.*
import java.util.logging.Level
import kotlin.collections.ArrayList

class PetEntityImpl(
    private val physicsComponent: MathComponent,
    private val moveToTargetComponent: MoveToTargetComponent,
    val playerComponent: PlayerComponent,
    val entityComponent: ArmorstandEntityComponent,
    private val plugin: Plugin,
    val pet: Pet,
    private val petMeta: PetMeta,
    private val packetService: PacketService,
    private val physicObjectDispatcher: PhysicObjectDispatcher,
    private val pathfinderService: PathfinderService,
    private val petActionExecutionService: PetActionExecutionService,
    private val breakBlockService: BreakBlockService,
    private val rayTracingService: RayTracingService,
    private val clickCoolDownMs: Long,
    private val pathFinderCube: Vector3d,
    private val visualizePath: Boolean,
    private val ridePositionUpdateMs: Int
) : PhysicObject {
    private var velocity = Vector3d(0.0, 0.0, 0.0)
    private var lastClickTimeStamp = 0L
    private var cancellationTokenLoop = CancellationToken()
    private var cancellationTokenLongRunning = CancellationToken()
    private var lastRideUpdate = 0L
    var isBreakingBlock = false

    // Mover
    private var lastRandomTimeStamp = 0L
    private var randomMoveOne = 0
    private var randomMoveTwo = 0
    private var randomMoveThree = 0

    companion object {
        var random = Random()
    }

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

                    if (pet.isHat()) {
                        // Update pet location on player.
                        pet.location = pet.player.location
                    }

                    cancellationTokenLoop = CancellationToken()
                    petActionExecutionService.executeAction(pet.player, pet, loop, cancellationTokenLoop)
                    delay(1.ticks)
                } catch (e: PetBlocksPetDisposedException) {
                    // Ignore Disposed exception.
                    break
                } catch (e: CancellationException) {
                    // Ignore Coroutine Cancel
                    break
                } catch (e: IllegalStateException) {
                    // Ignore Coroutine Cancel
                    break
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
        cancellationTokenLongRunning.isCancelled = true
        this.physicsComponent.setVelocity(vector3d)
    }

    /**
     * Teleports the pet in world.
     */
    fun teleportInWorld(vector3d: Vector3d) {
        cancellationTokenLongRunning.isCancelled = true
        this.physicsComponent.teleport(vector3d)
    }

    /**
     * Cancels the execution of the currently executed loop.
     */
    fun cancelLoop() {
        cancellationTokenLoop.isCancelled = true
    }

    /**
     * Cancels any long runnin actions like mining.
     */
    fun cancelLongRunningAction() {
        cancellationTokenLongRunning.isCancelled = true
    }

    /**
     * Breaks the block which is in front of the pet and no more than 1 block away.
     * The Drop Types are an ordered list, where the first element is attempted at first.
     * If the first element is not possible (e.g. OwnerInventory is Full) the second action is attempted.
     * If none work, the broken block item vanishes.
     */
    fun breakBlock(timeToBreakTicks: Int, dropTypes: List<DropType>) {
        cancellationTokenLongRunning.isCancelled = true
        cancellationTokenLongRunning = CancellationToken()
        val actualDropTypes = ArrayList(dropTypes)
        actualDropTypes.add(DropType.VANISH) // If no other matches.
        breakBlockService.breakBlock(this, timeToBreakTicks, dropTypes, cancellationTokenLongRunning)
    }

    /**
     * RightClicks the pet.
     */
    fun rightClick(player: Player) {
        cancellationTokenLongRunning.isCancelled = true
        val currentDateTime = Date().time

        if (currentDateTime - lastClickTimeStamp < clickCoolDownMs) {
            return
        }

        lastClickTimeStamp = currentDateTime

        val rightClickEvent = pet.template.events["rightClick"]
        if (rightClickEvent != null) {
            plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
                petActionExecutionService.executeAction(player, pet, rightClickEvent, CancellationToken())
            }
        }
    }

    /**
     * LeftClicks the pet.
     */
    fun leftClick(player: Player) {
        cancellationTokenLongRunning.isCancelled = true
        val currentDateTime = Date().time

        if (currentDateTime - lastClickTimeStamp < clickCoolDownMs) {
            return
        }

        lastClickTimeStamp = currentDateTime

        val leftClickEvent = pet.template.events["leftClick"]
        if (leftClickEvent != null) {
            plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
                petActionExecutionService.executeAction(player, pet, leftClickEvent, CancellationToken())
            }
        }
    }

    /**
     * Moves to the given location.
     */
    fun moveToLocation(location: Location, speed: Double) {
        cancellationTokenLongRunning.isCancelled = true
        val snapshot = pathfinderService.calculateFastPathfinderSnapshot(
            location, pathFinderCube.x.toInt(), pathFinderCube.y.toInt(), pathFinderCube.z.toInt()
        )

        val dateTime = Date().time

        if (dateTime - lastRandomTimeStamp > 3000) {
            // For multiple pets.
            lastRandomTimeStamp = dateTime
            randomMoveOne = random.nextInt(3)
            randomMoveTwo = random.nextInt(3)
        }

        plugin.launch(physicObjectDispatcher) {
            val sourceLocation = physicsComponent.position.toLocation()

            for (i in 0 until 3) {
                val targetLocation = location.toVector3d().addRelativeFront(-1.0 * i + randomMoveOne)
                    .addRelativeLeft(randomMoveTwo.toDouble()).addRelativeRight(randomMoveThree.toDouble()).toLocation()

                if (!attemptSolutions(snapshot, sourceLocation, targetLocation, speed)) {
                    if (!attemptSolutions(snapshot, sourceLocation.clone().add(0.0, 1.0, 0.0), targetLocation, speed)) {
                        if (!attemptSolutions(
                                snapshot, sourceLocation.clone().add(0.0, -1.0, 0.0), targetLocation, speed
                            )
                        ) {
                            if (!attemptSolutions(
                                    snapshot, sourceLocation, targetLocation.clone().add(0.0, 1.0, 0.0), speed
                                )
                            ) {
                                if (!attemptSolutions(
                                        snapshot, sourceLocation, targetLocation.clone().add(0.0, -1.0, 0.0), speed
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

    fun moveForward(speed: Double) {
        cancellationTokenLongRunning.isCancelled = true
        cancellationTokenLongRunning = CancellationToken()

        val token = cancellationTokenLongRunning

        plugin.launch {
            while (true) {
                if (token.isCancelled) {
                    return@launch
                }

                val snapshot = pathfinderService.calculateFastPathfinderSnapshot(
                    pet.location, 5, 5, 5
                )
                val sourceLocation = pet.location
                val targetLocation = sourceLocation.toVector3d().addRelativeFront(0.8).toLocation()

                plugin.launch(physicObjectDispatcher) {
                    val pathResult = pathfinderService.findPath(snapshot, sourceLocation, targetLocation)

                    if (pathResult.resultType != PathfinderResultType.FOUND) {
                        token.isCancelled = true
                    } else {
                        moveToTargetComponent.walkToTarget(pathResult.steps, speed).join()
                    }
                }.join()
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
        cancellationTokenLongRunning.isCancelled = true

        val current = Date().time
        if (current - lastRideUpdate >= ridePositionUpdateMs) {
            // Required so the position of the player stays in sync while packet riding.
            packetService.setServerPlayerPosition(player, physicsComponent.position.toLocation())
            for (visiblePlayers in playerComponent.visiblePlayers) {
                packetService.sendPacketOutEntityMount(visiblePlayers, PacketOutEntityMount().also {
                    it.entityId = entityComponent.entityId
                    it.passengers = listOf(player.entityId)
                })
            }
            lastRideUpdate = current
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
        val rayTraceResult = rayTracingService.rayTraceMotion(location.toVector3d(), Vector3d(0.0, -1.0, 0.0))
        return rayTraceResult.hitBlock
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
    fun updateMetaData() {
        for (player in playerComponent.visiblePlayers) {
            entityComponent.updateMetaData(player)
        }
    }

    /**
     * Updates the head Itemstack.
     */
    fun updateHeadItemStack() {
        for (player in playerComponent.visiblePlayers) {
            entityComponent.updateEquipment(player)
        }
    }

    /**
     * Updates the riding state of the player.
     */
    fun updateRidingState() {
        cancellationTokenLongRunning.isCancelled = true
        for (player in playerComponent.visiblePlayers) {
            entityComponent.updateRidingState(player)
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

        this.petMeta.lastStoredLocation = physicsComponent.position.copy()
        this.velocity = physicsComponent.motion.copy()
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
     * Gets the block the pet is looking at with the given maxDistance.
     */
    fun findTargetBlock(maxDistance: Double): Block? {
        val worldLocation = getLocation().toLocation()
        val rayTraceResult = rayTracingService.rayTraceMotion(
            worldLocation.toVector3d(),
            worldLocation.direction.normalize().multiply(maxDistance).toVector3d()
        )
        return rayTraceResult.block
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

        copy = pathfinderResult.steps.map { e -> Pair(e, Pair(e.toLocation().block.type, e.toLocation().block.data)) }
    }
}
