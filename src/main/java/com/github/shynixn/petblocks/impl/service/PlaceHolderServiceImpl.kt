package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class PlaceHolderServiceImpl : PlaceHolderService {
    private val simplePlaceHolderFunctions = HashMap<PlaceHolder, ((Player) -> String)>()
    private val petPlaceHolderFunctions = HashMap<PlaceHolder, ((Player, Pet) -> String)>()
    private val placeHolders = HashMap<String, PlaceHolder>()

    init {
        for (placeHolder in PlaceHolder.values()) {
            placeHolders.put(placeHolder.fullPlaceHolder, placeHolder)
        }

        simplePlaceHolderFunctions[PlaceHolder.PLAYER_NAME] = { player -> player.name }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_DISPLAYNAME] = { player -> player.displayName }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_WORLD] = { player -> player.location.world!!.name }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_X] = { player -> String.format(Locale.ENGLISH, "%.2f", player.location.x) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_Y] = { player -> String.format(Locale.ENGLISH, "%.2f", player.location.y) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_Z] = { player -> String.format(Locale.ENGLISH, "%.2f", player.location.z) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_YAW] = { player -> String.format(Locale.ENGLISH, "%.2f", player.location.yaw) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_PITCH] = { player -> String.format(Locale.ENGLISH, "%.2f", player.location.pitch) }

        petPlaceHolderFunctions[PlaceHolder.PET_NAME] = { player, pet -> pet.name }
        petPlaceHolderFunctions[PlaceHolder.PET_DISPLAYNAME] = { player, pet -> pet.displayName }
        petPlaceHolderFunctions[PlaceHolder.PET_DISTANCETOOWNER] = { player, pet -> calculatePetDistanceToOwner(pet).toString() }
    }

    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun replacePlaceHolders(player: Player, input: String, pet: Pet?): String {
        val locatedPlaceHolders = HashMap<PlaceHolder, String>()
        val characterCache = StringBuilder()

        for (character in input) {
            characterCache.append(character)

            if (character == '%') {
                val evaluatedPlaceHolder = characterCache.toString()
                if (placeHolders.containsKey(evaluatedPlaceHolder)) {
                    val placeHolder = placeHolders[evaluatedPlaceHolder]!!
                    if (!locatedPlaceHolders.containsKey(placeHolder)) {
                        if (pet != null && petPlaceHolderFunctions.containsKey(placeHolder)) {
                            val result = petPlaceHolderFunctions[placeHolder]!!.invoke(player, pet)
                            locatedPlaceHolders.put(placeHolder, result)
                        } else if (simplePlaceHolderFunctions.containsKey(placeHolder)) {
                            val result = simplePlaceHolderFunctions[placeHolder]!!.invoke(player)
                            locatedPlaceHolders.put(placeHolder, result)
                        }
                    }
                }

                characterCache.clear()
                characterCache.append(character)
            }
        }

        var output = input

        for (locatedPlaceHolder in locatedPlaceHolders.keys) {
            output = output.replace(locatedPlaceHolder.fullPlaceHolder, locatedPlaceHolders[locatedPlaceHolder]!!)
        }

        return output.translateChatColors()
    }



    private fun calculatePetDistanceToOwner(pet: Pet): Int {
        val playerLocation = pet.player.location
        val petLocation = pet.location

        if (playerLocation.world != petLocation.world) {
            return Int.MAX_VALUE
        }

        return playerLocation.distance(petLocation).toInt()
    }
}
