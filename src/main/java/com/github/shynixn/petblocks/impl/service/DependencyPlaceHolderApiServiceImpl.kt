package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.petblocks.contract.DependencyPlaceholderApiService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.google.inject.Inject
import me.clip.placeholderapi.expansion.PlaceholderExpansion
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

class DependencyPlaceHolderApiServiceImpl @Inject constructor(
    private val plugin: Plugin,
    private val placeHolderService: PlaceHolderService
) : PlaceholderExpansion(),
    DependencyPlaceholderApiService {
    /**
     * Registers the placeholder hook if it is not already registered.
     */
    override fun registerListener() {
    }

    override fun onPlaceholderRequest(p: Player?, params: String?): String? {
        if (params == null || p == null) {
            return null
        }

        try {

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
}
