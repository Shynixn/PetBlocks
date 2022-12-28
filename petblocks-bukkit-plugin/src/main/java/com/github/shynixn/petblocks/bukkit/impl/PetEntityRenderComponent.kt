package com.github.shynixn.petblocks.bukkit.impl

import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.physicobject.api.PhysicComponent
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetRidingState
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import org.bukkit.Location
import org.bukkit.entity.Player

class PetEntityRenderComponent(
    private val physicsComponent: MathComponent,
    private val playerComponent: PlayerComponent,
    val entityId: Int,
    val petMeta: PetMeta,
    /**
     * Reference to the owner.
     */
    var owner: Player? = null
) : PhysicComponent {
    init {
        playerComponent.onSpawnMinecraft.add { player, location -> onPlayerSpawn(player, location) }
        playerComponent.onRemoveMinecraft.add { player, _ -> onPlayerRemove(player) }
        physicsComponent.onPostPositionChange.add { position, motion, _ -> onPositionChange(position, motion) }
    }

    private fun onPlayerSpawn(player: Player, location: Location) {
        if (petMeta.visibility == PetVisibility.OWNER && player != owner) {
            return
        }

        val outer = this
        player.sendPacket(packetOutEntitySpawn {
            this.entityId = outer.entityId
            this.entityType = EntityType.ARMOR_STAND
            this.target = location
        })
        val itemStack = item {
            this.typeName = "GRASS_BLOCK"
        }.toItemStack()
        player.sendPacket(packetOutEntityEquipment {
            this.entityId = outer.entityId
            this.itemStack = itemStack
            this.slot = ArmorSlotType.HELMET
        })
        player.sendPacket(packetOutEntityMetadata {
            this.entityId = outer.entityId
            this.isInvisible = false
        })

        if (petMeta.ridingState == PetRidingState.HAT) {

        }

        if (petMeta.ridingState == PetRidingState.GROUND || petMeta.ridingState == PetRidingState.FLY) {

        }
    }

    private fun onPlayerRemove(player: Player) {
        val outer = this
        player.sendPacket(packetOutEntityDestroy {
            this.entityId = outer.entityId
        })
    }

    private fun onPositionChange(position: Vector3d, motion: Vector3d) {
        val players = playerComponent.visiblePlayers
        val outer = this

        for (player in players) {
            if (petMeta.visibility == PetVisibility.OWNER && player != owner) {
                continue
            }

            player.sendPacket(packetOutEntityVelocity {
                this.entityId = outer.entityId
                this.target = motion.toVector()
            })
            player.sendPacket(packetOutEntityTeleport {
                this.entityId = outer.entityId
                this.target = position.clone().addRelativeDown(0.3).toLocation()
            })
        }
    }

    /**
     * Closes the component.
     */
    override fun close() {
        owner = null
    }
}
