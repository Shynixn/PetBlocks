package com.github.shynixn.petblocks.bukkit.entity

import com.github.shynixn.mcutils.database.api.PlayerData

class PlayerInformation : PlayerData {
    /**
     * All stored pets.
     */
    var pets: MutableList<PetMeta> = arrayListOf()

    /**
     *  Marker if this player data has been stored before.
     */
    override var isPersisted: Boolean = false
}
