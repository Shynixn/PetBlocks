package com.github.shynixn.petblocks.bukkit.service

import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.physicobject.api.MathComponentSettings
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.github.shynixn.mcutils.physicobject.api.PlayerComponentSettings
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.Pet
import com.github.shynixn.petblocks.bukkit.PetEntity
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.impl.PetEntityRenderComponent
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl
import com.google.inject.Inject
import org.bukkit.plugin.Plugin

class PetEntityFactoryImpl @Inject constructor(
    private val physicObjectService: PhysicObjectService,
    private val plugin: Plugin,
    private val petActionExecutionService: PetActionExecutionService
) : PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(pet: Pet, meta: PetMeta, template: PetTemplate): PetEntity {
        val location = pet.location
        val mathComponentSettings = MathComponentSettings()
        mathComponentSettings.gravityAbsolute = 1.0
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathComponentSettings)

        val playerComponentSettings = PlayerComponentSettings()
        val playerComponent = PlayerComponent(mathPhysicComponent, playerComponentSettings)

        val armorStandEntityId = physicObjectService.createNewEntityId()

        val armorstandEntityComponent =
            PetEntityRenderComponent(mathPhysicComponent, playerComponent, armorStandEntityId, meta, pet.player)
        val petEntity = PetEntityImpl(
            mathPhysicComponent,
            playerComponent,
            armorstandEntityComponent,
            plugin,
            petActionExecutionService,
            pet,
            template,
            meta
        )

        physicObjectService.addPhysicObject(petEntity)
        return petEntity
    }
}
