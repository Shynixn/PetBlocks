package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIInventory
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.StorageInventoryCache
import com.google.inject.Inject

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class GUIPetStorageServiceImpl @Inject constructor(
    private val persistencePetMetaService: PersistencePetMetaService,
    private val itemTypeService: ItemTypeService,
    private val proxyService: ProxyService,
    private val loggingService: LoggingService
) :
    GUIPetStorageService {

    private val storageCache = HashMap<Any, StorageInventoryCache>()
    /**
     * Opens the storage of the given [petMeta] for the given player.
     * If the petMeta belongs to a different player than the given player, the
     * inventory gets opened in readOnlyMode.
     */
    override fun <P> openStorage(player: P, petMeta: PetMeta, from: Int, to: Int) {
        require(player is Any)

        if (from > to) {
            loggingService.warn("Cannot open storage. Script parameter to cannot be smaller than from!")
            return
        }

        val size = if (to - from > 27) {
            54
        } else {
            27
        }

        val guiTitle = Messages.guiTitle + " " + from + "-" + (from + size - 1)
        val inventory = proxyService.openInventory<Any, Any>(player, guiTitle, size)
        val inventoryAI = petMeta.aiGoals.firstOrNull { a -> a is AIInventory } as AIInventory?

        if (inventoryAI == null) {
            loggingService.warn("Player " + proxyService.getPlayerName(player) + " tried to open a pet inventory without a AIInventory.")
            loggingService.warn("Apply an AIInventory to your players.")
            proxyService.closeInventory(player)
            return
        }

        var index = from - 1

        for (i in 0 until size) {
            if (index >= inventoryAI.items.size) {
                break
            }

            try {
                proxyService.setInventoryItem(inventory, i, inventoryAI.items[index])
            } catch (e: Exception) {
                // Inventory might not be available.
            }

            index++
        }

        storageCache[player] =
            StorageInventoryCache(from, to, inventory, proxyService.getPlayerUUID(player) != petMeta.playerMeta.uuid)
        proxyService.updateInventory(player)
    }

    /**
     * Returns if the given [inventory] matches the storage inventory of this service.
     */
    override fun <I> isStorage(inventory: I): Boolean {
        require(inventory is Any)
        val holder = proxyService.getPlayerFromInventory<Any, I>(inventory) ?: return false
        val originInventory = proxyService.getLowerInventory(inventory)
        return this.storageCache.containsKey(holder) && this.storageCache[holder]!!.inventory == originInventory
    }

    /**
     * Saves the storage inventory to the database and clears all resources.
     */
    override fun <P> saveStorage(player: P) {
        require(player is Any)

        if (!storageCache.containsKey(player)) {
            return
        }

        if (storageCache[player]!!.readOnly) {
            storageCache.remove(player)
            return
        }

        val from = storageCache[player]!!.from - 1
        val to = storageCache[player]!!.to
        val inventory = storageCache[player]!!.inventory
        val petMeta = persistencePetMetaService.getPetMetaFromPlayer(player)
        val inventoryAI = petMeta.aiGoals.first { a -> a is AIInventory } as AIInventory
        val newItems = ArrayList<Any?>()
        var i = 0
        var inventoryI = 0

        while (true) {
            if (i in from until to) {
                val item = proxyService.getInventoryItem<Any, Any>(inventory, inventoryI)
                if (item == null) {
                    newItems.add(itemTypeService.toItemStack(ItemEntity(MaterialType.AIR.name)))
                } else {
                    newItems.add(item)
                }

                inventoryI++
            } else if (i >= inventoryAI.items.size) {
                break
            } else {
                newItems.add(inventoryAI.items[i])
            }

            i++
        }

        inventoryAI.items = newItems
        storageCache.remove(player)
    }
}