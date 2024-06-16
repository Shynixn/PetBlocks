package com.github.shynixn.petblocks.impl.provider

import com.github.shynixn.mcutils.common.placeholder.PlaceHolderProvider
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.petblocks.contract.DependencyPlaceholderApiService
import com.google.inject.Inject
import me.clip.placeholderapi.PlaceholderAPI
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class PetBlocksPlaceHolderApiProvider @Inject constructor(
    private val plugin: Plugin,
    private val placeHolderService: PlaceHolderService
) : PlaceholderExpansion(),
    DependencyPlaceholderApiService, PlaceHolderProvider {
    private var registerd: Boolean = false
    private val placeHolderApiKey = "placeholderapi"

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
            return placeHolderService.resolvePlaceHolder(
                p, "%petblocks_${params}%", mapOf(placeHolderApiKey to true)
            )
        } catch (ignored: Exception) {
            ignored.printStackTrace()
        }

        return null
    }

    override fun resolvePlaceHolder(player: Player, input: String, parameters: Map<String, Any>): String {
        if (parameters.containsKey(placeHolderApiKey)) {
            return input
        }

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
