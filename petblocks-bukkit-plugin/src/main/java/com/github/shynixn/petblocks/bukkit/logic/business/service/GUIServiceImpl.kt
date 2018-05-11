package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.GUIScriptService
import com.github.shynixn.petblocks.api.business.service.GUIService
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager
import com.github.shynixn.petblocks.bukkit.logic.business.helper.clearCompletely
import com.github.shynixn.petblocks.core.logic.business.entity.GuiPageContainer
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin

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
class GUIServiceImpl @Inject constructor(private val petBlockManager: PetBlockManager, private val configurationService: ConfigurationService, private val plugin: Plugin, private val scriptService: GUIScriptService) : GUIService {
    /**
     * Returns if the given [inventory] matches the inventory of this service.
     */
    override fun <I> isGUIInventory(inventory: I): Boolean {
        if (inventory !is Inventory) {
            throw IllegalArgumentException("Inventory has to be an BukkitInventory")
        }

        return petBlockManager.inventories.containsKey(inventory.holder)
                && petBlockManager.inventories[inventory.holder]!! == inventory
    }

    /**
     * Executes actions when the given [player] clicks on an [item].
     */
    override fun <P, I> clickInventoryItem(player: P, item: I) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be an BukkitPlayer!")
        }

        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be an BukkitItemStack!")
        }

        val optGuiItem = configurationService.findClickedGUIItem(item)
        if (!optGuiItem.isPresent) {
            return
        }

        val script = optGuiItem.get().executingScript
        if (!script.isPresent) {
            return
        }

        val scriptResult = scriptService.executeScript(petBlockManager.inventories[player], script.get())

        if (scriptResult.action == ScriptAction.LOAD_COLLECTION) {
            loadCollectionPage(petBlockManager.inventories[player], scriptResult.path.get())
        }
    }

    /**
     * Loads the collection from the given [path] into the given [inventory].
     */
    override fun <I> loadCollectionPage(inventory: I, path: String, permission: String?) {
        if (inventory !is Inventory) {
            throw IllegalArgumentException("Inventory has to be an BukkitInventory")
        }

        val optItems = configurationService.findGUIItemCollection(path)

        if (optItems.isPresent) {
            setItemsToInventory(inventory.holder as Player, inventory, 1, optItems.get(), permission)
        }
    }

    private fun setItemsToInventory(player: Player, inventory: Inventory, type: Int, items: List<GUIItem>, groupPermission: String?) {
        val previousContainer = this.petBlockManager.pages[player]
        val container: GuiPageContainer
        val page = GUIPage.CUSTOM

        if (previousContainer!!.page != page) {
            container = GuiPageContainer(page, previousContainer)
            this.petBlockManager.pages[player] = container
        } else {
            container = this.petBlockManager.pages[player]!!
        }

        if (type == 1 && (container.startCount % 45 != 0 || items.size == container.startCount)) {
            return
        }

        if (type == 2) {
            if (container.currentCount == 0) {
                return
            }
            container.startCount = container.currentCount - 45
        }

        var count = container.startCount
        if (count < 0)
            count = 0
        container.currentCount = container.startCount
        inventory.clearCompletely()
        var i: Int = 0
        var scheduleCounter = 0
        while (i < 45 && i + container.startCount < items.size) {
            if (inventory.getItem(i) == null || inventory.getItem(i).getType() == Material.AIR) {

                val containerSlot = i + container.startCount
                val mountBlock = container.currentCount
                val currentPage = container.page
                count++
                if (i % 2 == 0) {
                    scheduleCounter++
                }

                Bukkit.getScheduler().runTaskLater(plugin, {
                    if (container.currentCount == mountBlock && currentPage == this.petBlockManager.pages[player]!!.page) {
                        inventory.setItem(i, setPermissionLore(player, items[containerSlot].toItemStack(), groupPermission))
                    }
                }, scheduleCounter.toLong())
            }
            i++
        }
        container.startCount = count
        val optBackGuiItemContainer = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("back")
        if (!optBackGuiItemContainer.isPresent)
            throw IllegalArgumentException("Gui item back could not be loaded correctly!")
        inventory.setItem(optBackGuiItemContainer.get().position, optBackGuiItemContainer.get().generate(player) as ItemStack)

        if (!(container.startCount % 45 != 0 || items.size == container.startCount)) {
            val optNextPage = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("next-page")
            if (!optNextPage.isPresent)
                throw IllegalArgumentException("Gui item next-page could not be loaded correctly!")
            inventory.setItem(optNextPage.get().position, optNextPage.get().generate(player) as ItemStack)
        }

        if (container.currentCount != 0) {
            val optPreviousPage = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("previous-page")
            if (!optPreviousPage.isPresent)
                throw IllegalArgumentException("Gui item previous-page could not be loaded correctly!")
            inventory.setItem(optPreviousPage.get().position, optPreviousPage.get().generate(player) as ItemStack)
        }

        this.fillEmptySlots(inventory)
    }

    private fun setPermissionLore(player: Player, itemStack: ItemStack, permission: String?): ItemStack {
        return itemStack
    }

    /**
     * Fills up the given [inventory] with the default item.
     */
    private fun fillEmptySlots(inventory: Inventory) {
        for (i in 0 until inventory.contents.size) {
            if (inventory.getItem(i) == null || inventory.getItem(i).type == Material.AIR) {
                val optEmptySlot = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("empty-slot")
                if (!optEmptySlot.isPresent) {
                    throw RuntimeException("PetBlocks gui item 'empty-slot' is not correctly loaded.")
                } else {
                    inventory.setItem(i, optEmptySlot.get().generate(inventory.holder as Player) as ItemStack)
                }
            }
        }
    }
}