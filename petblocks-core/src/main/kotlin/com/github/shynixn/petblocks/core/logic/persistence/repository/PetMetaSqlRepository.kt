@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.YamlSerializationService
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.extension.getItem
import com.github.shynixn.petblocks.core.logic.persistence.entity.*

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
class PetMetaSqlRepository @Inject constructor(
    private val sqlDbContext: SqlDbContext,
    private val configurationService: ConfigurationService,
    private val yamlSerializationService: YamlSerializationService
) : PetMetaRepository {
    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    override fun getOrCreateFromPlayerIdentifiers(name: String, uuid: String): PetMeta {
        return sqlDbContext.transaction<PetMeta, Any> { connection -> getOrCreateFromPlayerIdentifiers(connection, name, uuid) }
    }

    /**
     * Returns [List] with a list of stored [PetMeta].
     */
    override fun getAll(): List<PetMeta> {
        return sqlDbContext.transaction<List<PetMeta>, Any> { connection ->
            val statement =
                "SELECT * " +
                        "FROM SHY_PET pet, SHY_SKIN skin, SHY_PLAYER player " +
                        "WHERE pet.shy_player_id = player.id " +
                        "AND shy_skin_id = skin.id "

            val aiStatement = "SELECT * FROM SHY_PET_AI WHERE shy_pet_id = ?"

            sqlDbContext.multiQuery(connection, statement, { resultSet ->
                val petMeta = mapResultSetToPetMeta(resultSet)

                petMeta.aiGoals.addAll(sqlDbContext.multiQuery(connection, aiStatement, { aiResultSet ->
                    mapResultSetToAI(aiResultSet)
                }, petMeta.id))

                petMeta
            })
        }
    }

    /**
     * Saves the given [petMeta] instance and returns the same [petMeta] instance.
     */
    override fun save(petMeta: PetMeta): PetMeta {
        return sqlDbContext.transaction<PetMeta, Any> { connection ->
            merge(connection, petMeta)
        }
    }

    /**
     * Merges the petMeta into the database.
     */
    private fun merge(connection: Any, petMeta: PetMeta): PetMeta {
        if (petMeta.id == 0L) {
            val optPlayerMeta = getPetMeta(connection, petMeta.playerMeta.uuid)

            return if (optPlayerMeta == null) {
                insertInto(connection, petMeta)
            } else {
                petMeta.id = optPlayerMeta.id
                petMeta.skin.id = optPlayerMeta.skin.id
                petMeta.playerMeta.id = optPlayerMeta.playerMeta.id

                update(connection, petMeta)
            }
        }

        return update(connection, petMeta)
    }

    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    private fun getOrCreateFromPlayerIdentifiers(connection: Any, name: String, uuid: String): PetMeta {
        val optResult = getPetMeta(connection, uuid)

        return if (optResult == null) {
            val petMeta = configurationService.generateDefaultPetMeta(uuid, name)
            return insertInto(connection, petMeta)
        } else {
            optResult
        }
    }

    /**
     * Gets the pet meta from the database.
     */
    private fun getPetMeta(connection: Any, uuid: String): PetMeta? {
        val statement = "SELECT * " +
                "FROM SHY_PET pet, SHY_SKIN skin, SHY_PLAYER player " +
                "WHERE player.uuid = ? " +
                "AND pet.shy_player_id = player.id " +
                "AND shy_skin_id = skin.id "

        val aiStatement = "SELECT * FROM SHY_PET_AI WHERE shy_pet_id = ?"

        return sqlDbContext.singleQuery(connection, statement, { resultSet ->
            val petMeta = mapResultSetToPetMeta(resultSet)

            petMeta.aiGoals.addAll(sqlDbContext.multiQuery(connection, aiStatement, { aiResultSet ->
                mapResultSetToAI(aiResultSet)
            }, petMeta.id))

            petMeta
        }, uuid)
    }

    /**
     * Updates the [petMeta] in the database.
     */
    private fun update(connection: Any, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta
        sqlDbContext.update(
            connection, "SHY_PLAYER", "WHERE id=" + playerMeta.id
            , "uuid" to playerMeta.uuid
            , "name" to playerMeta.name
        )

        val skinMeta = petMeta.skin
        sqlDbContext.update(
            connection, "SHY_SKIN", "WHERE id=" + skinMeta.id
            , "typename" to skinMeta.typeName
            , "owner" to skinMeta.owner
            , "datavalue" to skinMeta.dataValue
            , "unbreakable" to skinMeta.unbreakable
        )

        sqlDbContext.update(
            connection, "SHY_PET", "WHERE id=" + petMeta.id
            , "enabled" to petMeta.enabled
            , "displayname" to petMeta.displayName
            , "soundenabled" to petMeta.soundEnabled
            , "particleenabled" to petMeta.particleEnabled
        )

        sqlDbContext.delete(connection, "SHY_PET_AI", "WHERE shy_pet_id=" + petMeta.id)

        for (aiItem in petMeta.aiGoals) {
            val payload = yamlSerializationService.serialize(aiItem)
            val payloadString = configurationService.convertMapToString(payload)

            aiItem.id = sqlDbContext.insert(connection, "SHY_PET_AI"
                , "shy_pet_id" to petMeta.id
                , "typename" to aiItem.type
                , "content" to payloadString)
        }

        return petMeta
    }

    /**
     * Inserts the [petMeta] into the database.
     */
    private fun insertInto(connection: Any, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta

        sqlDbContext.singleQuery(connection, "SELECT * from SHY_PLAYER WHERE uuid = ?", { resultSet ->
            playerMeta.id = resultSet.getItem<Int>("id").toLong()
        }, playerMeta.uuid)

        if (playerMeta.id == 0L) {
            playerMeta.id = sqlDbContext.insert(
                connection, "SHY_PLAYER"
                , "uuid" to playerMeta.uuid
                , "name" to playerMeta.name
            )
        }

        val skinMeta = petMeta.skin
        skinMeta.id = sqlDbContext.insert(
            connection, "SHY_SKIN"
            , "typename" to skinMeta.typeName
            , "owner" to skinMeta.owner
            , "datavalue" to skinMeta.dataValue
            , "unbreakable" to skinMeta.unbreakable
        )

        petMeta.id = sqlDbContext.insert(
            connection, "SHY_PET"
            , "shy_player_id" to playerMeta.id
            , "shy_skin_id" to skinMeta.id
            , "enabled" to petMeta.enabled
            , "displayname" to petMeta.displayName
            , "soundenabled" to petMeta.soundEnabled
            , "particleenabled" to petMeta.particleEnabled
        )

        for (aiItem in petMeta.aiGoals) {
            val payload = yamlSerializationService.serialize(aiItem)
            val payloadString = configurationService.convertMapToString(payload)

            aiItem.id = sqlDbContext.insert(connection, "SHY_PET_AI"
                , "shy_pet_id" to petMeta.id
                , "typename" to aiItem.type
                , "content" to payloadString)
        }

        return petMeta
    }

    /**
     * Maps the resultSet to a new petMeta.
     */
    private fun mapResultSetToPetMeta(resultSet: Map<String, Any>): PetMeta {
        val skinEntity = SkinEntity()

        with(skinEntity) {
            id = resultSet.getItem<Int>("shy_skin_id").toLong()
            typeName = resultSet.getItem("typename")
            owner = resultSet.getItem("owner")
            dataValue = resultSet.getItem("datavalue")
            unbreakable = resultSet.getItem("unbreakable")
        }

        val playerMeta = PlayerMetaEntity("")

        with(playerMeta) {
            id = resultSet.getItem<Int>("shy_player_id").toLong()
            uuid = resultSet.getItem("uuid")
            name = resultSet.getItem("name")
        }

        val petMeta = PetMetaEntity(playerMeta, skinEntity)

        with(petMeta) {
            id = resultSet.getItem<Int>("id").toLong()
            enabled = resultSet.getItem("enabled")
            displayName = resultSet.getItem("displayname")
            soundEnabled = resultSet.getItem("soundenabled")
            particleEnabled = resultSet.getItem("particleenabled")
        }

        return petMeta
    }

    /**
     * Maps the resultSet to a new ai base.
     */
    private fun mapResultSetToAI(resultSet: Map<String, Any>): AIBase {
        val contentString = resultSet["content"]
        val type = resultSet["typename"] as String
        return configurationService.convertStringToAi(type, contentString as String)
    }
}