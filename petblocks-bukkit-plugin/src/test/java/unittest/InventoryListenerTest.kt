package unittest

import com.github.shynixn.petblocks.api.legacy.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.legacy.business.service.GUIPetStorageService
import com.github.shynixn.petblocks.api.legacy.business.service.GUIService
import com.github.shynixn.petblocks.api.legacy.persistence.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.logic.business.listener.InventoryListener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito

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
class InventoryListenerTest {
    /**
     * Given
     *    a player quit event with
     * When
     *    onPlayerQuitEvent is called
     * Then
     *    clearGUI service should be called.
     */
    @Test
    fun onPlayerQuitEvent_PlayerDisconnect_ShouldCallClear() {
        // Arrange
        val event = Mockito.mock(PlayerQuitEvent::class.java)
        val guiService = MockedGUIService()
        val classUnderTest = createWithDependencies(guiService)

        // Act
        classUnderTest.onPlayerQuitEvent(event)

        // Assert
        assertTrue(guiService.wasCleared)
    }

    /**
     * Given
     *    a player close inventory event with other inventory
     * When
     *    onPlayerCloseInventoryEvent is called
     * Then
     *    save should not be called.
     */
    @Test
    fun onPlayerCloseInventoryEvent_PlayerClosingStorageInventory_ShouldCallSave() {
        // Arrange
        val event = Mockito.mock(InventoryCloseEvent::class.java)
        val storageService = MockedStorageService(true)
        val classUnderTest = createWithDependencies(MockedGUIService(), storageService)

        // Act
        classUnderTest.onPlayerCloseInventoryEvent(event)

        // Assert
        assertTrue(storageService.wasSaveCalled)
    }

    /**
     * Given
     *    a player close inventory event with other inventory
     * When
     *    onPlayerCloseInventoryEvent is called
     * Then
     *    save should not be called.
     */
    @Test
    fun onPlayerCloseInventoryEvent_PlayerClosingOtherInventory_ShouldNotCallSave() {
        // Arrange
        val event = Mockito.mock(InventoryCloseEvent::class.java)
        val storageService = MockedStorageService(false)
        val classUnderTest = createWithDependencies(MockedGUIService(), storageService)

        // Act
        classUnderTest.onPlayerCloseInventoryEvent(event)

        // Assert
        assertFalse(storageService.wasSaveCalled)
    }

    companion object {
        fun createWithDependencies(
            guiService: GUIService = Mockito.mock(GUIService::class.java),
            storageService: GUIPetStorageService = Mockito.mock(GUIPetStorageService::class.java)
        ): InventoryListener {
            return InventoryListener(guiService, storageService, MockedConcurrencyService())
        }
    }

    private class MockedStorageService(var isStorageInventory: Boolean = false, var wasSaveCalled: Boolean = false) :
        GUIPetStorageService {
        override fun <P> openStorage(player: P, petMeta: PetMeta, from: Int, to: Int) {
        }

        override fun <I> isStorage(inventory: I): Boolean {
            return isStorageInventory
        }

        override fun <P> saveStorage(player: P) {
            wasSaveCalled = true
        }
    }

    private class MockedGUIService(var wasCleared: Boolean = false) : GUIService {
        override fun <P> open(player: P, pageName: String?) {
        }

        override fun <P> close(player: P) {
        }

        override fun <I> isGUIInventory(inventory: I, relativeSlot: Int): Boolean {
            return false
        }

        override fun <P> cleanResources(player: P) {
            wasCleared = true
        }

        override fun <P, I> clickInventoryItem(player: P, relativeSlot: Int, item: I) {
        }
    }

    private class MockedConcurrencyService : ConcurrencyService {
        override fun runTaskSync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }

        override fun runTaskAsync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }
    }
}
