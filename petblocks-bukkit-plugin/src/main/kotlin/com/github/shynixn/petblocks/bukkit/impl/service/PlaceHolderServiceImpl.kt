package com.github.shynixn.petblocks.bukkit.impl.service

import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PlaceHolderService
import com.github.shynixn.petblocks.bukkit.entity.PlaceHolder
import org.bukkit.entity.Player

class PlaceHolderServiceImpl : PlaceHolderService {
    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun replacePlaceHolders(player: Player, input: String): String {
        val output = input.replace(PlaceHolder.PLAYER_NAME.text, player.name)
            .replace(PlaceHolder.PLAYER_DISPLAYNAME.text, player.displayName)
        return output
    }

    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun replacePetPlaceHolders(player: Player, pet: Pet, input: String): String {
        val output = replacePlaceHolders(player, input).replace(PlaceHolder.PET_NAME.text, pet.name)
            .replace(PlaceHolder.PET_DISPLAYNAME.text, pet.displayName)
        return output
    }
}
