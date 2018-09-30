package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager
import com.github.shynixn.petblocks.bukkit.logic.business.helper.clearCompletely
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13
import com.github.shynixn.petblocks.core.logic.business.entity.GuiPageContainer
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerGUICache
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
class GUIServiceImpl @Inject constructor(private val configurationService: ConfigurationService, private val plugin: Plugin, private val scriptService: GUIScriptService, private val persistenceService: PersistencePetMetaService, private val messageService: MessageService) : GUIService {
    private val pageCache = HashMap<Player, PlayerGUICache>()
    private var collectedMinecraftHeadsMessage = chatMessage {
        text {
            Config.getInstance<Any>().prefix + "Pets collected by "
        }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Minecraft-Heads.com<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
    }

    /**
     * Closes the gui for the given [player]. Does nothing when the GUI is already closed.
     */
    override fun <P> close(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.closeInventory()
    }

    /**
     * Opens the gui for the given [player]. Does nothing when the GUI is already open.
     */
    override fun <P> open(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        PetBlockManager.instance.gui.open(player)

        persistenceService.getOrCreateFromPlayerUUID(player.uniqueId).thenAcceptSafely { petMeta ->
            PetBlockManager.instance.gui.setPage(player, GUIPage.MAIN, petMeta)
        }
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be a BukkitItemStack!")
        }

        if (!pageCache.containsKey(player)) {
            this.pageCache[player] = PlayerGUICache()
        }

        if (PetBlockManager.instance.pages[player]!!.page == GUIPage.CUSTOM_COLLECTION && !configurationService.findClickedGUIItem(item).isPresent) {
            val optItems = configurationService.findGUIItemCollection(pageCache[player]!!.path!!)
            if (!optItems.isPresent || optItems.get().isEmpty()) {
                return
            }

            val itemSlot = relativeSlot + PetBlockManager.instance.pages[player]!!.currentCount

            if (!hasPermission(player, itemSlot, pageCache[player]!!.permission)) {
                return
            }

            if (optItems.isPresent && itemSlot < optItems.get().size) {
                val guiItem = optItems.get()[itemSlot]
                setCollectionSkinItemToPlayer(player, guiItem)
            }

            return
        }

        val optGuiItem = configurationService.findClickedGUIItem(item)
        if (!optGuiItem.isPresent) {
            return
        }

        val script = optGuiItem.get().executingScript
        if (!script.isPresent) {
            return
        }

        val scriptResult = scriptService.executeScript(PetBlockManager.instance.inventories[player], script.get())

