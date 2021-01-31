package integrationtest;

import com.github.shynixn.petblocks.api.business.service.CoroutineSessionService;
import com.github.shynixn.petblocks.api.business.service.ProxyService;
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerDataEntity;
import com.github.shynixn.petblocks.core.logic.persistence.repository.PlayerDataRepositoryImpl;
import helper.MockedCoroutineSessionService;
import helper.TestSqliteDb;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.UUID;

public class PlayerDataRepositoryApiIT {
    /**
     * Given
     * a new player with no stored data
     * When
     * getPlayerDataFromPlayer is called
     * Then
     * a new save entry for the player should be generated.
     */
    @Test
    public void getPlayerDataFromPlayer_RetrievingPlayerData_ShouldCorrectlyStoreAndRestore() {
        // Arrange
        ProxyService proxyService = Mockito.mock(ProxyService.class);
        PlayerDataRepositoryImpl playerDataRepository = createWithDependencies(proxyService);
        String uuid = UUID.randomUUID().toString();
        Mockito.when(proxyService.getPlayerUUID(Mockito.anyString())).then(invocation -> uuid);

        // Act
        WrappedData<PlayerDataEntity> actual1 = new WrappedData<>();
        playerDataRepository.getPlayerDataFromPlayer("Pichu").thenAccept(e -> {
            actual1.value = e;
        });

        while (actual1.value == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        actual1.value.setName("Mario");

        WrappedData<PlayerDataEntity> actual2 = new WrappedData<>();
        playerDataRepository.getPlayerDataFromPlayer("Pichu").thenAccept(e -> {
            actual2.value = e;
        });

        while (actual2.value == null) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        // Assert
        Assertions.assertEquals(actual1.value, actual2.value);
        Assertions.assertEquals(actual1.value.getUuid(), uuid);
        Assertions.assertEquals("Mario", actual2.value.getName());
    }

    private TestSqliteDb testDb;

    @BeforeEach
    public void startDb() {
        testDb = new TestSqliteDb();
    }

    @AfterEach
    public void shutdownDb() {
        testDb.shutdown();
    }

    private PlayerDataRepositoryImpl createWithDependencies(ProxyService proxyService) {
        CoroutineSessionService sessionService = new MockedCoroutineSessionService();
        Mockito.when(proxyService.isPlayerUUIDOnline(Mockito.anyString())).thenReturn(true);
        return new PlayerDataRepositoryImpl(testDb, proxyService, sessionService);
    }

    private static class WrappedData<T> {
        public T value;
    }
}
