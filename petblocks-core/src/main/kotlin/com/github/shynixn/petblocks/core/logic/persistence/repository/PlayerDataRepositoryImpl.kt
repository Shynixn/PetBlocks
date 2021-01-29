package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.context.SqlContext
import com.github.shynixn.petblocks.api.persistence.entity.PlayerData
import com.github.shynixn.petblocks.api.persistence.repository.PlayerDataRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerDataEntity
import com.google.inject.Inject
import kotlinx.coroutines.*
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PlayerDataRepositoryImpl @Inject constructor(
    private val sqlContext: SqlContext,
    private val proxyService: ProxyService
) : PlayerDataRepository {
    private val objectMapper: ObjectMapper =
        ObjectMapper(YAMLFactory().disable(YAMLGenerator.Feature.WRITE_DOC_START_MARKER))

    private val cache = HashMap<UUID, Deferred<PlayerData>>()

    /**
     * Gets the stored data for the given player.
     * If no stored data is available, it will be automatically created.
     */
    override suspend fun <P> getPlayerDataFromPlayerAsync(player: P): Deferred<PlayerData> {
        val uuid = proxyService.getPlayerUUID(player)
        return getPlayerDataFromPlayerUUIDAsync(UUID.fromString(uuid))
    }

    /**
     * Gets the stored data for the given player uuid.
     * If no stored data is available, it will be automatically created.
     */
    override suspend fun getPlayerDataFromPlayerUUIDAsync(playerUUID: UUID): Deferred<PlayerData> {
        if (cache.containsKey(playerUUID)) {
            return cache[playerUUID]!!
        }

        coroutineScope {
            cache[playerUUID] = async {
                sqlContext.getConnection().use { connection ->
                    connection.prepareStatement("SELECT * FROM PETBLOCKS WHERE UUID = ?").use { preparedStatement ->
                        preparedStatement.executeQuery().use { resultSet ->
                            if (resultSet.next()) {
                                val content = resultSet.getString("content")
                                objectMapper.readValue<PlayerDataEntity>(
                                    content,
                                    object : TypeReference<List<PlayerDataEntity>>() {})
                            } else {
                                PlayerDataEntity {
                                    this.uuid = playerUUID
                                }
                            }
                        }
                    }
                }
            }
        }

        return cache[playerUUID]!!
    }

    /**
     * Gets the stored data for the given player name.
     * As player names are not guranteed to be unique, a list of player data is
     * returned which however mostly only contains 1 single element.
     * If no stored data is available, the list will be empty.
     */
    override suspend fun getPlayerDataFromPlayerNameAsync(playerName: String): Deferred<List<PlayerData>> {
        val dataBaseResults = coroutineScope {
            withContext(Dispatchers.IO) {
                sqlContext.getConnection().use { connection ->
                    connection.prepareStatement("SELECT * FROM PETBLOCKS WHERE NAME = ?").use { preparedStatement ->
                        preparedStatement.executeQuery().use { resultSet ->
                            val results = ArrayList<PlayerDataEntity>()

                            while (resultSet.next()) {
                                val content = resultSet.getString("content")
                                val result = objectMapper.readValue<PlayerDataEntity>(
                                    content,
                                    object : TypeReference<List<PlayerDataEntity>>() {})
                                results.add(result)
                            }

                            results
                        }
                    }
                }
            }
        }

        val playerDataResult = ArrayList<PlayerData>()

        for (uuid in cache.keys) {
            val playerDataDeferred = cache[uuid]!!
            val playerData = playerDataDeferred.await()

            if (playerData.name == playerName) {
                playerDataResult.add(playerData)
            }
        }

        for (dataBaseResult in dataBaseResults) {
            val cacheElement = playerDataResult.firstOrNull { e -> e.name == playerName }

            if (cacheElement == null) {
                playerDataResult.add(dataBaseResult)
            }
        }

        return
    }

    /**
     * Disposes the repository.
     */
    override fun dispose() {
        TODO("Not yet implemented")
    }
}
