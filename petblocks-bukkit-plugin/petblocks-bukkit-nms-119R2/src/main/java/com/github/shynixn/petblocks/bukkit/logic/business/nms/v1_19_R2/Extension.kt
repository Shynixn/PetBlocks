package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_19_R2

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import org.bukkit.entity.Player


/**
 * Finds the version compatible class.
 */
fun findClazz(name: String): Class<*> {
    return Class.forName(
        name.replace(
            "VERSION",
            PetBlocksApi.resolve(PluginProxy::class.java).getServerVersion().bukkitId
        )
    )
}

/**
 * Sends the given [packet] to this player.
 */
fun Player.sendPacket(packet: Any) {
    val craftPlayerClazz = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer")
    val getHandleMethod = craftPlayerClazz.getDeclaredMethod("getHandle")
    val nmsPlayer = getHandleMethod.invoke(player)
    val version = PetBlocksApi.resolve(PluginProxy::class.java).getServerVersion()

    if (version.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1)) {
        val nmsPlayerClazz = findClazz("net.minecraft.server.level.EntityPlayer")
        val playerConnectionField = nmsPlayerClazz.getDeclaredField("b")
        playerConnectionField.isAccessible = true
        val connection = playerConnectionField.get(nmsPlayer)

        val playerConnectionClazz = findClazz("net.minecraft.server.network.PlayerConnection")
        val packetClazz = findClazz("net.minecraft.network.protocol.Packet")

        if (version.isVersionSameOrGreaterThan(Version.VERSION_1_18_R1)) {
            val sendPacketMethod = playerConnectionClazz.getDeclaredMethod("a", packetClazz)
            sendPacketMethod.invoke(connection, packet)
        } else {
            val sendPacketMethod = playerConnectionClazz.getDeclaredMethod("sendPacket", packetClazz)
            sendPacketMethod.invoke(connection, packet)
        }
    } else {
        val nmsPlayerClazz = findClazz("net.minecraft.server.VERSION.EntityPlayer")
        val playerConnectionField = nmsPlayerClazz.getDeclaredField("playerConnection")
        playerConnectionField.isAccessible = true
        val connection = playerConnectionField.get(nmsPlayer)

        val playerConnectionClazz = findClazz("net.minecraft.server.VERSION.PlayerConnection")
        val packetClazz = findClazz("net.minecraft.server.VERSION.Packet")
        val sendPacketMethod = playerConnectionClazz.getDeclaredMethod("sendPacket", packetClazz)
        sendPacketMethod.invoke(connection, packet)
    }
}
