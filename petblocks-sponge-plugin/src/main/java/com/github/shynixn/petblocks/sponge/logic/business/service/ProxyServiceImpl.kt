@file:Suppress("UNCHECKED_CAST", "RemoveExplicitTypeArguments", "DEPRECATION")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.PotionEffect
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.toText
import com.github.shynixn.petblocks.sponge.logic.business.extension.updateInventory
import com.google.inject.Inject
import net.minecraft.entity.player.EntityPlayerMP
import net.minecraft.inventory.ContainerChest
import org.spongepowered.api.Sponge
import org.spongepowered.api.command.CommandSource
import org.spongepowered.api.data.manipulator.mutable.PotionEffectData
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.effect.potion.PotionEffectType
import org.spongepowered.api.effect.potion.PotionEffectTypes
import org.spongepowered.api.entity.EntityTypes
import org.spongepowered.api.entity.Transform
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.InventoryArchetypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.InventoryTitle
import org.spongepowered.api.item.inventory.property.SlotPos
import org.spongepowered.api.item.inventory.type.CarriedInventory
import org.spongepowered.api.item.inventory.type.GridInventory
import org.spongepowered.api.plugin.PluginContainer
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
class ProxyServiceImpl @Inject constructor(private val pluginContainer: PluginContainer, private val loggingService: LoggingService) : ProxyService {
    /**
     * Gets a list of points between 2 locations.
     */
    override fun <L> getPointsBetweenLocations(location1: L, location2: L, amount: Int): List<L> {
        require(location1 is Transform<*>)
        require(location2 is Transform<*>)

        if (location1.extent != location2.extent) {
            return ArrayList()
        }

        val locations = ArrayList<Transform<*>>()
        val vectorBetween = location1.position.sub(location2.position)
        val onePointLength = vectorBetween.length() / amount

        try {
            for (i in 0 until amount) {
                val location = Transform(location2.extent, location2.position)
                val pos1 = location.position.add(0.0, 0.7, 0.0)
                val pos2 = pos1.add(vectorBetween.normalize().mul(i.toFloat()).mul(onePointLength))
                locations.add(Transform(location2.extent, pos2))
            }
        } catch (e: Exception) {
            // Ignore normalizing errors.
        }

        return locations as List<L>
    }

    /**
     * Applies the given [potionEffect] to the given [player].
     */
    override fun <P> applyPotionEffect(player: P, potionEffect: PotionEffect) {
        require(player is Player)

        var foundPotionTypeField =
            PotionEffectTypes::class.java.declaredFields.firstOrNull { t -> t.name.equals(potionEffect.potionType, true) }

        if (foundPotionTypeField == null && potionEffect.potionType.equals("INCREASE_DAMAGE", true)) {
            foundPotionTypeField = PotionEffectTypes::class.java.declaredFields.single { f -> f.name == "STRENGTH" }
        }

        if (foundPotionTypeField == null) {
            loggingService.warn("PotionEffectType: ${potionEffect.potionType} does not exist! Check your config.yml.")
            return
        }

        val foundPotionEffect = foundPotionTypeField.get(null) as PotionEffectType

        val potionEffectSponge = org.spongepowered.api.effect.potion.PotionEffect.builder().potionType(foundPotionEffect).duration(potionEffect.duration * 20)
            .amplifier(potionEffect.amplifier).ambience(potionEffect.ambient).particles(potionEffect.particles)

        val dataClazz = player.getOrCreate(PotionEffectData::class.java).get()

        for (prevItem in dataClazz.asList().filter { d -> d.type == foundPotionEffect }) {
            dataClazz.remove(prevItem)
        }

        dataClazz.addElement(potionEffectSponge.build())
        player.offer(dataClazz)
    }

    /**
     * Drops the given item at the given position.
     */
    override fun <L, I> dropInventoryItem(location: L, item: I) {
        require(location is Transform<*>)
        require(item is ItemStack)

        location.extent.createEntity(EntityTypes.ITEM, location.position)
    }

