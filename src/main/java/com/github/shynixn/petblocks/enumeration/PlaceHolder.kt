package com.github.shynixn.petblocks.enumeration

enum class PlaceHolder(val fullPlaceHolder : String) {
    // Common PlaceHolders.
    PLAYER_NAME( "%petblocks_owner_name%"),
    PLAYER_DISPLAYNAME("%petblocks_owner_displayName%"),

    // Pet PlaceHolders
    PET_NAME("%petblocks_pet_name%"),
    PET_DISPLAYNAME( "%petblocks_pet_displayName%"),
    PET_DISTANCETOOWNER( "%petblocks_pet_distanceToOwner%")
}
