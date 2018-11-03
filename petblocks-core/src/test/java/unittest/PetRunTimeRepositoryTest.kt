package unittest

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetModifierEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetRunTimeRepository
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
class PetRunTimeRepositoryTest {
    /**
     * Given
     *      a valid pet proxy
     * When
     *      save is called.
     * Then
     *     getAll should return all stored proxies.
     */
    @Test
    fun save_ValidPetProxy_ShouldStoreAndRetrieve() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)

        // Act
        classUnderTest.save(proxy)
        val proxies = classUnderTest.getAll()

        // Assert
        Assertions.assertEquals(1, proxies.size)
        Assertions.assertEquals(proxy.hashCode(), proxies[0].hashCode())
    }

    /**
     * Given
     *      a valid pet proxy
     * When
     *      remove is called.
     * Then
     *     getAll should return no proxies anymore.
     */
    @Test
    fun remove_ValidPetProxy_ShouldReturnNoProxies() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)

        // Act
        classUnderTest.save(proxy)
        classUnderTest.remove(proxy)
        val proxies = classUnderTest.getAll()

        // Assert
        Assertions.assertEquals(0, proxies.size)
    }

    /**
     * Given
     *      a matching uuid with proxy
     * When
     *      hasPet is called.
     * Then
     *     true should be returned.
     */
    @Test
    fun hasPet_MatchingUUID_ShouldFindPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)
        val uuid = UUID.randomUUID()
        val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Creeper"), SkinEntity(), PetModifierEntity())
        Mockito.`when`(proxy.meta).thenReturn(petMeta)

        // Act
        classUnderTest.save(proxy)
        val hasPet = classUnderTest.hasPet(uuid)

        // Assert
        Assertions.assertTrue(hasPet)
    }

    /**
     * Given
     *      a non matching uuid with proxy
     * When
     *      hasPet is called.
     * Then
     *     false should be returned.
     */
    @Test
    fun hasPet_NonMatchingUUID_ShouldNotFindPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)
        val uuid = UUID.randomUUID()
        val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Creeper"), SkinEntity(), PetModifierEntity())
        Mockito.`when`(proxy.meta).thenReturn(petMeta)

        // Act
        classUnderTest.save(proxy)
        val hasPet = classUnderTest.hasPet(UUID.randomUUID())

        // Assert
        Assertions.assertFalse(hasPet)
    }


    /**
     * Given
     *      a matching uuid with proxy
     * When
     *      hasPet is called.
     * Then
     *     true should be returned.
     */
    @Test
    fun getFromPlayerUUID_MatchingUUID_ShouldFindPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)
        val uuid = UUID.randomUUID()
        val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Creeper"), SkinEntity(), PetModifierEntity())
        Mockito.`when`(proxy.meta).thenReturn(petMeta)

        // Act
        classUnderTest.save(proxy)
        val petProxy = classUnderTest.getFromPlayerUUID(uuid)

        // Assert
        Assertions.assertNotNull(petProxy)
    }

    /**
     * Given
     *      a non matching uuid with proxy
     * When
     *      hasPet is called.
     * Then
     *     false should be returned.
     */
    @Test
    fun getFromPlayerUUID_NonMatchingUUID_ShouldNotFindPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val proxy = Mockito.mock(PetProxy::class.java)
        val uuid = UUID.randomUUID()
        val petMeta = PetMetaEntity(PlayerMetaEntity(uuid, "Creeper"), SkinEntity(), PetModifierEntity())
        Mockito.`when`(proxy.meta).thenReturn(petMeta)

        // Act
        classUnderTest.save(proxy)
        Assertions.assertThrows(IllegalArgumentException::class.java) {
            classUnderTest.getFromPlayerUUID(UUID.randomUUID())
        }
    }

    companion object {
        fun createWithDependencies(): PetRepository {
            return PetRunTimeRepository()
        }
    }
}