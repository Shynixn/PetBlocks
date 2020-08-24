@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.entity.PotionEffect
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import helper.MockedLoggingService
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
class PersistencePetMetaServiceTest {
    /**
     * Given
     *      one existing petMeta in the repository
     * When
     *      getOrCreateFromPlayerUUID by uuid is called
     * Then
     *     the petMeta stored of this uuid should be returned.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ExistingPetMeta_ShouldReturnThisPetMeta() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val player = "ecd66f19-3b5b-4910-b8e6-1716b5a636bf"

        // Act
        val petMeta = classUnderTest.getPetMetaFromPlayer(player)

        // Assert
        Assertions.assertEquals("Kenny", petMeta.playerMeta.name)
        Assertions.assertEquals("Cloud", petMeta.skin.owner)
    }

    /**
     * Given
     *     pet meta to be stored
     * When
     *     save is called
     * Then
     *     it should be stored in the repository.
     */
    @Test
    fun save_ValidPetMeta_ShouldCallSave() {
        // Arrange
        val petMetaRepository = MockedPetMetaRepository()
        val classUnderTest = createWithDependencies(petMetaRepository)

        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID().toString(), "Mario"), SkinEntity())
        petMeta.skin.owner = "Pikachu"
        petMeta.id = 2

        // Act
        classUnderTest.save(petMeta)

        // Assert
        Assertions.assertTrue(petMetaRepository.saveCalled)
    }

    /**
     * Given
     *     pet meta to be stored
     * When
     *     save is called
     * Then
     *     it should throw RunTimeException.
     */
    @Test
    fun save_InvalidPetMeta_ShouldCallSave() {
        // Arrange
        val petMetaRepository = MockedPetMetaRepository(true)
        val classUnderTest = createWithDependencies(petMetaRepository)

        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID().toString(), "Mario"), SkinEntity())
        petMeta.skin.owner = "Pikachu"
        petMeta.id = 2

        // Act
        Assertions.assertThrows(RuntimeException::class.java) {
            classUnderTest.save(petMeta).get()
        }
    }

    companion object {
        fun createWithDependencies(petMetaRepository: PetMetaRepository = MockedPetMetaRepository()): PersistencePetMetaService {
            return PersistencePetMetaServiceImpl(
                MockedProxyService(),
                petMetaRepository,
                MockedConcurrencyService(),
                MockedEventService(),
                Mockito.mock(AIService::class.java),
                MockedLoggingService()
            )
        }
    }

    class MockedPetMetaRepository(val shouldFail: Boolean = false) : PetMetaRepository {
        var saveCalled = false

        /**
         * Returns [List] with a list of stored [PetMeta].
         */
        override fun getAll(): List<PetMeta> {
            val petMeta1 = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID().toString(), "Batman"), SkinEntity())
            petMeta1.id = 1

            val petMeta2 = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID().toString(), "Mario"), SkinEntity())
            petMeta2.skin.owner = "Pikachu"
            petMeta2.id = 2

            return arrayListOf(petMeta1, petMeta2)
        }

        /**
         * Returns the petMeta of from the given player uniqueId. Creates
         * a new one if it does not exist yet. Gets it from the runtime when a pet
         * currently uses the meta data of the player.
         */
        override fun getOrCreateFromPlayerIdentifiers(name: String, uuid: String): PetMeta {
            if (uuid == "ecd66f19-3b5b-4910-b8e6-1716b5a636bf") {
                val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Kenny"), SkinEntity())
                petMeta.skin.owner = "Cloud"
                return petMeta
            }

            throw IllegalArgumentException()
        }

        /**
         * Saves the given [petMeta] instance and returns the same [petMeta] instance.
         */
        override fun save(petMeta: PetMeta): PetMeta {
            if (shouldFail) {
                throw IllegalArgumentException()

            }

            saveCalled = true

            return petMeta
        }
    }

    class MockedProxyService : ProxyService {
        /**
         * Applies the given [potionEffect] to the given [player].
         */
        override fun <P> applyPotionEffect(player: P, potionEffect: PotionEffect) {
        }

        /**
         * Gets a list of points between 2 locations.
         */
        override fun <L> getPointsBetweenLocations(location1: L, location2: L, amount: Int): List<L> {
            throw IllegalArgumentException()
        }

        /**
         * Drops the given item at the given position.
         */
        override fun <L, I> dropInventoryItem(location: L, item: I) {
            throw IllegalArgumentException()
        }

        /**
         * Gets the inventory item at the given index.
         */
        override fun <I, IT> getInventoryItem(inventory: I, index: Int): IT? {
            throw IllegalArgumentException()
        }

        /**
         * Gets if the given player has got the given permission.
         */
        override fun <P> hasPermission(player: P, permission: String): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Closes the inventory of the given player.
         */
        override fun <P> closeInventory(player: P) {
            throw IllegalArgumentException()
        }

        /**
         * Gets if the given inventory belongs to a player. Returns null if not.
         */
        override fun <P, I> getPlayerFromInventory(inventory: I): P? {
            throw IllegalArgumentException()
        }

        /**
         * Gets the lower inventory of an inventory.
         */
        override fun <I> getLowerInventory(inventory: I): I {
            throw IllegalArgumentException()
        }

        /**
         * Clears the given inventory.
         */
        override fun <I> clearInventory(inventory: I) {
            throw IllegalArgumentException()
        }

        /**
         * Opens a new inventory for the given player.
         */
        override fun <P, I> openInventory(player: P, title: String, size: Int): I {
            throw IllegalArgumentException()
        }

        /**
         * Updates the inventory.
         */
        override fun <I, IT> setInventoryItem(inventory: I, index: Int, item: IT) {
            throw IllegalArgumentException()
        }

        /**
         * Updates the given player inventory.
         */
        override fun <P> updateInventory(player: P) {
            throw IllegalArgumentException()
        }

        /**
         * Gets if the given instance can be converted to a player.
         */
        override fun <P> isPlayer(instance: P): Boolean {
            return true
        }

        /**
         * Gets the name of a player.
         */
        override fun <P> getPlayerName(player: P): String {
            return "Kenny"
        }

        /**
         * Gets the player from the given UUID.
         */
        override fun <P> getPlayerFromUUID(uuid: String): P {
            throw IllegalArgumentException()
        }

        /**
         * Gets the location of the player.
         */
        override fun <L, P> getPlayerLocation(player: P): L {
            throw IllegalArgumentException()
        }

        /**
         * Converts the given [location] to a [Position].
         */
        override fun <L> toPosition(location: L): Position {
            throw IllegalArgumentException()
        }

        /**
         * Gets the looking direction of the player.
         */
        override fun <P> getDirectionVector(player: P): Position {
            throw IllegalArgumentException()
        }

        /**
         * Gets the item in the player hand.
         */
        override fun <P, I> getPlayerItemInHand(player: P, offhand: Boolean): I? {
            throw IllegalArgumentException()
        }

        /**
         * Sets the item in the player hand.
         */
        override fun <P, I> setPlayerItemInHand(player: P, itemStack: I, offhand: Boolean) {
            throw IllegalArgumentException()
        }

        /**
         * Gets if the given player has got the given permission.
         */
        override fun <P> hasPermission(player: P, permission: Permission): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Gets the player uuid.
         */
        override fun <P> getPlayerUUID(player: P): String {
            if (player == "ecd66f19-3b5b-4910-b8e6-1716b5a636bf") {
                return "ecd66f19-3b5b-4910-b8e6-1716b5a636bf"
            }

            throw IllegalArgumentException()
        }

        /**
         * Sends a message to the [sender].
         */
        override fun <S> sendMessage(sender: S, message: String) {
            throw IllegalArgumentException()
        }

        /**
         * Executes a server command.
         */
        override fun executeServerCommand(message: String) {
            throw IllegalArgumentException()
        }

        /**
         * Executes a player command.
         */
        override fun <P> executePlayerCommand(player: P, message: String) {
            throw IllegalArgumentException()
        }
    }

    class MockedEventService : EventService {
        /**
         * Calls a framework event and returns if it was cancelled.
         */
        override fun callEvent(event: Any): Boolean {
            return true
        }
    }

    class MockedConcurrencyService : ConcurrencyService {
        /**
         * Runs the given [function] synchronised with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskSync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }

        /**
         * Runs the given [function] asynchronous with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskAsync(delayTicks: Long, repeatingTicks: Long, function: () -> Unit) {
            function.invoke()
        }
    }
}