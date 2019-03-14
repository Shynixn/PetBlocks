@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.EventService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.Position
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.`when`
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
                MockedEventService()
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

    class MockedPlayerProxy(
        override val uniqueId: String,
        override val handle: Any = "?",
        override val name: String = "tmp",
        override val position: Position = PositionEntity(20.2, 20.2, 20.2, 0.0, 0.0),
        override val isOnline: Boolean = true
    ) : PlayerProxy{
        /**
         * Sends a message to the player.
         */
        override fun sendMessage(text: String) {
            throw IllegalArgumentException()
        }

        /**
         * Sets the item at the given index in the inventory.
         */
        override fun <I> setInventoryItem(index: Int, itemstack: I) {
            throw IllegalArgumentException()
        }

        /**
         * Sets the item in the players hand.
         */
        override fun <I> setItemInHand(itemStack: I, offHand: Boolean) {
            throw IllegalArgumentException()
        }

        /**
         * Gets the item in the players hand.
         */
        override fun <I> getItemInHand(offHand: Boolean): I? {
            throw IllegalArgumentException()
        }

        /**
         * Gets the location of the player.
         */
        override fun <L> getLocation(): L {
            throw IllegalArgumentException()
        }

        /**
         * Gets if this player has got permissions.
         */
        override fun hasPermission(permission: Permission): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Updates the player inventory.
         */
        override fun updateInventory() {
            throw IllegalArgumentException()
        }

        /**
         * Generates a vector for the launching direction.
         */
        override fun getDirectionLaunchVector(): Position {
            throw IllegalArgumentException()
        }
    }

    class MockedProxyService : ProxyService {

        /**
         * Returns a player proxy object for the given instance.
         * Throws a [IllegalArgumentException] if the proxy could not be generated.
         */
        override fun <P> findPlayerProxyObject(instance: P): PlayerProxy {
            return MockedPlayerProxy(instance as String)
        }

        /**
         * Gets if the given instance can be converted to a player.
         */
        override fun <P> isPlayer(instance: P): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player uuid.
         */
        override fun findPlayerProxyObjectFromUUID(uuid: String): PlayerProxy {
            if (uuid == "ecd66f19-3b5b-4910-b8e6-1716b5a636bf") {
                val playerProxy = Mockito.mock(PlayerProxy::class.java)
                `when`(playerProxy.name).thenReturn("Kenny")

                return playerProxy
            }

            throw IllegalArgumentException()
        }

        /**
         * Clears any resources the given instance has allocated.
         */
        override fun cleanResources(instance: Any) {
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