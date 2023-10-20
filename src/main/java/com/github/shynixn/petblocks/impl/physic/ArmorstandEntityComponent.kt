package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.packet.*
import com.github.shynixn.petblocks.entity.PetMeta
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle

class ArmorstandEntityComponent(
    physicsComponent: MathComponent,
    private val packetService: PacketService,
    private val playerComponent: PlayerComponent,
    private val petMeta: PetMeta,
    val entityId: Int,
) : PhysicComponent {
    init {
        playerComponent.onSpawnMinecraft.add { player, location -> onPlayerSpawn(player, location) }
        playerComponent.onRemoveMinecraft.add { player, _ -> onPlayerRemove(player) }
        physicsComponent.onPostPositionChange.add { position, motion, _ -> onPositionChange(position, motion) }
    }

    private fun onPlayerSpawn(player: Player, location: Location) {
        packetService.sendPacketOutEntitySpawn(player, PacketOutEntitySpawn().also {
            it.entityId = this.entityId
            it.entityType = EntityType.ARMOR_STAND
            it.target = location
        })

        val itemStack = petMeta.headItem.toItemStack()

        packetService.sendPacketOutEntityEquipment(player, PacketOutEntityEquipment().also {
            it.entityId = this.entityId
            it.items = listOf(Pair(ArmorSlotType.HELMET, itemStack))
        })

        packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
            it.entityId = this.entityId
            it.isArmorstandSmall = true
        })
    }

    private fun onPlayerRemove(player: Player) {
        val outer = this
        packetService.sendPacketOutEntityDestroy(player, PacketOutEntityDestroy().also {
            it.entityIds = listOf(outer.entityId)
        })
    }

    private fun onPositionChange(position: Vector3d, motion: Vector3d) {
        val players = playerComponent.visiblePlayers

        for (player in players) {
            packetService.sendPacketOutEntityVelocity(player, PacketOutEntityVelocity().also {
                it.entityId = this.entityId
                it.target = motion.toVector()
            })

            packetService.sendPacketOutEntityTeleport(player, PacketOutEntityTeleport().also {
                it.entityId = this.entityId
                it.target = position.clone().addRelativeDown(0.3).toLocation()
            })

            packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                it.entityId = this.entityId
                it.armorStandHeadRotation = convertPitchToEulerAngle(position.pitch)
            })
        }
    }

    private fun convertPitchToEulerAngle(pitch: Double): EulerAngle {
        return EulerAngle(pitch, 0.0, 0.0)
    }
}
