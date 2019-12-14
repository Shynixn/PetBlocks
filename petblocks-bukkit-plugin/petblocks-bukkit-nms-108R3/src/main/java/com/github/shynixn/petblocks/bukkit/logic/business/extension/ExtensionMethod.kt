@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.extension

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

/**
 * Finds the version compatible class.
 */
fun findClazz(name: String): Class<*> {
    return Class.forName(name.replace("VERSION", PetBlocksApi.resolve(PluginProxy::class.java).getServerVersion().bukkitId))
}

/**
 * Calls the distance method safely.
 */
fun Location.distanceSafely(target: Location): Double {
    if (this.world == null || target.world == null || this.world!!.name != target.world!!.name) {
        return Double.MAX_VALUE
    }

    return this.distance(target)
}

/**
 * Converts the [Position] to a BukkitVector.
 */
fun Position.toVector(): Vector {
    return Vector(this.x, this.y, this.z)
}

/**
 * Converts the [Position] to a BukkitLocation.
 */
fun Position.toLocation(): Location {
    return Location(Bukkit.getWorld(this.worldName!!), this.x, this.y, this.z, this.yaw.toFloat(), this.pitch.toFloat())
}

/**
 * Converts the [Vector] to a position
 */
fun Vector.toPosition(): Position {
    return PositionEntity(this.x, this.y, this.z)
}

/**
 * Converts the [Location] to a Position.
 */
fun Location.toPosition(): Position {
    val position = PositionEntity()

    if (this.world != null) {
        position.worldName = this.world!!.name
    }

    position.x = this.x
    position.y = this.y
    position.z = this.z
    position.yaw = this.yaw.toDouble()
    position.pitch = this.pitch.toDouble()

    return position
}

/**
 * Sends the given [packet] to this player.
 */
fun Player.sendPacket(packet: Any) {
    val version = PetBlocksApi.resolve(PluginProxy::class.java).getServerVersion()
    val craftPlayer = Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".replace("VERSION", version.bukkitId)).cast(player)
    val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
    val entityPlayer = methodHandle.invoke(craftPlayer)

    val field = Class.forName("net.minecraft.server.VERSION.EntityPlayer".replace("VERSION", version.bukkitId)).getDeclaredField("playerConnection")
    field.isAccessible = true
    val connection = field.get(entityPlayer)

    val sendMethod = connection.javaClass.getDeclaredMethod("sendPacket", packet.javaClass.interfaces[0])
    sendMethod.invoke(connection, packet)
}


/**
 * Teleports the player via packets to keep his state in the world.
 */
fun Player.teleportUnsafe(location: Location) {
    val version = PetBlocksApi.resolve(PluginProxy::class.java).getServerVersion()
    val craftPlayer = Class.forName("org.bukkit.craftbukkit.VERSION.entity.CraftPlayer".replace("VERSION", version.bukkitId)).cast(player)
    val methodHandle = craftPlayer.javaClass.getDeclaredMethod("getHandle")
    val entityPlayer = methodHandle.invoke(craftPlayer)
    val entityClazz = Class.forName("net.minecraft.server.VERSION.Entity".replace("VERSION", version.bukkitId))

    val setPositionMethod = entityClazz.getDeclaredMethod(
        "setPositionRotation",
        Double::class.java,
        Double::class.java,
        Double::class.java,
        Float::class.java,
        Float::class.java
    )

    setPositionMethod.invoke(entityPlayer, location.x, location.y, location.z, location.yaw, location.pitch)

    val packetTeleport = Class.forName("net.minecraft.server.VERSION.PacketPlayOutEntityTeleport".replace("VERSION", version.bukkitId))
        .getDeclaredConstructor(entityClazz)
        .newInstance(entityPlayer)

    location.world!!.players.forEach { worldPlayer ->
        worldPlayer.sendPacket(packetTeleport)
    }
}