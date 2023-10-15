package com.github.shynixn.petblocks.contract

import org.bukkit.entity.Player

interface PlaceHolderService {
    /**
     * Replaces incoming strings with the escaped version.
     */
    fun replacePlaceHolders(player: Player, input: String, pet: Pet?): String
}

