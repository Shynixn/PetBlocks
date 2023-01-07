package com.github.shynixn.petblocks.bukkit.service

import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.physicobject.api.MathComponentSettings
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.github.shynixn.mcutils.physicobject.api.PlayerComponentSettings
import com.github.shynixn.mcutils.physicobject.api.component.AIComponent
import com.github.shynixn.mcutils.physicobject.api.component.MoveToTargetComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.Pet
import com.github.shynixn.petblocks.bukkit.PetEntity
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.impl.*
import com.github.shynixn.petblocks.bukkit.impl.ai.DoNothingAction
import com.github.shynixn.petblocks.bukkit.impl.ai.IdleAction
import com.github.shynixn.petblocks.bukkit.impl.ai.MoveToOwnerAction
import com.google.inject.Inject
import org.bukkit.plugin.Plugin

class PetEntityFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService,
    private val pathfinderService: PathfinderService,
    private val placeHolderService: PlaceHolderService
) : PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(pet: Pet, meta: PetMeta, template: PetTemplate): PetEntity {
        val location = pet.location
        val mathComponentSettings = MathComponentSettings()
        // Needed that pets can work stairs.
        mathComponentSettings.rayTraceYOffset = 3.0
        mathComponentSettings.gravityAbsolute = 0.1
        mathComponentSettings.groundResistanceRelative = 0.0 // Otherwise riding does not work.
        mathComponentSettings.groundResistanceAbsolute = 0.0
        val mathPhysicComponent = PetMathComponent(location.toVector3d(), mathComponentSettings)

        val playerComponentSettings = PlayerComponentSettings()
        val playerComponent = PlayerComponent(mathPhysicComponent, playerComponentSettings)

        val armorStandEntityId = physicObjectService.createNewEntityId()

        val armorstandEntityComponent =
            PetEntityRenderComponent(mathPhysicComponent, playerComponent, armorStandEntityId, meta, pet,placeHolderService, pet.player)

        val moveToTargetComponent = MoveToTargetComponent(mathPhysicComponent, 0.2)

        val idleAction = IdleAction()
        val doNothingAction = DoNothingAction(meta)
        val moveToOwnerAction = MoveToOwnerAction(pathfinderService)
        val aiComponent = AIComponent(arrayListOf(idleAction, moveToOwnerAction, doNothingAction))

        val petEntity = PetEntityImpl(
            mathPhysicComponent,
            playerComponent,
            armorstandEntityComponent,
            plugin,
            petActionExecutionService,
            pet,
            template,
            meta,
            placeHolderService,
            moveToTargetComponent,
            aiComponent
        )

        physicObjectService.addPhysicObject(petEntity)
        return petEntity
    }
}
