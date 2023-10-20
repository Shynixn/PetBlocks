package com.github.shynixn.petblocks.enumeration

enum class PlaceHolder(val simplePlaceHolder: String, val fullPlaceHolder : String) {
    // Common PlaceHolders.
    PLAYER_NAME("player_name", "%petblocks_player_name%"),
    PLAYER_DISPLAYNAME("player_displayName", "%petblocks_player_displayName%"),

    // Pet PlaceHolders
    PET_NAME("pet_name", "%petblocks_pet_name%"),
    PET_DISPLAYNAME("pet_displayName", "%petblocks_pet_displayName%"),
    PET_DISTANCETOOWNER("pet_distanceToOwner", "%petblocks_pet_distanceToOwner%")
}
