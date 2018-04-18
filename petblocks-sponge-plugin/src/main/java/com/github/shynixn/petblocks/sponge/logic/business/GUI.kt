package com.github.shynixn.petblocks.sponge.logic.business

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.core.logic.business.entity.GuiPageContainer
import com.github.shynixn.petblocks.sponge.logic.business.helper.translateToText
import com.github.shynixn.petblocks.sponge.logic.business.helper.updateInventory
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.google.inject.Inject
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.ItemTypes
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.InventoryArchetypes
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.item.inventory.property.InventoryTitle
import org.spongepowered.api.item.inventory.property.SlotIndex
import org.spongepowered.api.item.inventory.property.SlotPos
import org.spongepowered.api.item.inventory.type.GridInventory
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.util.concurrent.TimeUnit

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
class GUI {
    @Inject
    private lateinit var manager: PetBlocksManager
    @Inject
    private lateinit var plugin: PluginContainer

    /**
     * Opens the gui for a player
     *
     * @param player player
     */
    fun open(player: Player) {
        if (this.manager.inventories.containsKey(player)) {
            this.closeInventory(player)
            this.manager.inventories.remove(player)
        }

        if (player.openInventory.isPresent) {
            this.closeInventory(player)
        }

        val inventory = Inventory.builder()
                .of(InventoryArchetypes.DOUBLE_CHEST)
                .property(
                        InventoryTitle.PROPERTY_NAME,
                        InventoryTitle.of(Config.guiTitle.translateToText())
                )
                .build(plugin)
        this.manager.inventories[player] = inventory
        player.openInventory(inventory)
    }

    /**
     * Sets a specific GUI page
     *
     * @param player  player
     * @param page    page
     * @param petMeta meta
     */
    fun setPage(player: Player, page: GUIPage, petMeta: PetMeta) {
        if (!this.manager.inventories.containsKey(player)) {
            return
        }

        val inventory = this.manager.inventories[player]
        clearInventory(inventory!!)

        when (page) {
            GUIPage.MAIN -> {
                this.setOtherItems(player, inventory, petMeta, GUIPage.MAIN)
                this.manager.pages[player] = GuiPageContainer(GUIPage.MAIN, null)
            }
            GUIPage.WARDROBE -> {
                this.setOtherItems(player, inventory, petMeta, page)
                this.manager.pages[player] = GuiPageContainer(page, this.manager.pages[player])
            }
            else -> this.setListAble(player, page, 0)
        }

        val optBackGuiItemContainer = Config.guiItemsController.getGUIItemFromName("back")
        if (!optBackGuiItemContainer.isPresent) {
            throw IllegalArgumentException("Gui item back could not be loaded correctly!")
        }

        this.setItem(inventory, optBackGuiItemContainer.get().position, optBackGuiItemContainer.get().generate(player) as ItemStack)
        this.fillEmptySlots(inventory, player)
        player.updateInventory()
    }

    /**
     * Moves a player in the GUI back to the previous GUI page it opened
     *
     * @param player  player
     * @param petMeta petMeta
     */
    fun backPage(player: Player, petMeta: PetMeta) {
        val container = this.manager.pages[player]!!

        if (container.page == GUIPage.MAIN) {
            this.closeInventory(player)
        } else {
            if (container.previousPage?.previousPage != null) {
                this.manager.pages[player] = container.previousPage.previousPage
            }

            this.setPage(player, container.previousPage.page, petMeta)
        }
    }

    /**
     * Moves a listable already opened GUi page one page forward or backwards
     *
     * @param player  player
     * @param forward forwards
     */
    fun moveList(player: Player, forward: Boolean) {
        if (forward) {
            this.setListAble(player, this.manager.pages[player]!!.page, 1)
        } else {
            this.setListAble(player, this.manager.pages[player]!!.page, 2)
        }
    }