    /**
     * Gets the inventory item at the given index.
     */
    override fun <I, IT> getInventoryItem(inventory: I, index: Int): IT? {
        require(inventory is CarriedInventory<*>)

        val row = index / 9
        val column = index % 9

        return inventory.query<Inventory>(GridInventory::class.java)
            .query<Inventory>(SlotPos.of(column, row)).peek().orElse(null) as IT
    }

    /**
     * Gets if the given player has got the given permission.
     */
    override fun <P> hasPermission(player: P, permission: String): Boolean {
        require(player is Player) { "Player has to be a SpongePlayer!" }
        return player.hasPermission(permission)
    }

    /**
     * Clears the given inventory.
     */
    override fun <I> clearInventory(inventory: I) {
        require(inventory is CarriedInventory<*>)
        inventory.clear()
    }

    /**
     * Gets if the given inventory belongs to a player. Returns null if not.
     */
    override fun <P, I> getPlayerFromInventory(inventory: I): P? {
        require(inventory is CarriedInventory<*>)

        if (!inventory.carrier.isPresent) {
            return null
        }

        return inventory.carrier.get() as P
    }

    /**
     * Gets the lower inventory of an inventory.
     */
    override fun <I> getLowerInventory(inventory: I): I {
        if (inventory is ContainerChest) {
            return inventory.lowerChestInventory as I
        }

        return inventory
    }

    /**
     * Closes the inventory of the given player.
     */
    override fun <P> closeInventory(player: P) {
        require(player is Player) { "Player has to be a SpongePlayer!" }
        player.closeInventory()
    }

    /**
     * Opens a new inventory for the given player.
     */
    override fun <P, I> openInventory(player: P, title: String, size: Int): I {
        require(player is Player) { "Player has to be a SpongePlayer!" }

        val type = if (size <= 27) {
            InventoryArchetypes.CHEST
        } else {
            InventoryArchetypes.DOUBLE_CHEST
        }

        val inventory =
            Inventory.builder().of(type).withCarrier(player)
                .property(InventoryTitle.PROPERTY_NAME, InventoryTitle.of(title.toText())).build(pluginContainer)
        player.openInventory(inventory)
        return inventory as I
    }

    /**
     * Updates the inventory.
     */
    override fun <I, IT> setInventoryItem(inventory: I, index: Int, item: IT) {
        require(inventory is CarriedInventory<*>) { "Inventory has to be a SpongeInventory!" }
        require(item is ItemStack) { "Item has to be a SpongeItem!" }

        val row = index / 9
        val column = index % 9

        inventory.query<Inventory>(GridInventory::class.java)
            .query<Inventory>(SlotPos.of(column, row)).set(item)
    }

    /**
     * Updates the given player inventory.
     */
    override fun <P> updateInventory(player: P) {
        require(player is Player) { "Player has to be a SpongePlayer!" }
        (player as EntityPlayerMP).sendContainerToPlayer((player as EntityPlayerMP).openContainer)
    }

    /**
     * Gets the name of a player.
     */
    override fun <P> getPlayerName(player: P): String {
        require(player is Player) { "Player has to be a SpongePlayer!" }

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
        require(player is Player) { "Player has to be a SpongePlayer!" }

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
        require(player is Player) { "Player has to be a SpongePlayer!" }
        require(itemStack is ItemStack?) { "ItemStack has to be a SpongeItemStack!" }

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
        return hasPermission(player, permission.permission)
    }

    /**
     * Gets the player uuid.
     */
    override fun <P> getPlayerUUID(player: P): String {
        require(player is Player) { "Player has to be a SpongePlayer!" }

        return player.uniqueId.toString()
    }

    /**
     * Sends a message to the [sender].
     */
    override fun <S> sendMessage(sender: S, message: String) {
        require(sender is CommandSource) { "Sender has to be a SpongeSender!" }

        sender.sendMessage(message.toText())
    }

    /**
     * Gets if the given instance can be converted to a player.
     */
    override fun <P> isPlayer(instance: P): Boolean {
        return instance is Player
    }
}