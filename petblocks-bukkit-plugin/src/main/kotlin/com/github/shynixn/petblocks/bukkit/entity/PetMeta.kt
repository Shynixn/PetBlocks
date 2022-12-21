package com.github.shynixn.petblocks.bukkit.entity

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.github.shynixn.mcutils.common.Vector3d
import com.github.shynixn.mcutils.database.api.PlayerData

/**
 * Persistence data.
 */
class PetMeta : PlayerData {
    /**
     *  Marker if this player data has been stored before.
     */
    override var isPersisted: Boolean = false

    /**
     * Identifier of the pet.
     */
    var name: String = "?"

    /**
     * DisplayName of the pet.
     */
    var displayName: String = "?"

    /**
     * Last persisted location of the pet.
     */
    @JsonIgnoreProperties(value = arrayOf("blockX", "blockY", "blockZ", "empty"))
    var lastStoredLocation: Vector3d = Vector3d()

    /**
     * Visibility state.
     */
    var visibility: PetVisibility = PetVisibility.ALL
}
