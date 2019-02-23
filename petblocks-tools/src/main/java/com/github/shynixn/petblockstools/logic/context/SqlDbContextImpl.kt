@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblockstools.logic.context

import com.github.shynixn.petblockstools.logic.proxy.SqlProxyImpl
import java.sql.Connection
import java.sql.SQLException
import java.sql.Statement
import java.util.*
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.collections.HashMap

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
class SqlDbContextImpl constructor(private val sqlConnectionPoolProxy: SqlProxyImpl, private val loggingService: Logger)  {
    /**
     * Updates the given row by the [rowSelection] of the given [table] with the given [parameters].
     * Does not close the connection.
     */
    fun <C> update(connection: C, table: String, rowSelection: String, vararg parameters: Pair<String, Any?>) {
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
    fun <C> insert(connection: C, table: String, vararg parameters: Pair<String, Any?>): Long {
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
   fun <R, C> multiQuery(connection: C, sqlStatement: String, f: (Map<String, Any>) -> R, vararg parameters: Any): List<R> {
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
                    val data = HashMap<String, Any>()

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
    fun <R, C> singleQuery(connection: C, sqlStatement: String, f: (Map<String, Any>) -> R, vararg parameters: Any): R? {
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
                    val data = HashMap<String, Any>()

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
    fun <R, C> transaction(f: (C) -> R): R {
        val con = sqlConnectionPoolProxy.openConnection<Connection>()
        var result: R? = null

        con.use { connection ->
            connection.autoCommit = false

            try {
                result = f.invoke(connection as C)
                connection.commit()
            } catch (e: SQLException) {
                loggingService.log(Level.WARNING,"Failed to execute sql statement.", e)
            }

            connection.autoCommit = true
        }

        return result!!
    }
}