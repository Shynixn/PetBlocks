package com.github.shynixn.petblocks.entity

import com.github.shynixn.fasterxml.jackson.annotation.JsonIgnore
import com.github.shynixn.mcutils.database.api.PlayerData
import java.util.UUID

class PlayerInformation : PlayerData {
    /**
     * All stored pets.
     */
    var pets: MutableList<PetMeta> = arrayListOf()

    /**
     * Last selected pet.
     */
    var selectedPet: String? = null

    /**
     * All uuids which where cached by this player.
     * Allows dispose watched pets.
     */
    @JsonIgnore()
    var retrievedUuids = HashSet<UUID>()

    /**
     *  Marker if this player data has been stored before.
     */
    override var isPersisted: Boolean = false

    /**
     * Name of the player.
     */
    override var playerName: String = ""

    /**
     * UUID of the player.
     */
    override var playerUUID: String = ""
}
