package unittest

import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.sponge.logic.business.listener.PetListener
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.network.ClientConnectionEvent
import java.util.*
import java.util.concurrent.CompletableFuture
import kotlin.collections.ArrayList

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
class PetListenerTest {
    /**
     * Given
     *    a player quit event with a player without a pet
     * When
     *    onPlayerQuitEvent is called
     * Then
     *    resources should be cleared.
     */
    @Test
    fun onPlayerQuitEvent_PlayerWithoutPet_ShouldClearResources() {
        // Arrange
        val player = Mockito.mock(Player::class.java)
        val event = Mockito.mock(ClientConnectionEvent.Disconnect::class.java)
        Mockito.`when`(event.targetEntity).thenReturn(player)
        val petService = MockedPetService()
        val persistencePetMetaService = MockedPersistencePetMetaService()
        val debugService = MockedPetDebugService()

        val classUnderTest = createWithDependencies(petService,
            persistencePetMetaService,
            MockedConcurrencyService(),
            Mockito.mock(EntityService::class.java),
            debugService)

        // Act
        classUnderTest.onPlayerQuitEvent(event)

        // Assert
        Assertions.assertTrue(persistencePetMetaService.clearResourcesCalled)
        Assertions.assertTrue(debugService.unregisterCalled)
    }

    /**
     * Given
     *    a player quit event with a player with a pet
     * When
     *    onPlayerQuitEvent is called
     * Then
     *    resources should be cleared.
     */
    @Test
    fun onPlayerQuitEvent_PlayerWithPet_ShouldClearResources() {
        // Arrange
        val player = Mockito.mock(Player::class.java)
        val event = Mockito.mock(ClientConnectionEvent.Disconnect::class.java)
        Mockito.`when`(event.targetEntity).thenReturn(player)
        val pet = Mockito.mock(PetProxy::class.java)
        val armorstandEntity = Mockito.mock(EntityPetProxy::class.java)
        var removedArmorstandEntity = false
        Mockito.`when`(armorstandEntity.deleteFromWorld()).then {
            removedArmorstandEntity = true
            ""
        }
        val hitBoxEntity = Mockito.mock(EntityPetProxy::class.java)
        var removedHitBoxEntity = false
        Mockito.`when`(hitBoxEntity.deleteFromWorld()).then {
            removedHitBoxEntity = true
            ""
        }
        Mockito.`when`(pet.getHeadArmorstand<Any>()).thenReturn(armorstandEntity)
        Mockito.`when`(pet.getHitBoxLivingEntity<Any>()).thenReturn(Optional.of(hitBoxEntity))

        val petService = MockedPetService(true, pet)
        val persistencePetMetaService = MockedPersistencePetMetaService()
        val debugService = MockedPetDebugService()

        val classUnderTest = createWithDependencies(petService,
            persistencePetMetaService,
            MockedConcurrencyService(),
            Mockito.mock(EntityService::class.java),
            debugService)

        // Act
        classUnderTest.onPlayerQuitEvent(event)

        // Assert
        Assertions.assertTrue(removedArmorstandEntity)
        Assertions.assertTrue(removedHitBoxEntity)
        Assertions.assertTrue(persistencePetMetaService.clearResourcesCalled)
        Assertions.assertTrue(debugService.unregisterCalled)
    }

    companion object {
        fun createWithDependencies(
            petService: PetService = MockedPetService(), persistencePetMetaService: PersistencePetMetaService = MockedPersistencePetMetaService(),
            concurrencyService: ConcurrencyService = MockedConcurrencyService(), entityService: EntityService = Mockito.mock(EntityService::class.java),
            debugService: PetDebugService = Mockito.mock(PetDebugService::class.java),
            configurationService: ConfigurationService = Mockito.mock(ConfigurationService::class.java)
        ): PetListener {
            return PetListener(petService, persistencePetMetaService, concurrencyService, entityService, debugService, configurationService, Mockito.mock(GUIItemLoadService::class.java))
        }
    }

    private class MockedPetDebugService(var unregisterCalled: Boolean = false) : PetDebugService {
        /**
         * Registers the given [source] to get notified by the given player pet.
         */
        override fun <P> register(source: P, player: P) {
        }

        /**
         * Gets if the given [source] is registered.
         */
        override fun <P> isRegistered(source: P): Boolean {
            return false
        }

        /**
         * Unregister the given [source].
         */
        override fun <S> unRegister(source: S) {
            unregisterCalled = true
        }
    }

    private class MockedConcurrencyService : ConcurrencyService {
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

    private class MockedPersistencePetMetaService(var clearResourcesCalled: Boolean = false) :
        PersistencePetMetaService {
        /**
         * Returns future with a list of all stored [PetMeta].
         * As not all PetMeta data is available during runtime this call completes in the future.
         */
        override fun getAll(): CompletableFuture<List<PetMeta>> {
            throw IllegalArgumentException()
        }

        /**
         * Clears the cache of the player and saves the allocated resources.
         */
        override fun <P> clearResources(player: P): CompletableFuture<Void?> {
            clearResourcesCalled = true
            return CompletableFuture()
        }

        /**
         * Gets the [PetMeta] from the player.
         * This will never return null.
         */
        override fun <P> getPetMetaFromPlayer(player: P): PetMeta {
            throw IllegalArgumentException()
        }

        /**
         * Gets or creates [PetMeta] from the player.
         */
        override fun <P> refreshPetMetaFromRepository(player: P): CompletableFuture<PetMeta> {
            throw IllegalArgumentException()
        }

        /**
         * Saves the given [petMeta] instance and returns a future.
         */
        override fun save(petMeta: PetMeta): CompletableFuture<PetMeta> {
            throw IllegalArgumentException()
        }

        /**
         * Gets all currently loaded pet metas.
         */
        override val cache: List<PetMeta>
            get() = ArrayList()

        /**
         * Closes all resources immediately.
         */
        override fun close() {
        }
    }

    private class MockedPetService(var hasPet: Boolean = false, val pet: PetProxy = Mockito.mock(PetProxy::class.java)) : PetService {
        /**
         * Gets or spawns the pet of the given player.
         * An empty optional gets returned if the pet cannot spawn by one of the following reasons:
         * Current world, region is disabled for pets, PreSpawnEvent was cancelled or Pet is not available due to Ai State.
         * For example HealthAI defines pet ai as 0 which results into impossibility to spawn.
         */
        override fun <P> getOrSpawnPetFromPlayer(player: P): Optional<PetProxy> {
            return Optional.of(pet)
        }

        /**
         * Tries to find the pet from the given entity.
         * Returns null if the pet does not exist.
         */
        override fun <E> findPetByEntity(entity: E): PetProxy? {
            if (entity == pet.getHeadArmorstand()) {
                return pet
            }

            return null
        }

        /**
         * Gets if the given [player] has got an active pet.
         */
        override fun <P> hasPet(player: P): Boolean {
            return hasPet
        }
    }
}