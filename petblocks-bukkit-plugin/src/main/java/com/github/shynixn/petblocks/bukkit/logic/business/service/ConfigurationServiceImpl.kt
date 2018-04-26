package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController
import com.github.shynixn.petblocks.api.persistence.entity.GUIItem
import com.github.shynixn.petblocks.core.logic.persistence.entity.CustomGUIItem
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import java.util.*

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
class ConfigurationServiceImpl @Inject constructor(private val plugin: Plugin, private val guiItemsController: OtherGUIItemsController<GUIItemContainer<Player>>) : ConfigurationService {

    /**
     * Tries to return a [GUIItem] matching the displayName and the lore of the given [item].
     * Can be called from Asynchronly.
     */
    override fun <I> findClickedGUIItem(item: I): Optional<GUIItem> {
        if (item !is ItemStack) {
            throw IllegalArgumentException("Item has to be an BukkitItemStack")
        }

        guiItemsController.all.forEach { i ->
            try {

                if (item.itemMeta.displayName == i.displayName.get()) {
                    val lore = i.lore.get()
                    if (item.itemMeta.lore.size == lore.size) {
                        return if ((0..item.itemMeta.lore.size).any { item.itemMeta.lore[it] != lore[it] }) Optional.empty() else Optional.of(CustomGUIItem(i))
                    }
                }
            } catch (e: Exception) {
                // Ignored
            }
        }

        return Optional.empty()
    }
}