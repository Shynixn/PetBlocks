@file:Suppress("UNCHECKED_CAST")

package integrationtest

import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.SqlProxyImpl
import com.github.shynixn.petblocks.core.logic.business.service.LoggingUtilServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetRunTimeRepository
import org.apache.commons.io.FileUtils
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.Plugin
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import java.io.File
import java.io.FileInputStream
import java.util.*
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
class PersistenceIT {
    /**
     * Given
     *      initial empty database and production configuration in config.yml
     * When
     *      getAll is called the database should still be empty
     * Then
     *     the default pet specified in the config should be inserted and retrieved correctly from
     *     the database.
     */
    @Test
    fun resolve_ValidPluginAndServiceClass_ShouldCallProxy() {
        // Arrange
        val classUnderTest = createWithDependencies()
        val uuid = UUID.fromString("c7d21810-d2a0-407d-a389-14efd3eb79d2")

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val initialPetMeta = classUnderTest.getOrCreateFromPlayerUUID(uuid).get()
        initialPetMeta.displayName = "Peter Brown"
        classUnderTest.save(initialPetMeta)
        val resultPetMeta = classUnderTest.getOrCreateFromPlayerUUID(uuid).get()

        // Assert
        Assertions.assertEquals(0, initialSize)
        Assertions.assertEquals("Peter Brown", resultPetMeta.displayName)

        // TODO: Test all properties if correctly stored.
    }

    companion object {
        fun createWithDependencies(): PersistencePetMetaService {
            val configuration = YamlConfiguration()
            configuration.set("sql.enabled", false)
            configuration.set("sql.host", "localhost")
            configuration.set("sql.port", 3306)
            configuration.set("sql.database", "db")
            configuration.set("sql.username", "root")
            configuration.set("sql.password", "")

            val folder = File("integrationtest-sqlite")

            if (folder.exists()) {
                FileUtils.deleteDirectory(folder)
            }

            val plugin = Mockito.mock(Plugin::class.java)
            Mockito.`when`(plugin.config).thenReturn(configuration)
            Mockito.`when`(plugin.dataFolder).thenReturn(File("integrationtest-sqlite"))
            Mockito.`when`(plugin.getResource(Mockito.anyString())).then { parameter ->
                if (parameter.arguments[0].toString() == "assets/petblocks/sql/create-sqlite.sql") {
                    FileInputStream(File("../petblocks-core/src/main/resources/assets/petblocks/sql/create-sqlite.sql"))
                } else {
                    Unit
                }
            }

            val sqlite = PetMetaSqlRepository(SqlDbContextImpl(SqlProxyImpl(plugin, LoggingUtilServiceImpl(Logger.getAnonymousLogger())), LoggingUtilServiceImpl(Logger.getAnonymousLogger())))
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
        override fun findPlayerProxyObjectFromName(name: String): Optional<PlayerProxy> {
            throw IllegalArgumentException()
        }

        /**
         * Tries to return a player proxy for the given player uuid.
         */
        override fun findPlayerProxyObjectFromUUID(uuid: UUID): Optional<PlayerProxy> {
            val playerProxy = Mockito.mock(PlayerProxy::class.java)
            Mockito.`when`(playerProxy.uniqueId).thenReturn(uuid)
            Mockito.`when`(playerProxy.name).thenReturn("Kenny")

            return Optional.of(playerProxy)
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