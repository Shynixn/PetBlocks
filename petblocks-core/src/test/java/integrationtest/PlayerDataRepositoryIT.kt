package integrationtest

import com.github.shynixn.petblocks.api.legacy.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerDataEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PlayerDataRepositoryImpl
import helper.MockedCoroutineSessionService
import helper.TestSqliteDb
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.mockito.Mockito
import java.util.*

class PlayerDataRepositoryIT {
    /**
     * Given
     *      a new player with no stored data
     * When
     *      getPlayerDataFromPlayer is called
     * Then
     *     a new save entry for the player should be generated.
     */
    // IGNORED @Test
    fun getPlayerDataFromPlayerAsync_RetrievingPlayerData_ShouldCorrectlyStoreAndRestore() {
        // Arrange
        val proxyService = Mockito.mock(ProxyService::class.java)
        val uuid = UUID.randomUUID().toString()
        Mockito.`when`(proxyService.getPlayerUUID(Mockito.anyString())).then {
            uuid
        }
        val classUnderTest = createWithDependencies(proxyService)
        val player = "Pikachu"
        val expected = PlayerDataEntity {
            this.uuid = uuid
            this.name = "Pingu"
        }

        // Act
        val actual = runBlocking {
            val userData = classUnderTest.getPlayerDataFromPlayerAsync(player).await()
            userData.name = "Paul"

            delay(200)
            userData.name = "Pingu"
            delay(200)

            classUnderTest.getPlayerDataFromPlayerAsync(player).await()
        }

        // Assert
        Assertions.assertEquals(1, actual.databaseId)
        Assertions.assertEquals(expected.uuid, actual.uuid)
        Assertions.assertEquals(expected.name, actual.name)
    }

    /**
     * Given
     *      multiple players retrieving data at the same time
     * When
     *      getPlayerDataFromPlayer is called
     * Then
     *     all save entries should be correctly retrieved and stored.
     */
    // IGNORED @Test
    fun getPlayerDataFromPlayerAsync_MultipleRetrievingPlayerData_ShouldCorrectlyStoreAndRestore() {
        // Arrange
        val proxyService = Mockito.mock(ProxyService::class.java)
        Mockito.`when`(proxyService.getPlayerUUID(Mockito.anyString())).then {
            it.arguments[0]
        }
        val classUnderTest = createWithDependencies(proxyService)
        val player1 = UUID.randomUUID().toString()
        val player2 = UUID.randomUUID().toString()

        val expected = PlayerDataEntity {
            this.uuid = player2
            this.name = "Luigi"
        }

        // Act
        val actual = runBlocking {
            for (i in 0 until 10) {
                val ob1 = classUnderTest.getPlayerDataFromPlayerAsync(player1)
                delay(10)
                val ob2 = classUnderTest.getPlayerDataFromPlayerAsync(player2)

                ob1.await()
                ob2.await().name = "Luigi"
            }

            delay(200)
            classUnderTest.getPlayerDataFromPlayerAsync(player2).await()
        }

        // Assert
        Assertions.assertEquals(2, actual.databaseId)
        Assertions.assertEquals(expected.uuid, actual.uuid)
        Assertions.assertEquals(expected.name, actual.name)
    }

    /**
     * Given
     *      a new player with unstored data
     * When
     *      dispose is called
     * Then
     *     all changes should be saved.
     */
    // IGNORED @Test
    fun dispose_StoringChangesOnShutdown_ShouldCorrectlyStoreAndRestore() {
        // Arrange
        val proxyService = Mockito.mock(ProxyService::class.java)
        val uuid = UUID.randomUUID().toString()
        Mockito.`when`(proxyService.getPlayerUUID(Mockito.anyString())).then {
            uuid
        }
        val classUnderTest = createWithDependencies(proxyService)
        val player = "Pikachu"
        val expected = PlayerDataEntity {
            this.uuid = uuid
            this.name = "Pingu"
        }

        // Act
        val actual = runBlocking {
            classUnderTest.getPlayerDataFromPlayerAsync(player).await()
        }
        val actualDatabaseId = actual.databaseId
        actual.name = "Pingu"

        runBlocking {
            classUnderTest.dispose()
        }

        println("Retrieve")

        val actual2 = runBlocking {
            classUnderTest.getPlayerDataFromPlayerAsync(player).await()
        }

        // Assert
        Assertions.assertEquals(0, actualDatabaseId)
        Assertions.assertEquals(1, actual2.databaseId)
        Assertions.assertEquals(expected.uuid, actual2.uuid)
        Assertions.assertEquals(expected.name, actual2.name)
    }

    private var testDb: TestSqliteDb? = null

    // IGNORED @Test
    fun startDb() {
        testDb = TestSqliteDb()
    }

    // IGNORED @Test
    fun shutdownDb() {
        testDb!!.shutdown()
    }

    private fun createWithDependencies(proxyService: ProxyService): PlayerDataRepositoryImpl {
        val mockedCoroutineSessionService = MockedCoroutineSessionService()
        Mockito.`when`(proxyService.isPlayerUUIDOnline(Mockito.anyString())).thenReturn(true)
        val classUnderTest = PlayerDataRepositoryImpl(testDb!!, proxyService, mockedCoroutineSessionService)
        classUnderTest.savingInterval = 2L
        return classUnderTest
    }
}