    /**
     * Sets a listable page
     *
     * @param player player
     * @param page   page
     * @param type   moveType
     */
    private fun setListAble(player: Player, page: GUIPage, type: Int) {
        when (page) {
            GUIPage.ENGINES -> this.setEngineItems(player, type)
            GUIPage.PARTICLES -> this.setParticleItems(player, type)
            GUIPage.DEFAULT_COSTUMES -> this.setSimpleBlockItems(player, type)
            GUIPage.COLOR_COSTUMES -> this.setColorBlockItems(player, type)
            GUIPage.CUSTOM_COSTUMES -> this.setPlayerHeadItems(player, type)
            GUIPage.MINECRAFTHEADS_COSTUMES -> this.setMinecraftHeadsCostumeItems(player, type)
        }
    }

    /**
     * Sets other GUI items
     *
     * @param player    player
     * @param inventory inventory
     * @param petMeta   petMeta
     * @param page      page
     */
    private fun setOtherItems(player: Player, inventory: Inventory, petMeta: PetMeta, page: GUIPage) {
        if (!this.manager.petBlockController.getFromPlayer(player).isPresent) {
            petMeta.isEnabled = false
        }

        Config.guiItemsController.all
                .filter { it.page == page }
                .forEach { this.setItem(inventory, it.position, it.generate(player) as ItemStack) }

        if (page == GUIPage.MAIN) {
            val myPetContainer = Config.guiItemsController.getGUIItemFromName("my-pet").get()
            this.setItem(inventory, myPetContainer.position, petMeta.headItemStack as ItemStack)
        }

        if (petMeta.isSoundEnabled) {
            val container = Config.guiItemsController.getGUIItemFromName("sounds-enabled-pet").get()
            if (page == container.page) {
                this.setItem(inventory, container.position, container.generate(player) as ItemStack)
            }
        } else {
            val container = Config.guiItemsController.getGUIItemFromName("sounds-disabled-pet").get()
            if (page == container.page) {
                this.setItem(inventory, container.position, container.generate(player) as ItemStack)
            }
        }

        if (!petMeta.isEnabled) {
            val container = Config.guiItemsController.getGUIItemFromName("enable-pet").get()
            if (page == container.page) {
                this.setItem(inventory, container.position, container.generate(player) as ItemStack)
            }
        } else {
            val container = Config.guiItemsController.getGUIItemFromName("disable-pet").get()
            if (page == container.page) {
                this.setItem(inventory, container.position, container.generate(player) as ItemStack)
            }
        }

        val container = Config.guiItemsController.getGUIItemFromName("minecraft-heads-costume").get()
        if (page == container.page) {
            this.setItem(inventory, container.position, container.generate(player, "minecraft-heads") as ItemStack)
        }
    }

    /**
     * Set engine items
     *
     * @param player player
     * @param type   type
     */
    private fun setEngineItems(player: Player, type: Int) {
        this.setCostumes(player, Config.engineController.allGUIItems, GUIPage.ENGINES, type, Permission.ALL_ENGINES)
    }

    /**
     * Set simple block items
     *
     * @param player player
     * @param type   type
     */
    private fun setSimpleBlockItems(player: Player, type: Int) {
        this.setCostumes(player, Config.ordinaryCostumesController.all, GUIPage.DEFAULT_COSTUMES, type, Permission.ALL_SIMPLEBLOCKCOSTUMES)
    }

    /**
     * Sets color block items
     *
     * @param player player
     * @param type   type
     */
    private fun setColorBlockItems(player: Player, type: Int) {
        this.setCostumes(player, Config.colorCostumesController.all, GUIPage.COLOR_COSTUMES, type, Permission.ALL_COLOREDBLOCKCOSTUMES)
    }

    /**
     * Sets playerHead costumes
     *
     * @param player player
     * @param type   type
     */
    private fun setPlayerHeadItems(player: Player, type: Int) {
        this.setCostumes(player, Config.rareCostumesController.all, GUIPage.CUSTOM_COSTUMES, type, Permission.ALL_PLAYERHEADCOSTUMES)
    }

    /**
     * Set particle items
     *
     * @param player player
     * @param type   type
     */
    private fun setParticleItems(player: Player, type: Int) {
        this.setCostumes(player, Config.particleController.all, GUIPage.PARTICLES, type, Permission.ALL_PARTICLES)
    }

