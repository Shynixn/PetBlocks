package com.github.shynixn.petblocks.api.legacy.business.enumeration

enum class ConfigEventType(
    /**
     * Config path.
     */
    val path: String
) {
    /**
     * Triggered when a player joins.
     */
    ONJOIN("events.onjoin"),

    /**
     * Triggered when a player quits.
     */
    ONQUIT("events.onquit"),

    /**
     * Triggered when a player sneaks.
     */
    ONSNEAK("events.onsneak"),

    /**
     * Triggered when a pet spawns.
     */
    ONPETSPAWN("events.onpetspawn"),

    /**
     * Triggered when a pet de-spawns.
     */
    ONPETDESPAWN("events.onpetdespawn")
}