        when {
            scriptResult.action == ScriptAction.LOAD_COLLECTION -> loadCollectionPage(PetBlockManager.instance.inventories[player], scriptResult.path.get(), scriptResult.permission.get())
            scriptResult.action == ScriptAction.SCROLL_COLLECTION -> scrollCollectionPage(player, scriptResult.valueContainer.get() as Int)
            scriptResult.action == ScriptAction.RENAME_PET -> sendGuiMessage(player, configurationService.findValue("messages.naming-suggest"), scriptResult.permission.get())
            scriptResult.action == ScriptAction.CUSTOM_SKIN -> sendGuiMessage(player, configurationService.findValue("messages.skullnaming-suggest"), scriptResult.permission.get())
        }
    }

    /**
     * Sends a gui action message.
     */
    private fun sendGuiMessage(player: Player, message: ChatMessage, permission: String) {
        if (player.hasPermission(permission)) {
            messageService.sendPlayerMessage(player, message)
        } else {
            player.sendMessage(configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.no-perms"))
        }
    }

    /**
     * Clears all resources the given player has allocated from this service.
     */
    override fun <P> cleanResources(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (pageCache.containsKey(player)) {
            pageCache.remove(player)
        }
    }

    /**
     * Returns if the given [inventory] matches the inventory of this service.
     */
    override fun <I> isGUIInventory(inventory: I): Boolean {
        if (inventory !is Inventory) {
            throw IllegalArgumentException("Inventory has to be a BukkitInventory!")
        }

        return PetBlockManager.instance.inventories.containsKey(inventory.holder)
                && PetBlockManager.instance.inventories[inventory.holder]!! == inventory
    }

    /**
     * Scrolls the loaded collection page of a [player] the given [amountOfSlots] to the right when entered a positive value.
     * Scrolls to the left when given a negative amount of Slots value.
     */
    override fun <P> scrollCollectionPage(player: P, amountOfSlots: Int) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (!pageCache.containsKey(player)) {
            this.pageCache[player] = PlayerGUICache()
        }

        if (!PetBlockManager.instance.pages.containsKey(player) || PetBlockManager.instance.pages[player]!!.page != GUIPage.CUSTOM_COLLECTION) {
            return
        }

        val path = pageCache[player]!!.path ?: return
        val permission = pageCache[player]!!.permission ?: return
        val optItems = configurationService.findGUIItemCollection(path)

        if (optItems.isPresent) {
            if (amountOfSlots < 0) {
                setItemsToInventory(player, PetBlockManager.instance.inventories[player]!!, amountOfSlots, optItems.get(), permission)
            } else {
                setItemsToInventory(player, PetBlockManager.instance.inventories[player]!!, amountOfSlots, optItems.get(), permission)
            }
        }
    }

    /**
     * Loads the collection from the given [path] into the given [inventory].
     */
    override fun <I> loadCollectionPage(inventory: I, path: String, permission: String?) {
        if (inventory !is Inventory) {
            throw IllegalArgumentException("Inventory has to be an BukkitInventory!")
        }

        if (!pageCache.containsKey(inventory.holder as Player)) {
            this.pageCache[inventory.holder as Player] = PlayerGUICache()
        }

        val optItems = configurationService.findGUIItemCollection(path)

        if (path.startsWith("minecraft-heads-com.")) {
            messageService.sendPlayerMessage(inventory.holder, collectedMinecraftHeadsMessage)
        }

        if (optItems.isPresent) {
            this.pageCache[inventory.holder as Player]!!.path = path
            this.pageCache[inventory.holder as Player]!!.permission = permission
            setItemsToInventory(inventory.holder as Player, inventory, 45, optItems.get(), permission)
        }
    }

    private fun setItemsToInventory(player: Player, inventory: Inventory, type: Int, items: List<GUIItem>, groupPermission: String?) {
        val previousContainer = PetBlockManager.instance.pages[player]
        val container: GuiPageContainer
        val page = GUIPage.CUSTOM_COLLECTION

        if (previousContainer!!.page != page) {
            container = GuiPageContainer(page, previousContainer)
            PetBlockManager.instance.pages[player] = container
        } else {
            container = PetBlockManager.instance.pages[player]!!
        }

        if (type > 0) {
            if (container.startCount % 45 != 0 || items.size == container.startCount) {
                return
            } else {
                container.startCount = container.startCount - (45 - type)
            }
        } else {
            if (container.currentCount == 0) {
                return
            } else {
                container.startCount = container.currentCount + type
            }
        }

        var count = container.startCount
        if (count < 0)
            count = 0
        container.currentCount = container.startCount
        inventory.clearCompletely()
        var i = 0
        var scheduleCounter = 0
        while (i < 45 && i + container.startCount < items.size) {
            if (inventory.getItem(i) == null || inventory.getItem(i).type == Material.AIR) {

                val slot = i
                val containerSlot = i + container.startCount
                val mountBlock = container.currentCount
                val currentPage = container.page
                count++
                if (i % 2 == 0) {
                    scheduleCounter++
                }

                Bukkit.getScheduler().runTaskLater(plugin, {
                    if (container.currentCount == mountBlock && currentPage == PetBlockManager.instance.pages[player]!!.page) {
                        inventory.setItem(slot, setPermissionLore(player, items[containerSlot].toItemStack(), items[containerSlot].position, groupPermission))
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

    private fun hasPermission(player: Player, position: Int, permission: String?): Boolean {
        val slot = position + 1
        if (player.hasPermission("$permission.all") || player.hasPermission("$permission.$slot")) {
            return true
        }

        player.sendMessage(Config.getInstance<Any>().prefix + Config.getInstance<Any>().noPermission)
        return false
    }

    private fun setPermissionLore(player: Player, itemStack: ItemStack, position: Int, permission: String?): ItemStack {
        if (itemStack.itemMeta != null && itemStack.itemMeta.lore != null) {
            var i = 0
            val meta = itemStack.itemMeta
            val lore = meta.lore
            while (i < lore.size) {
                if (player.hasPermission("$permission.all") || player.hasPermission("$permission.$position")) {
                    lore[i] = lore[i].replace("<permission>", com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.Config.permissionIconYes)
                } else {
                    lore[i] = lore[i].replace("<permission>", com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.Config.permissionIconNo)
                }
                i++
            }

            meta.lore = lore
            itemStack.itemMeta = meta
        }

        return itemStack
    }

    /**
     * Sets the given itemstack as new pet skin for the given [player].
     */
    private fun setCollectionSkinItemToPlayer(player: Player, guiItem: GUIItem) {
        persistenceService.getOrCreateFromPlayerUUID(player.uniqueId).thenAcceptSafely { petMeta ->
            petMeta.setSkin(MaterialCompatibility13.getMaterialFromId(guiItem.type).name, guiItem.data, guiItem.skin, guiItem.unbreakable)
            persistenceService.save(petMeta)
            PetBlockManager.instance.gui.setPage(player, GUIPage.MAIN, petMeta)
        }
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