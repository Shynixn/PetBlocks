package com.github.shynixn.petblocks.core.logic.business.entity

import java.nio.file.Files
import java.nio.file.Path
import java.sql.Connection
import java.sql.PreparedStatement
import java.sql.SQLException
import java.sql.Statement

/**
 * Db Context for holding database connections.
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
abstract class DbContext : AutoCloseable {
    private val cache = HashMap<String, String>()

    /**
     * Returns a new connection from the db context which should be closed after being used.
     */
    abstract fun getConnection(): Connection

    /**
     * Returns the content from the given filename.
     */
    abstract fun getContentFromName(name: String): String

    /**
     * Executes the sql statement stored in the given fileName.
     */
    @Throws(SQLException::class)
    fun executeStored(fileName: String, connection: Connection, vararg parameters: Any): Boolean {
        return this.execute(this.getStatementFromFile(fileName), connection, *parameters)
    }

    /**
     * Executes the given sql statement.
     */
    @Throws(SQLException::class)
    fun execute(sql: String, connection: Connection, vararg parameters: Any): Boolean {
        if (connection.isClosed)
            throw IllegalArgumentException("Connection is closed. Cannot create statement!")
        connection.prepareStatement(sql).use { preparedStatement ->
            this.setParameters(preparedStatement, parameters)
            return preparedStatement.execute()
        }
    }

    /**
     * Creates a query sql statement stored in the given fileName and returns it.
     */
    @Throws(SQLException::class)
    fun executeStoredQuery(fileName: String, connection: Connection, vararg parameters: Any): PreparedStatement {
        return this.executeQuery(this.getStatementFromFile(fileName), connection, *parameters)
    }

    /**
     * Executes the given query sql statement.
     */
    @Throws(SQLException::class)
    fun executeQuery(sql: String, connection: Connection, vararg parameters: Any): PreparedStatement {
        if (connection.isClosed)
            throw IllegalArgumentException("Connection is closed. Cannot create statement!")
        val preparedStatement = connection.prepareStatement(sql)
        this.setParameters(preparedStatement, parameters)
        return preparedStatement
    }

    /**
     * Executes the sql update statement stored in the given fileName.
     */
    @Throws(SQLException::class)
    fun executeStoredUpdate(fileName: String, connection: Connection, vararg parameters: Any): Int {
        return this.executeUpdate(this.getStatementFromFile(fileName), connection, *parameters)
    }

    /**
     * Executes the given update sql statement.
     */
    @Throws(SQLException::class)
    fun executeUpdate(sql: String, connection: Connection, vararg parameters: Any): Int {
        if (connection.isClosed)
            throw IllegalArgumentException("Connection is closed. Cannot create statement!")
        connection.prepareStatement(sql).use { preparedStatement ->
            this.setParameters(preparedStatement, parameters)
            return preparedStatement.executeUpdate()
        }
    }

    /**
     * Executes the sql insert Statement stored in the given fileName. Returns the id of the object created.
     */
    @Throws(SQLException::class)
    fun executeStoredInsert(fileName: String, connection: Connection, vararg parameters: Any): Int {
        return this.executeInsert(this.getStatementFromFile(fileName), connection, *parameters)
    }

    /**
     * Executes the sql insert Statement stored in the given fileName. Returns the id of the object created.
     */
    @Throws(SQLException::class)
    fun executeInsert(sql: String, connection: Connection, vararg parameters: Any): Int {
        if (connection.isClosed)
            throw IllegalArgumentException("Connection is closed. Cannot create statement!")
        connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS).use { preparedStatement ->
            this.setParameters(preparedStatement, parameters)
            preparedStatement.executeUpdate()
            preparedStatement.generatedKeys.use { resultSet ->
                resultSet.next()
                return resultSet.getInt(1)
            }
        }
    }

    /**
     * Returns a statement from the given statementName.
     */
    protected fun getStatementFromFile(statementName: String): String {
        if (!this.cache.containsKey(statementName)) {
            this.cache[statementName] = getContentFromName(statementName)
        }
        return this.cache[statementName]!!
    }

    /**
     * Sets the parameters to the given preparedStatement.
     */
    @Throws(SQLException::class)
    private fun setParameters(preparedStatement: PreparedStatement, vararg parameters: Any) {
        for (i in parameters.indices) {
            preparedStatement.setObject(i + 1, parameters[i])
        }
    }
}