package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.petblocks.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.DependencyPlaceholderApiService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import com.google.inject.Inject
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DependencyPlaceHolderApiServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val petService: PetService
) : PlaceholderExpansion(),
    DependencyPlaceholderApiService, PlaceHolderService {
    private var registerd: Boolean = false
    private val placeHolderService = PlaceHolderServiceImpl()

    init {
        this.registerListener()
    }

    /**
     * Registers the placeholder hook if it is not already registered.
     */
    override fun registerListener() {
        if (!registerd) {
            this.register()
            registerd = true
        }
    }

    override fun onPlaceholderRequest(p: Player?, params: String?): String? {
        if (params == null || p == null) {
            return null
        }

        try {
            if (!petService.getCache().containsKey(p)) {
                return placeHolderService.replacePlaceHolders(p, "%petblocks_${params}%", null)
            }

            val pets = petService.getCache()[p]!!
            val parts = params.split("_")
            val finalPart = parts[parts.size - 1]

            if (finalPart.toIntOrNull() == null) {
                if (pets.size > 0) {
                    return placeHolderService.replacePlaceHolders(p, "%petblocks_${params}%", pets[0])
                }

                return placeHolderService.replacePlaceHolders(p, "%petblocks_${params}%", null)
            }

            val index = finalPart.toInt() - 1
            val newParams = parts.dropLast(1).joinToString("_")

            if (index >= 0 && index < pets.size) {
                if (PlaceHolder.PET_EXISTS.fullPlaceHolder == newParams) {
                    return "true"
                }

                return placeHolderService.replacePlaceHolders(p, "%petblocks_${newParams}%", pets[index])
            }

            if (PlaceHolder.PET_EXISTS.fullPlaceHolder == newParams) {
                return "false"
            }

            return PetBlocksLanguage.placeHolderPetNotFound
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }

        return null
    }

    override fun getIdentifier(): String {
        return "petblocks"
    }

    override fun getAuthor(): String {
        return plugin.description.authors[0]
    }

    override fun getVersion(): String {
        return plugin.description.version
    }

    /**
     * Replaces incoming strings with the escaped version.
     */
    override fun replacePlaceHolders(player: Player, input: String, pet: Pet?): String {
        val replacedInput = placeHolderService.replacePlaceHolders(player, input, pet)
        return PlaceholderAPI.setPlaceholders(player, replacedInput)
    }
}
