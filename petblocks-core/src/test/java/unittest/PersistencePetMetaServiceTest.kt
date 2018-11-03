@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetModifierEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
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
     *      petMetas in runtime cache and petMetas in database
     * When
     *      getAll is called
     * Then
     *     all petMeta of the runtime and database cache should be returned without one being double returned.
     */
    @Test
    fun getAll_RunTimePetMetaStoredPetMeta_ShouldReturnUniquePetMetas() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val petMetas = classUnderTest.getAll().get()
        val amountWithSameId = petMetas.count { p -> p.id == 1L }
        val petMeta = petMetas[1]

        // Assert
        Assertions.assertEquals(1, amountWithSameId)
        Assertions.assertEquals("Keks", petMeta.displayName)
        Assertions.assertEquals("Batman", petMeta.playerMeta.name)
    }

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
        val uuid = UUID.fromString("ecd66f19-3b5b-4910-b8e6-1716b5a636bf")

        // Act
        val petMeta = classUnderTest.getOrCreateFromPlayerUUID(uuid).get()

        // Assert
        Assertions.assertEquals("Kenny", petMeta.playerMeta.name)
        Assertions.assertEquals("Cloud", petMeta.skin)
    }

    /**
     * Given
     *      one existing petMeta in the runtime cache.
     * When
     *      getOrCreateFromPlayerUUID by uuid is called
     * Then
     *     the petMeta in runtime cache of this uuid should be returned.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ExistingPetMetaRunTime_ShouldReturnThisPetMeta() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")

        // Act
        val petMeta = classUnderTest.getOrCreateFromPlayerUUID(uuid).get()

        // Assert
        Assertions.assertEquals("Beeman", petMeta.playerMeta.name)
        Assertions.assertEquals("Test", petMeta.displayName)
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

        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Mario"), SkinEntity(), PetModifierEntity())
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

        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Mario"), SkinEntity(), PetModifierEntity())
        petMeta.skin.owner = "Pikachu"
        petMeta.id = 2

        // Act
        Assertions.assertThrows(RuntimeException::class.java) {
            classUnderTest.save(petMeta).get()
        }
    }

    companion object {
        fun createWithDependencies(petMetaRepository: PetMetaRepository = MockedPetMetaRepository()): PersistencePetMetaService {
            return PersistencePetMetaServiceImpl(MockedConcurrencyService(), MockedProxyService(), petMetaRepository, MockedPetRepository())
        }
    }

    class MockedPetRepository : PetRepository {
        /**
         * Returns [List] with a list of stored [PetProxy].
         */
        override fun getAll(): List<PetProxy> {
            val pet = Mockito.mock(PetProxy::class.java)
            val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Batman"), SkinEntity(), PetModifierEntity())
            petMeta.displayName = "Keks"
            petMeta.id = 1

            `when`(pet.meta).thenReturn(petMeta)

            return arrayListOf(pet)
        }

        /**
         * Removes the given petProxy from being managed.
         */
        override fun remove(petProxy: PetProxy) {
            throw IllegalArgumentException()
        }

        /**
         * Saves the petProxy into the repository.
         */
        override fun save(petProxy: PetProxy) {
            throw IllegalArgumentException()
        }

        /**
         * Gets the pet from the uuid. Throws exception if not exist.
         */
        override fun getFromPlayerUUID(uuid: UUID): PetProxy {
            if (uuid.toString() == "c7d21810-d2a0-407d-a389-14efd3eb79d2") {
                val pet = Mockito.mock(PetProxy::class.java)
                val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Beeman"), SkinEntity(), PetModifierEntity())
                petMeta.displayName = "Test"
                petMeta.id = 1

                `when`(pet.meta).thenReturn(petMeta)

                return pet
            }

            throw IllegalArgumentException()
        }

        /**
         * Gets if the given player uniqueId has got an active pet.
         */
        override fun hasPet(uuid: UUID): Boolean {
            if (uuid.toString() == "c7d21810-d2a0-407d-a389-14efd3eb79d2") {
                return true
            }

            return false
        }
    }

    class MockedPetMetaRepository(val shouldFail: Boolean = false) : PetMetaRepository {
        var saveCalled = false

        /**
         * Returns [List] with a list of stored [PetMeta].
         */
        override fun getAll(): List<PetMeta> {
            val petMeta1 = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Batman"), SkinEntity(), PetModifierEntity())
            petMeta1.id = 1

            val petMeta2 = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Mario"),SkinEntity(), PetModifierEntity())
            petMeta2.skin.owner = "Pikachu"
            petMeta2.id = 2

            return arrayListOf(petMeta1, petMeta2)
        }

        /**
         * Returns the petMeta of from the given player uniqueId. Creates
         * a new one if it does not exist yet. Gets it from the runtime when a pet
         * currently uses the meta data of the player.
         */
        override fun getOrCreateFromPlayerIdentifiers(name: String, uuid: UUID): PetMeta {
            if (uuid.toString() == "ecd66f19-3b5b-4910-b8e6-1716b5a636bf") {
                val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Kenny"), SkinEntity(), PetModifierEntity())
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
         * Returns a proxy object for the given instance.
         * Throws a [IllegalArgumentException] if the proxy could not be generated.
         */
        override fun <P> findProxyObject(instance: Any): P {
            throw IllegalArgumentException()
        }

        /**
         * Returns a player proxy object for the given instance.
         * Throws a [IllegalArgumentException] if the proxy could not be generated.
         */
        override fun <P> findPlayerProxyObject(instance: P): PlayerProxy {
            throw IllegalArgumentException()
        }

        /**
         * Gets if the given instance can be converted to a player.
         */
        override fun <P> isPlayer(instance: P): Boolean {
            throw IllegalArgumentException()
        }

        /**
         * Gets the name of a  instance.
         */
        override fun <I> getNameOfInstance(instance: I): String {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player name.
         */
        override fun findPlayerProxyObjectFromName(name: String): Optional<PlayerProxy> {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player uuid.
         */
        override fun findPlayerProxyObjectFromUUID(uuid: UUID): Optional<PlayerProxy> {
            if (uuid.toString() == "ecd66f19-3b5b-4910-b8e6-1716b5a636bf") {
                val playerProxy = Mockito.mock(PlayerProxy::class.java)
                `when`(playerProxy.name).thenReturn("Kenny")

                return Optional.of(playerProxy)
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

    class MockedConcurrencyService : ConcurrencyService {
        /**
         * Runs the given [function] synchronised with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskSync(delayTicks: Long, repeatingTicks: Long, function: Runnable) {
            function.run()
        }

        /**
         * Runs the given [function] asynchronous with the given [delayTicks] and [repeatingTicks].
         */
        override fun runTaskAsync(delayTicks: Long, repeatingTicks: Long, function: Runnable) {
            function.run()
        }
    }
}