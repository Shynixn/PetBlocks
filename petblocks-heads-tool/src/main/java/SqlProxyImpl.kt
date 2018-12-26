@file:Suppress("UNCHECKED_CAST")

import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.util.logging.Level
import java.util.logging.Logger

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
class SqlProxyImpl constructor(private val loggingService: Logger) :  AutoCloseable {
    companion object {
        /**
         * SQLiteDriver classPath.
         */
        const val SQLITE_DRIVER = "org.sqlite.JDBC"
    }

    private lateinit var dataSource: HikariDataSource

    /**
     * Initializes the db context.
     */
    init {
        initialize(SQLITE_DRIVER)
    }

    /**
     * Creates a basic jdbc connection which the caller is responsible for.
     */
    fun <C> openConnection(): C {
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

        val path = createSQLLiteFile()
        this.dataSource = createDataSource(driver, "jdbc:sqlite:" + path.toAbsolutePath().toString())

        val connection = this.openConnection<Connection>()
        connection.use {
            connection.prepareStatement("PRAGMA foreign_keys=ON").use { statement ->
                statement.execute()
            }

                    val text = "CREATE TABLE IF NOT EXISTS SHY_MCHEAD\n" +
                            "(\n" +
                            "  id INTEGER PRIMARY KEY AUTOINCREMENT,\n" +
                            "  name TEXT NOT NULL,\n" +
                            "  skin TEXT NOT NULL UNIQUE,\n" +
                            "  headtype TEXT NOT NULL\n" +
                            ")\n"
            connection.prepareStatement(text).use { statement ->
                statement.execute()
            }
        }

        loggingService.info("Connected to " + this.dataSource.jdbcUrl)
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
        val path = Paths.get("MinecraftHeads.db")
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
            loggingService.log(Level.WARNING, "JDBC Driver not found!", ex)
        }
    }
}