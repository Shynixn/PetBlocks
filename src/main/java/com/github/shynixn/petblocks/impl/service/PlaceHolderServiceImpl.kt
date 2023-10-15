package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import org.bukkit.entity.Player

class PlaceHolderServiceImpl : PlaceHolderService {
    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun replacePlaceHolders(player: Player, input: String, pet: Pet?): String {
        var output = input.replace(PlaceHolder.PLAYER_NAME.fullPlaceHolder, player.name)
            .replace(PlaceHolder.PLAYER_DISPLAYNAME.fullPlaceHolder, player.displayName)

        if (pet != null) {
            output = output.replace(PlaceHolder.PET_NAME.fullPlaceHolder, pet.name)
                .replace(PlaceHolder.PET_DISPLAYNAME.fullPlaceHolder, pet.displayName)
        }

        return output
    }
}
