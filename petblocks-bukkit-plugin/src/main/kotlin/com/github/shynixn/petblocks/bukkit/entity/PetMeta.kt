package com.github.shynixn.petblocks.bukkit.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.shynixn.mcutils.common.Item
import com.github.shynixn.mcutils.common.Vector3d

/**
 * Persistence data.
 */
class PetMeta {
    /**
     * Identifier of the pet.
     */
    var name: String = "?"

    /**
     * DisplayName of the pet.
     */
    var displayName: String = "?"

    /**
     * Used template.
     */
    var template: String = "?"

    /**
     * Last persisted location of the pet.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var lastStoredLocation: Vector3d = Vector3d()

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
    var headItem : Item = Item("HEAD")
}
