package com.github.shynixn.petblocks.enumeration

import com.github.shynixn.fasterxml.jackson.databind.ObjectMapper
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.PetBlocksPlugin
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PlayerInformation
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

enum class PlaceHolder(val text: String, val f: (Player?, Pet?, Map<String, Any>) -> String?) {
    // Owner PlaceHolders.
    PLAYER_OWNER_NAME("%petblocks_owner_name%", { player, _, _ -> player?.name }),
    PLAYER_OWNER_DISPLAYNAME("%petblocks_owner_displayName%", { player, _, _ -> player?.displayName }),
    PLAYER_OWNER_LOCATION_WORLD("%petblocks_owner_locationWorld%", { player, _, _ -> player?.location?.world?.name }),
    PLAYER_OWNER_LOCATION_X(
        "%petblocks_owner_locationX%",
        { player, _, _ -> PetBlocksPlugin.formatDoubleIfNotNull(player?.location?.x) }),
    PLAYER_OWNER_LOCATION_Y(
        "%petblocks_owner_locationY%",
        { player, _, _ -> PetBlocksPlugin.formatDoubleIfNotNull(player?.location?.y) }),
    PLAYER_OWNER_LOCATION_Z(
        "%petblocks_owner_locationZ%",
        { player, _, _ -> PetBlocksPlugin.formatDoubleIfNotNull(player?.location?.z) }),
    PLAYER_OWNER_LOCATION_YAW(
        "%petblocks_owner_locationYaw%",
        { player, _, _ -> PetBlocksPlugin.formatFloatIfNotNull(player?.location?.yaw) }),
    PLAYER_OWNER_LOCATION_PITCH(
        "%petblocks_owner_locationPitch%",
        { player, _, _ -> PetBlocksPlugin.formatFloatIfNotNull(player?.location?.pitch) }),
    PLAYER_OWNER_ITEMMAINHAND_TYPE(
        "%petblocks_owner_itemMainHand_type%",
        { player, _, _ -> "minecraft:" + player?.inventory?.itemInMainHand?.type?.name?.lowercase() }),
    PLAYER_OWNER_ISFLYING("%petblocks_owner_isFlying%", { player, _, _ -> player?.isFlying?.toString() }),

