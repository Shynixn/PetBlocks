@file:Suppress("UNCHECKED_CAST")

package integrationtest

import ch.vorburger.mariadb4j.DB
import com.github.shynixn.petblocks.api.business.enumeration.ParticleType
import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.logic.persistence.entity.AIMovementEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
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
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uuid)

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val actual = classUnderTest.getPetMetaFromPlayer(player)

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
        Assertions.assertEquals(6, actual.aiGoals.size)

        Assertions.assertEquals("hopping", (actual.aiGoals[0] as AIMovementEntity).type)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).climbingHeight)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSpeed)
        Assertions.assertEquals(-1.0, (actual.aiGoals[0] as AIMovementEntity).movementYOffSet)
        Assertions.assertEquals("CHICKEN_WALK", (actual.aiGoals[0] as AIMovementEntity).movementSound.name)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.volume)
        Assertions.assertEquals(1.0, (actual.aiGoals[0] as AIMovementEntity).movementSound.pitch)
        Assertions.assertEquals(ParticleType.REDSTONE, (actual.aiGoals[0] as AIMovementEntity).movementParticle.type)
        Assertions.assertEquals(20, (actual.aiGoals[0] as AIMovementEntity).movementParticle.amount)

        Assertions.assertEquals("follow-owner", (actual.aiGoals[1] as AIFollowOwner).type)
        Assertions.assertEquals(3.0, (actual.aiGoals[1] as AIFollowOwner).distanceToOwner)
        Assertions.assertEquals(50.0, (actual.aiGoals[1] as AIFollowOwner).maxRange)
        Assertions.assertEquals(1.5, (actual.aiGoals[1] as AIFollowOwner).speed)

        Assertions.assertEquals("float-in-water", (actual.aiGoals[2] as AIFloatInWater).type)

        Assertions.assertEquals("feeding", (actual.aiGoals[3] as AIFeeding).type)
        Assertions.assertEquals("391", (actual.aiGoals[3] as AIFeeding).typeName)
        Assertions.assertEquals(ParticleType.HEART, (actual.aiGoals[3] as AIFeeding).clickParticle.type)
        Assertions.assertEquals("EAT", (actual.aiGoals[3] as AIFeeding).clickSound.name)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[5] as AIAmbientSound).type)
        Assertions.assertEquals("CHICKEN_IDLE", (actual.aiGoals[5] as AIAmbientSound).sound.name)
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
        val player = Mockito.mock(Player::class.java)
        Mockito.`when`(player.uniqueId).thenReturn(uuid)

        // Act
        val initialSize = classUnderTest.getAll().get().size
        val petMeta = classUnderTest.getPetMetaFromPlayer(player)

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
        (petMeta.aiGoals[3] as AIFeeding).clickParticle.offSetZ = 25.4
        (petMeta.aiGoals[3] as AIFeeding).clickSound.name = "COOKIE_SOUND"
        (petMeta.aiGoals[3] as AIFeeding).dataValue = 4
        (petMeta.aiGoals[3] as AIFeeding).typeName = "POWER_BANK"

        (petMeta.aiGoals[5] as AIAmbientSound).sound.volume = 41.55

        classUnderTest.save(petMeta).get()
        val actual = classUnderTest.getPetMetaFromPlayer(player)

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

        Assertions.assertEquals("feeding", (actual.aiGoals[3] as AIFeeding).type)
        Assertions.assertEquals("POWER_BANK", (actual.aiGoals[3] as AIFeeding).typeName)
        Assertions.assertEquals(4, (actual.aiGoals[3] as AIFeeding).dataValue)
        Assertions.assertEquals("COOKIE_SOUND", (actual.aiGoals[3] as AIFeeding).clickSound.name)
        Assertions.assertEquals(25.4, (actual.aiGoals[3] as AIFeeding).clickParticle.offSetZ)

        Assertions.assertEquals("ambient-sound", (actual.aiGoals[5] as AIAmbientSound).type)
        Assertions.assertEquals(41.55, (actual.aiGoals[5] as AIAmbientSound).sound.volume)
    }

    companion object {
        private var database: DB? = null
        private var dbContext: SqlDbContext? = null

        fun createWithDependencies(): PersistencePetMetaService {
            val configuration = YamlConfiguration()
            configuration.load(File("../petblocks-core/src/main/resources/assets/petblocks/config.yml"))
            configuration.set("sql.type", "mysql")
            configuration.set("sql.database", "db")

            if (dbContext != null) {
                dbContext!!.close()
            }

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

            val aiService = AIServiceImpl(LoggingUtilServiceImpl(Logger.getAnonymousLogger()), MockedProxyService(), YamlServiceImpl())
            val configService = ConfigurationServiceImpl(plugin)

            EntityServiceImpl(
                configService, MockedProxyService(),
                Mockito.mock(EntityRegistrationService::class.java), Mockito.mock(PetService::class.java), YamlSerializationServiceImpl(),
                plugin, Version.VERSION_1_8_R1, aiService
            )

            val guiItemLoadService =
                GUIItemLoadServiceImpl(
                    configService,
                    ItemTypeServiceImpl(Version.VERSION_UNKNOWN),
                    aiService
                )

            dbContext = SqlDbContextImpl(configService, LoggingUtilServiceImpl(Logger.getAnonymousLogger()))

            val sqlite = PetMetaSqlRepository(
                dbContext!!,
                aiService, guiItemLoadService, configService
            )
            return PersistencePetMetaServiceImpl(MockedProxyService(), sqlite, MockedConcurrencyService(), MockedEventService())
        }
    }

    class MockedProxyService : ProxyService {
        /**
         * Gets a list of points between 2 locations.
         */
        override fun <L> getPointsBetweenLocations(location1: L, location2: L, amount: Int): List<L> {
            throw IllegalArgumentException()
        }

        /**
         * Applies the given [potionEffect] to the given [player].
         */
        override fun <P> applyPotionEffect(player: P, potionEffect: PotionEffect) {
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
            return (player as Player).uniqueId.toString()
        }

        /**
         * Sends a message to the [sender].
         */
        override fun <S> sendMessage(sender: S, message: String) {
            throw IllegalArgumentException()
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

    class MockedEventService : EventService {
        /**
         * Calls a framework event and returns if it was cancelled.
         */
        override fun callEvent(event: Any): Boolean {
            return false
        }
    }
}