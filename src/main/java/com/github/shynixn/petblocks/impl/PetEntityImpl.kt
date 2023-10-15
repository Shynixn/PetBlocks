package com.github.shynixn.petblocks.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.AIComponent
import com.github.shynixn.mcutils.common.physic.PhysicObject
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityEquipment
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMetadata
import com.github.shynixn.mcutils.packet.api.packet.PacketOutEntityMount
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import kotlinx.coroutines.delay
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

class PetEntityImpl(
    val physicsComponent: MathComponent,
    val moveToTargetComponent: MoveToTargetComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: ArmorstandEntityComponent,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val pet: Pet,
    private val template: PetTemplate,
    private val petMeta: PetMeta,
    private val placeHolderService: PlaceHolderService,
    private val packetService: PacketService,
    private val physicObjectDispatcher: PhysicObjectDispatcher,
    val aiComponent: AIComponent<PetEntityImpl>
) : PhysicObject {
    private var positionUpdateCounter = 0

    /**
     * Location of the owner.
     */
    var ownerLocation = Vector3d()

    init {
        aiComponent.actor = this
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            while (!isDead) {
                petActionExecutionService.executeAction(pet, template.loopDefinition)
                delay(1.ticks)
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

    /**
     * Teleports the pet in world.
     */
    fun teleportInWorld(vector3d: Vector3d) {
        this.physicsComponent.teleport(vector3d)
    }

    /**
     * LeftClick on the physic object.
     */
    fun leftClick(player: Player) {
        plugin.launch {
            petActionExecutionService.executeAction(pet, template.leftClickDefinition)
        }
    }

    /**
     * RightClick on the physic object.
     */
    fun rightClick(player: Player) {
        plugin.launch {
            petActionExecutionService.executeAction(pet, template.rightClickDefinition)
        }
    }

    /**
     * Gets called when the player is riding the entity.
     */
    fun ride(player: Player, forward: Double, sideward: Double, isJumping: Boolean) {
        positionUpdateCounter++
        if (positionUpdateCounter > 10) {
            // Required so the position of the player stays in sync while packet riding.
            packetService.setServerPlayerPosition(player, physicsComponent.position.toLocation())
            positionUpdateCounter = 0
        }

        if (forward != 0.0) {
            val movementVector = if (forward > 0.0) {
                player.location.direction.normalize().multiply(0.5).toVector3d()
            } else {
                player.location.direction.normalize().multiply(-0.5).toVector3d()
            }

            val isOnGround = isOnGround(getLocation().toLocation())
            val otherGround = physicsComponent.isOnGround

            if (isJumping && isOnGround) {
                movementVector.y = 0.5

            } else if (isOnGround || otherGround) {
                movementVector.y = 0.0
            } else {
                movementVector.y = -1.0
            }

            plugin.launch(physicObjectDispatcher) {
                physicsComponent.motion = movementVector
            }
        } else if (sideward != 0.0) {
            val movementVector = if (sideward > 0.0) {
                player.location.direction.normalize().rotateAroundY(90.0).multiply(0.5).toVector3d()
            } else {
                player.location.direction.normalize().rotateAroundY(-90.0).multiply(0.5).toVector3d()
            }

            val isOnGround = isOnGround(getLocation().toLocation())
            val otherGround = physicsComponent.isOnGround

            if (isJumping && isOnGround) {
                movementVector.y = 0.5

            } else if (isOnGround || otherGround) {
                movementVector.y = 0.0
            } else {
                movementVector.y = -1.0
            }

            plugin.launch(physicObjectDispatcher) {
                physicsComponent.motion = movementVector
            }
        } else if (isJumping) {
            val isOnGround = isOnGround(getLocation().toLocation())

            if (isOnGround) {
                plugin.launch(physicObjectDispatcher) {
                    physicsComponent.motion.y = 0.5
                }
            }
        }

        physicsComponent.position.yaw = player.location.yaw.toDouble()
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
        aiComponent.tickPhysic()
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        if (this.pet.isDisposed) {
            this.remove()
            return
        }

        this.ownerLocation = this.pet.player.location.toVector3d()
        this.petMeta.lastStoredLocation = physicsComponent.position.clone()
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent.tickMinecraft()
        aiComponent.tickMinecraft()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        // Entity needs to be closed first.
        entityComponent.close()
        physicsComponent.close()
        playerComponent.close()
        aiComponent.close()
        isDead = true
    }
}