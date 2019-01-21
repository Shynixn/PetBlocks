@file:Suppress("UNCHECKED_CAST")

package integrationtest

import ch.vorburger.mariadb4j.DB
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.proxy.CompletableFutureProxy
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.ItemService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.toMaterial
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.SqlProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.AIServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.ConfigurationServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.Item18R1ServiceImpl
import com.github.shynixn.petblocks.core.jvm.logic.business.proxy.CompletableFutureProxyImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.service.LoggingUtilServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.PersistencePetMetaServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIMovementEntity
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
        Assertions.assertEquals(7, actual.aiGoals.size)

        Assertions.assertEquals("hopping", (actual.aiGoals[0] as AIMovementEntity).type)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).climbingHeight)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSpeed)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementYOffSet)
        Assertions.assertEquals("CHICKEN_WALK", (actual.aiGoals[0] as AIMovementEntity).movementSound.name)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.volume)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.pitch)
        Assertions.assertEquals(ParticleType.REDSTONE, (actual.aiGoals[0] as AIMovementEntity).movementParticle.type)
        Assertions.assertEquals(20, (actual.aiGoals[0] as AIMovementEntity).movementParticle.amount)

        Assertions.assertEquals("follow-owner", (actual.aiGoals[1] as AIFollowOwner).type)
        Assertions.assertEquals(3.0, (actual.aiGoals[1] as AIFollowOwner).distanceToOwner)
        Assertions.assertEquals(50.0, (actual.aiGoals[1] as AIFollowOwner).maxRange)
        Assertions.assertEquals(2.5, (actual.aiGoals[1] as AIFollowOwner).speed)

        Assertions.assertEquals("float-in-water", (actual.aiGoals[2] as AIFloatInWater).type)
        Assertions.assertEquals("wearing", (actual.aiGoals[3] as AIWearing).type)

        Assertions.assertEquals("ground-riding", (actual.aiGoals[4] as AIGroundRiding).type)
        Assertions.assertEquals(1.0, (actual.aiGoals[4] as AIGroundRiding).climbingHeight)
        Assertions.assertEquals(1.0, (actual.aiGoals[4] as AIGroundRiding).ridingSpeed)
        Assertions.assertEquals(1.0, (actual.aiGoals[4] as AIGroundRiding).ridingYOffSet)

        Assertions.assertEquals("feeding", (actual.aiGoals[5] as AIFeeding).type)
        Assertions.assertEquals("CARROT_ITEM", (actual.aiGoals[5] as AIFeeding).typeName.toMaterial().name)
        Assertions.assertEquals(ParticleType.HEART, (actual.aiGoals[5] as AIFeeding).clickParticle.type)
        Assertions.assertEquals("EAT", (actual.aiGoals[5] as AIFeeding).clickSound.name)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[6] as AIAmbientSound).type)
        Assertions.assertEquals("CHICKEN_IDLE", (actual.aiGoals[6] as AIAmbientSound).sound.name)
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

        (petMeta.aiGoals[0] as AIHopping).climbingHeight = 23.5
        (petMeta.aiGoals[0] as AIHopping).movementSpeed = 105.3
        (petMeta.aiGoals[0] as AIHopping).movementYOffSet = 93.4
        (petMeta.aiGoals[0] as AIHopping).movementParticle.offSetY = 471.2
        (petMeta.aiGoals[0] as AIHopping).movementSound.pitch = 44.2

        (petMeta.aiGoals[1] as AIFollowOwner).maxRange = 100.2
        (petMeta.aiGoals[1] as AIFollowOwner).distanceToOwner = 14.45
        (petMeta.aiGoals[1] as AIFollowOwner).speed = 42.2

        (petMeta.aiGoals[2] as AIFloatInWater).userId = "43"
        (petMeta.aiGoals[3] as AIWearing).userId = "465"
        (petMeta.aiGoals[4] as AIGroundRiding).climbingHeight = 88.2
        (petMeta.aiGoals[4] as AIGroundRiding).ridingSpeed = 165.2
        (petMeta.aiGoals[4] as AIGroundRiding).ridingYOffSet = 99.44

        (petMeta.aiGoals[5] as AIFeeding).clickParticle.offSetZ = 25.4
        (petMeta.aiGoals[5] as AIFeeding).clickSound.name = "COOKIE_SOUND"
        (petMeta.aiGoals[5] as AIFeeding).dataValue = 4
        (petMeta.aiGoals[5] as AIFeeding).typeName = "POWER_BANK"

        (petMeta.aiGoals[6] as AIAmbientSound).sound.volume = 41.55

        classUnderTest.save(petMeta).get()
        val actual = classUnderTest.getOrCreateFromPlayerUUID(uuid.toString()).get()
        sqlProxy!!.close()

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

        Assertions.assertEquals("hopping", (actual.aiGoals[0] as AIMovementEntity).type)
        Assertions.assertEquals(23.5, (actual.aiGoals[0] as AIMovementEntity).climbingHeight)
        Assertions.assertEquals(105.3, (actual.aiGoals[0] as AIMovementEntity).movementSpeed)
        Assertions.assertEquals(93.4, (actual.aiGoals[0] as AIMovementEntity).movementYOffSet)
        Assertions.assertEquals(44.2, (actual.aiGoals[0] as AIMovementEntity).movementSound.pitch)
        Assertions.assertEquals(471.2, (actual.aiGoals[0] as AIMovementEntity).movementParticle.offSetY)

        Assertions.assertEquals("follow-owner", (actual.aiGoals[1] as AIFollowOwner).type)
        Assertions.assertEquals(14.45, (actual.aiGoals[1] as AIFollowOwner).distanceToOwner)
        Assertions.assertEquals(100.2, (actual.aiGoals[1] as AIFollowOwner).maxRange)
        Assertions.assertEquals(42.2, (actual.aiGoals[1] as AIFollowOwner).speed)

        Assertions.assertEquals("43", (actual.aiGoals[2] as AIFloatInWater).userId!!)
        Assertions.assertEquals("465", (actual.aiGoals[3] as AIWearing).userId)

        Assertions.assertEquals("ground-riding", (actual.aiGoals[4] as AIGroundRiding).type)
        Assertions.assertEquals(88.2, (actual.aiGoals[4] as AIGroundRiding).climbingHeight)
        Assertions.assertEquals(165.2, (actual.aiGoals[4] as AIGroundRiding).ridingSpeed)
        Assertions.assertEquals(99.44, (actual.aiGoals[4] as AIGroundRiding).ridingYOffSet)

        Assertions.assertEquals("feeding", (actual.aiGoals[5] as AIFeeding).type)
        Assertions.assertEquals("POWER_BANK", (actual.aiGoals[5] as AIFeeding).typeName)
        Assertions.assertEquals(4, (actual.aiGoals[5] as AIFeeding).dataValue)
        Assertions.assertEquals("COOKIE_SOUND", (actual.aiGoals[5] as AIFeeding).clickSound.name)
        Assertions.assertEquals(25.4, (actual.aiGoals[5] as AIFeeding).clickParticle.offSetZ)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[6] as AIAmbientSound).type)
        Assertions.assertEquals(41.55, (actual.aiGoals[6] as AIAmbientSound).sound.volume)
    }

    companion object {
        private var sqlProxy: SqlProxyImpl? = null
        private var database: DB? = null

        fun createWithDependencies(): PersistencePetMetaService {
            val configuration = YamlConfiguration()
            configuration.load(File("../petblocks-core/src/main/resources/assets/petblocks/config.yml"))
            configuration.set("sql.type", "mysql")
            configuration.set("sql.database", "db")

            if (database != null) {
                database!!.stop()
            }

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
                    FileInputStream(File("../petblocks-core/src/main/resources/assets/petblocks/sql/create-mysql.sql"))
                } else {
                    Unit
                }
            }

            sqlProxy = SqlProxyImpl(plugin, LoggingUtilServiceImpl(Logger.getAnonymousLogger()))
            val sqlite = PetMetaSqlRepository(SqlDbContextImpl(sqlProxy!!, LoggingUtilServiceImpl(Logger.getAnonymousLogger())),
                    AIServiceImpl(plugin, LoggingUtilServiceImpl(Logger.getAnonymousLogger()), YamlSerializationServiceImpl()), ConfigurationServiceImpl(plugin, Item18R1ServiceImpl(),  AIServiceImpl(plugin, LoggingUtilServiceImpl(Logger.getAnonymousLogger()), YamlSerializationServiceImpl())))
            val repo = PetRunTimeRepository()

            val method = PetBlocksApi::class.java.getDeclaredMethod("initializePetBlocks", PluginProxy::class.java)
            method.isAccessible = true
            method.invoke(PetBlocksApi, MockedPluginProxy())

            return PersistencePetMetaServiceImpl(MockedConcurrencyService(), MockedProxyService(), sqlite, repo)
        }
    }

    class MockedPluginProxy : PluginProxy {
        /**
         * Gets a business logic from the PetBlocks plugin.
         * All types in the service package can be accessed.
         * Throws a [IllegalArgumentException] if the service could not be found.
         * @param S the type of service class.
         */
        override fun <S> resolve(service: Any): S {
            if (service == ItemService::class.java) {
                return Item18R1ServiceImpl() as S
            }

            throw IllegalArgumentException()
        }

        /**
         * Creates a new entity from the given [entity].
         * Throws a [IllegalArgumentException] if the entity could not be found.
         * @param E the type of entity class.
         */
        override fun <E> create(entity: Any): E {
            throw IllegalArgumentException()
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