package com.github.shynixn.petblocks.impl.service

import checkForPluginMainThread
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.petblocks.contract.*
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.github.shynixn.petblocks.impl.physic.ArmorstandEntityComponent
import com.github.shynixn.petblocks.impl.physic.MathComponent
import com.github.shynixn.petblocks.impl.physic.MoveToTargetComponent
import com.github.shynixn.petblocks.impl.physic.PlayerComponent
import org.bukkit.plugin.Plugin

class PetEntityFactoryImpl (
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val pathfinderService: PathfinderService,
    private val placeHolderService: PlaceHolderService,
    private val packetService: PacketService,
    private val rayTracingService: RayTracingService,
    private val configurationService: ConfigurationService,
    private val breakBlockService: BreakBlockService,
    private val itemService: ItemService
) : PetEntityFactory {
    private val entities = HashMap<Int, PetEntityImpl>()

    /**
     * Tries to locate a pet entity by ids.
     */
    override fun findPetEntityById(id: Int): PetEntityImpl? {
        return entities[id]
    }

    /**
     * Removes pet entities.
     */
    override fun removePetEntityById(id: Int) {
        entities.remove(id)
    }

    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(pet: Pet, meta: PetMeta): PetEntityImpl {
        checkForPluginMainThread()

        if (meta.lastStoredLocation.world == null) {
            // On First spawn
            meta.lastStoredLocation = pet.player.location.toVector3d().addRelativeFront(3.0)
        }

        val location = pet.location
        val mathPhysicComponent = MathComponent(location.toVector3d(), meta.physics, rayTracingService)

        val playerComponent = PlayerComponent(mathPhysicComponent, pet = pet)
        val armorStandEntityId = packetService.getNextEntityId()

        val armorstandEntityComponent =
            ArmorstandEntityComponent(
                mathPhysicComponent,
                packetService,
                playerComponent,
                meta,
                placeHolderService,
                itemService,
                pet,
                plugin,
                armorStandEntityId
            )
        val moveToTargetComponent = MoveToTargetComponent(mathPhysicComponent)

        val clickCoolDown = configurationService.findValue<Int>("pet.clickCoolDownMs")
        val pathfinderCubeX = configurationService.findValue<Double>("pet.pathFinderCube.x")
        val pathfinderCubeY = configurationService.findValue<Double>("pet.pathFinderCube.y")
        val pathfinderCubeZ = configurationService.findValue<Double>("pet.pathFinderCube.z")
        val visualizePath = configurationService.findValue<Boolean>("pet.showPathfinder")
        val rideUpdateMs = configurationService.findValue<Int>("pet.ridePositionUpdateMs")

        val petEntity = PetEntityImpl(
            mathPhysicComponent,
            moveToTargetComponent,
            playerComponent,
            armorstandEntityComponent,
            plugin,
            pet,
            meta,
            packetService,
            pathfinderService,
            petActionExecutionService,
            breakBlockService,
            rayTracingService,
            clickCoolDown.toLong(),
            Vector3d(null, pathfinderCubeX, pathfinderCubeY, pathfinderCubeZ),
            visualizePath,
            rideUpdateMs
        )

        entities[armorStandEntityId] = petEntity
        return petEntity
    }

    override fun close() {
        entities.clear()
    }
}
