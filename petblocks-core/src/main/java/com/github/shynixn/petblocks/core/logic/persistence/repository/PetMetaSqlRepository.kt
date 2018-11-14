@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.business.extension.get
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetModifierEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.SkinEntity
import com.google.inject.Inject
import java.sql.Connection
import java.sql.ResultSet
import java.util.*

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
class PetMetaSqlRepository @Inject constructor(private val sqlDbContext: SqlDbContext, private val configurationService: ConfigurationService) : PetMetaRepository {
    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    override fun getOrCreateFromPlayerIdentifiers(name: String, uuid: UUID): PetMeta {
        return sqlDbContext.transaction { connection -> getOrCreateFromPlayerIdentifiers(connection, name, uuid) }
    }

    /**
     * Returns [List] with a list of stored [PetMeta].
     */
    override fun getAll(): List<PetMeta> {
        return sqlDbContext.transaction { connection ->
            val statement = "SELECT pet.id as petid, shy_player_id, shy_skin_id, shy_modifier_id, enabled, displayname" +
                    ", hitboxentitytype, soundenabled, particleenabled, climbingheight, movementspeed, uuid, name" +
                    ", typename, owner, datavalue, unbreakable, invincible, health " +
                    "FROM SHY_PET pet, SHY_SKIN skin, SHY_PLAYER player, SHY_PET_MODIFIER modifier " +
                    "WHERE pet.shy_player_id = player.id " +
                    "AND shy_skin_id = skin.id " +
                    "AND shy_modifier_id = modifier.id"

            sqlDbContext.multiQuery(connection, statement, { resultSet ->
                mapResultSetToPetMeta(resultSet)
            })
        }
    }

    /**
     * Saves the given [petMeta] instance and returns the same [petMeta] instance.
     */
    override fun save(petMeta: PetMeta): PetMeta {
        return sqlDbContext.transaction { connection ->
            if (petMeta.id == 0L) {
                insertInto(connection, petMeta)
            } else {
                update(connection, petMeta)
            }
        }
    }

    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    private fun getOrCreateFromPlayerIdentifiers(connection: Connection, name: String, uuid: UUID): PetMeta {
        val statement = "SELECT pet.id as petid, shy_player_id, shy_skin_id, shy_modifier_id, enabled, displayname" +
                ", hitboxentitytype, soundenabled, particleenabled, climbingheight, movementspeed, uuid, name" +
                ", typename, owner, datavalue, unbreakable, invincible, health " +
                "FROM SHY_PET pet, SHY_SKIN skin, SHY_PLAYER player, SHY_PET_MODIFIER modifier " +
                "WHERE player.uuid = ? " +
                "AND pet.shy_player_id = player.id " +
                "AND shy_skin_id = skin.id " +
                "AND shy_modifier_id = modifier.id"

        val optResult = sqlDbContext.singleQuery(connection, statement, { resultSet ->
            mapResultSetToPetMeta(resultSet)
        }, uuid.toString())

        return if (!optResult.isPresent) {
            val petMeta = configurationService.generateDefaultPetMeta(uuid, name)
            insertInto(connection, petMeta)
            getOrCreateFromPlayerIdentifiers(connection, name, uuid)
        } else {
            optResult.get()
        }
    }


