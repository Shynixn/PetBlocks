package com.github.shynixn.petblocks.impl.physic

import checkForPluginMainThread
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.meta.enumeration.ArmorSlotType
import com.github.shynixn.mcutils.packet.api.meta.enumeration.EntityType
import com.github.shynixn.mcutils.packet.api.packet.*
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.util.EulerAngle

class ArmorstandEntityComponent(
    physicsComponent: MathComponent,
    private val packetService: PacketService,
    private val playerComponent: PlayerComponent,
    private val petMeta: PetMeta,
    private val placeHolderService: PlaceHolderService,
    private val itemService: ItemService,
    private val pet: Pet,
    private val plugin: Plugin,
    val entityId: Int,
) {
    private var lastVisibleVector3d = Vector3d()
    private val voidFallingLimit = plugin.config.getDouble("pet.deSpawnAtYAxe")

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
        val itemStack = itemService.toItemStack(petMeta.headItem)
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
            it.customname = placeHolderService.resolvePlaceHolder(petMeta.displayName, pet.player)
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

        if (position.y < voidFallingLimit) {
            // Protection for falling into the void.
            pet.unmount()
            pet.remove()
            return
        }

        val hasDistanceChanged = lastVisibleVector3d.distance(position) > 0.1
        val hasYawChanged = position.yaw.toFloat() != lastVisibleVector3d.yaw.toFloat()
        val hasPitchChanged = position.pitch.toFloat() != lastVisibleVector3d.pitch.toFloat()
        lastVisibleVector3d = position.copy()

        for (player in players) {
            if (hasDistanceChanged || hasYawChanged) {
                packetService.sendPacketOutEntityVelocity(player, PacketOutEntityVelocity().also {
                    it.entityId = this.entityId
                    it.target = motion.toVector()
                })

                packetService.sendPacketOutEntityTeleport(player, PacketOutEntityTeleport().also {
                    it.entityId = this.entityId
                    it.target = position.copy().addRelativeUp(petMeta.physics.groundOffset).toLocation()
                })
            }

            if (parsedEntityType != null && parsedEntityType == EntityType.ARMOR_STAND) {
                if (hasPitchChanged) {
                    // It causes lag when sent to other entities.
                    packetService.sendPacketOutEntityMetadata(player, PacketOutEntityMetadata().also {
                        it.entityId = this.entityId
                        it.armorStandHeadRotation = convertPitchToEulerAngle(position.pitch)
                    })
                }
            } else {
                if (hasYawChanged && !pet.isRiding()) {
                    // Needed for some living entities other than armor stands.
                    packetService.sendPacketOutEntityHeadRotation(player, PacketOutEntityHeadRotation().also {
                        it.entityId = this.entityId
                        it.yaw = position.yaw
                    })
                }
            }
        }
    }

    private fun convertPitchToEulerAngle(pitch: Double): EulerAngle {
        return EulerAngle(pitch, 0.0, 0.0)
    }
}
