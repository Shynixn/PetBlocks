package com.github.shynixn.petblocks.impl.provider

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderProvider
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import org.bukkit.entity.Player
import java.util.*
import kotlin.collections.HashMap

class PetBlocksPlaceHolderProvider(
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>, private val petService: PetService
) : PlaceHolderProvider {
    private val playerPlaceHolderFunctions = HashMap<String, ((Player) -> String)>()
    private val petPlaceHolderFunctions = HashMap<String, ((Pet) -> String)>()
    private val selectedPetPlaceHolderFunctions = HashMap<String, ((Pet) -> String)>()

    private val mapper: ObjectMapper = ObjectMapper()

    companion object {
        val petKey = "pet"
    }

    init {
        // Owner Player
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_NAME) { player: Player -> player.name }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_DISPLAYNAME) { player: Player -> player.displayName }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_WORLD) { player: Player -> player.location.world!!.name }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_X) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.x
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_Y) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.y
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_Z) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.z
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_YAW) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.yaw
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_LOCATION_PITCH) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.pitch
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_ITEMMAINHAND_TYPE) { player -> "minecraft:" + player.inventory.itemInMainHand.type.name.lowercase() }
        registerPlayerPlaceHolder(PlaceHolder.PLAYER_OWNER_ISFLYING) { player -> player.isFlying.toString() }

        // Event Player
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_NAME) { player: Player -> player.name }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_DISPLAYNAME) { player: Player -> player.displayName }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_WORLD) { player: Player -> player.location.world!!.name }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_X) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.x
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_Y) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.y
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_Z) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.z
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_YAW) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.yaw
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_LOCATION_PITCH) { player ->
            String.format(
                Locale.ENGLISH, "%.2f", player.location.pitch
            )
        }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_ITEMMAINHAND_TYPE) { player -> "minecraft:" + player.inventory.itemInMainHand.type.name.lowercase() }
        registerPlayerPlaceHolder(PlaceHolder.EVENT_PLAYER_OWNER_ISFLYING) { player -> player.isFlying.toString() }

        // Pet
        registerPetPlaceHolder(PlaceHolder.PET_NAME) { pet -> pet.name }
        registerPetPlaceHolder(PlaceHolder.PET_DISPLAYNAME) { pet -> pet.displayName }
        registerPetPlaceHolder(PlaceHolder.PET_DISTANCETOOWNER) { pet -> calculatePetDistanceToOwner(pet).toString() }
        registerPetPlaceHolder(PlaceHolder.PET_ISSPAWNED) { pet -> pet.isSpawned.toString() }
        registerPetPlaceHolder(PlaceHolder.PET_TEMPLATE) { pet -> pet.template.name }
        registerPetPlaceHolder(PlaceHolder.PET_VISIBILITY) { pet -> pet.visibility.name }
        registerPetPlaceHolder(PlaceHolder.PET_MOUNTED) { pet -> pet.isMounted().toString() }
        registerPetPlaceHolder(PlaceHolder.PET_LOOP) { pet -> pet.loop }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_WORLD) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.world!!.name
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_X) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.x
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_Y) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.y
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_Z) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.z
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_YAW) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.yaw
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_LOCATION_PITCH) { pet ->
            String.format(
                Locale.ENGLISH, "%.2f", pet.location.pitch
            )
        }
        registerPetPlaceHolder(PlaceHolder.PET_ITEM_TYPE) { pet -> pet.headItemStack.type.name }
        registerPetPlaceHolder(PlaceHolder.PET_ITEM_DURABILITY) { pet -> pet.headItemStack.durability.toString() }
        registerPetPlaceHolder(PlaceHolder.PET_ITEM_NBT) { pet ->
            if (pet.headItem.nbt == null) {
                ""
            } else {
                pet.headItem.nbt!!
            }
        }
        registerPetPlaceHolder(PlaceHolder.PET_ITEM_COMPONENT) { pet ->
            if (pet.headItem.component == null) {
                ""
            } else {
                pet.headItem.component!!
            }
        }
        registerPetPlaceHolder(PlaceHolder.PET_HEAD_BASE64) { pet ->
            if (!pet.headItem.skinBase64.isNullOrBlank()) {
                pet.headItem.skinBase64!!
            } else {
                ""
            }
        }
        registerPetPlaceHolder(PlaceHolder.PET_ISBREAKINGBLOCK) { pet -> pet.isBreakingBlock().toString() }
        registerPetPlaceHolder(PlaceHolder.PET_BLOCKINFRONT_TYPE) { pet ->
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
        val locatedPlaceHolders = HashMap<String, String>()
        val characterCache = StringBuilder()
        var pet = parameters[petKey] as Pet?

        for (character in input) {
            characterCache.append(character)

            if (character == '%') {
                val placeHolderText = characterCache.toString()

                if (playerPlaceHolderFunctions.containsKey(placeHolderText)) {
                    locatedPlaceHolders[placeHolderText] =
                        if (placeHolderText.contains("_eventPlayer_")) {
                            playerPlaceHolderFunctions[placeHolderText]!!.invoke(player)
                        } else if (pet != null) {
                            playerPlaceHolderFunctions[placeHolderText]!!.invoke(pet.player)
                        } else {
                            ""
                        }
                } else if (petPlaceHolderFunctions.containsKey(placeHolderText) && pet != null) {
                    locatedPlaceHolders[placeHolderText] = petPlaceHolderFunctions[placeHolderText]!!.invoke(pet)
                } else if (selectedPetPlaceHolderFunctions.containsKey(placeHolderText) || (placeHolderText.endsWith("_selected%") && placeHolderText.startsWith(
                        "%petblocks_js"
                    ))
                ) {
                    val petsOfPlayer = petService.getCache()[player]
                    val playerInformation = petMetaRepository.getCachedByPlayer(player)
                    if (petsOfPlayer != null && playerInformation != null) {
                        val selectedPet = petsOfPlayer.firstOrNull { e -> e.name == playerInformation.selectedPet }
                        if (selectedPet != null) {
                            pet = selectedPet
                        } else {
                            pet = petsOfPlayer.firstOrNull()
                        }

                        if (pet != null && selectedPetPlaceHolderFunctions.containsKey(placeHolderText)) {
                            locatedPlaceHolders[placeHolderText] =
                                selectedPetPlaceHolderFunctions[placeHolderText]!!.invoke(pet)
                        }
                    }
                }

                characterCache.clear()
                characterCache.append(character)
            }
        }

        var output = input

        for (locatedPlaceHolder in locatedPlaceHolders.keys) {
            output = output.replace(locatedPlaceHolder, locatedPlaceHolders[locatedPlaceHolder]!!)
        }

        if (pet != null && output.contains("%petblocks_js")) {
            for (key in pet.memory.keys) {
                val value = pet.memory[key]!!
                if (key.contains("json")) {
                    val parsedJsonObject = mapper.readValue(value, Map::class.java)
                    for (innerKey in parsedJsonObject.keys) {
                        output =
                            output.replace("%petblocks_js_${key}_${innerKey}%", parsedJsonObject[innerKey].toString())
                    }
                } else {
                    output =
                        output.replace("%petblocks_js_${key}%", value).replace("%petblocks_js_${key}_selected%", value)
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

    private fun registerPlayerPlaceHolder(placeHolder: PlaceHolder, f: ((Player) -> String)) {
        playerPlaceHolderFunctions[placeHolder.fullPlaceHolder] = f
    }

    private fun registerPetPlaceHolder(placeHolder: PlaceHolder, f: ((Pet) -> String)) {
        petPlaceHolderFunctions[placeHolder.fullPlaceHolder] = f
        selectedPetPlaceHolderFunctions[placeHolder.fullPlaceHolder.toCharArray().dropLast(1)
            .joinToString("") + "_selected%"] = f
    }
}
