package com.github.shynixn.petblocks.bukkit.impl.service

import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.physicobject.api.MathComponentSettings
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.github.shynixn.mcutils.physicobject.api.PlayerComponentSettings
import com.github.shynixn.mcutils.physicobject.api.component.MathComponent
import com.github.shynixn.mcutils.physicobject.api.component.PlayerComponent
import com.github.shynixn.petblocks.bukkit.contract.PetEntityFactory
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.impl.PetArmorstandEntityComponentImpl
import com.github.shynixn.petblocks.bukkit.impl.PetEntityImpl
import com.google.inject.Inject
import org.bukkit.Location
import org.bukkit.entity.Player

class PetEntityFactoryImpl @Inject constructor(private val physicObjectService: PhysicObjectService) :
    PetEntityFactory {
    /**
     * Creates a new pet entity.
     */
    override fun createPetEntity(location: Location, petMeta: PetMeta, owner: Player): PetEntityImpl {
        val mathComponentSettings = MathComponentSettings()
        mathComponentSettings.gravityAbsolute = 1.0
        val mathPhysicComponent = MathComponent(location.toVector3d(), mathComponentSettings)

        val playerComponentSettings = PlayerComponentSettings()
        val playerComponent = PlayerComponent(mathPhysicComponent, playerComponentSettings)

        val armorStandEntityId = physicObjectService.createNewEntityId()

        val armorstandEntityComponent =
            PetArmorstandEntityComponentImpl(mathPhysicComponent, playerComponent, armorStandEntityId, petMeta, owner)
        val petEntity = PetEntityImpl(mathPhysicComponent, playerComponent, armorstandEntityComponent)

        physicObjectService.addPhysicObject(petEntity)
        return petEntity
    }
}
