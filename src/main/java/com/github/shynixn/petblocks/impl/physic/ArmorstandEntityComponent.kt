package com.github.shynixn.petblocks.impl.physic

import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.physic.PhysicComponent
import com.github.shynixn.mcutils.packet.api.*
import com.github.shynixn.mcutils.packet.api.meta.enumeration.ArmorSlotType
import com.github.shynixn.mcutils.packet.api.meta.enumeration.EntityType
import com.github.shynixn.mcutils.packet.api.packet.*
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.EulerAngle

class ArmorstandEntityComponent(
    physicsComponent: MathComponent,
    private val packetService: PacketService,
    private val playerComponent: PlayerComponent,
    private val petMeta: PetMeta,
    private val placeHolderService: PlaceHolderService,
    private val pet: Pet,
    val entityId: Int,
) : PhysicComponent {
    init {
        playerComponent.onSpawnMinecraft.add { player, location -> onPlayerSpawn(player, location) }
        playerComponent.onRemoveMinecraft.add { player, _ -> onPlayerRemove(player) }
        physicsComponent.onPostPositionChange.add { position, motion -> onPositionChange(position, motion) }
    }

    private fun onPlayerSpawn(player: Player, location: Location) {
        packetService.sendPacketOutEntitySpawn(player, PacketOutEntitySpawn().also {
            it.entityId = this.entityId
            it.entityTypeRaw = petMeta.entityType
            it.target = location.toVector3d().addRelativeUp(petMeta.physics.groundOffset).toLocation()
        })

        updateEquipment(player)
        updateMetaData(player)
        updateRidingState(player)
    }

    private fun onPlayerRemove(player: Player) {
        val outer = this
        packetService.sendPacketOutEntityDestroy(player, PacketOutEntityDestroy().also {
            it.entityIds = listOf(outer.entityId)
        })
    }

    fun updateEquipment(player: Player) {
        val itemStack = petMeta.headItem.toItemStack()
        packetService.sendPacketOutEntityEquipment(player, PacketOutEntityEquipment().also {
            it.entityId = this.entityId
            it.items = listOf(Pair(ArmorSlotType.HELMET, itemStack))
        })
    }

    fun updateMetaData(player: Player) {
        packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
            it.entityId = this.entityId
            it.isArmorstandSmall = true
            it.isInvisible = !petMeta.isEntityVisible
            it.customNameVisible = true
            it.customname = placeHolderService.replacePlaceHolders(pet.player, petMeta.displayName, pet)
        })
    }

    fun updateRidingState(player: Player) {
        val owner = pet.player

        if (petMeta.visibility == PetVisibility.OWNER && player != owner) {
            return
        }

        val ridingState = petMeta.ridingState

        if (ridingState == PetRidingState.NO) {
            // Remove ground and fly
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = entityId
            })
            // Remove hat
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = owner.entityId
            })
        }

        if (ridingState == PetRidingState.HAT) {
            // Remove ground and fly
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = entityId
            })
            // Set pet as passenger of player
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = owner.entityId
                it.passengers = listOf(entityId)
            })
        }

        if (ridingState == PetRidingState.GROUND) {
            // Remove hat
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = owner.entityId
            })
            // Set pet as passenger of player
            packetService.sendPacketOutEntityMount(player, PacketOutEntityMount().also {
                it.entityId = entityId
                it.passengers = listOf(owner.entityId)
            })
        }
    }

    private fun onPositionChange(position: Vector3d, motion: Vector3d) {
        val players = playerComponent.visiblePlayers
        val parsedEntityType = EntityType.findType(this.petMeta.entityType)

        for (player in players) {
            packetService.sendPacketOutEntityVelocity(player, PacketOutEntityVelocity().also {
                it.entityId = this.entityId
                it.target = motion.toVector()
            })

            packetService.sendPacketOutEntityTeleport(player, PacketOutEntityTeleport().also {
                it.entityId = this.entityId
                it.target = position.clone().addRelativeUp(petMeta.physics.groundOffset).toLocation()
            })

            if (parsedEntityType != null && parsedEntityType == EntityType.ARMOR_STAND) {
                // It causes lag when sent to other entities.
                packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                    it.entityId = this.entityId
                    it.armorStandHeadRotation = convertPitchToEulerAngle(position.pitch)
                })
            } else {
                // Needed for some living entities other than armor stands.
                packetService.sendPacketOutEntityHeadRotation(player, PacketOutEntityHeadRotation().also {
                    it.entityId = this.entityId
                    it.yaw = position.yaw
                })
            }
        }
    }

    private fun convertPitchToEulerAngle(pitch: Double): EulerAngle {
        return EulerAngle(pitch, 0.0, 0.0)
    }
}
