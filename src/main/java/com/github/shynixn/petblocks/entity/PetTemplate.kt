package com.github.shynixn.petblocks.entity

import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.mcutils.common.repository.Element
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility

class PetTemplate : Element {
    /**
     * Unique Identifier.
     */
    override var name: String = "template"

    /**
     * Hashcode of the template file.
     */
    var fileHashCode: String = ""

    /**
     * DisplayName of the pet.
     */
    var displayName: String = ""

    /**
     * Is the pet currently spawned.
     */
    var isSpawned: Boolean = false

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
    var item: Item = Item("HEAD")

    /**
     * All RightClick actions.
     */
    var rightClickDefinition: PetActionDefinition = PetActionDefinition()

    /**
     * All leftclick actions.
     */
    var leftClickDefinition: PetActionDefinition = PetActionDefinition()

    /**
     * All loop actions.
     */
    var loopDefinition: PetActionDefinition = PetActionDefinition()

    /**
     * A sneak definition.
     */
    var sneakDefinition: PetActionDefinition = PetActionDefinition()
}
