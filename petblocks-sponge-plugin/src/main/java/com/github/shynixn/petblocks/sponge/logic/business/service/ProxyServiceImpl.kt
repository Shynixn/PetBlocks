@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.toText
import com.github.shynixn.petblocks.sponge.logic.business.extension.updateInventory
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.world.World
import java.util.*

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
class ProxyServiceImpl : ProxyService {
    /**
     * Gets the name of a player.
     */
    override fun <P> getPlayerName(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        return player.name
    }

    /**
     * Gets the player from the given UUID.
     */
    override fun <P> getPlayerFromUUID(uuid: String): P {
        val player = Sponge.getServer().getPlayer(UUID.fromString(uuid))

        if (player.isPresent && player.get().isOnline) {
            return player.get() as P
        }

        throw IllegalArgumentException("Player is no longer online!")
    }

    /**
     * Gets the location of the player.
     */
    override fun <L, P> getPlayerLocation(player: P): L {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        return player.transform as L
    }

    /**
     * Converts the given [location] to a [Position].
     */
    override fun <L> toPosition(location: L): Position {
        if (location !is Transform<*>) {
            throw IllegalArgumentException("Location has to be a SpongeLocation!")
        }

        return (location as Transform<World>).toPosition()
    }

    /**
     * Gets the looking direction of the player.
     */
    override fun <P> getDirectionVector(player: P): Position {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        val vector = PositionEntity()
        val location = player.transform.toPosition()

        val rotX = location.yaw
        val rotY = location.pitch
        vector.y = -Math.sin(Math.toRadians(rotY))
        val h = Math.cos(Math.toRadians(rotY))
        vector.x = -h * Math.sin(Math.toRadians(rotX))
        vector.z = h * Math.cos(Math.toRadians(rotX))
        return vector
    }

    /**
     * Gets the item in the player hand.
     */
    override fun <P, I> getPlayerItemInHand(player: P, offhand: Boolean): I? {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        val opt = if (offhand) {
            player.getItemInHand(HandTypes.OFF_HAND)
        } else {
            player.getItemInHand(HandTypes.MAIN_HAND)
        }

        if (opt.isPresent) {
            return opt.get() as I
        }

        return null
    }

    /**
     * Sets the item in the player hand.
     */
    override fun <P, I> setPlayerItemInHand(player: P, itemStack: I, offhand: Boolean) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (itemStack !is ItemStack?) {
            throw IllegalArgumentException("ItemStack has to be a SpongeItemStack!")
        }

        if (offhand) {
            player.setItemInHand(HandTypes.OFF_HAND, itemStack)
        } else {
            player.setItemInHand(HandTypes.MAIN_HAND, itemStack)
        }

        player.inventory.updateInventory()
    }

    /**
     * Gets if the given player has got the given permission.
     */
    override fun <P> hasPermission(player: P, permission: Permission): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        return player.hasPermission(permission.permission)
    }

    /**
     * Gets the player uuid.
     */
    override fun <P> getPlayerUUID(player: P): String {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        return player.uniqueId.toString()
    }

    /**
     * Sends a message to the [sender].
     */
    override fun <S> sendMessage(sender: S, message: String) {
        if (sender !is CommandSource) {
            throw IllegalArgumentException("Sender has to be a SpongeSender!")
        }

        sender.sendMessage(message.toText())
    }

    /**
     * Gets if the given instance can be converted to a player.
     */
    override fun <P> isPlayer(instance: P): Boolean {
        return instance is Player
    }
}