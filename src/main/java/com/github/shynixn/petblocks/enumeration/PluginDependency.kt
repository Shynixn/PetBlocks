package com.github.shynixn.petblocks.enumeration

enum class PluginDependency(
    /**
     * Plugin name.
     */
    val pluginName: String
) {

    /**
     * HeadDatabase plugin.
     */
    HEADDATABASE("HeadDatabase"),

    /**
     * PlaceHolderApi plugin.
     */
    PLACEHOLDERAPI("PlaceholderAPI")
}
