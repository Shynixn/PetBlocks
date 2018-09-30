@file:Suppress("CAST_NEVER_SUCCEEDS")

package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.CarryPetService
import com.github.shynixn.petblocks.bukkit.logic.business.extension.isPet
import com.github.shynixn.petblocks.bukkit.logic.business.extension.isPetOfPlayer
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.player.*
import org.bukkit.inventory.ItemStack

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
class CarryPetListener @Inject constructor(private val carryPetService: CarryPetService) : Listener {
    /**
     * Gets called when a player interacts at the given entity.
     */
    @EventHandler
    fun entityRightClickEvent(event: PlayerInteractAtEntityEvent) {
        if (event.isCancelled) {
            return
        }

        if (carryPetService.isCarryingPet(event.player)) {
            event.isCancelled = true
            return
        }

        if (event.rightClicked != null && event.rightClicked.isPetOfPlayer(event.player)) {
            carryPetService.carryPet(event.player)
            event.isCancelled = true
            return
        }

        if (event.rightClicked != null && event.rightClicked.isPet()) {
            event.isCancelled = true
            return
        }
    }

    /**
     * Gets called when a player interacts with the given entity.
     */
    @EventHandler
    fun onPlayerEntityEvent(event: PlayerInteractEntityEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when player interacts with anything.
     */
    @EventHandler
    fun onPlayerInteractEvent(event: PlayerInteractEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            carryPetService.throwPet(event.player)
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player sends a command.
     */
    @EventHandler
    fun onPlayerCommandEvent(event: PlayerCommandPreprocessEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player opens an inventory.
     */
    @EventHandler
    fun onInventoryOpenEvent(event: InventoryOpenEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            event.isCancelled = true
            event.player.closeInventory()
        }
    }

    /**
     * Gets called when the player dies.
     */
    @EventHandler
    fun onPlayerDeathEvent(event: PlayerDeathEvent) {
        if (carryPetService.isCarryingPet(event.entity)) {
            val itemStack = carryPetService.getCarryPetItemStack<Player, ItemStack>(event.entity)

            if (itemStack.isPresent) {
                event.drops.remove(itemStack.get())
                carryPetService.dropPet(event.entity)
            }
        }
    }

    /**
     * Gets called when the player quits.
     */
    @EventHandler
    fun onPlayerQuitEvent(event: PlayerQuitEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            carryPetService.clearResources(event.player)
        }
    }

    /**
     * Gets called when the player clicks in inventory.
     */
    @EventHandler
    fun onInventoryClick(event: InventoryClickEvent) {
        if (carryPetService.isCarryingPet(event.whoClicked)) {
            carryPetService.dropPet(event.whoClicked)
            event.isCancelled = true
        }
    }

    /**
     * Gets called when the player drops an item.l
     */
    @EventHandler
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            carryPetService.dropPet(event.player)
            event.isCancelled = true
            event.itemDrop.remove()
        }
    }

    /**
     * Gets called when the player slot changes.
     */
    @EventHandler
    fun onSlotChange(event: PlayerItemHeldEvent) {
        if (carryPetService.isCarryingPet(event.player)) {
            carryPetService.dropPet(event.player)
            event.player.inventory.setItem(event.previousSlot, null)
        }
    }
}