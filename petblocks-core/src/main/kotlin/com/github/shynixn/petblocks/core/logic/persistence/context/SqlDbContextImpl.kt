@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.persistence.context

import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.core.logic.business.extension.isBukkitServer
import com.google.inject.Inject
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.concurrent.TimeUnit

class SqlDbContextImpl @Inject constructor(
    private val configurationService: ConfigurationService,
    private val loggingService: LoggingService
) : SqlDbContext {
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

    init {
        if (configurationService.findValue<String>("sql.type") == "sqlite") {
            connectToSqlite()
        } else {
            try {
                connectToMySql()
            } catch (e: Exception) {
                loggingService.warn("Cannot connect to the MYSQL database!", e)
                loggingService.warn("Fallback mode activated. Using SQLite database instead.")
                connectToSqlite()
            }
        }
    }

    /**
     * Deletes the given [parameters] into the given [connection] [table].
     */
    override fun <C> delete(connection: C, table: String, rowSelection: String, vararg parameters: Pair<String, Any?>) {
        if (connection !is Connection) {
            throw IllegalArgumentException("Connection has to be a Java Connection!")
        }

        val statement = StringBuilder("DELETE FROM ")
            .append(table)
            .append(" ")
            .append(rowSelection)

        val preparedStatement = connection.prepareStatement(statement.toString())

        preparedStatement.use {
            for (i in parameters.indices) {
                preparedStatement.setObject(i + 1, parameters[i].second)
            }

            preparedStatement.executeUpdate()
        }
    }

    /**
     * Updates the given row by the [rowSelection] of the given [table] with the given [parameters].
     * Does not close the connection.
     */
    override fun <C> update(connection: C, table: String, rowSelection: String, vararg parameters: Pair<String, Any?>) {
        if (connection !is Connection) {
            throw IllegalArgumentException("Connection has to be a Java Connection!")
        }

        val statement = StringBuilder("UPDATE ")
            .append(table)
            .append(" SET ")

        parameters.forEach { p ->
            if (!statement.endsWith("SET ")) {
                statement.append(", ")
            }

            statement.append(p.first).append(" = ?")
        }

        statement.append(" ").append(rowSelection)

        val preparedStatement = connection.prepareStatement(statement.toString())

        preparedStatement.use {
            for (i in parameters.indices) {
                preparedStatement.setObject(i + 1, parameters[i].second)
            }

            preparedStatement.executeUpdate()
        }
    }

    /**
     * Inserts the given [parameters] into the given [connection] [table].
     * Gets the created id of the inserted data. Does not close the connection.
     */
    override fun <C> insert(connection: C, table: String, vararg parameters: Pair<String, Any?>): Long {
        if (connection !is Connection) {
            throw IllegalArgumentException("Connection has to be a Java Connection!")
        }

        val statement = StringBuilder("INSERT INTO ")
            .append(table)
            .append(" (")

        parameters.forEach { p ->
            if (!statement.endsWith("(")) {
                statement.append(", ")
            }

            statement.append(p.first)
        }

        statement.append(") VALUES (")

        parameters.forEach {
            if (!statement.endsWith("(")) {
                statement.append(", ")
            }

            statement.append("?")
        }

        statement.append(")")

        val preparedStatement = connection.prepareStatement(statement.toString(), Statement.RETURN_GENERATED_KEYS)

        preparedStatement.use {
            for (i in parameters.indices) {
                preparedStatement.setObject(i + 1, parameters[i].second)
            }

            preparedStatement.executeUpdate()

            preparedStatement.generatedKeys.use { resultSet ->
                resultSet.next()
                return resultSet.getInt(1).toLong()
            }
        }
    }

    /**
     * Creates a query to the database with the given [connection] [sqlStatement] [parameters]. Iterates the
     * result set automatically. Does not close the connection.
     * [R] result type.
     */
    override fun <R, C> multiQuery(
        connection: C,
        sqlStatement: String,
        f: (Map<String, Any?>) -> R,
        vararg parameters: Any
    ): List<R> {
        if (connection !is Connection) {
            throw IllegalArgumentException("Connection has to be a Java Connection!")
        }

        val preparedStatement = connection.prepareStatement(sqlStatement)
        val list = ArrayList<R>()

        preparedStatement.use {
            for (i in parameters.indices) {
                preparedStatement.setObject(i + 1, parameters[i])
            }

            val resultSet = preparedStatement.executeQuery()

            resultSet.use {
                while (resultSet.next()) {
                    val metaData = resultSet.metaData
                    val data = HashMap<String, Any?>()

                    for (i in 1..metaData.columnCount) {
                        data[metaData.getColumnLabel(i)] = resultSet.getObject(i)
                    }

                    list.add(f.invoke(data))
                }
            }
        }

        return list
    }

    /**
     * Creates a query to the database with the given [connection] [sqlStatement] [parameters]. Iterates the
     * result set automatically. Does not close the connection.
     * [R] result type.
     */
    override fun <R, C> singleQuery(
        connection: C,
        sqlStatement: String,
        f: (Map<String, Any?>) -> R,
        vararg parameters: Any
    ): R? {
        if (connection !is Connection) {
            throw IllegalArgumentException("Connection has to be a Java Connection!")
        }

        val preparedStatement = connection.prepareStatement(sqlStatement)

        preparedStatement.use {
            for (i in parameters.indices) {
                preparedStatement.setObject(i + 1, parameters[i])
            }

            val resultSet = preparedStatement.executeQuery()

            resultSet.use {
                while (resultSet.next()) {
                    val metaData = resultSet.metaData
                    val data = HashMap<String, Any?>()

                    for (i in 1..metaData.columnCount) {
                        data[metaData.getColumnName(i)] = resultSet.getObject(i)
                    }

                    return f.invoke(data)
                }
            }
        }

        return null
    }

    /**
     * Creates a new transaction to the database.
     * [f] Handles creation and closing the transaction connection automatically and
     * manages connection pools in the background.
     */
    override fun <R, C> transaction(f: (C) -> R): R {
        val con = this.dataSource.connection
        var result: R? = null

        con.use { connection ->
            connection.autoCommit = false

            try {
                result = f.invoke(connection as C)
                connection.commit()
            } catch (e: SQLException) {
                loggingService.error("Failed to execute sql statement.", e)
            }

            connection.autoCommit = true
        }

        return result!!
    }

    /**
     * Closes remaining resources.
     */
    override fun close() {
        if (!this.dataSource.isClosed) {
            this.dataSource.close()
        }
    }

    /**
     * Connects the service to sqlite.
     */
    private fun connectToSqlite() {
        val path = createSQLLiteFile()
        this.dataSource = createDataSource(SQLITE_DRIVER, "jdbc:sqlite:" + path.toAbsolutePath().toString())

        val connection = this.dataSource.connection

        connection.use {
            connection.prepareStatement("PRAGMA foreign_keys=ON").use { statement ->
                statement.execute()
            }

            // Compatibility < 8.5.0
            val tablePrefix = if (configurationService.containsValue("sql.table-prefix")) {
                configurationService.findValue("sql.table-prefix")
            } else {
                "SHY"
            }

            configurationService.openResource("assets/petblocks/sql/create-sqlite.sql").bufferedReader()
                .use { reader ->
                    for (text in reader.readText().replace("TABLE_PREFIX", tablePrefix).split(";")) {
                        connection.prepareStatement(text).use { statement ->
                            statement.execute()
                        }
                    }
                }

            // Compatibility < 8.15.0
            var foundColumnName = true
            val skinTable = "${tablePrefix}_SKIN"
            connection.prepareStatement("PRAGMA table_info($skinTable);").use { statement ->
                statement.executeQuery().use { resultSet ->
                    while (resultSet.next()) {
                        foundColumnName = false
                        val columnName = resultSet.getString("name")
                        if (columnName == "nbt") {
                            foundColumnName = true
                            break
                        }
                    }
                }
            }

            // Compatibility < 8.15.0
            if (!foundColumnName) {
                addNbtColumn(connection, skinTable)
            }
        }

        loggingService.info("Connected to " + this.dataSource.jdbcUrl)
    }

    /**
     * Connect to the mysql database.
     */
    private fun connectToMySql() {
        val maxLifetime = if (configurationService.containsValue("sql.maxLifetime")) {
            TimeUnit.MINUTES.toMillis(configurationService.findValue<Int>("sql.maxLifetime").toLong())
        } else {
            TimeUnit.MINUTES.toMillis(30)
        }

        val connectionTimeout = if (configurationService.containsValue("sql.connectionTimeout")) {
            TimeUnit.SECONDS.toMillis(configurationService.findValue<Int>("sql.connectionTimeout").toLong())
        } else {
            TimeUnit.SECONDS.toMillis(30)
        }

        val validationTimeout = if (configurationService.containsValue("sql.validationTimeout")) {
            TimeUnit.SECONDS.toMillis(configurationService.findValue<Int>("sql.validationTimeout").toLong())
        } else {
            TimeUnit.SECONDS.toMillis(5)
        }

        val idleTimeout = if (configurationService.containsValue("sql.idleTimeout")) {
            TimeUnit.MINUTES.toMillis(configurationService.findValue<Int>("sql.idleTimeout").toLong())
        } else {
            TimeUnit.MINUTES.toMillis(10)
        }

        this.dataSource = createDataSource(
            MYSQL_DRIVER,
            "jdbc:mysql://" + configurationService.findValue<String>("sql.host") + ":" + configurationService.findValue<Int>(
                "sql.port"
            ) + "/" + configurationService.findValue<String>(
                "sql.database"
            ),
            configurationService.findValue<String>("sql.username"),
            configurationService.findValue<String>("sql.password"),
            configurationService.findValue("sql.usessl"),
            maxLifetime,
            connectionTimeout,
            validationTimeout,
            idleTimeout
        )

        val connection = this.dataSource.connection

        // Compatibility < 8.5.0
        val tablePrefix = if (configurationService.containsValue("sql.table-prefix")) {
            configurationService.findValue("sql.table-prefix")
        } else {
            "SHY"
        }

        connection.use {
            configurationService.openResource("assets/petblocks/sql/create-mysql.sql").bufferedReader()
                .use { reader ->
                    for (text in reader.readText().replace("TABLE_PREFIX", tablePrefix).split(";")) {
                        connection.prepareStatement(text).use { statement ->
                            statement.execute()
                        }
                    }
                }

            // Compatibility < 8.15.0
            var foundColumnName = true
            val skinTable = "${tablePrefix}_SKIN"
            connection.prepareStatement("select COLUMN_NAME from INFORMATION_SCHEMA.COLUMNS where TABLE_NAME = '$skinTable'")
                .use { statement ->
                    statement.executeQuery().use { resultSet ->
                        while (resultSet.next()) {
                            foundColumnName = false
                            val columnName = resultSet.getString(1)
                            if (columnName == "nbt") {
                                foundColumnName = true
                                break
                            }
                        }
                    }
                }

            // Compatibility < 8.15.0
            if (!foundColumnName) {
                addNbtColumn(connection, skinTable)
            }
        }

        loggingService.info("Connected to " + this.dataSource.jdbcUrl)
    }

    /**
     * Adds the nbt tag item column.
     */
    private fun addNbtColumn(connection: Connection, skinTable: String) {
        loggingService.info("Updating database to support item NBT tags...")

        connection.prepareStatement("ALTER TABLE $skinTable ADD COLUMN nbt TEXT").use { statement ->
            statement.execute()
        }

        var hasRowsLeft = true
        var offset = 0

        while (hasRowsLeft) {
            hasRowsLeft = false

            multiQuery(connection, "SELECT * FROM $skinTable LIMIT 100 OFFSET $offset", { map -> map }).forEach { e ->
                hasRowsLeft = true
                val unbreakableNumber = e["unbreakable"] as Int

                update(
                    connection, skinTable, "WHERE id=" + e["id"],
                    "unbreakable" to false,
                    "nbt" to "{Unbreakable:$unbreakableNumber}"
                )
            }

            offset += 100
        }

        loggingService.info("Finished updating database.")
    }

    /**
     * Creates the sqlLite file.
     */
    private fun createSQLLiteFile(): Path {
        if (!Files.exists(configurationService.applicationDir)) {
            Files.createDirectories(configurationService.applicationDir)
        }

        val path = Paths.get(configurationService.applicationDir.toFile().absolutePath, "PetBlocks.db")

        if (!Files.exists(path)) {
            Files.createFile(path)
        }

        return path
    }

    /**
     * Creates a new hikari datasource.
     */
    private fun createDataSource(
        driver: String,
        url: String,
        userName: String? = null,
        password: String? = null,
        useSSL: Boolean = false,
        maxLifetime: Long = TimeUnit.MINUTES.toMillis(30),
        connectionTimeout: Long = TimeUnit.SECONDS.toMillis(30),
        validationTimeout: Long = TimeUnit.SECONDS.toMillis(5),
        idleTimeout: Long = TimeUnit.MINUTES.toMillis(10)
    ): HikariDataSource {
        val config = HikariConfig()
        config.connectionTestQuery = "SELECT 1"
        config.jdbcUrl = url

        if (isBukkitServer) {
            config.driverClassName = driver
        }

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

        config.maxLifetime = maxLifetime
        config.connectionTimeout = connectionTimeout
        config.validationTimeout = validationTimeout
        config.idleTimeout = idleTimeout

        return HikariDataSource(config)
    }
}