    /**
     * Sets all minecraft-heads costumes
     *
     * @param player player
     */
    private fun setMinecraftHeadsCostumeItems(player: Player, type: Int) {
        this.setCostumes(player, Config.minecraftHeadsCostumesController.all, GUIPage.MINECRAFTHEADS_COSTUMES, type, Permission.ALL_MINECRAFTHEADCOSTUMES)
    }

    /**
     * Manages listable page setting
     *
     * @param player          player
     * @param containers      containers
     * @param page            page
     * @param type            type
     * @param groupPermission groupPermissions
     */
    private fun setCostumes(player: Player, containers: List<GUIItemContainer<Player>>, page: GUIPage, type: Int, groupPermission: Permission) {
        if (this.manager.inventories.containsKey(player)) {
            val previousContainer = this.manager.pages[player]!!
            val container: GuiPageContainer

            if (previousContainer.page != page) {
                container = GuiPageContainer(page, previousContainer)
                this.manager.pages[player] = container
            } else {
                container = this.manager.pages[player]!!
            }

            if (type == 1 && (container.startCount % 45 != 0 || containers.size == container.startCount)) {
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
            val inventory = this.costumePreparation(player)
            var i = 0
            var scheduleCounter = 4
            while (i < 45 && i + container.startCount < containers.size) {
                val containerSlot = i + container.startCount
                val mountBlock = container.currentCount
                val currentPage = container.page
                val slot = i
                count++
                if (i % 2 == 0) {
                    scheduleCounter++
                }
                Task.builder().delay(scheduleCounter.toLong(), TimeUnit.MILLISECONDS).execute(Runnable {
                    if (container.currentCount == mountBlock && currentPage == this.manager.pages[player]!!.page) {
                        this.setItem(inventory, slot, containers[containerSlot].generate(player, *groupPermission.permission) as ItemStack)
                    }
                }).submit(plugin)
                i++
            }
            container.startCount = count
            val backGuiItemContainer = Config.guiItemsController.getGUIItemFromName("back").get()
            this.setItem(inventory, backGuiItemContainer.position, backGuiItemContainer.generate(player) as ItemStack)
            if (!(container.startCount % 45 != 0 || containers.size == container.startCount)) {
                val nextPage = Config.guiItemsController.getGUIItemFromName("next-page").get()
                this.setItem(inventory, nextPage.position, nextPage.generate(player) as ItemStack)
            }
            if (container.currentCount != 0) {
                val previousPage = Config.guiItemsController.getGUIItemFromName("previous-page").get()
                this.setItem(inventory, previousPage.position, previousPage.generate(player) as ItemStack)
            }
            this.fillEmptySlots(inventory, player)
        }
    }

    /**
     * Prepares the costume inventory
     *
     * @param player player
     * @return inventory
     */
    private fun costumePreparation(player: Player): Inventory {
        val inventory = this.manager.inventories[player]!!
        this.clearInventory(inventory)
        return inventory
    }

    /**
     * Fills empty slots in the inventory with the default item
     *
     * @param inventory inventory
     */
    private fun fillEmptySlots(inventory: Inventory, player: Player) {
        for (i in 0..53) {
            inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(ItemTypes.AIR)
                    .offer(Config.guiItemsController.getGUIItemFromName("empty-slot").get().generate(player) as ItemStack)
        }

    }

    private fun closeInventory(player: Player) {
        if (this.manager.inventories.containsKey(player)) {
            this.manager.inventories.remove(player)
        }
        player.closeInventory()
    }

    private fun setItem(inventory: Inventory, slot: Int, itemStack: ItemStack) {
        if (slot == 0) {
            inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(SlotPos.of(0, 0)).set(itemStack)
        } else {
            inventory.query<Inventory>(GridInventory::class.java)
                    .query<Inventory>(SlotIndex.of(slot)).set(itemStack)
        }
    }

    /**
     * Clears the inventory of a player
     *
     * @param inventory inventory
     */
    private fun clearInventory(inventory: Inventory) {
        inventory.clear()
    }
}