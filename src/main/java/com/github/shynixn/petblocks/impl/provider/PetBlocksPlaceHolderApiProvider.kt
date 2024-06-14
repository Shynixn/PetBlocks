package com.github.shynixn.petblocks.impl.provider

import com.github.shynixn.mcutils.common.placeholder.PlaceHolderProvider
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.DependencyPlaceholderApiService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import com.google.inject.Inject
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PetBlocksPlaceHolderApiProvider @Inject constructor(
    private val plugin: Plugin,
    private val petService: PetService,
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val placeHolderService: PlaceHolderService
) : PlaceholderExpansion(),
    DependencyPlaceholderApiService, PlaceHolderProvider {
    private var registerd: Boolean = false

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
                return placeHolderService.resolvePlaceHolder(p, "%petblocks_${params}%", emptyMap())
            }

            val pets = petService.getCache()[p]!!
            val parts = params.split("_")
            val finalPart = parts[parts.size - 1]

            if (finalPart.toIntOrNull() == null) {
                if (finalPart == "selected") {
                    val playerInformation = petMetaRepository.getCachedByPlayer(p)

                    if (playerInformation != null) {
                        val selectedPet = pets.firstOrNull { e -> e.name == playerInformation.selectedPet }
                        val newParams = parts.dropLast(1).joinToString("_")

                        if (selectedPet != null) {
                            return placeHolderService.resolvePlaceHolder(
                                p,
                                "%petblocks_${newParams}%",
                                mapOf(PetBlocksPlaceHolderProvider.petKey to selectedPet)
                            )
                        } else if (pets.size > 0) {
                            return placeHolderService.resolvePlaceHolder(
                                p,
                                "%petblocks_${newParams}%",
                                mapOf(PetBlocksPlaceHolderProvider.petKey to pets[0])
                            )
                        }
                    }
                }

                if (pets.size > 0) {
                    return placeHolderService.resolvePlaceHolder(
                        p,
                        "%petblocks_${params}%",
                        mapOf(PetBlocksPlaceHolderProvider.petKey to pets[0])
                    )
                }

                return placeHolderService.resolvePlaceHolder(p, "%petblocks_${params}%", emptyMap())
            }

            val index = finalPart.toInt() - 1
            val newParams = parts.dropLast(1).joinToString("_")

            if (index >= 0 && index < pets.size) {
                if (PlaceHolder.PET_EXISTS.fullPlaceHolder == newParams) {
                    return "true"
                }

                return placeHolderService.resolvePlaceHolder(
                    p,
                    "%petblocks_${newParams}%",
                    mapOf(PetBlocksPlaceHolderProvider.petKey to pets[index])
                )
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

    override fun resolvePlaceHolder(player: Player, input: String, parameters: Map<String, Any>): String {
        return PlaceholderAPI.setPlaceholders(player, input)
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
}
