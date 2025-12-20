package com.github.shynixn.petblocks.impl

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.github.shynixn.mccoroutine.folia.ticks
import com.github.shynixn.mcutils.common.CancellationToken
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.api.meta.enumeration.RidingMoveType
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
import com.github.shynixn.petblocks.exception.PetBlocksPetDisposedException
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PetEntityImpl(
    private val physicsComponent: MathComponent,
    private val moveToTargetComponent: MoveToTargetComponent,
    val playerComponent: PlayerComponent,
    val entityComponent: ArmorstandEntityComponent,
    private val plugin: Plugin,
    val pet: Pet,
    private val petMeta: PetMeta,
    private val packetService: PacketService,
    private val pathfinderService: PathfinderService,
    private val petActionExecutionService: PetActionExecutionService,
    private val breakBlockService: BreakBlockService,
    private val rayTracingService: RayTracingService,
    private val clickCoolDownMs: Long,
    private val pathFinderCube: Vector3d,
    private val visualizePath: Boolean,
    private val ridePositionUpdateMs: Int
) {
    private var velocity = Vector3d(0.0, 0.0, 0.0)
    private var lastClickTimeStamp = 0L
    private var cancellationTokenLoop = CancellationToken()
    private var cancellationTokenLongRunning = CancellationToken()
    private var lastRideUpdate = 0L
    private var lastSneakUpdate = 0L
    private var ridingMoveType: RidingMoveType = RidingMoveType.STOP

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
        plugin.launch {
            while (!isDead) {
                withContext(plugin.regionDispatcher(pet.location)) {
                    tickMinecraft()
                }

                tickPhysic()
            }
        }
        plugin.launch {
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
                    if (pet.player.isOnline) {
                        petActionExecutionService.executeAction(pet.player, pet, loop, cancellationTokenLoop)
                    } else {
                        remove()
                    }
                } catch (e: PetBlocksPetDisposedException) {
                    // Ignore Disposed exception.
                    break
                } catch (e: CancellationException) {
                    // Ignore Coroutine Cancel
                    break
                } catch (e: IllegalStateException) {
                    break
                } catch (e: Exception) {
                    plugin.logger.log(Level.SEVERE, "Cannot execute pet loop '${pet.loop}'.", e)
                    break
                }
            }
        }
    }

    /**
     * Is the physicObject dead.
     */
    var isDead: Boolean = false
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

        val key = if (player.isSneaking) {
            "rightClickSneak"
        } else {
            "rightClick"
        }
        val rightClickEvent = pet.template.events[key]
        if (rightClickEvent != null) {
            plugin.launch {
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

        val key = if (player.isSneaking) {
            "leftClickSneak"
        } else {
            "leftClick"
        }
        val leftClickEvent = pet.template.events[key]
        if (leftClickEvent != null) {
            plugin.launch {
                petActionExecutionService.executeAction(player, pet, leftClickEvent, CancellationToken())
            }
        }
    }

    /**
     * Spawn is initiated.
     */
    fun onSpawn(player: Player) {
        cancellationTokenLongRunning.isCancelled = true
        val spawnEvent = pet.template.events["spawn"]
        if (spawnEvent != null) {
            plugin.launch {
                petActionExecutionService.executeAction(player, pet, spawnEvent, CancellationToken())
            }
        }
    }

    /**
     * Despawn is initiated.
     */
    fun onDespawn(player: Player) {
        cancellationTokenLongRunning.isCancelled = true
        val despawnEvent = pet.template.events["despawn"]
        if (despawnEvent != null) {
            plugin.launch {
                petActionExecutionService.executeAction(player, pet, despawnEvent, CancellationToken())
            }
        }
    }

    /**
     * Moves to the given location.
     */
    fun moveToLocation(location: Location, speed: Double) {
        cancellationTokenLongRunning.isCancelled = true

        plugin.launch {
            val snapshot = withContext(plugin.regionDispatcher(location)) {
                pathfinderService.calculateFastPathfinderSnapshot(
                    location, pathFinderCube.x.toInt(), pathFinderCube.y.toInt(), pathFinderCube.z.toInt()
                )
            }

            val dateTime = Date().time

            if (dateTime - lastRandomTimeStamp > 3000) {
                // For multiple pets.
                lastRandomTimeStamp = dateTime
                randomMoveOne = random.nextInt(3)
                randomMoveTwo = random.nextInt(3)
            }

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

        while (true) {
            if (token.isCancelled) {
                return
            }

            val snapshot = pathfinderService.calculateFastPathfinderSnapshot(
                pet.location, 5, 5, 5
            )
            val sourceLocation = pet.location
            val targetLocation = sourceLocation.toVector3d().addRelativeFront(0.8).toLocation()

            val pathResult = pathfinderService.findPath(snapshot, sourceLocation, targetLocation)

            if (pathResult.resultType != PathfinderResultType.FOUND) {
                token.isCancelled = true
            } else {
                moveToTargetComponent.walkToTarget(pathResult.steps, speed)
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
    fun ride(player: Player, moveType: RidingMoveType, isJumping: Boolean, isSneaking: Boolean) {
        cancellationTokenLongRunning.isCancelled = true

        if (isJumping) {
            plugin.launch {
                val location = getLocation().toLocation()
                val isOnGround = withContext(plugin.regionDispatcher(location)) {
                    isOnGround(getLocation().toLocation())
                }
                if (isOnGround) {
                    physicsComponent.motion.y = 1.0
                }
                synchronizeRidingState(player)
            }

            return
        }

        if (this.ridingMoveType == RidingMoveType.STOP) {
            this.ridingMoveType = moveType
            plugin.launch {
                while (!isDead && ridingMoveType != RidingMoveType.STOP && player.isOnline && pet.isRiding()) {
                    synchronizeRidingState(player)
                    val movementVector = if (ridingMoveType == RidingMoveType.FORWARD) {
                        player.location.direction.normalize().multiply(petMeta.physics.ridingSpeed).toVector3d()
                    } else {
                        player.location.direction.normalize().multiply(petMeta.physics.ridingSpeed * -1).toVector3d()
                    }

                    physicsComponent.motion.x = movementVector.x
                    physicsComponent.motion.z = movementVector.z

                    physicsComponent.position.pitch = 0.0
                    physicsComponent.position.yaw = player.location.yaw.toDouble()
                    delay(5.ticks)
                }
            }
        }

        this.ridingMoveType = moveType

        if (this.ridingMoveType == RidingMoveType.STOP) {
            // Fast Stop.
            physicsComponent.motion.x = 0.0
            physicsComponent.motion.z = 0.0
        }

        val current = System.currentTimeMillis()

        if (isSneaking && current - lastSneakUpdate >= 200) {
            lastSneakUpdate = current
            val sneakEvent = pet.template.events["ridingSneak"]
            if (sneakEvent != null) {
                plugin.launch {
                    petActionExecutionService.executeAction(player, pet, sneakEvent, CancellationToken())
                }
            }
        }
    }

    private suspend fun synchronizeRidingState(player: Player) {
        val current = Date().time
        if (current - lastRideUpdate >= ridePositionUpdateMs) {
            // Required so the position of the player stays in sync while packet riding.
            // Has to be on the main thread.
            withContext(plugin.entityDispatcher(player)) {
                packetService.setServerPlayerPosition(player, physicsComponent.position.toLocation())
            }
            for (visiblePlayers in playerComponent.visiblePlayers) {
                packetService.sendPacketOutEntityMount(visiblePlayers, PacketOutEntityMount().also {
                    it.entityId = entityComponent.entityId
                    it.passengers = listOf(player.entityId)
                })
            }
            lastRideUpdate = current
        }
    }

    private fun isOnGround(location: Location): Boolean {
        val rayTraceResult = rayTracingService.rayTraceMotion(
            location.toVector3d(),
            Vector3d(0.0, -1.0, 0.0),
            petMeta.physics.collideWithWater,
            petMeta.physics.collideWithPassableBlocks
        )
        return rayTraceResult.hitBlock
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
    fun tickPhysic() {
        physicsComponent.tickPhysic()
        moveToTargetComponent.tickPhysic()
    }

    /**
     * Ticks on minecraft thread.
     */
    fun tickMinecraft() {
        if (this.pet.isDisposed) {
            this.remove()
            return
        }

        this.petMeta.lastStoredLocation = physicsComponent.position.copy()
        this.velocity = physicsComponent.motion.copy()

        try {
            physicsComponent.tickMinecraft()
        } catch (e: Exception) {
            // Can happen if an invalid world reference exists. e.g. pet references unloaded world
            this.petMeta.lastStoredLocation = pet.player.location.toVector3d()
            this.physicsComponent.position = pet.player.location.toVector3d()
            this.pet.call()
            return
        }

        playerComponent.tickMinecraft()
    }

    /**
     * Removes the physic object.
     */
    fun remove() {
        // Entity needs to be closed first.
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
            worldLocation.direction.normalize().multiply(maxDistance).toVector3d(),
            petMeta.physics.collideWithWater,
            petMeta.physics.collideWithPassableBlocks
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
