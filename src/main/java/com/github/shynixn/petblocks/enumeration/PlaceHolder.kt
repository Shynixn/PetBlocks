package com.github.shynixn.petblocks.enumeration

enum class PlaceHolder(val simplePlaceHolder: String, val fullPlaceHolder : String) {
    // Common PlaceHolders.
    PLAYER_NAME("playerName", "%petblocks_player_name%"),
    PLAYER_DISPLAYNAME("playerDisplayName", "%petblocks_playerDisplayName%"),

    // Pet PlaceHolders
    PET_NAME("petName", "%petblocks_petName%"),
    PET_DISPLAYNAME("petDisplayName", "%petblocks_petDisplayName%"),
}
