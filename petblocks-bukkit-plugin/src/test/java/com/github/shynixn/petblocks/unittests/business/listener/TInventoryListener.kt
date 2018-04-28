package com.github.shynixn.petblocks.unittests.business.listener

import com.github.shynixn.petblocks.api.business.service.GUIService
import com.github.shynixn.petblocks.bukkit.logic.business.listener.InventoryListener
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.mockito.Mockito
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
class TInventoryListener {

    /**
     * Given
     *      valid InventoryClickEvent on [ItemStack] in a valid GUI
     * when
     *     playerClickInInventoryEvent is called
     * then
     *     the GUIService guiService.clickInventoryItem should be called.
     */
    @org.junit.Test
    fun playerClickInInventoryEvent_GUIItemInventoryClickEvent_ShouldCallGUIService() {
        // Arrange
        val guiService = MockedGUIService()
        val classUnderTest = Companion.createWithDependencies(guiService)
        val clickEvent = Companion.createClickEvent(ItemStack(Material.APPLE))

        // Act
        classUnderTest.playerClickInInventoryEvent(clickEvent)

        // Assert
        assertTrue(guiService.called)
    }

    /**
     * Given
     *     invalid InventoryClickEvent on null [ItemStack] in sa valid GUI
     * when
     *     playerClickInInventoryEvent is called
     * then
     *     the GUIService guiService.clickInventoryItem should not be called.
     */
    @org.junit.Test
    fun playerClickInInventoryEvent_NullItemInventoryClickEvent_ShouldNotCallGUIService() {
        // Arrange
        val guiService = MockedGUIService()
        val classUnderTest = Companion.createWithDependencies(guiService)
        val clickEvent = Companion.createClickEvent(null)

        // Act
        classUnderTest.playerClickInInventoryEvent(clickEvent)

        // Assert
        assertFalse(guiService.called)
    }

    /**s
     * Given
     *     invalid InventoryClickEvent on air [ItemStack] in a valid GUI
     * when
     *     playerClickInInventoryEvent is called
     * then
     *     the GUIService guiService.clickInventoryItem should not be called.
     */
    @org.junit.Test
    fun playerClickInInventoryEvent_AirItemInventoryClickEvent_ShouldNotCallGUIService() {
        // Arrange
        val guiService = MockedGUIService()
        val classUnderTest = Companion.createWithDependencies(guiService)
        val clickEvent = Companion.createClickEvent(ItemStack(Material.AIR))

        // Act
        classUnderTest.playerClickInInventoryEvent(clickEvent)

        // Assert
        assertFalse(guiService.called)
    }

    //region Region Helper

    object Companion {

        fun createWithDependencies(guiService: GUIService? = null): InventoryListener {

            val testPlugin = Mockito.mock(Plugin::class.java)
            val testPluginManager = Mockito.mock(PluginManager::class.java)

            if (guiService == null) {
                return InventoryListener(MockedGUIService(), testPlugin, testPluginManager)
            } else {
                return InventoryListener(guiService, testPlugin, testPluginManager)
            }
        }

        fun createClickEvent(itemStack: ItemStack?): InventoryClickEvent {
            val clickEvent = Mockito.mock(InventoryClickEvent::class.java)
            Mockito.`when`(clickEvent.currentItem).then({
                itemStack
            })
            Mockito.`when`(clickEvent.whoClicked).then({
                Mockito.mock(Player::class.java)!!
            })

            return clickEvent
        }
    }


    class MockedGUIService : GUIService {
        var called = false

        /**
         * Returns if the given [inventory] matches the inventory of this service.
         */
        override fun <I> isGUIInventory(inventory: I): Boolean {
            return true
        }

        /**
         * Executes actions when the given [player] clicks on an [item].
         */
        override fun <P, I> clickInventoryItem(player: P, item: I) {
            called = true
        }
    }

    //endregion
}