    /**
     * Updates the [petMeta] in the database.
     */
    private fun update(connection: Connection, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta
        sqlDbContext.update(connection, "SHY_PLAYER", "WHERE id=" + playerMeta.id
                , "uuid" to playerMeta.uuid.toString()
                , "name" to playerMeta.name)

        val modifierMeta = petMeta.modifier
        sqlDbContext.update(connection, "SHY_PET_MODIFIER", "WHERE id=" + modifierMeta.id
                , "climbingheight" to modifierMeta.climbingHeight
                , "movementspeed" to modifierMeta.movementSpeed)

        val skinMeta = petMeta.skin
        sqlDbContext.update(connection, "SHY_SKIN", "WHERE id=" + skinMeta.id
                , "typename" to skinMeta.typeName
                , "owner" to skinMeta.owner
                , "datavalue" to skinMeta.dataValue
                , "unbreakable" to skinMeta.unbreakable)

        sqlDbContext.update(connection, "SHY_PET", "WHERE id=" + petMeta.id
                , "enabled" to petMeta.enabled
                , "invincible" to petMeta.invincible
                , "health" to petMeta.health
                , "displayname" to petMeta.displayName
                , "hitboxentitytype" to petMeta.hitBoxEntityType.name
                , "soundenabled" to petMeta.soundEnabled
                , "particleenabled" to petMeta.particleEnabled)

        return petMeta
    }

    /**
     * Inserts the [petMeta] into the database.
     */
    private fun insertInto(connection: Connection, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta

        sqlDbContext.singleQuery(connection, "SELECT * from SHY_PLAYER WHERE uuid = ?", { resultSet ->
            playerMeta.id = resultSet["id"]
        }, playerMeta.uuid.toString())

        if (playerMeta.id == 0L) {
            playerMeta.id = sqlDbContext.insert(connection, "SHY_PLAYER"
                    , "uuid" to playerMeta.uuid.toString()
                    , "name" to playerMeta.name)
        }

        val modifierMeta = petMeta.modifier
        modifierMeta.id = sqlDbContext.insert(connection, "SHY_PET_MODIFIER"
                , "climbingheight" to modifierMeta.climbingHeight
                , "movementspeed" to modifierMeta.movementSpeed)

        val skinMeta = petMeta.skin
        skinMeta.id = sqlDbContext.insert(connection, "SHY_SKIN"
                , "typename" to skinMeta.typeName
                , "owner" to skinMeta.owner
                , "datavalue" to skinMeta.dataValue
                , "unbreakable" to skinMeta.unbreakable)

        petMeta.id = sqlDbContext.insert(connection, "SHY_PET"
                , "shy_player_id" to playerMeta.id
                , "shy_skin_id" to skinMeta.id
                , "shy_modifier_id" to modifierMeta.id
                , "enabled" to petMeta.enabled
                , "invincible" to petMeta.invincible
                , "health" to petMeta.health
                , "displayname" to petMeta.displayName
                , "hitboxentitytype" to petMeta.hitBoxEntityType.name
                , "soundenabled" to petMeta.soundEnabled
                , "particleenabled" to petMeta.particleEnabled)

        return petMeta
    }

    /**
     * Maps the resultSet to a new petMeta.
     */
    private fun mapResultSetToPetMeta(resultSet: ResultSet): PetMeta {
        val modifierEntity = PetModifierEntity()

        with(modifierEntity) {
            id = resultSet["shy_modifier_id"]
            climbingHeight = resultSet["climbingheight"]
            movementSpeed = resultSet["movementspeed"]
        }

        val skinEntity = SkinEntity()

        with(skinEntity) {
            id = resultSet["shy_skin_id"]
            typeName = resultSet["typename"]
            owner = resultSet["owner"]
            dataValue = resultSet["dataValue"]
            unbreakable = resultSet["unbreakable"]
        }

        val playerMeta = PlayerMetaEntity()

        with(playerMeta) {
            id = resultSet["shy_player_id"]
            uuid = UUID.fromString(resultSet["uuid"])
            name = resultSet["name"]
        }

        val petMeta = PetMetaEntity(playerMeta, skinEntity, modifierEntity)

        with(petMeta) {
            id = resultSet["petid"]
            enabled = resultSet["enabled"]
            health = resultSet["health"]
            invincible = resultSet["invincible"]
            displayName = resultSet["displayname"]
            hitBoxEntityType = EntityType.valueOf(resultSet["hitboxentitytype"])
            soundEnabled = resultSet["soundenabled"]
            particleEnabled = resultSet["particleenabled"]
        }

        return petMeta
    }
}