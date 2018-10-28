package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.core.logic.persistence.entity.ParticleEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PetMetaEntity
import com.github.shynixn.petblocks.core.logic.persistence.entity.PlayerMetaEntity
import com.google.inject.Inject
import java.sql.Connection
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
class PetMetaSqlRepository @Inject constructor(private val sqlDbContext: SqlDbContext) : PetMetaRepository {
    /**
     * Returns the petMeta of from the given player uniqueId. Creates
     * a new one if it does not exist yet. Gets it from the runtime when a pet
     * currently uses the meta data of the player.
     */
    override fun getOrCreateFromPlayerIdentifiers(name: String, uuid: UUID): PetMeta {
        return sqlDbContext.transaction { connection ->
            val statement = "SELECT * FROM SHY_PETBLOCK petblock, SHY_PARTICLE_EFFECT particle, SHY_PLAYER player" +
                    "WHERE player.uuid = ? " +
                    "AND petblock.shy_player_id = player.id " +
                    "AND shy_particle_effect_id = particle.id"

            val optResult = sqlDbContext.singleQuery<PetMeta>(connection, statement, { resultset ->
                val playerMeta = PlayerMetaEntity(uuid, name)
                val particle = ParticleEntity()

                // Parse resultSet.


                PetMetaEntity(playerMeta, particle)
            }, uuid.toString())

            if (!optResult.isPresent) {
                val playerMeta = PlayerMetaEntity(uuid, name)
                val particle = ParticleEntity()
                val petMetaEntity = PetMetaEntity(playerMeta, particle)

                insertInto(connection, petMetaEntity)
                getOrCreateFromPlayerIdentifiers(name, uuid)
            } else {
                optResult.get()
            }
        }
    }

    /**
     * Returns [List] with a list of stored [PetMeta].
     */
    override fun getAll(): List<PetMeta> {
        return sqlDbContext.transaction { connection ->
            val statement = "SELECT * FROM SHY_PETBLOCK petblock, SHY_PARTICLE_EFFECT particle, SHY_PLAYER player" +
                    "WHERE petblock.shy_player_id = player.id " +
                    "AND shy_particle_effect_id = particle.id"

            sqlDbContext.multiQuery<PetMeta>(connection, statement, { resultset ->
                val playerMeta = PlayerMetaEntity(UUID.randomUUID(), "")
                val particle = ParticleEntity()

                // Parse resultSet.


                PetMetaEntity(playerMeta, particle)
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
     * Updates the [petMeta] in the database.
     */
    private fun update(connection: Connection, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta
        val particleMeta = petMeta.particle

        sqlDbContext.update(connection, "SHY_PLAYER", "WHERE id=" + playerMeta.id
                , "uuid" to playerMeta.uuid
                , "name" to playerMeta.name)

        sqlDbContext.update(connection, "SHY_PARTICLE_EFFECT ", "WHERE id=" + particleMeta.id
                , "name" to particleMeta.type.gameId_113
                , "amount" to particleMeta.amount
                , "speed" to particleMeta.speed
                , "x" to particleMeta.offSetX
                , "y" to particleMeta.offSetY
                , "z" to particleMeta.offSetZ
                , "material" to particleMeta.materialName
                , "data" to particleMeta.data)

        return petMeta
    }

    /**
     * Inserts the [petMeta] into the database.
     */
    private fun insertInto(connection: Connection, petMeta: PetMeta): PetMeta {
        val playerMeta = petMeta.playerMeta
        val particleMeta = petMeta.particle

        playerMeta.id = sqlDbContext.insert(connection, "SHY_PLAYER"
                , "uuid" to playerMeta.uuid
                , "name" to playerMeta.name)

        particleMeta.id = sqlDbContext.insert(connection, "SHY_PARTICLE_EFFECT "
                , "name" to particleMeta.type.gameId_113
                , "amount" to particleMeta.amount
                , "speed" to particleMeta.speed
                , "x" to particleMeta.offSetX
                , "y" to particleMeta.offSetY
                , "z" to particleMeta.offSetZ
                , "material" to particleMeta.materialName
                , "data" to particleMeta.data)

        petMeta.id = sqlDbContext.insert(connection, "SHY_PET_META")

        return petMeta
    }
}