package com.github.shynixn.petblocks.bukkit.contract

import org.bukkit.entity.Player

interface PlaceHolderService {
    /**
     * Replaces incoming strings with the escaped version.
     */
    fun replacePlaceHolders(player: Player, input: String): String

    /**
     * Replaces incoming strings with the escaped version.
     */
    fun replacePetPlaceHolders(player: Player, pet: Pet, input: String) : String
}
