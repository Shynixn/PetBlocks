package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.ChatClickAction
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.getSkin
import com.github.shynixn.petblocks.core.logic.business.extension.chatMessage
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
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
class DependencyHeadDatabaseServiceImpl @Inject constructor(private val dependencyService: DependencyService, private val configurationService: ConfigurationService, private val messageService: MessageService, private val petMetaService: PersistencePetMetaService, private val concurrencyService: ConcurrencyService) : DependencyHeadDatabaseService {
    private val headDatabasePlayers = HashSet<Player>()
    private var headDatabaseTitle: String? = null
    private var headDatabaseSearch: String? = null

    /**
     * Opens the virtual connection to the HeadDatabase plugin.
     * Prints a message to the console if connection is not possible.
     */
    override fun <P> openConnection(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        player.closeInventory()

        if (!dependencyService.isInstalled(PluginDependency.HEADDATABASE)) {
            val prefix = configurationService.findValue<String>("messages.prefix")
            val message = chatMessage {
                text {
                    prefix + "Download the plugin "
                }
                component {
                    color(ChatColor.YELLOW) {
                        text {
                            ">>Head Database<<"
                        }
                    }
                    clickAction {
                        ChatClickAction.OPEN_URL to "https://www.spigotmc.org/resources/14280/"
                    }
                    hover {
                        text {
                            "A valid spigot account is required!"
                        }
                    }
                }
            }

            messageService.sendPlayerMessage(player, message)
            player.sendMessage(prefix + ChatColor.GRAY + "Please consider that PetBlocks is not responsible for any legal agreements between the author of Head Database and yourself.")

            return
        }

        sync(concurrencyService, 10L) {
            headDatabasePlayers.add(player)
            player.performCommand("hdb")
        }
    }

    /**
     * Executes actions when the given [player] clicks on an [item] at the given [relativeSlot].
     * @param P the type of the player.
     * @param I the type of the inventory.
     */
    override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be a BukkitItemStack!")
        }

        if (!headDatabasePlayers.contains(player)) {
            return false
        }

        if (headDatabaseTitle == null) {
            val plugin = Bukkit.getPluginManager().getPlugin("HeadDatabase")
            this.headDatabaseTitle = this.constructPrefix(ChatColor.translateChatColorCodes('&', "&4&r" + plugin.config.getString("messages.database").split("%count%")[0]))
            this.headDatabaseSearch = this.constructPrefix(ChatColor.translateChatColorCodes('&', "&4&r" + plugin.config.getString("messages.search").split("%count%")[0]))
        }

        val currentTitleName = org.bukkit.ChatColor.stripColor(player.openInventory.topInventory.title)

        if (!currentTitleName.startsWith(this.headDatabaseTitle!!) && !currentTitleName.startsWith(this.headDatabaseSearch!!)) {
            return false
        }

        if (item.type != Material.SKULL_ITEM || item.itemMeta == null
                || item.itemMeta.displayName == null || !item.itemMeta.displayName.startsWith(org.bukkit.ChatColor.BLUE.toString())) {
            return false
        }

        player.closeInventory()

        petMetaService.getOrCreateFromPlayerUUID(player.uniqueId).thenAccept { petMeta ->
            petMeta.itemId = item.type.id
            petMeta.itemDamage = item.durability.toInt()
            petMeta.skin = item.getSkin().get()

            petMetaService.save(petMeta)

            val command = configurationService.findValue<String>("petblocks-gui.command")
            player.performCommand(command)
        }

        sync(concurrencyService, 5L) {
            for (itemStack in player.openInventory.topInventory.contents) {
                if (itemStack != null
                        && itemStack.itemMeta != null
                        && itemStack.itemMeta.displayName != null) {
                    if (itemStack.itemMeta.displayName == item.itemMeta.displayName) {
                        player.inventory.remove(itemStack)
                        player.updateInventory()
                        break
                    }
                }
            }
        }

        return true
    }

    /**
     * Clears all resources the given [player] has allocated from this service.
     */
    override fun <P> clearResources(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        if (headDatabasePlayers.contains(player)) {
            headDatabasePlayers.remove(player)
        }
    }


    /**
     * Constructs a prefix from the given source.
     *
     * @param source source
     * @return prefix
     */
    private fun constructPrefix(source: String): String {
        val b = StringBuilder()

        for (i in 0 until source.length) {
            val t = source[i]
            if (t != '%') {
                b.append(t)
            } else {
                return org.bukkit.ChatColor.stripColor(b.toString())
            }
        }

        return org.bukkit.ChatColor.stripColor(b.toString())
    }
}