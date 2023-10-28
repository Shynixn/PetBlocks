package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetActionExecutionService
import com.github.shynixn.petblocks.contract.PetEntityFactory
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import com.google.inject.Inject
import org.bukkit.plugin.Plugin

class PetEntityFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val pathfinderService: PathfinderService,
    private val placeHolderService: PlaceHolderService,
    private val entityService: EntityService,
    private val packetService: PacketService,
    private val rayTracingService: RayTracingService,
    private val physicObjectDispatcher: PhysicObjectDispatcher,
    private val configurationService: ConfigurationService
) : PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(pet: Pet, meta: PetMeta): PetEntityImpl {
        if (meta.lastStoredLocation.world == null) {
            // On First spawn
            meta.lastStoredLocation = pet.player.location.toVector3d()
        }

        val location = pet.location
        val mathPhysicComponent = MathComponent(location.toVector3d(), meta.physics, rayTracingService)

        val playerComponent = PlayerComponent(mathPhysicComponent, pet = pet)
        val armorStandEntityId = entityService.createNewEntityId()

        val armorstandEntityComponent =
            ArmorstandEntityComponent(
                mathPhysicComponent,
                packetService,
                playerComponent,
                meta,
                placeHolderService,
                pet,
                armorStandEntityId
            )
        val moveToTargetComponent = MoveToTargetComponent(mathPhysicComponent)

        val clickCoolDown = configurationService.findValue<Int>("pet.clickCoolDownMs")
        val pathfinderCubeX = configurationService.findValue<Double>("pet.pathFinderCube.x")
        val pathfinderCubeY = configurationService.findValue<Double>("pet.pathFinderCube.y")
        val pathfinderCubeZ = configurationService.findValue<Double>("pet.pathFinderCube.z")
        val visualizePath = configurationService.findValue<Boolean>("pet.showPathfinder")

        val petEntity = PetEntityImpl(
            mathPhysicComponent,
            moveToTargetComponent,
            playerComponent,
            armorstandEntityComponent,
            plugin,
            pet,
            meta,
            placeHolderService,
            packetService,
            physicObjectDispatcher,
            pathfinderService,
            petActionExecutionService,
            clickCoolDown.toLong(),
            Vector3d(null, pathfinderCubeX, pathfinderCubeY, pathfinderCubeZ),
            visualizePath
        )

        physicObjectService.addPhysicObject(petEntity)
        return petEntity
    }
}
