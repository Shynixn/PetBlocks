@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.CarryPetService
import com.github.shynixn.petblocks.sponge.logic.business.extension.isPet
import com.github.shynixn.petblocks.sponge.logic.business.extension.isPetOfPlayer
import com.google.inject.Inject
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.command.SendCommandEvent
import org.spongepowered.api.event.data.ChangeDataHolderEvent
import org.spongepowered.api.event.entity.DestructEntityEvent
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent
import org.spongepowered.api.event.item.inventory.DropItemEvent
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent
import org.spongepowered.api.event.network.ClientConnectionEvent

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
class CarryPetListener @Inject constructor(private val carryPetService: CarryPetService) {
    /**
     * Gets called when a player interacts at the given entity.
     */
    @Listener
    fun onPlayerInteract(event: InteractEntityEvent.Secondary, @First(typeFilter = [(Player::class)]) player: Player) {
        if (!event.cause.first(Entity::class.java).isPresent) {
            return
        }

        val entity = event.targetEntity

        if (event.isCancelled) {
            return
        }

        if (carryPetService.isCarryingPet(player)) {
            event.isCancelled = true
            return
        }

        if (entity.isPetOfPlayer(player)) {
            carryPetService.carryPet(player)
            event.isCancelled = true
            return
        }

        if (entity.isPet()) {
            event.isCancelled = true
            return
        }
    }

    /**
     * Gets called when a player interacts with the given entity.
     */
    @Listener
    fun onPlayerInteractEvent(event: HandInteractEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            carryPetService.throwPet(player)
            event.isCancelled = true
        }
    }

    /**
     * Gets called when player interacts with anything.
     */
    @Listener
    fun onPlayerEntityEvent(event: InteractEntityEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            carryPetService.dropPet(player)
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player sends a command.
     */
    @Listener
    fun onPlayerCommandEvent(event: SendCommandEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player opens an inventory.
     */
    @Listener
    fun onInventoryOpenEvent(event: InteractInventoryEvent.Open, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            event.isCancelled = true
            player.closeInventory()
        }
    }

    /**
     * Gets called when the player dies.
     */
    @Listener
    fun onPlayerDeathEvent(event: DestructEntityEvent.Death) {
        if (event.targetEntity is Player && carryPetService.isCarryingPet(event.targetEntity)) {
            carryPetService.dropPet(event.targetEntity)
        }
    }

    /**
     * Gets called when the player quits.
     */
    @Listener
    fun onPlayerQuitEvent(event: ClientConnectionEvent.Disconnect) {
        carryPetService.clearResources(event.targetEntity)
    }

    /**
     * Gets called when the player clicks in inventory.
     */
    @Listener
    fun onInventoryClick(event: ClickInventoryEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            event.isCancelled = true
            player.closeInventory()
        }
    }

    /**
     * Gets called when the player drops an item.l
     */
    @Listener
    fun onPlayerDropItem(event: DropItemEvent.Dispense, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            carryPetService.dropPet(player)
            event.isCancelled = true
            event.entities.forEach { entity ->
                entity.remove()
            }
        }
    }

    /**
     * Gets called when the player slot changes.
     */
    @Listener
    fun onSlotChange(event: ChangeDataHolderEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (carryPetService.isCarryingPet(player)) {
            carryPetService.dropPet(player)
            event.isCancelled = true
        }
    }
}