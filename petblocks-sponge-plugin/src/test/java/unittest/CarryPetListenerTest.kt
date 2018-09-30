package unittest

import com.github.shynixn.petblocks.api.business.service.CarryPetService
import com.github.shynixn.petblocks.sponge.logic.business.listener.CarryPetListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.entity.living.Living
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.entity.DestructEntityEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import java.util.*
import java.util.concurrent.CompletableFuture

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
class CarryPetListenerTest {
    /**
     * Given
     *    a valid death event of a player
     * When
     *    onPlayerDeathEvent is called
     * Then
     *    isCarryingPet should be called.
     */
    @Test
    fun onPlayerDeathEvent_ValidPlayerDeath_ShouldCallCarryPet() {
        // Arrange
        val event = Mockito.mock(DestructEntityEvent.Death::class.java)
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(event.targetEntity).thenReturn(player)
        val mockedCarryPetService = MockedCarryPetService()
        val classUnderTest = createWithDependencies(mockedCarryPetService)

        // Act
        classUnderTest.onPlayerDeathEvent(event)

        // Assert
        Assertions.assertTrue(mockedCarryPetService.isCarryingPetCalled)
        Assertions.assertTrue(mockedCarryPetService.isDropPetCalled)
    }

    /**
     * Given
     *    a random death of any entity.
     * When
     *    onPlayerDeathEvent is called
     * Then
     *    isCarryingPet should not be called.
     */
    @Test
    fun onPlayerDeathEvent_EntityDeath_ShouldNotCallCarryPet() {
        // Arrange
        val event = Mockito.mock(DestructEntityEvent.Death::class.java)
        val entity = Mockito.mock(Living::class.java)
        Mockito.`when`(event.targetEntity).thenReturn(entity)
        val mockedCarryPetService = MockedCarryPetService()
        val classUnderTest = createWithDependencies(mockedCarryPetService)

        // Act
        classUnderTest.onPlayerDeathEvent(event)

        // Assert
        Assertions.assertFalse(mockedCarryPetService.isCarryingPetCalled)
    }

    /**
     * Given
     *    a valid quit event of a player
     * When
     *    onPlayerQuitEvent is called
     * Then
     *    resources should be cleared.
     */
    @Test
    fun onPlayerQuitEvent_ValidQuit_ShouldCallClearResources() {
        // Arrange
        val event = Mockito.mock(ClientConnectionEvent.Disconnect::class.java)
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(event.targetEntity).thenReturn(player)
        val mockedCarryPetService = MockedCarryPetService()
        val classUnderTest = createWithDependencies(mockedCarryPetService)

        // Act
        classUnderTest.onPlayerQuitEvent(event)

        // Assert
        Assertions.assertTrue(mockedCarryPetService.clearResourcesCalled)
    }

    companion object {
        fun createWithDependencies(carryPetService: CarryPetService? = MockedCarryPetService()): CarryPetListener {
            return if (carryPetService == null) {
                CarryPetListener(MockedCarryPetService())
            } else {
                CarryPetListener(carryPetService)
            }
        }
    }

    private class MockedCarryPetService : CarryPetService {
        var isCarryingPetCalled = false
        var isDropPetCalled = false
        var clearResourcesCalled = false

        override fun <P> carryPet(player: P) {
            throw IllegalArgumentException()
        }

        override fun <P> dropPet(player: P): CompletableFuture<Void?> {
            isDropPetCalled = true
            return CompletableFuture()
        }

        override fun <P> throwPet(player: P): CompletableFuture<Void?> {
            throw IllegalArgumentException()
        }

        override fun <P, I> getCarryPetItemStack(player: P): Optional<I> {
            throw IllegalArgumentException()
        }

        override fun <P> isCarryingPet(player: P): Boolean {
            isCarryingPetCalled = true
            return true
        }

        override fun <P> clearResources(player: P) {
            clearResourcesCalled = true
        }
    }
}