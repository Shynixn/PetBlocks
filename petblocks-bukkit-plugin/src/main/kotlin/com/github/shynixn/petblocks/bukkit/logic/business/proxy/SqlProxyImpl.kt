@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.proxy.SqlConnectionPoolProxy
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.google.inject.Inject
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.bukkit.plugin.Plugin
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection

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
class SqlProxyImpl @Inject constructor(private val plugin: Plugin, private val loggingService: LoggingService) : SqlConnectionPoolProxy, AutoCloseable {
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

    private lateinit var dataSource: HikariDataSource

    /**
     * Initializes the db context.
     */
    init {
        if (plugin.config.getString("sql.type") == "sqlite") {
            initialize(SQLITE_DRIVER)
        } else {
            initialize(MYSQL_DRIVER)
        }
    }

    /**
     * Creates a basic jdbc connection which the caller is responsible for.
     */
    override fun <C> openConnection(): C {
        return this.dataSource.connection as C
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
        if (!this.dataSource.isClosed) {
            this.dataSource.close()
        }
    }

    /**
     * Initializes the datasource.
     */
    private fun initialize(driver: String) {
        initializeDriver(driver)

        if (driver == SQLITE_DRIVER) {
            val path = createSQLLiteFile()
            this.dataSource = createDataSource(driver, "jdbc:sqlite:" + path.toAbsolutePath().toString())

            val connection = this.openConnection<Connection>()
            connection.use {
                connection.prepareStatement("PRAGMA foreign_keys=ON").use { statement ->
                    statement.execute()
                }

                plugin.getResource("assets/petblocks/sql/create-sqlite.sql").bufferedReader().use { reader ->
                    reader.readText().split(";").forEach { text ->
                        connection.prepareStatement(text).use { statement ->
                            statement.execute()
                        }
                    }
                }
            }

            loggingService.info("Connected to " + this.dataSource.jdbcUrl)
        } else if (driver == MYSQL_DRIVER) {
            try {
                val config = plugin.config
                this.dataSource = createDataSource(
                    driver,
                    "jdbc:mysql://" + config.getString("sql.host") + ":" + config.getString("sql.port") + "/" + config.getString("sql.database"),
                    config.getString("sql.username"),
                    config.getString("sql.password"),
                    config.getBoolean("sql.usessl")
                )

                val connection = this.openConnection<Connection>()
                connection.use {
                    plugin.getResource("assets/petblocks/sql/create-mysql.sql").bufferedReader().use { reader ->
                        reader.readText().split(";").forEach { text ->
                            connection.prepareStatement(text).use { statement ->
                                statement.execute()
                            }
                        }
                    }
                }

                loggingService.info("Connected to " + this.dataSource.jdbcUrl)
            } catch (e: Exception) {
                loggingService.warn("Cannot connect to the MYSQL database!", e)
                loggingService.warn("Fallback mode activated. Using SQLite database instead.")
                initialize(SQLITE_DRIVER)
            }
        }
    }

    /**
     * Creates a new hikari datasource.
     */
    private fun createDataSource(driver: String, url: String, userName: String? = null, password: String? = null, useSSL: Boolean = false): HikariDataSource {
        val config = HikariConfig()
        config.driverClassName = driver
        config.connectionTestQuery = "SELECT 1"
        config.jdbcUrl = url

        if (userName != null) {
            config.username = userName
        }

        if (password != null) {
            config.password = password
        }

        config.addDataSourceProperty("useSSL", useSSL)
        config.addDataSourceProperty("cachePrepStmts", "true")
        config.addDataSourceProperty("prepStmtCacheSize", "250")
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")

        if (driver == SQLITE_DRIVER) {
            config.maximumPoolSize = 1
        } else {
            config.maximumPoolSize = 10
        }

        return HikariDataSource(config)
    }

    /**
     * Creates the sqlLite file.
     */
    private fun createSQLLiteFile(): Path {
        if (!plugin.dataFolder.exists()) {
            if (!plugin.dataFolder.mkdir()) {
                throw IOException("Creating directory failed.")
            }
        }

        val path = Paths.get(plugin.dataFolder.absolutePath, "PetBlocks.db")
        if (!Files.exists(path)) {
            Files.createFile(path)
        }

        return path
    }

    /**
     * Initializes the given driver.
     */
    private fun initializeDriver(driver: String) {
        try {
            Class.forName(driver)
        } catch (ex: ClassNotFoundException) {
            loggingService.warn("JDBC Driver not found!", ex)
        }
    }
}