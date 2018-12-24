@file:Suppress("UNCHECKED_CAST")

package integrationtest

import ch.vorburger.mariadb4j.DB
import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.proxy.CompletableFutureProxy
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.SqlProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.ConfigurationServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.Item18R1ServiceImpl
import com.github.shynixn.petblocks.core.jvm.logic.business.proxy.CompletableFutureProxyImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.LoggingUtilServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetRunTimeRepository
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File
import java.io.FileInputStream
import java.sql.DriverManager
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.logging.Logger

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
class PersistenceMySQLIT {
    /**
     * Given
     *      initial empty database and production configuration in config.yml
     * When
     *      getAll is called the database should still be empty and when getOrCreateFromPlayerUUID with a new uuid is called
     * Then
     *     the default pet with the default production configuration from the config.yml should be generated.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ProductionConfiguration_ShouldGenerateCorrectPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val actual = classUnderTest.getOrCreateFromPlayerUUID(uuid.toString()).get()
        sqlProxy!!.close()
        database!!.stop()

        // Assert
        Assertions.assertEquals(0, initialSize)
        Assertions.assertEquals(1, actual.id)
        Assertions.assertEquals(false, actual.enabled)
        Assertions.assertEquals("Kenny's Pet", actual.displayName)
        Assertions.assertEquals(true, actual.soundEnabled)
        Assertions.assertEquals(true, actual.particleEnabled)
        Assertions.assertEquals(1, actual.skin.id)
        Assertions.assertEquals("GRASS", actual.skin.typeName)
        Assertions.assertEquals(0, actual.skin.dataValue)
        Assertions.assertEquals(false, actual.skin.unbreakable)
        Assertions.assertEquals("", actual.skin.owner)
        Assertions.assertEquals(1, actual.playerMeta.id)
        Assertions.assertEquals("Kenny", actual.playerMeta.name)
    }

    /**
     * Given
     *      initial empty database and production configuration in config.yml
     * When
     *      getAll is called the database should still be empty and when getOrCreateFromPlayerUUID is called
     * Then
     *     the default pet with the default production configuration should be correctly editable and storeAble again.
     */
    @Test
    fun getOrCreateFromPlayerUUID_ProductionConfiguration_ShouldAllowChangingPet() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val petMeta = classUnderTest.getOrCreateFromPlayerUUID(uuid.toString()).get()

        petMeta.enabled = true
        petMeta.displayName = "Captain Pet"
        petMeta.soundEnabled = false
        petMeta.particleEnabled = false
        petMeta.skin.typeName = "DIRT"
        petMeta.skin.dataValue = 2
        petMeta.skin.unbreakable = true
        petMeta.skin.owner = "Pikachu"
        petMeta.playerMeta.name = "Superman"

        classUnderTest.save(petMeta).get()
        val actual = classUnderTest.getOrCreateFromPlayerUUID(uuid.toString()).get()
        sqlProxy!!.close()
        database!!.stop()

        // Assert
        Assertions.assertEquals(0, initialSize)
        Assertions.assertEquals(1, actual.id)
        Assertions.assertEquals(true, actual.enabled)
        Assertions.assertEquals("Captain Pet", actual.displayName)
        Assertions.assertEquals(false, actual.soundEnabled)
        Assertions.assertEquals(false, actual.particleEnabled)
        Assertions.assertEquals(1, actual.skin.id)
        Assertions.assertEquals("DIRT", actual.skin.typeName)
        Assertions.assertEquals(2, actual.skin.dataValue)
        Assertions.assertEquals(true, actual.skin.unbreakable)
        Assertions.assertEquals("Pikachu", actual.skin.owner)
        Assertions.assertEquals(1, actual.playerMeta.id)
        Assertions.assertEquals("Superman", actual.playerMeta.name)
    }

    companion object {
        private var sqlProxy: SqlProxyImpl? = null
        private var database: DB? = null

        fun createWithDependencies(): PersistencePetMetaService {
            val configuration = YamlConfiguration()
            configuration.load(File("./src/petblocksCoreMain/resources/assets/petblocks/config.yml"))
            configuration.set("sql.type", "mysql")
            configuration.set("sql.database", "db")

            database = DB.newEmbeddedDB(3306)
            database!!.start()

            DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=").use { conn ->
                conn.createStatement().use { statement ->
                    statement.executeUpdate("CREATE DATABASE db")
                }
            }

            val plugin = Mockito.mock(Plugin::class.java)
            Mockito.`when`(plugin.config).thenReturn(configuration)
            Mockito.`when`(plugin.dataFolder).thenReturn(File("integrationtest-sqlite"))
            Mockito.`when`(plugin.getResource(Mockito.anyString())).then { parameter ->
                if (parameter.arguments[0].toString() == "assets/petblocks/sql/create-mysql.sql") {
                    FileInputStream(File("./src/petblocksCoreMain/resources/assets/petblocks/sql/create-mysql.sql"))
                } else {
                    Unit
                }
            }

            sqlProxy = SqlProxyImpl(plugin, LoggingUtilServiceImpl(Logger.getAnonymousLogger()))
            val sqlite = PetMetaSqlRepository(SqlDbContextImpl(sqlProxy!!, LoggingUtilServiceImpl(Logger.getAnonymousLogger()))
                , ConfigurationServiceImpl(plugin, Item18R1ServiceImpl(), YamlSerializationServiceImpl()), YamlSerializationServiceImpl())
            val repo = PetRunTimeRepository()

            return PersistencePetMetaServiceImpl(MockedConcurrencyService(), MockedProxyService(), sqlite, repo)
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
        override fun findPlayerProxyObjectFromName(name: String): PlayerProxy? {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player uuid.
         */
        override fun findPlayerProxyObjectFromUUID(uuid: String): PlayerProxy? {
            val playerProxy = Mockito.mock(PlayerProxy::class.java)
            Mockito.`when`(playerProxy.uniqueId).thenReturn(uuid)
            Mockito.`when`(playerProxy.name).thenReturn("Kenny")

            return playerProxy
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
         * Creates a new completable future.
         */
        override fun <T> createCompletableFuture(): CompletableFutureProxy<T> {
            return CompletableFutureProxyImpl()
        }

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