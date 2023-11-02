package com.github.shynixn.petblocks.enumeration

enum class PluginDependency(
    /**
     * Plugin name.
     */
    val pluginName: String
) {
    /**
     * PlaceHolderApi plugin.
     */
    PLACEHOLDERAPI("PlaceholderAPI"),

    /**
     * HeadDatabase plugin.
     */
    HEADDATABASE("HeadDatabase")
}
