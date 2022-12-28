package com.github.shynixn.petblocks.bukkit.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.ArmorSlotType
import com.github.shynixn.mcutils.packet.api.packetOutEntityEquipment
import com.github.shynixn.mcutils.packet.api.packetOutEntityMetadata
import com.github.shynixn.mcutils.packet.api.sendPacket
import com.github.shynixn.mcutils.physicobject.api.PhysicObject
import com.github.shynixn.mcutils.physicobject.api.component.AIComponent
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.api.component.MoveToTargetComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.Pet
import com.github.shynixn.petblocks.bukkit.PetEntity
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetRidingState
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import com.github.shynixn.petblocks.bukkit.service.PetActionExecutionService
import kotlinx.coroutines.delay
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

class PetEntityImpl(
    val physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: PetEntityRenderComponent,
    plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val pet: Pet,
    private val template: PetTemplate,
    private val petMeta: PetMeta,
    val moveToTargetComponent: MoveToTargetComponent,
    val aiComponent: AIComponent<PetEntityImpl>,
) : PhysicObject, PetEntity {
    private var currentLocation = Vector3d()

    /**
     * Location of the owner.
     */
    var ownerLocation = Vector3d()

    init {
        aiComponent.actor = this
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            while (!isDead) {
             //   petActionExecutionService.executeAction(pet, template.loopDefinition)
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
        return currentLocation
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
    override fun leftClick(player: Player) {
    }

    /**
     * RightClick on the physic object.
     */
    override fun rightClick(player: Player) {
        petActionExecutionService.executeAction(pet, template.rightClickDefinition)
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
            player.sendPacket(packetOutEntityMetadata {
                this.customname = name
            })
        }
    }

    /**
     * Updates the head Itemstack.
     */
    fun updateHeadItemStack(itemStack: ItemStack) {
        for (player in playerComponent.visiblePlayers) {
            player.sendPacket(packetOutEntityEquipment {
                this.entityId = entityId
                this.slot = ArmorSlotType.HELMET
                this.itemStack = itemStack
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

            if (petMeta.ridingState == PetRidingState.NO) {
                // Send Unmount Packet.
            }

            if (petMeta.ridingState == PetRidingState.HAT) {
                // Send Hat Packet
            }

            if (petMeta.ridingState == PetRidingState.GROUND || petMeta.ridingState == PetRidingState.FLY) {
                // Send Ground Packet
            }
        }
    }

    /**
     * Tick on async thread.
     */
    override fun tickAsync() {
        physicsComponent.tickAsync()
        playerComponent.tickAsync()
        entityComponent.tickAsync()
        moveToTargetComponent.tickAsync()
        aiComponent.tickAsync()
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        this.ownerLocation = this.pet.player.location.toVector3d()
        this.currentLocation = physicsComponent.position.clone()
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent.tickMinecraft()
        moveToTargetComponent.tickMinecraft()
        aiComponent.tickMinecraft()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        physicsComponent.close()
        playerComponent.close()
        entityComponent.close()
        moveToTargetComponent.close()
        aiComponent.close()
        isDead = true
    }
}
