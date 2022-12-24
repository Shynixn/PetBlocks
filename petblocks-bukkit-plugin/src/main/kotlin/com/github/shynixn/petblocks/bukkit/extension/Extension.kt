package com.github.shynixn.petblocks.bukkit.extension

import com.github.shynixn.petblocks.bukkit.entity.Permission
import org.bukkit.command.CommandSender
import java.util.*

/**
 * Checks for the matching permission.
 */
fun CommandSender.hasPermission(permission: Permission): Boolean {
    return this.hasPermission(permission.text)
}

fun String.toFirstLetterUpperCase(): String {
    return "${this[0].toString().toUpperCase(Locale.ENGLISH)}${this.substring(1)}"
}