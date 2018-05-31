package com.github.shynixn.petblocks.bukkit.logic.business

import com.github.shynixn.petblocks.core.logic.business.entity.DbContext
import com.google.inject.Inject
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.apache.commons.io.IOUtils
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
class BukkitDBContext @Inject constructor(private val plugin: Plugin, private val logger: org.slf4j.Logger) : DbContext() {

    companion object {
        val SQLITE_DRIVER = "org.sqlite.JDBC"
        val MYSQL_DRIVER = "com.mysql.jdbc.Driver"
    }

    private lateinit var dataSource: HikariDataSource

    /**
     * Initializes the db context.
     */
    init {
        if (!plugin.config.getBoolean("sql.enabled")) {
            initialize(SQLITE_DRIVER)
        } else {
            initialize(MYSQL_DRIVER)
        }
    }

    /**
     * Returns a new connection from the db context which should be closed after being used.
     */
    override fun getConnection(): Connection {
        return this.dataSource.connection
    }

    /**
     * Returns the path from the given filename.
     */
    override fun getContentFromName(name: String): String {
        try {
            plugin.getResource("assets/petblocks/sql/$name.sql").use { stream ->
                return IOUtils.toString(stream, "UTF-8")
            }
        } catch (e: IOException) {
            logger.warn("Failed to read file '$name.")
            throw RuntimeException(e)
        }
    }

    /**
     * Initializes the database with the given driver.
     */
    private fun initialize(driver: String) {
        initializeDriver(driver)

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
                val config = plugin.config
                this.dataSource = createDataSource(driver, "jdbc:mysql://" + config.getString("sql.host") + ":" + config.getString("sql.port") + "/" + config.getString("sql.database"), config.getString("sql.username"), config.getString("sql.password"), config.getBoolean("sql.usessl"))

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
            logger.warn("JDBC Driver not found!", ex)
        }
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        if (!this.dataSource.isClosed) {
            this.dataSource.close()
        }
    }
}