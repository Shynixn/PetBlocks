package com.github.shynixn.petblocks.impl.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderProvider
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import org.bukkit.entity.Player
import java.util.*

class PetBlocksPlaceHolderProvider : PlaceHolderProvider {
    private val simplePlaceHolderFunctions = HashMap<PlaceHolder, ((Player) -> String)>()
    private val petPlaceHolderFunctions = HashMap<PlaceHolder, ((Pet) -> String)>()
    private val placeHolders = HashMap<String, PlaceHolder>()
    private val mapper: ObjectMapper = ObjectMapper()
    companion object{
        val petKey = "pet"
    }

    init {
        for (placeHolder in PlaceHolder.values()) {
            placeHolders[placeHolder.fullPlaceHolder] = placeHolder
        }

        val playerOwnerNameFun = { player: Player -> player.name }
        val playerDisplayNameFun = { player: Player -> player.displayName }
        val playerLocationWorldFun = { player: Player -> player.location.world!!.name }

        // Owner Player
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_NAME] = playerOwnerNameFun
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_DISPLAYNAME] = playerDisplayNameFun
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_WORLD] = playerLocationWorldFun
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_X] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.x) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_Y] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.y) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_Z] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.z) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_YAW] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.yaw) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_LOCATION_PITCH] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.pitch) }
        simplePlaceHolderFunctions[PlaceHolder.PLAYER_OWNER_ITEMMAINHAND_TYPE] = { player ->
            "minecraft:" + player.inventory.itemInMainHand.type.name.lowercase()
        }
        // Event Player
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_NAME] = playerOwnerNameFun
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_DISPLAYNAME] = playerDisplayNameFun
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_WORLD] = playerLocationWorldFun
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_X] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.x) }
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_Y] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.y) }
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_Z] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.z) }
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_YAW] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.yaw) }
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_PITCH] =
            { player -> String.format(Locale.ENGLISH, "%.2f", player.location.pitch) }
        simplePlaceHolderFunctions[PlaceHolder.EVENT_PLAYER_OWNER_ITEMMAINHAND_TYPE] = { player ->
            "minecraft:" + player.inventory.itemInMainHand.type.name.lowercase()
        }
        // Pet
        petPlaceHolderFunctions[PlaceHolder.PET_NAME] = { pet -> pet.name }
        petPlaceHolderFunctions[PlaceHolder.PET_DISPLAYNAME] = { pet -> pet.displayName }
        petPlaceHolderFunctions[PlaceHolder.PET_DISTANCETOOWNER] =
            { pet -> calculatePetDistanceToOwner(pet).toString() }
        petPlaceHolderFunctions[PlaceHolder.PET_ISSPAWNED] = { pet -> pet.isSpawned.toString() }
        petPlaceHolderFunctions[PlaceHolder.PET_TEMPLATE] = { pet -> pet.template.name }
        petPlaceHolderFunctions[PlaceHolder.PET_VISIBILITY] = { pet -> pet.visibility.name }
        petPlaceHolderFunctions[PlaceHolder.PET_MOUNTED] = { pet -> pet.isMounted().toString() }
        petPlaceHolderFunctions[PlaceHolder.PET_LOOP] = { pet -> pet.loop }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_WORLD] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.world!!.name) }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_X] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.x) }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_Y] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.y) }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_Z] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.z) }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_YAW] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.yaw) }
        petPlaceHolderFunctions[PlaceHolder.PET_LOCATION_PITCH] =
            { pet -> String.format(Locale.ENGLISH, "%.2f", pet.location.pitch) }
        petPlaceHolderFunctions[PlaceHolder.PET_ITEM_TYPE] = { pet -> pet.headItemStack.type.name }
        petPlaceHolderFunctions[PlaceHolder.PET_ITEM_NBT] = { pet ->
            if (pet.headItem.nbt == null) {
                ""
            } else {
                pet.headItem.nbt!!
            }
        }
        petPlaceHolderFunctions[PlaceHolder.PET_ITEM_COMPONENT] = { pet ->
            if (pet.headItem.component == null) {
                ""
            } else {
                pet.headItem.component!!
            }
        }
        petPlaceHolderFunctions[PlaceHolder.PET_HEAD_BASE64] = { pet ->
            if (!pet.headItem.skinBase64.isNullOrBlank()) {
                pet.headItem.skinBase64!!
            } else {
                ""
            }
        }
        petPlaceHolderFunctions[PlaceHolder.PET_ISBREAKINGBLOCK] = { pet -> pet.isBreakingBlock().toString() }
        petPlaceHolderFunctions[PlaceHolder.PET_BLOCKINFRONT_TYPE] = { pet ->
            val block = pet.getBlockInFrontOf()
            if (block == null) {
                "minecraft:air"
            } else {
                "minecraft:" + block.type.name.lowercase()
            }
        }
    }

    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun resolvePlaceHolder(player: Player, input: String, parameters: Map<String, Any>): String {
        val locatedPlaceHolders = HashMap<PlaceHolder, String>()
        val characterCache = StringBuilder()
        val pet = parameters[petKey] as Pet?

        for (character in input) {
            characterCache.append(character)

            if (character == '%') {
                val evaluatedPlaceHolder = characterCache.toString()
                if (placeHolders.containsKey(evaluatedPlaceHolder)) {
                    val placeHolder = placeHolders[evaluatedPlaceHolder]!!
                    if (!locatedPlaceHolders.containsKey(placeHolder)) {
                        if (pet != null && petPlaceHolderFunctions.containsKey(placeHolder)) {
                            val result = petPlaceHolderFunctions[placeHolder]!!.invoke(pet)
                            locatedPlaceHolders[placeHolder] = result
                        } else if (simplePlaceHolderFunctions.containsKey(placeHolder)) {
                            val result = if (pet != null && placeHolder.name.startsWith("PLAYER_OWNER_")) {
                                simplePlaceHolderFunctions[placeHolder]!!.invoke(pet.player)
                            } else {
                                simplePlaceHolderFunctions[placeHolder]!!.invoke(player)
                            }

                            locatedPlaceHolders[placeHolder] = result
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

        if (pet != null && output.contains("%petblocks_js")) {
            for (key in pet.javaScriptMemory.keys) {
                val value = pet.javaScriptMemory[key]!!
                if (key.contains("json")) {
                    val parsedJsonObject = mapper.readValue(value, Map::class.java)
                    for (innerKey in parsedJsonObject.keys) {
                        output =
                            output.replace("%petblocks_js_${key}_${innerKey}%", parsedJsonObject[innerKey].toString())
                    }
                } else {
                    output = output.replace("%petblocks_js_${key}%", value)
                }
            }
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
