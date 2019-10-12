@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.enumeration.ScriptAction
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiIconEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiPlayerCacheEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.StorageInventoryCache
import com.google.inject.Inject
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

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
class GUIServiceImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val petActionService: PetActionService,
    private val loggingService: LoggingService,
    private val loadService: GUIItemLoadService,
    private val persistenceService: PersistencePetMetaService,
    private val itemService: ItemTypeService,
    private val concurrencyService: ConcurrencyService,
    private val headDatabaseService: DependencyHeadDatabaseService,
    private val messageService: MessageService,
    private val proxyService: ProxyService,
    private val guiPetStorageService: GUIPetStorageService
) : GUIService {

    private val clickProtection = ArrayList<Any>()
    private val pageCache = HashMap<Any, GuiPlayerCache>()

    private var collectedMinecraftHeadsMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + "Pets collected by "
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

    private var suggestHeadMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + "Click here: "
        }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Submit skin<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/custom/heads-generator"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
        text { " " }
        component {
            color(ChatColor.YELLOW) {
                text {
                    ">>Suggest new pet<<"
                }
            }
            clickAction {
                ChatClickAction.OPEN_URL to "http://minecraft-heads.com/forum/suggesthead"
            }
            hover {
                text {
                    "Goto the Minecraft-Heads website!"
                }
            }
        }
    }

    private var skullNamingMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + configurationService.findValue("messages.customhead-suggest-prefix")
        }
        component {
            text {
                configurationService.findValue("messages.customhead-suggest-clickable")
            }
            clickAction {
                ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " skin "
            }
            hover {
                text {
                    configurationService.findValue("messages.customhead-suggest-hover")
                }
            }
        }
        text {
            configurationService.findValue("messages.customhead-suggest-suffix")
        }
    }

    private var namingMessage = chatMessage {
        text {
            configurationService.findValue<String>("messages.prefix") + configurationService.findValue("messages.rename-suggest-prefix")
        }
        component {
            text {
                configurationService.findValue("messages.rename-suggest-clickable")
            }
            clickAction {
                ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " rename "
            }
            hover {
                text {
                    configurationService.findValue("messages.rename-suggest-hover")
                }
            }
        }
        text {
            configurationService.findValue("messages.rename-suggest-suffix")
        }
    }

    /**
     * Closes the gui for the given [player]. Does nothing when the GUI is already closed.
     */
    override fun <P> close(player: P) {
        proxyService.closeInventory(player)
    }

    /**
     * Clears all resources the given player has allocated from this service.
     */
    override fun <P> cleanResources(player: P) {
        require(player is Any)

        if (pageCache.containsKey(player)) {
            pageCache.remove(player)
        }
    }

    /**
     * Returns if the given [inventory] matches the inventory of this service.
     */
    override fun <I> isGUIInventory(inventory: I, relativeSlot: Int): Boolean {
        require(inventory is Any)

        val holder = proxyService.getPlayerFromInventory<Any, I>(inventory) ?: return false
        val originInventory = proxyService.getLowerInventory(inventory)
        return this.pageCache.containsKey(holder) && this.pageCache[holder]!!.getInventory<Any>() == originInventory
    }

    /**
     * Opens the gui for the given [player]. Does nothing when the GUI is already open.
     */
    override fun <P> open(player: P, pageName: String?) {
        require(player is Any)

        proxyService.closeInventory(player)
        var page = pageName

        if (page == null) {
            page = "gui.main"
        }

        headDatabaseService.clearResources(player)

        val guiTitle = configurationService.findValue<String>("messages.gui-title")
        val inventory = proxyService.openInventory<Any, Any>(player, guiTitle, 54)
        val petMeta = persistenceService.getPetMetaFromPlayer(player)

        pageCache[player] = GuiPlayerCacheEntity(page, inventory)
        renderPage(player, petMeta, page)
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        require(player is Any)

        val optGuiItem = loadService.findClickedGUIItem(pageCache[player]!!.path, item) ?: return

        if (optGuiItem.permission.isNotEmpty() && !proxyService.hasPermission(player, optGuiItem.permission)) {
            if (!lockGui(player)) {
                proxyService.sendMessage(
                    player,
                    configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>(
                        "messages.no-permission"
                    )
                )
            }

            return
        }

        val petMeta = persistenceService.getPetMetaFromPlayer(player)

        if (optGuiItem.blockedCondition != null && isConditionMatching(petMeta, optGuiItem.blockedCondition!!)) {
            return
        }

        if (pageCache.containsKey(player)) {
            var pageCache = pageCache[player]!!

            if (optGuiItem.targetSkin != null) {
                val skin = optGuiItem.targetSkin!!

                if (skin.sponsored) {
                    sendSponsoringMessage(pageCache, player)
                }

                petMeta.skin.typeName = skin.typeName
                petMeta.skin.dataValue = skin.dataValue
                petMeta.skin.owner = skin.owner
                petMeta.skin.unbreakable = skin.unbreakable

                while (pageCache.parent != null) {
                    pageCache = pageCache.parent!!
                }

                this.pageCache[player] = pageCache
            }

            if (optGuiItem.icon.skin.sponsored) {
                sendSponsoringMessage(pageCache, player)
            }

            if (optGuiItem.targetPetName != null) {
                petMeta.displayName = optGuiItem.targetPetName!!
            }

            for (aiBase in optGuiItem.removeAIs.toTypedArray()) {
                petMeta.aiGoals.removeIf { a -> a.type == aiBase.type }
            }

            for (aiBase in optGuiItem.addAIs.toTypedArray()) {
                petMeta.aiGoals.add(aiBase)
            }

            renderPage(player, petMeta, this.pageCache[player]!!.path)
        }

        if (optGuiItem.script != null) {
            try {
                executeScript(player, optGuiItem, petMeta)
            } catch (e: Exception) {
                loggingService.warn("Failed to execute script '$optGuiItem.script'.")
            }
        }
    }

    /**
     * Renders a single gui page.
     */
    private fun renderPage(player: Any, petMeta: PetMeta, path: String) {
        val inventory = this.pageCache[player]!!.getInventory<Any>()
        proxyService.clearInventory(inventory)

        val items = loadService.findGUIItemCollection(path)

        if (items == null) {
            loggingService.warn("Failed to load gui path '$path'.")
            return
        }

        for (item in items) {
            if (item.hidden) {
                continue
            }

            if (item.hiddenCondition != null && isConditionMatching(petMeta, item.hiddenCondition!!)) {
                continue
            }

            var hasPermission = true

            if (item.permission.isNotEmpty()) {
                hasPermission = proxyService.hasPermission(player, item.permission)

                if (!hasPermission && item.hiddenCondition != null && item.hiddenCondition!!.contains("no-permission")) {
                    continue
                }
            }

            val position = if (item.fixed) {
                item.position
            } else {
                scrollCollection(player, item.position)
            }

            if (position < 0 || position > 53) {
                continue
            }

            if (item.icon.script != null) {
                val scriptResult = getScriptActionFromScript(item.icon.script!!)

                if (scriptResult == ScriptAction.COPY_PET_SKIN) {
                    val guiIcon = GuiIconEntity()
                    guiIcon.displayName = petMeta.displayName

                    with(guiIcon.skin) {
                        typeName = petMeta.skin.typeName
                        dataValue = petMeta.skin.dataValue
                        owner = petMeta.skin.owner
                        unbreakable = petMeta.skin.unbreakable
                    }

                    renderIcon(inventory, position, guiIcon, hasPermission)
                } else if (scriptResult == ScriptAction.HIDE_RIGHT_SCROLL && item.script != null) {
                    val offsetData = Pair(item.script!!.split(" ")[1].toInt(), item.script!!.split(" ")[2].toInt())

                    val cachedData = Pair(pageCache[player]!!.offsetX, pageCache[player]!!.offsetY)
                    pageCache[player]!!.offsetX += offsetData.first

                    var found = false

                    if (offsetData.first > 0) {
                        for (s in items) {
                            if (s.hidden) {
                                continue
                            }

                            val pos = scrollCollection(player, s.position)

                            if (pos in 0..53) {
                                found = true
                            }
                        }
                    }

                    pageCache[player]!!.offsetX = cachedData.first
                    pageCache[player]!!.offsetY = cachedData.second

                    if (found) {
                        renderIcon(inventory, position, item.icon, hasPermission)
                    }
                } else if (scriptResult == ScriptAction.HIDE_LEFT_SCROLL && item.script != null) {
                    val offsetData = Pair(item.script!!.split(" ")[1].toInt(), item.script!!.split(" ")[2].toInt())

                    if (offsetData.first < 0) {
                        if (pageCache[player]!!.offsetX > 0) {
                            renderIcon(inventory, position, item.icon, hasPermission)
                        }

                        continue
                    }
                }
            } else {
                renderIcon(inventory, position, item.icon, hasPermission)
            }
        }

        fillEmptySlots(inventory)
        proxyService.updateInventory(player)
    }

    /**
     * Renders a gui Icon.
     */
    private fun renderIcon(inventory: Any, position: Int, guiIcon: GuiIcon, hasPermission: Boolean) {
        if (position < 0) {
            return
        }

        val permissionMessage = if (hasPermission) {
            configurationService.findValue("messages.has-permission")
        } else {
            configurationService.findValue<String>("messages.has-no-permission")
        }

        val lore = ArrayList<String>()

        for (line in guiIcon.lore) {
            lore.add(line.replace("<permission>", permissionMessage).translateChatColors())
        }

        val item = ItemEntity(
            guiIcon.skin.typeName,
            guiIcon.skin.dataValue,
            guiIcon.skin.unbreakable,
            guiIcon.displayName,
            lore,
            guiIcon.skin.owner
        )
        val itemStack = itemService.toItemStack<Any>(item)

        try {
            proxyService.setInventoryItem(inventory, position, itemStack)
        } catch (e: Exception) {
            // Inventory might not be available.
        }
    }

    /**
     * Checks if the condition holds or not.
     */
    private fun isConditionMatching(petMeta: PetMeta, names: Array<String>): Boolean {
        for (name in names) {
            for (aiGoal in petMeta.aiGoals) {
                if (aiGoal.type.equals(name, true)) {
                    return true
                }
            }

            if (petMeta.enabled && name.equals("pet-enabled", true)) {
                return true
            }

            if (!petMeta.enabled && name.equals("pet-disabled", true)) {
                return true
            }

            if (petMeta.soundEnabled && name.equals("sound-enabled", true)) {
                return true
            }

            if (!petMeta.soundEnabled && name.equals("sound-disabled", true)) {
                return true
            }

            if (petMeta.particleEnabled && name.equals("particle-enabled", true)) {
                return true
            }

            if (!petMeta.particleEnabled && name.equals("particle-disabled", true)) {
                return true
            }
        }

        return false
    }

    /**
     * Sends the sponsoring message to the given player.
     */
    private fun sendSponsoringMessage(guiPlayerCache: GuiPlayerCache, player: Any) {
        val current = Date().time
        val difference = current - guiPlayerCache.advertisingMessageTime
        val timeOut = 2 * 60 * 1000

        if (difference > timeOut) {
            messageService.sendPlayerMessage(player, collectedMinecraftHeadsMessage)
            guiPlayerCache.advertisingMessageTime = current
        }
    }

    /**
     * Fills up the given [inventory] with the default item.
     */
    private fun fillEmptySlots(inventory: Any) {
        val guiItem = loadService.findGUIItemCollection("static-gui")!![0]
        val airType = itemService.findItemType<Any>(MaterialType.AIR)

        for (i in 0..53) {
            val item = proxyService.getInventoryItem<Any, Any>(inventory, i)

            if (item == null || itemService.findItemType<Any>(item) == airType) {
                renderIcon(inventory, i, guiItem.icon, true)
            }
        }
    }

    /**
     * Locks the GUI against spamming.
     * Returns if locked.
     */
    private fun lockGui(player: Any): Boolean {
        if (clickProtection.contains(player)) {
            return true
        }

        this.clickProtection.add(player)

        sync(concurrencyService, 10L) {
            this.clickProtection.remove(player)
        }

        return false
    }

    /**
     * Gets the scriptAction from the given script.
     */
    private fun getScriptActionFromScript(script: String): ScriptAction {
        var scriptResult = ScriptAction.NONE

        for (action in ScriptAction.values()) {
            if (action != ScriptAction.NONE && script.toLowerCase().startsWith(action.action.toLowerCase())) {
                scriptResult = action
                break
            }
        }

        return scriptResult
    }

    /**
     * Executes the given scripts.
     */
    private fun executeScript(player: Any, guiItem: GuiItem, petMeta: PetMeta) {
        val scriptResult = getScriptActionFromScript(guiItem.script!!)

        if (scriptResult == ScriptAction.SCROLL_PAGE) {
            val result = Pair(guiItem.script!!.split(" ")[1].toInt(), guiItem.script!!.split(" ")[2].toInt())
            pageCache[player]!!.offsetX += result.first
            pageCache[player]!!.offsetY += result.second

            renderPage(player, petMeta, pageCache[player]!!.path)
        } else if (scriptResult == ScriptAction.PRINT_CUSTOM_SKIN_MESSAGE) {
            messageService.sendPlayerMessage(player, skullNamingMessage)
            this.close(player)
        } else if (scriptResult == ScriptAction.PRINT_SUGGEST_HEAD_MESSAGE) {
            messageService.sendPlayerMessage(player, suggestHeadMessage)
            this.close(player)
        } else if (scriptResult == ScriptAction.PRINT_CUSTOM_NAME_MESSAGE) {
            messageService.sendPlayerMessage(player, namingMessage)
            this.close(player)
        } else if (scriptResult == ScriptAction.DISABLE_PET) {
            petActionService.disablePet(player)
            this.close(player)
        } else if (scriptResult == ScriptAction.CONNECT_HEAD_DATABASE) {
            headDatabaseService.openConnection(player)
        } else if (scriptResult == ScriptAction.CALL_PET) {
            petActionService.callPet(player)
            this.close(player)
        } else if (scriptResult == ScriptAction.LAUNCH_CANNON) {
            petActionService.launchPet(player)
            this.close(player)
        } else if (scriptResult == ScriptAction.ENABLE_SOUND) {
            petMeta.soundEnabled = true
            renderPage(player, petMeta, pageCache[player]!!.path)
        } else if (scriptResult == ScriptAction.DISABLE_SOUND) {
            petMeta.soundEnabled = false
            renderPage(player, petMeta, pageCache[player]!!.path)
        } else if (scriptResult == ScriptAction.ENABLE_PARTICLES) {
            petMeta.particleEnabled = true
            renderPage(player, petMeta, pageCache[player]!!.path)
        } else if (scriptResult == ScriptAction.DISABLE_PARTICLES) {
            petMeta.particleEnabled = false
            renderPage(player, petMeta, pageCache[player]!!.path)
        } else if (scriptResult == ScriptAction.SHOW_INVENTORY) {
            close(player)
            sync(concurrencyService, 10L) {
                val split = guiItem.script!!.split(" ")
                guiPetStorageService.openStorage(player, petMeta, split[1].toInt(), split[2].toInt())
            }
        } else if (scriptResult == ScriptAction.CLOSE_GUI) {
            val page = pageCache[player]!!

            if (page.parent == null) {
                this.close(player)
            } else {
                pageCache[player] = page.parent!!
                renderPage(player, petMeta, pageCache[player]!!.path)
            }
        } else if (scriptResult == ScriptAction.OPEN_PAGE) {
            val parent = pageCache[player]!!
            pageCache[player] = GuiPlayerCacheEntity(
                guiItem.script!!.split(" ")[1],
                parent.getInventory(),
                parent.advertisingMessageTime
            )
            pageCache[player]!!.parent = parent
            renderPage(player, petMeta, guiItem.script!!.split(" ")[1])
        }
    }

    /**
     * Scrolls the page to the axes.
     */
    private fun scrollCollection(player: Any, sourcePosition: Int): Int {
        val vPosition = sourcePosition % 54
        val multiplier = sourcePosition / 54

        val offsetX = pageCache[player]!!.offsetX
        val row = vPosition / 9
        var column = (vPosition % 9) + multiplier * 9

        column -= offsetX

        if (column < 0 || column > 8) {
            return -1
        }

        return row * 9 + column
    }
}