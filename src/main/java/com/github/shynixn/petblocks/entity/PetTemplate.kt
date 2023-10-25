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
    var item: Item = Item("minecraft:player_head,HEAD")

    /**
     * Loops.
     */
    var loops: HashMap<String, PetActionDefinition> = hashMapOf()
}
