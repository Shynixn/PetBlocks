package com.github.shynixn.petblocks.bukkit.impl

import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.packet.api.packetOutEntityMetadata
import com.github.shynixn.mcutils.packet.api.sendPacket
import com.github.shynixn.mcutils.physicobject.api.PhysicObject
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import org.bukkit.Location
import org.bukkit.entity.Player

class PetEntityImpl(
    private val physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    private val entityComponent: PetArmorstandEntityComponentImpl
) : PhysicObject {
    private var currentLocation = Vector3d()

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
     * Tick on async thread.
     */
    override fun tickAsync() {
        physicsComponent.tickAsync()
        playerComponent.tickAsync()
        entityComponent.tickAsync()
    }

    /**
     * Ticks on minecraft thread.
     */
    override fun tickMinecraft() {
        this.currentLocation = physicsComponent.position.clone()
        physicsComponent.tickMinecraft()
        playerComponent.tickMinecraft()
        entityComponent.tickMinecraft()
    }

    /**
     * Removes the physic object.
     */
    override fun remove() {
        physicsComponent.close()
        playerComponent.close()
        entityComponent.close()
        isDead = true
    }
}
