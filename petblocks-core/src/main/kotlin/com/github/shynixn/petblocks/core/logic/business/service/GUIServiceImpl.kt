@file:Suppress("UNCHECKED_CAST", "DuplicatedCode")

package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.*
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiIconEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.GuiPlayerCacheEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.ItemEntity
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

    private var collectedMinecraftHeadsMessage = lazy {
        chatMessage {
            text {
                Messages.prefix + "Pets collected by "
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
    }

    private var suggestHeadMessage = lazy {
        chatMessage {
            text {
                Messages.prefix + "Click here: "
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
    }

    private var skullNamingMessage = lazy {
        chatMessage {
            text {
                Messages.prefix + Messages.customHeadSuggestPrefix
            }
            component {
                text {
                    Messages.customHeadSuggestClickable
                }
                clickAction {
                    ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " skin "
                }
                hover {
                    text {
                        Messages.customHeadSuggestHover
                    }
                }
            }
            text {
                Messages.customHeadSuggestSuffix
            }
        }
    }

    private var namingMessage = lazy {
        chatMessage {
            text {
                Messages.prefix + Messages.renameSuggestPrefix
            }
            component {
                text {
                    Messages.renameSuggestClickable
                }
                clickAction {
                    ChatClickAction.SUGGEST_COMMAND to "/" + configurationService.findValue("commands.petblock.command") + " rename "
                }
                hover {
                    text {
                        Messages.renameSuggestHover
                    }
                }
            }
            text {
                Messages.renameSuggestSuffix
            }
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

        if (page == null || !configurationService.containsValue(page)) {
            page = "gui.main"
        }

        headDatabaseService.clearResources(player)

        val inventory = proxyService.openInventory<Any, Any>(player, Messages.guiTitle, 54)
        val petMeta = persistenceService.getPetMetaFromPlayer(player)

        pageCache[player] = GuiPlayerCacheEntity(page, inventory)
        renderPage(player, petMeta, page)
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        require(player is Any)

        if (lockGui(player)) {
            return
        }

        val optGuiItem = loadService.findClickedGUIItem(pageCache[player]!!.path, item) ?: return

        if (optGuiItem.permission.isNotEmpty() && !proxyService.hasPermission(player, optGuiItem.permission)) {
            proxyService.sendMessage(player, Messages.prefix + Messages.noPermissionMessage)
            return
        }

        val petMeta = persistenceService.getPetMetaFromPlayer(player)

        if (optGuiItem.blockedConditions.isNotEmpty() && isConditionMatching(petMeta, optGuiItem.blockedConditions)) {
            return
        }

        if (optGuiItem.allowedConditions.isNotEmpty() && !isConditionMatching(petMeta, optGuiItem.allowedConditions)) {
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
                petMeta.skin.nbtTag = skin.nbtTag

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

            petActionService.applyAI(player, optGuiItem.addAIs, optGuiItem.removeAIs)
            renderPage(player, petMeta, this.pageCache[player]!!.path)
        }

        for (script in optGuiItem.scripts) {
            try {
                executeScript(player, petMeta, script)
            } catch (e: Exception) {
                loggingService.warn("Failed to execute script '$script'.", e)
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

            val hasPermission = hasGUIItemPermission(player, item)

            if (item.hiddenConditions.isNotEmpty() && isConditionMatching(petMeta, item.hiddenConditions, hasPermission)) {
                continue
            }

            if (item.showConditions.isNotEmpty() && !isConditionMatching(petMeta, item.showConditions, hasPermission)) {
                continue
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
                        nbtTag = petMeta.skin.nbtTag
                    }

                    renderIcon(inventory, position, guiIcon, hasPermission)
                } else if (scriptResult == ScriptAction.HIDE_RIGHT_SCROLL) {
                    for (script in item.scripts) {
                        try {
                            val scriptAction = getScriptActionFromScript(script)

                            if (scriptAction == ScriptAction.SCROLL_PAGE) {
                                val offsetData = Pair(script.split(" ")[1].toInt(), script.split(" ")[2].toInt())
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
                            }
                        } catch (e: Exception) {
                            loggingService.warn("Failed to execute script '$script'.")
                        }
                    }
                } else if (scriptResult == ScriptAction.HIDE_LEFT_SCROLL) {
                    for (script in item.scripts) {
                        try {
                            val scriptAction = getScriptActionFromScript(script)

                            if (scriptAction == ScriptAction.SCROLL_PAGE) {
                                val offsetData = Pair(script.split(" ")[1].toInt(), script.split(" ")[2].toInt())

                                if (offsetData.first < 0) {
                                    if (pageCache[player]!!.offsetX > 0) {
                                        renderIcon(inventory, position, item.icon, hasPermission)
                                    }

                                    continue
                                }
                            }

                        } catch (e: Exception) {
                            loggingService.warn("Failed to execute script '$script'.")
                        }
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
            Messages.permissionTranslationYes
        } else {
            Messages.permissionTranslationNo
        }

        val lore = ArrayList<String>()

        for (line in guiIcon.lore) {
            lore.add(line.replace("<permission>", permissionMessage).translateChatColors())
        }

        val item = ItemEntity(
            guiIcon.skin.typeName,
            guiIcon.skin.dataValue,
            guiIcon.skin.nbtTag,
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
     * HasPermission value can be ignored if a check for permission is not necessary.
     */
    private fun isConditionMatching(petMeta: PetMeta,conditions: List<String>, hasPermission : Boolean = true): Boolean {
        for (condition in conditions) {
            for (aiGoal in petMeta.aiGoals) {
                if (aiGoal.type.equals(condition, true)) {
                    return true
                }

                if (aiGoal.userId != null && condition.toLowerCase().endsWith(aiGoal.userId!!.toLowerCase())
                    && condition.toLowerCase().startsWith(aiGoal.type.toLowerCase())
                ) {
                    return true
                }
            }

            val conditionState = try {
                PetState.values().first { e -> e.description.equals(condition, true) }
            } catch (e: Exception) {
                return false
            }

            when (conditionState) {
                PetState.ENABLED -> {
                    return petMeta.enabled
                }
                PetState.DISABLED -> {
                    return !petMeta.enabled
                }
                PetState.SOUND_ENABLED -> {
                    return petMeta.soundEnabled
                }
                PetState.SOUND_DISABLED -> {
                    return !petMeta.soundEnabled
                }
                PetState.PARTICLE_ENABLED -> {
                    return petMeta.particleEnabled
                }
                PetState.PARTICLE_DISABLED -> {
                    return !petMeta.particleEnabled
                }
                PetState.PERMISSION_ENABLED -> {
                    return hasPermission
                }
                PetState.PERMISSION_DISABLED -> {
                    return !hasPermission
                }
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
            messageService.sendPlayerMessage(player, collectedMinecraftHeadsMessage.value)
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

        sync(concurrencyService, 5L) {
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
    private fun executeScript(player: Any, petMeta: PetMeta, script: String) {
        when (getScriptActionFromScript(script)) {
            ScriptAction.SCROLL_PAGE -> {
                val result = Pair(script.split(" ")[1].toInt(), script.split(" ")[2].toInt())
                pageCache[player]!!.offsetX += result.first
                pageCache[player]!!.offsetY += result.second

                renderPage(player, petMeta, pageCache[player]!!.path)
            }
            ScriptAction.PRINT_CUSTOM_SKIN_MESSAGE -> {
                messageService.sendPlayerMessage(player, skullNamingMessage.value)
                this.close(player)
            }
            ScriptAction.PRINT_SUGGEST_HEAD_MESSAGE -> {
                messageService.sendPlayerMessage(player, suggestHeadMessage.value)
                this.close(player)
            }
            ScriptAction.PRINT_CUSTOM_NAME_MESSAGE -> {
                messageService.sendPlayerMessage(player, namingMessage.value)
                this.close(player)
            }
            ScriptAction.DISABLE_PET -> {
                petActionService.disablePet(player)
                this.close(player)
            }
            ScriptAction.CONNECT_HEAD_DATABASE -> {
                headDatabaseService.openConnection(player)
            }
            ScriptAction.CALL_PET -> {
                petActionService.callPet(player)
                this.close(player)
            }
            ScriptAction.LAUNCH_CANNON -> {
                petActionService.launchPet(player)
                this.close(player)
            }
            ScriptAction.ENABLE_SOUND -> {
                petMeta.soundEnabled = true
                renderPage(player, petMeta, pageCache[player]!!.path)
            }
            ScriptAction.DISABLE_SOUND -> {
                petMeta.soundEnabled = false
                renderPage(player, petMeta, pageCache[player]!!.path)
            }
            ScriptAction.ENABLE_PARTICLES -> {
                petMeta.particleEnabled = true
                renderPage(player, petMeta, pageCache[player]!!.path)
            }
            ScriptAction.DISABLE_PARTICLES -> {
                petMeta.particleEnabled = false
                renderPage(player, petMeta, pageCache[player]!!.path)
            }
            ScriptAction.SHOW_INVENTORY -> {
                close(player)
                sync(concurrencyService, 10L) {
                    val split = script.split(" ")
                    guiPetStorageService.openStorage(player, petMeta, split[1].toInt(), split[2].toInt())
                }
            }
            ScriptAction.CLOSE_GUI -> {
                val page = pageCache[player]!!

                if (page.parent == null) {
                    this.close(player)
                } else {
                    pageCache[player] = page.parent!!
                    renderPage(player, petMeta, pageCache[player]!!.path)
                }
            }
            ScriptAction.OPEN_PAGE -> {
                val parent = pageCache[player]!!
                pageCache[player] = GuiPlayerCacheEntity(
                    script.split(" ")[1],
                    parent.getInventory(),
                    parent.advertisingMessageTime
                )
                pageCache[player]!!.parent = parent
                renderPage(player, petMeta, script.split(" ")[1])
            }
            ScriptAction.EXECUTE_PLAYERCOMMAND -> {
                val splitScript = script.split(" ")
                val command = splitScript.subList(1, splitScript.size)
                    .joinToString(" ")
                    .replace("<player>", proxyService.getPlayerName(player))
                proxyService.executePlayerCommand(player, command)
            }
            ScriptAction.EXECUTE_SERVERCOMMAND -> {
                val splitScript = script.split(" ")
                val command = splitScript.subList(1, splitScript.size)
                    .joinToString(" ")
                    .replace("<player>", proxyService.getPlayerName(player))
                proxyService.executeServerCommand(command)
            }
            else -> {
            }
        }
    }

    /**
     * Gets if the given player has got the permission defined at the gui item.
     */
    private fun hasGUIItemPermission(player: Any, guiItem: GuiItem): Boolean {
        if(guiItem.permission.isEmpty()){
            return true
        }

        return proxyService.hasPermission(player, guiItem.permission)
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