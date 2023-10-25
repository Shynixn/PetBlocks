package com.github.shynixn.petblocks.enumeration

enum class PlaceHolder(val fullPlaceHolder : String) {
    // Common PlaceHolders.
    PLAYER_NAME( "%petblocks_owner_name%"),
    PLAYER_DISPLAYNAME("%petblocks_owner_displayName%"),
    PLAYER_OWNER_LOCATION_WORLD("%petblocks_owner_locationWorld%"),
    PLAYER_OWNER_LOCATION_X("%petblocks_owner_locationX%"),
    PLAYER_OWNER_LOCATION_Y("%petblocks_owner_locationY%"),
    PLAYER_OWNER_LOCATION_Z("%petblocks_owner_locationZ%"),
    PLAYER_OWNER_LOCATION_YAW("%petblocks_owner_locationYaw%"),
    PLAYER_OWNER_LOCATION_PITCH("%petblocks_owner_locationPitch%"),

    // Pet PlaceHolders
    PET_NAME("%petblocks_pet_name%"),
    PET_DISPLAYNAME( "%petblocks_pet_displayName%"),
    PET_DISTANCETOOWNER( "%petblocks_pet_distanceToOwner%")
}
