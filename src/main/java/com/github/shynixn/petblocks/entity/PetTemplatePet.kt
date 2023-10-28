package com.github.shynixn.petblocks.entity

import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility

class PetTemplatePet {
    /**
     * DisplayName of the pet.
     */
    var displayName: String = ""

    /**
     * Is the pet initially spawned.
     */
    var spawned: Boolean = false

    /**
     * Visibility state.
     */
    var visibility: PetVisibility = PetVisibility.ALL

    /**
     * Riding state.
     */
    var ridingState: PetRidingState = PetRidingState.NO

    /**
     * The item the pet is wearing.
     */
    var item: Item = Item()

    /**
     * Default loop.
     */
    var loop: String = ""

    /**
     * Physic settings.
     */
    var physics: PhysicSettings = PhysicSettings()
}
