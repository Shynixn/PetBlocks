package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mcutils.common.physic.AIComponent
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.petblocks.contract.*
import com.github.shynixn.petblocks.entity.MathSettings
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.impl.PetEntityImpl
import com.github.shynixn.petblocks.impl.ai.DoNothingAction
import com.github.shynixn.petblocks.impl.ai.IdleAction
import com.github.shynixn.petblocks.impl.ai.MoveToOwnerAction
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
    private val physicObjectDispatcher: PhysicObjectDispatcher
) : PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(pet: Pet, meta: PetMeta, template: PetTemplate): PetEntityImpl {
        if (meta.lastStoredLocation.world == null) {
            // On First spawn
            meta.lastStoredLocation = pet.player.location.toVector3d()
        }

        val location = pet.location
        val mathComponentSettings = MathSettings()
        // Needed that pets can work stairs.
        mathComponentSettings.rayTraceYOffset = 3.0
        mathComponentSettings.gravityAbsolute = 0.1
        mathComponentSettings.groundResistanceRelative = 0.0 // Otherwise riding does not work.
        mathComponentSettings.groundResistanceAbsolute = 0.0
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathComponentSettings, rayTracingService)

        val playerComponent = PlayerComponent(mathPhysicComponent)
        val armorStandEntityId = entityService.createNewEntityId()

        val armorstandEntityComponent =
            ArmorstandEntityComponent(mathPhysicComponent, packetService, playerComponent, meta, armorStandEntityId)

        val idleAction = IdleAction()
        val doNothingAction = DoNothingAction(meta)
        val moveToOwnerAction = MoveToOwnerAction(pathfinderService)
        val aiComponent = AIComponent(arrayListOf(idleAction, moveToOwnerAction, doNothingAction))

        val moveToTargetComponent = MoveToTargetComponent(mathPhysicComponent, 0.5)

        val petEntity = PetEntityImpl(
            mathPhysicComponent,
            moveToTargetComponent,
            playerComponent,
            armorstandEntityComponent,
            plugin,
            petActionExecutionService,
            pet,
            template,
            meta,
            placeHolderService,
            packetService,
            physicObjectDispatcher,
            aiComponent
        )

        physicObjectService.addPhysicObject(petEntity)
        return petEntity
    }
}
