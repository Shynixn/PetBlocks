package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.fasterxml.jackson.core.JsonFactory
import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.github.shynixn.petblocks.api.business.service.CoroutineSessionService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.context.SqlContext
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerDataEntity
import com.google.inject.Inject
import kotlinx.coroutines.*
import kotlinx.coroutines.future.future
import java.sql.Statement
import java.util.*
import java.util.concurrent.CompletionStage
import kotlin.collections.HashMap

class PlayerDataRepositoryImpl @Inject constructor(
    private val sqlContext: SqlContext,
    private val proxyService: ProxyService,
    private val coroutineSessionService: CoroutineSessionService
) {
    private val objectMapper: ObjectMapper =
        ObjectMapper(JsonFactory())

    private val cache = HashMap<UUID, Deferred<PlayerDataEntity>>()

    /**
     * Time interval between auto saving.
     */
    var savingInterval = 1000 * 60L * 5L

    /**
     * Initialize
     */
    init {
        coroutineSessionService.launch(coroutineSessionService.minecraftDispatcher) {
            while (true) {
                val dataCopy = createSaveChangeset()

                withContext(coroutineSessionService.asyncDispatcher) {
                    synchronized(cache) {
                        for (data in dataCopy) {
                            save(data.first, data.second)
                        }
                    }
                }

                for (data in dataCopy) {
                    val uuid = UUID.fromString(data.first.uuid)

                    if (cache.containsKey(uuid)) {
                        val content = cache[uuid]!!.await()
                        content.databaseId = data.first.databaseId

                        if (!proxyService.isPlayerUUIDOnline(data.first.uuid)) {
                            cache.remove(uuid)?.await()
                        }
                    }
                }

                delay(savingInterval)
            }
        }
    }

    /**
     * Gets the stored data for the given player.
     * If no stored data is available, it will be automatically created.
     */
    fun <P> getPlayerDataFromPlayer(player: P): CompletionStage<PlayerDataEntity> {
        return coroutineSessionService.scope.future {
            getPlayerDataFromPlayerAsync(player).await()
        }
    }

    /**
     * Gets the stored data for the given player.
     * If no stored data is available, it will be automatically created.
     */
    suspend fun <P> getPlayerDataFromPlayerAsync(player: P): Deferred<PlayerDataEntity> {
        val uuid = proxyService.getPlayerUUID(player)
        return getPlayerDataFromPlayerUUIDAsync(UUID.fromString(uuid))
    }

    /**
     * Gets the stored data for the given player uuid.
     * @param uuid Identifier of a player.
     * If no stored data is available, it will be automatically created.
     */
    fun <P> getPlayerDataFromPlayer(uuid: UUID): CompletionStage<PlayerDataEntity> {
        return coroutineSessionService.scope.future {
            getPlayerDataFromPlayerUUIDAsync(uuid).await()
        }
    }

    /**
     * Gets the stored data for the given player uuid.
     * @param uuid Identifier of a player.
     * If no stored data is available, it will be automatically created.
     */
    suspend fun getPlayerDataFromPlayerUUIDAsync(uuid: UUID): Deferred<PlayerDataEntity> {
        if (cache.containsKey(uuid)) {
            return cache[uuid]!!
        }

        coroutineScope {
            cache[uuid] = async(coroutineSessionService.asyncDispatcher) {
                sqlContext.getConnection().use { connection ->
                    connection.prepareStatement("SELECT * FROM PETBLOCKS WHERE UUID = ?").use { preparedStatement ->
                        preparedStatement.setString(1, uuid.toString())
                        preparedStatement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                val content = resultSet.getString("content")
                                val id = resultSet.getInt("id")

                                val result = objectMapper.readValue<PlayerDataEntity>(
                                    content,
                                    object : TypeReference<PlayerDataEntity>() {})
                                result.databaseId = id
                                result
                            } else {
                                PlayerDataEntity {
                                    this.uuid = uuid.toString()
                                }
                            }
                        }
                    }
                }
            }
        }

        return cache[uuid]!!
    }

    /**
     * Disposes the repository.
     */
    suspend fun dispose() {
        val dataCopy = createSaveChangeset()

        synchronized(cache) {
            for (playerData in dataCopy) {
                save(playerData.first, playerData.second)
            }
        }

        cache.clear()
    }

    /**
     * Creates a changeset which can be saved to the database.
     */
    private suspend fun createSaveChangeset(): List<Pair<PlayerDataEntity, String>> {
        return cache.values.map { e ->
            val result = e.await()

            Pair(PlayerDataEntity {
                this.databaseId = result.databaseId
                this.uuid = result.uuid
                this.name = result.name
            }, objectMapper.writeValueAsString(result))
        }
    }

    /**
     * Saves a player data to the database.
     */
    private fun save(playerDataEntity: PlayerDataEntity, payload: String) {
        sqlContext.getConnection().use { connection ->
            if (playerDataEntity.databaseId == 0) {
                connection.prepareStatement(
                    "INSERT INTO PETBLOCKS (uuid, name, content) VALUES (?,?,?)",
                    Statement.RETURN_GENERATED_KEYS
                )
                    .use {
                        it.setString(1, playerDataEntity.uuid)
                        it.setString(2, playerDataEntity.name)
                        it.setString(3, payload)
                        it.executeUpdate()

                        it.generatedKeys.use { resultSet ->
                            resultSet.next()
                            playerDataEntity.databaseId = resultSet.getInt(1)
                        }
                    }
                return
            }

            connection.prepareStatement("UPDATE PETBLOCKS SET name=?, content=? WHERE uuid = ?").use {
                it.setString(1, playerDataEntity.name)
                it.setString(2, payload)
                it.setString(3, playerDataEntity.uuid)
                it.executeUpdate()
            }
        }
    }
}
