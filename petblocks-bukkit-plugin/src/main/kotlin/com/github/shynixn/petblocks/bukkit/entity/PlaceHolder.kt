package com.github.shynixn.petblocks.bukkit.entity

enum class PlaceHolder(val text: String, val requiresPet: Boolean) {
    // Common PlaceHolders.
    PLAYER_NAME("%petblocks_player_name%", false),
    PLAYER_DISPLAYNAME("%petblocks_player_displayName%", false),

    // Pet PlaceHolders
    PET_NAME("%petblocks_pet_name%", true),
    PET_DISPLAYNAME("%petblocks_pet_displayName%", true)
}
