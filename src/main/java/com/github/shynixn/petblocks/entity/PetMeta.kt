package com.github.shynixn.petblocks.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility

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
     * Used to check which template version has been used.
     */
    var templateHashCode: String = ""

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
    var headItem: Item = Item("minecraft:player_head,HEAD")

    /**
     * Last persisted location of the pet.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty", "direction"))
    var lastStoredLocation: Vector3d = Vector3d()
}
