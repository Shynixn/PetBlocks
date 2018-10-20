@file:Suppress("UNCHECKED_CAST")

package unittest

import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.sql.Connection
import java.sql.ResultSet
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
class PetMetaSqlRepositoryTest {
    /**
     * Given
     *      a valid sql context
     * When
     *      getAll is called.
     * Then
     *     all petMetas should be returned.
     */
    @Test
    fun getAll_ValidSqlContext_ShouldReturnPetMetas() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val petMetas = classUnderTest.getAll()

        // Assert
        Assertions.assertEquals("Elias", petMetas[1].playerMeta.name)
        Assertions.assertEquals(ParticleType.BLOCK_CRACK, petMetas[1].particle.type)
    }

    /**
     * Given
     *      a fresh petMeta and a valid sql context
     * When
     *     save is called.
     * Then
     *     the petMeta should be inserted into the context.
     */
    @Test
    fun save_FreshPetMeta_ShouldReturnPetMetas() {
        // Arrange
        val mockedContext = MockedSqlDbContext()
        val classUnderTest = createWithDependencies(mockedContext)
        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Pikachu"), ParticleEntity())

        // Act
        val databasePetMeta = classUnderTest.save(petMeta)

        // Assert
        Assertions.assertEquals(1, databasePetMeta.id)
        Assertions.assertTrue(mockedContext.insertCalled)
        Assertions.assertFalse(mockedContext.updateCalled)
    }

    /**
     * Given
     *      a existing petMeta and a valid sql context
     * When
     *     save is called.
     * Then
     *     the petMeta should be updated in the context.
     */
    @Test
    fun save_ExistingPetMeta_ShouldReturnPetMetas() {
        // Arrange
        val mockedContext = MockedSqlDbContext()
        val classUnderTest = createWithDependencies(mockedContext)
        val petMeta = PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Pikachu"), ParticleEntity())
        petMeta.id = 32
        petMeta.playerMeta.id = 106
        petMeta.particle.id = 117

        // Act
        val databasePetMeta = classUnderTest.save(petMeta)

        // Assert
        Assertions.assertEquals(32, databasePetMeta.id)
        Assertions.assertTrue(mockedContext.updateCalled)
        Assertions.assertFalse(mockedContext.insertCalled)
    }

    /**
     * Given
     *      a existing petMeta
     * When
     *      getOrCreateFromPlayerIdentifiers is called.
     * Then
     *     the identified petMeta should be returned.
     */
    @Test
    fun getOrCreateFromPlayerIdentifiers_ExistingPetMeta_ShouldReturnPetMeta() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val petMeta = classUnderTest.getOrCreateFromPlayerIdentifiers("Alina", UUID.fromString("16625034-af3d-4781-b157-64572759ad1c"))

        // Assert
        Assertions.assertEquals("Alina", petMeta.playerMeta.name)
        Assertions.assertEquals(ParticleType.BARRIER, petMeta.particle.type)
    }

    /**
     * Given
     *      no existing petMeta
     * When
     *      getOrCreateFromPlayerIdentifiers is called.
     * Then
     *     a new identified petMeta should be returned.
     */
    @Test
    fun getOrCreateFromPlayerIdentifiers_NoExistingPetMeta_ShouldReturnPetMeta() {
        // Arrange
        val classUnderTest = createWithDependencies()

        // Act
        val petMeta = classUnderTest.getOrCreateFromPlayerIdentifiers("Mosalina", UUID.randomUUID())

        // Assert
        Assertions.assertEquals("Mosalina", petMeta.playerMeta.name)
    }

    companion object {
        fun createWithDependencies(dbContext: SqlDbContext = MockedSqlDbContext()): PetMetaRepository {
            return PetMetaSqlRepository(dbContext)
        }
    }

    class MockedSqlDbContext : SqlDbContext {
        var insertCalled = false
        var updateCalled = false
        var singleQueryCounter = 0
        private val petMetas = arrayListOf(PetMetaEntity(PlayerMetaEntity(UUID.fromString("16625034-af3d-4781-b157-64572759ad1c"), "Alina"), ParticleEntity(ParticleType.BARRIER)),
                PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Elias"), ParticleEntity(ParticleType.BLOCK_CRACK)))

        /**
         * Creates a new transaction to the database.
         * [f] Handles creation and closing the transaction connection automatically and
         * manages connection pools in the background.
         * [R] result type.
         */
        override fun <R> transaction(f: (Connection) -> R): R {
            val connection = Mockito.mock(Connection::class.java)
            return f.invoke(connection)
        }

        /**
         * Creates a query to the database with the given [connection] [sqlStatement] [parameters]. Iterates the
         * result set automatically. Does not close the connection.
         * [R] result type.
         */
        override fun <R> multiQuery(connection: Connection, sqlStatement: String, f: (ResultSet) -> R, vararg parameters: Any): List<R> {
            val resultSet = Mockito.mock(ResultSet::class.java)
            f.invoke(resultSet)

            return petMetas as List<R>
        }

        /**
         * Creates a query to the database with the given [connection] [sqlStatement] [parameters]. Iterates the
         * result set automatically. Does not close the connection.
         * [R] result type.
         */
        override fun <R> singleQuery(connection: Connection, sqlStatement: String, f: (ResultSet) -> R, vararg parameters: Any): Optional<R> {
            if (parameters.isNotEmpty() && parameters[0] == "16625034-af3d-4781-b157-64572759ad1c") {
                return Optional.of(petMetas[0] as R)
            }

            val resultSet = Mockito.mock(ResultSet::class.java)
            f.invoke(resultSet)

            if (singleQueryCounter == 0) {
                singleQueryCounter++
                return Optional.empty()
            }

            return Optional.of(PetMetaEntity(PlayerMetaEntity(UUID.randomUUID(), "Mosalina"), ParticleEntity()) as R)
        }

        /**
         * Inserts the given [parameters] into the given [connection] [table].
         * Gets the created id of the inserted data. Does not close the connection.
         */
        override fun insert(connection: Connection, table: String, vararg parameters: Pair<String, Any?>): Long {
            insertCalled = true
            return 1
        }

        /**
         * Updates the given row by the [rowSelection] of the given [table] with the given [parameters].
         * Does not close the connection.
         */
        override fun update(connection: Connection, table: String, rowSelection: String, vararg parameters: Pair<String, Any?>) {
            updateCalled = true
        }
    }
}