    // Event Player
    EVENT_PLAYER_OWNER_NAME("%petblocks_eventPlayer_name%", { _, _, context ->
        val eventPlayer = context[PetBlocksPlugin.eventPlayer] as Player?
        eventPlayer?.name
    }),
    EVENT_PLAYER_OWNER_DISPLAYNAME(
        "%petblocks_eventPlayer_displayName%",
        { _, _, context ->
            val eventPlayer = context[PetBlocksPlugin.eventPlayer] as Player?
            eventPlayer?.displayName
        }),
    EVENT_PLAYER_OWNER_LOCATION_WORLD(
        "%petblocks_eventPlayer_locationWorld%",
        { _, _, context ->
            val eventPlayer = context[PetBlocksPlugin.eventPlayer] as Player?
            eventPlayer?.location?.world?.name
        }),
    EVENT_PLAYER_OWNER_LOCATION_X(
        "%petblocks_eventPlayer_locationX%",
        { _, _, context ->

            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            PetBlocksPlugin.formatDoubleIfNotNull(eventPlayer?.location?.x)
        }),
    EVENT_PLAYER_OWNER_LOCATION_Y(
        "%petblocks_eventPlayer_locationY%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            PetBlocksPlugin.formatDoubleIfNotNull(eventPlayer?.location?.y)
        }),
    EVENT_PLAYER_OWNER_LOCATION_Z(
        "%petblocks_eventPlayer_locationZ%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            PetBlocksPlugin.formatDoubleIfNotNull(eventPlayer?.location?.z)
        }),
    EVENT_PLAYER_OWNER_LOCATION_YAW(
        "%petblocks_eventPlayer_locationYaw%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            PetBlocksPlugin.formatFloatIfNotNull(eventPlayer?.location?.yaw)
        }),
    EVENT_PLAYER_OWNER_LOCATION_PITCH(
        "%petblocks_eventPlayer_locationPitch%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            PetBlocksPlugin.formatFloatIfNotNull(eventPlayer?.location?.pitch)
        }),
    EVENT_PLAYER_OWNER_ITEMMAINHAND_TYPE(
        "%petblocks_eventPlayer_itemMainHand_type%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            eventPlayer?.inventory?.itemInMainHand?.type?.name?.lowercase()
        }),
    EVENT_PLAYER_OWNER_ISFLYING(
        "%petblocks_eventPlayer_isFlying%",
        { _, _, context ->
            val eventPlayer =
                context[PetBlocksPlugin.eventPlayer] as Player?
            eventPlayer?.isFlying?.toString()
        }),

    // Pet PlaceHolders
    PET_NAME("%petblocks_pet_name_[index]%", { _, pet, _ -> pet?.name }),
    PET_DISPLAYNAME("%petblocks_pet_displayName_[index]%", { _, pet, _ -> pet?.displayName }),
    PET_DISTANCETOOWNER("%petblocks_pet_distanceToOwner_[index]%", { _, pet, _ ->
        if (pet == null) {
            null
        } else {
            val playerLocation = pet.player.location
            val petLocation = pet.location

            if (playerLocation.world != petLocation.world) {
                Int.MAX_VALUE.toString()
            } else {
                playerLocation.distance(petLocation).toInt().toString()
            }
        }
    }),
    PET_ISSPAWNED("%petblocks_pet_isSpawned_[index]%", { _, pet, _ -> pet?.isSpawned?.toString() }),
    PET_TEMPLATE("%petblocks_pet_template_[index]%", { _, pet, _ -> pet?.template?.name }),
    PET_VISIBILITY("%petblocks_pet_visibility_[index]%", { _, pet, _ -> pet?.visibility?.name }),
    PET_MOUNTED("%petblocks_pet_isMounted_[index]%", { _, pet, _ -> pet?.isMounted()?.toString() }),
    PET_LOOP("%petblocks_pet_loop_[index]%", { _, pet, _ -> pet?.loop }),
    PET_LOCATION_WORLD("%petblocks_pet_locationWorld_[index]%", { _, pet, _ -> pet?.location?.world?.name }),
    PET_LOCATION_X(
        "%petblocks_pet_locationX_[index]%",
        { _, pet, _ -> PetBlocksPlugin.formatDoubleIfNotNull(pet?.location?.x) }),
    PET_LOCATION_Y(
        "%petblocks_pet_locationY_[index]%",
        { _, pet, _ -> PetBlocksPlugin.formatDoubleIfNotNull(pet?.location?.y) }),
    PET_LOCATION_Z(
        "%petblocks_pet_locationZ_[index]%",
        { _, pet, _ -> PetBlocksPlugin.formatDoubleIfNotNull(pet?.location?.z) }),
    PET_LOCATION_YAW(
        "%petblocks_pet_locationYaw_[index]%",
        { _, pet, _ -> PetBlocksPlugin.formatFloatIfNotNull(pet?.location?.yaw) }),
    PET_LOCATION_PITCH(
        "%petblocks_pet_locationPitch_[index]%",
        { _, pet, _ -> PetBlocksPlugin.formatFloatIfNotNull(pet?.location?.pitch) }),
    PET_ITEM_TYPE("%petblocks_pet_itemType_[index]%", { _, pet, _ -> pet?.headItemStack?.type?.name }),
    PET_ITEM_DURABILITY("%petblocks_pet_itemDurability_[index]%",
        { _, pet, _ -> pet?.headItemStack?.durability?.toString() }),
    PET_ITEM_NBT("%petblocks_pet_itemNbt_[index]%", { _, pet, _ -> pet?.headItem?.nbt }),
    PET_ITEM_COMPONENT("%petblocks_pet_itemComponent_[index]%", { _, pet, _ -> pet?.headItem?.component }),
    PET_HEAD_BASE64("%petblocks_pet_itemHeadBase64_[index]%", { _, pet, _ -> pet?.headItem?.skinBase64 }),
    PET_ISBREAKINGBLOCK("%petblocks_pet_isBreakingBlock_[index]%", { _, pet, _ -> pet?.isBreakingBlock()?.toString() }),
    PET_BLOCKINFRONT_TYPE("%petblocks_pet_blockInFrontType_[index]%", { _, pet, _ ->
        val block = pet?.getBlockInFrontOf()
        if (block == null) {
            "minecraft:air"
        } else {
            "minecraft:" + block.type.name.lowercase()
        }
    }),
    PET_JAVASCRIPT_VALUE("%petblocks_js_[jsKey]_[index]%", { _, pet, context ->
        val jsKey = context["[jsKey]"] as String?
        if (jsKey != null && pet != null) {
            val parts = jsKey.split(".")
            val memoryContent = pet.memory[parts[0]]

            if (parts.size > 1) {
                val mapper = ObjectMapper()
                val parsedJsonObject = mapper.readValue(memoryContent, Map::class.java)
                parsedJsonObject[parts[1]]?.toString()
            } else {
                memoryContent
            }
        } else {
            null
        }
    });

    companion object {
        /**
         * Registers all placeHolder. Overrides previously registered placeholders.
         */
        fun registerAll(
            placeHolderService: PlaceHolderService,
            petMetaRepository: CachePlayerRepository<PlayerInformation>,
            petService: PetService
        ) {
            for (placeHolder in PlaceHolder.values()) {
                placeHolderService.register(placeHolder.text) { player, context ->
                    val petCache = petService.getCache()[player]
                    val pet = if (petCache != null && player != null) {
                        val petIndex = context[PetBlocksPlugin.index] as String?
                        val playerInformation = petMetaRepository.getCachedByPlayer(player)
                        if (petIndex == "selected") {
                            val selectedPet = petCache.firstOrNull { e -> e.name == playerInformation?.selectedPet }
                            if (selectedPet != null) {
                                selectedPet
                            } else if (petCache.size > 0) {
                                petCache[0]
                            } else {
                                null
                            }
                        } else if (petIndex?.toIntOrNull() != null && (petIndex.toInt() - 1) >= 0 && (petIndex.toInt() - 1) < petCache.size) {
                            petCache[petIndex.toInt() - 1]
                        } else if (petCache.size > 0) {
                            petCache[0]
                        } else {
                            null
                        }
                    } else {
                        null
                    }
                    placeHolder.f.invoke(player, pet, context)
                }
            }
        }
    }
}
