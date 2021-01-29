package integrationtest

import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerDataEntity
import com.github.shynixn.petblocks.core.logic.persistence.repository.PlayerDataRepositoryImpl
import helper.MockedCoroutineSessionService
import helper.TestSqliteDb
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
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
    @Test
    fun getPlayerDataFromPlayerAsync_RetrievingPlayerData_ShouldCorrectlyStoreAndRestore() {
        // Arrange
        val testDb = TestSqliteDb()
        val mockedCoroutineSessionService = MockedCoroutineSessionService()
        val proxyService = Mockito.mock(ProxyService::class.java)
        val uuid = UUID.randomUUID().toString()
        Mockito.`when`(proxyService.getPlayerUUID(Mockito.anyString())).thenReturn(uuid)
        Mockito.`when`(proxyService.isPlayerUUIDOnline(Mockito.anyString())).thenReturn(true)
        val classUnderTest = PlayerDataRepositoryImpl(testDb, proxyService, mockedCoroutineSessionService)
        classUnderTest.savingInterval = 2L
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
}
