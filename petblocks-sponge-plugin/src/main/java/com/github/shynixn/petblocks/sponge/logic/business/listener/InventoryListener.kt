package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.GUIService
import com.github.shynixn.petblocks.sponge.logic.business.helper.updateInventory
import com.google.inject.Inject
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.SlotIndex

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
class InventoryListener @Inject constructor(private val guiService: GUIService) {
    /**
     * Gets called from [Sponge] and handles action to the inventory.
     */
    @Listener(order = Order.LATE)
    fun playerClickEvent(event: ClickInventoryEvent, @First(typeFilter = [(Player::class)]) player: Player) {
        if (!guiService.isGUIInventory(event.targetInventory)) {
            return
        }

        if (event.transactions.isEmpty()) {
            return
        }

        val itemStack = event.transactions[0].original.createStack()
        val newSlot = event.transactions[0].slot.getProperties(SlotIndex::class.java).toTypedArray()[0].value

        if (itemStack.type == ItemTypes.AIR || newSlot == null) {
            return
        }

        event.isCancelled = true
        player.updateInventory()

        guiService.clickInventoryItem<Player, ItemStack>(player, newSlot, itemStack)
    }

    /**
     * Gets called from [Sponge] and handles cleaning up remaining resources.
     */
    @Listener
    fun playerQuitEvent(event: ClientConnectionEvent.Disconnect) {
        guiService.cleanResources(event.targetEntity)
    }
}