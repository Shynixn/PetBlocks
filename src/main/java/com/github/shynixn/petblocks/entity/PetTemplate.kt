package com.github.shynixn.petblocks.entity

import com.fasterxml.jackson.annotation.JsonProperty
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
    @JsonProperty("pet.displayName")
    var displayName: String = ""

    /**
     * Is the pet initially spawned.
     */
    @JsonProperty("pet.isSpawned")
    var isSpawned: Boolean = false

    /**
     * Visibility state.
     */
    @JsonProperty("pet.visibility")
    var visibility: PetVisibility = PetVisibility.ALL

    /**
     * Riding state.
     */
    @JsonProperty("pet.ridingState")
    var ridingState: PetRidingState = PetRidingState.NO

    /**
     * The item the pet is wearing.
     */
    @JsonProperty("pet.item")
    var item: Item = Item()

    /**
     * Physic settings.
     */
    @JsonProperty("pet.physics")
    var physics: PhysicSettings = PhysicSettings()

    /**
     * Loops.
     */
    var loops: HashMap<String, PetActionDefinition> = hashMapOf()
}
