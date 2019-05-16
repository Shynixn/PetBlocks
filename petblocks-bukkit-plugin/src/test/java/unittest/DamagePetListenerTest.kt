package unittest

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.CombatPetService
import com.github.shynixn.petblocks.api.business.service.HealthService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.logic.business.listener.DamagePetListener
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
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
class DamagePetListenerTest {
    /**
     * Given
     *    a pet owner taking damage
     * When
     *    onEntityReceiveDamageEvent is called
     * Then
     *    flee should be called.
     */
    @Test
    fun onEntityReceiveDamageEvent_PetOwnerGettingDamaged_ShouldCallFlee() {
        // Arrange
        val player = Mockito.mock(Player::class.java)
        val combatPetService = MockedCombatService()
        val entityDamageEvent = Mockito.mock(EntityDamageEvent::class.java)
        Mockito.`when`(entityDamageEvent.cause).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        Mockito.`when`(entityDamageEvent.entity).thenReturn(player)

        val classUnderTest = createWithDependencies(MockedPetService(), MockedPersistencePetMetaService(), combatPetService)

        // Act
        classUnderTest.onEntityReceiveDamageEvent(entityDamageEvent)

        // Assert
        Assertions.assertTrue(combatPetService.fleed)
    }

    /**
     * Given
     *    a pet taking damage
     * When
     *    onEntityReceiveDamageEvent is called
     * Then
     *    flee should be called.
     */
    @Test
    fun onEntityReceiveDamageEvent_PetGettingDamaged_ShouldCallFlee() {
        // Arrange
        val pet = Mockito.mock(PetProxy::class.java)
        val petEntity = Mockito.mock(LivingEntity::class.java)
        Mockito.`when`(pet.getHeadArmorstand<LivingEntity>()).thenReturn(petEntity)
        Mockito.`when`(pet.meta).thenReturn(Mockito.mock(PetMeta::class.java))

        val combatPetService = MockedCombatService()
        val petService = MockedPetService(pet)
        val entityDamageEvent = Mockito.mock(EntityDamageEvent::class.java)
        Mockito.`when`(entityDamageEvent.cause).thenReturn(EntityDamageEvent.DamageCause.ENTITY_ATTACK)
        Mockito.`when`(entityDamageEvent.entity).thenReturn(petEntity)

        val classUnderTest = createWithDependencies(petService, MockedPersistencePetMetaService(), combatPetService)

        // Act
        classUnderTest.onEntityReceiveDamageEvent(entityDamageEvent)

        // Assert
        Assertions.assertTrue(combatPetService.fleed)
    }

    companion object {
        fun createWithDependencies(
            petService: PetService = MockedPetService(), persistencePetMetaService: PersistencePetMetaService = MockedPersistencePetMetaService(),
            combatPetService: CombatPetService = MockedCombatService(), healthService: HealthService = MockedHealthService()
        ): DamagePetListener {
            return DamagePetListener(petService, persistencePetMetaService, combatPetService, healthService)
        }
    }

    private class MockedCombatService(var fleed: Boolean = false) : CombatPetService {
        /**
         * Lets the pet flee and reappears after some time.
         */
        override fun flee(petMeta: PetMeta) {
            fleed = true
        }
    }

    private class MockedHealthService : HealthService {
        /**
         * Damages the given [petMeta] with the given [damage].
         * The pet needs a health ai otherwise this operation gets ignored.
         */
        override fun damagePet(petMeta: PetMeta, damage: Double) {
        }
    }

    private class MockedPersistencePetMetaService(val player: Player = Mockito.mock(Player::class.java), val petMeta: PetMeta = PetMetaEntity(PlayerMetaEntity(""), SkinEntity())) :
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
            throw IllegalArgumentException()
        }

        /**
         * Gets the [PetMeta] from the player.
         * This will never return null.
         */
        override fun <P> getPetMetaFromPlayer(player: P): PetMeta {
            return petMeta
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

    private class MockedPetService(val pet: PetProxy = Mockito.mock(PetProxy::class.java)) : PetService {
        /**
         * Gets or spawns the pet of the given player.
         * An empty optional gets returned if the pet cannot spawn by one of the following reasons:
         * Current world, region is disabled for pets, PreSpawnEvent was cancelled or Pet is not available due to Ai State.
         * For example HealthAI defines pet ai as 0 which results into impossibility to spawn.
         */
        override fun <P> getOrSpawnPetFromPlayer(player: P): Optional<PetProxy> {
            throw IllegalArgumentException()
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
            throw IllegalArgumentException()
        }
    }
}