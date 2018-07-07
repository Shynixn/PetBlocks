package com.github.shynixn.petblocks.sponge.logic.business.entity

import com.github.shynixn.petblocks.core.logic.business.entity.DbContext
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.service.sql.SqlService
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import javax.sql.DataSource

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
class SpongeDBContext @Inject constructor(private val pluginContainer: PluginContainer, private val logger: Logger, @ConfigDir(sharedRoot = false) private var privateConfigDir: Path) : DbContext() {
    companion object {
        /**
         * SQLiteDriver classPath.
         */
        const val SQLITE_DRIVER = "org.sqlite.JDBC"

        /**
         * MySQLDriver classPath.
         */
        const val MYSQL_DRIVER = "com.mysql.jdbc.Driver"
    }

    private lateinit var dataSource: DataSource

    /**
     * Initializes the db context.
     */
    init {
        if (!Config.getData<Boolean>("sql.enabled")!!) {
            initialize(SQLITE_DRIVER)
        } else {
            initialize(MYSQL_DRIVER)
        }
    }

    /**
     * Returns a new connection from the db context which should be closed after being used.
     */
    override fun getConnection(): Connection {
        return dataSource.connection
    }

    /**
     * Returns the content from the given filename.
     */
    override fun getContentFromName(name: String): String {
        try {
            val asset = Sponge.getAssetManager().getAsset(pluginContainer, "sql/$name.sql").get()
            return asset.readString()
        } catch (e: IOException) {
            logger.warn("Cannot read file.")
            throw RuntimeException(e)
        }
    }

    /**
     * Initializes the database with the given driver.
     */
    private fun initialize(driver: String) {
        if (driver == SQLITE_DRIVER) {
            val path = createSQLLiteFile()
            this.dataSource = createDataSource(driver, "jdbc:sqlite:" + path.toAbsolutePath().toString())

            val connection = this.getConnection()
            connection.use {
                execute("PRAGMA foreign_keys=ON", connection)
                getStatementFromFile("create-sqlite").split(";").forEach { l ->
                    executeUpdate(l, connection)
                }
            }
        } else if (driver == MYSQL_DRIVER) {
            try {
                this.dataSource = createDataSource(driver, Config.getData<String>("sql.host") + ":" + Config.getData<Int>("sql.port") + "/" + Config.getData<String>("sql.database"), Config.getData<String>("sql.username"), Config.getData<String>("sql.password"), Config.getData<Boolean>("sql.usessl")!!)

                val connection = this.getConnection()
                connection.use {
                    getStatementFromFile("create-mysql").split(";").forEach { l ->
                        executeUpdate(l, connection)
                    }
                }
            } catch (e: Exception) {
                logger.warn("Cannot connect to the MYSQL database!", e)
                logger.warn("Fallback mode activated. Using SQLite database instead.")
                initialize(SQLITE_DRIVER)
            }
        }
    }

    /**
     * Creates a new hikari datasource.
     */
    private fun createDataSource(driver: String, url: String, userName: String? = null, password: String? = null, useSSL: Boolean = false): DataSource {
        val connectionURL: String = if (driver == MYSQL_DRIVER) {
            "jdbc:mysql://$userName:$password@$url?useSSL=$useSSL"
        } else {
            url
        }

        val service = Sponge.getServiceManager().provide(SqlService::class.java).get()
        return service.getDataSource(connectionURL)
    }

    /**
     * Creates the sqlLite file.
     */
    private fun createSQLLiteFile(): Path {
        if (!Files.exists(privateConfigDir)) {
            Files.createDirectory(privateConfigDir)
        }

        val path = Paths.get(privateConfigDir.toAbsolutePath().toString(), "PetBlocks.db")
        if (!Files.exists(path)) {
            Files.createFile(path)
        }

        return path
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
    }
}