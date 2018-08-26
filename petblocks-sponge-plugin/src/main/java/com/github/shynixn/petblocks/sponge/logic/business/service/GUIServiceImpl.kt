package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.GUIScriptService
import com.github.shynixn.petblocks.api.business.service.GUIService
import com.github.shynixn.petblocks.api.business.service.PersistenceService
import com.github.shynixn.petblocks.api.persistence.entity.ChatMessage
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.core.logic.business.entity.GuiPageContainer
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerGUICache
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager
import com.github.shynixn.petblocks.sponge.logic.business.helper.*
import com.google.inject.Inject
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.type.CarriedInventory
import org.spongepowered.api.item.inventory.type.GridInventory
import org.spongepowered.api.plugin.PluginContainer

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
class GUIServiceImpl @Inject constructor(private val configurationService: ConfigurationService, private val plugin: PluginContainer, private val scriptService: GUIScriptService, private val persistenceService: PersistenceService) : GUIService {
    private val pageCache = HashMap<Player, PlayerGUICache>()
    private val petBlocksManager = PetBlocksManager.petBlocksManager!!
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
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        player.closeInventory()
    }

    /**
     * Opens the gui for the given [player]. Does nothing when the GUI is already open.
     */
    override fun <P> open(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        petBlocksManager.gui.open(player)

        persistenceService.getOrCreateFromPlayer(player).thenAccept({ petMeta ->
            petBlocksManager.gui.setPage(player, GUIPage.MAIN, petMeta)
        })
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be a SpongeItemStack!")
        }

        if (!pageCache.containsKey(player)) {
            this.pageCache[player] = PlayerGUICache()
        }

        if (petBlocksManager.pages[player]!!.page == GUIPage.CUSTOM_COLLECTION && !configurationService.findClickedGUIItem(item).isPresent) {
            val optItems = configurationService.findGUIItemCollection(pageCache[player]!!.path!!)
            if (!optItems.isPresent || optItems.get().isEmpty()) {
                return
            }

            val itemSlot = relativeSlot + petBlocksManager.pages[player]!!.currentCount

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

        val scriptResult = scriptService.executeScript(petBlocksManager.inventories[player], script.get())

        when {
            scriptResult.action == ScriptAction.LOAD_COLLECTION -> loadCollectionPage(petBlocksManager.inventories[player], scriptResult.path.get(), scriptResult.permission.get())
            scriptResult.action == ScriptAction.SCROLL_COLLECTION -> scrollCollectionPage(player, scriptResult.valueContainer.get() as Int)
            scriptResult.action == ScriptAction.RENAME_PET -> sendGuiMessage(player, configurationService.findValue("messages.naming-suggest"), scriptResult.permission.get())
            scriptResult.action == ScriptAction.CUSTOM_SKIN -> sendGuiMessage(player, configurationService.findValue("messages.skullnaming-suggest"), scriptResult.permission.get())
        }

        sync(plugin, 5L) {
            player.updateInventory()
        }
    }

    /**
     * Clears all resources the given player has allocated from this service.
     */
    override fun <P> cleanResources(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
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
            throw IllegalArgumentException("Inventory has to be a SpongeInventory!")
        }

        if (inventory !is CarriedInventory<*>) {
            return false
        }

        if (!inventory.carrier.isPresent) {
            return false
        }

        return petBlocksManager.inventories.containsKey(inventory.carrier.get())
    }

    /**
     * Scrolls the loaded collection page of a [player] the given [amountOfSlots] to the right when entered a positive value.
     * Scrolls to the left when given a negative amount of Slots value.
     */
    override fun <P> scrollCollectionPage(player: P, amountOfSlots: Int) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a SpongePlayer!")
        }

        if (!pageCache.containsKey(player)) {
            this.pageCache[player] = PlayerGUICache()
        }

        if (!petBlocksManager.pages.containsKey(player) || petBlocksManager.pages[player]!!.page != GUIPage.CUSTOM_COLLECTION) {
            return
        }

        val path = pageCache[player]!!.path ?: return
        val permission = pageCache[player]!!.permission ?: return
        val optItems = configurationService.findGUIItemCollection(path)

        if (optItems.isPresent) {
            if (amountOfSlots < 0) {
                setItemsToInventory(player, petBlocksManager.inventories[player]!! as CarriedInventory<Player>, amountOfSlots, optItems.get(), permission)
            } else {
                setItemsToInventory(player, petBlocksManager.inventories[player]!! as CarriedInventory<Player>, amountOfSlots, optItems.get(), permission)
            }
        }
    }

    /**
     * Loads the collection from the given [path] into the given [inventory].
     */
    override fun <I> loadCollectionPage(inventory: I, path: String, permission: String?) {
        if (inventory !is Inventory) {
            throw IllegalArgumentException("Inventory has to be an SpongeInventory!")
        }

        if (inventory !is CarriedInventory<*>) {
            return
        }

        val player = (inventory as CarriedInventory<Player>).carrier.get()

        if (!pageCache.containsKey(player)) {
            this.pageCache[player] = PlayerGUICache()
        }

        val optItems = configurationService.findGUIItemCollection(path)

        if (path.startsWith("minecraft-heads-com.")) {
            player.sendMessage(collectedMinecraftHeadsMessage)
        }

        if (optItems.isPresent) {
            this.pageCache[player]!!.path = path
            this.pageCache[player]!!.permission = permission
            setItemsToInventory(player, inventory, 45, optItems.get(), permission)
        }
    }

    private fun setItemsToInventory(player: Player, inventory: CarriedInventory<Player>, type: Int, items: List<GUIItem>, groupPermission: String?) {
        val previousContainer = petBlocksManager.pages[player]
        val container: GuiPageContainer
        val page = GUIPage.CUSTOM_COLLECTION

        if (previousContainer!!.page != page) {
            container = GuiPageContainer(page, previousContainer)
            petBlocksManager.pages[player] = container
        } else {
            container = petBlocksManager.pages[player]!!
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
        inventory.clear()
        var i = 0
        var scheduleCounter = 0
        while (i < 45 && i + container.startCount < items.size) {
            val invItem = inventory.getItem(i)
            if (invItem == null || invItem.type == ItemTypes.AIR) {
                val slot = i
                val containerSlot = i + container.startCount
                val mountBlock = container.currentCount
                val currentPage = container.page
                count++
                if (i % 2 == 0) {
                    scheduleCounter++
                }

                sync(plugin, scheduleCounter.toLong()) {
                    if (container.currentCount == mountBlock && currentPage == petBlocksManager.pages[player]!!.page) {
                        inventory.setItem(slot, setPermissionLore(player, items[containerSlot].toItemStack(), items[containerSlot].position, groupPermission))
                    }
                }
            }
            i++
        }
        container.startCount = count

        val optBackGuiItemContainer = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("back")
        if (!optBackGuiItemContainer.isPresent) {
            throw IllegalArgumentException("Gui item back could not be loaded correctly!")
        }

        inventory.setItem(optBackGuiItemContainer.get().position, optBackGuiItemContainer.get().generate(player) as ItemStack)

        if (!(container.startCount % 45 != 0 || items.size == container.startCount)) {
            val optNextPage = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("next-page")
            if (!optNextPage.isPresent) {
                throw IllegalArgumentException("Gui item next-page could not be loaded correctly!")
            }

            inventory.setItem(optNextPage.get().position, optNextPage.get().generate(player) as ItemStack)
        }

        if (container.currentCount != 0) {
            val optPreviousPage = Config.getInstance<Player>().guiItemsController.getGUIItemFromName("previous-page")
            if (!optPreviousPage.isPresent) {
                throw IllegalArgumentException("Gui item previous-page could not be loaded correctly!")
            }

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
        if (itemStack.getLore().isNotEmpty()) {
            var i = 0
            val lore = itemStack.getLore()
            while (i < lore.size) {
                if (player.hasPermission("$permission.all") || player.hasPermission("$permission.$position")) {
                    lore[i] = lore[i].replace("<permission>", com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config.permissionIconYes)
                } else {
                    lore[i] = lore[i].replace("<permission>", com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config.permissionIconNo)
                }

                i++
            }

            itemStack.setLore(lore)
        }

        return itemStack
    }

    /**
     * Sets the given itemstack as new pet skin for the given [player].
     */
    private fun setCollectionSkinItemToPlayer(player: Player, guiItem: GUIItem) {
        persistenceService.getFromPlayer(player).thenAccept { p ->
            val petMeta = p.get()
            petMeta.setSkin(CompatibilityItemType.getFromId(guiItem.type).name, guiItem.data, guiItem.skin, guiItem.unbreakable)
            petBlocksManager.gui.setPage(player, GUIPage.MAIN, petMeta)

            async(plugin) {
                persistenceService.save(petMeta)
            }
        }
    }

    /**
     * Fills up the given [inventory] with the default item.
     */
    private fun fillEmptySlots(inventory: CarriedInventory<Player>) {
        val player = inventory.carrier.get()

        for (i in 0..53) {
            inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(ItemTypes.AIR)
                    .offer(com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config.guiItemsController.getGUIItemFromName("empty-slot").get().generate(player) as ItemStack)
        }
    }

    /**
     * Sends a gui action message.
     */
    private fun sendGuiMessage(player: Player, message: ChatMessage, permission: String) {
        if (player.hasPermission(permission)) {
            player.sendMessage(message)
        } else {
            player.sendMessage(configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.no-perms"))
        }
    }
}