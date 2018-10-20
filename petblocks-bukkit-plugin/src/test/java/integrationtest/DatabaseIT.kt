package integrationtest

import ch.vorburger.mariadb4j.DB
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.sql.DriverManager

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
class DatabaseIT {
    /**
     * Given
     *   a sqlite database.
     * When
     *    hikari connection is setup
     * Then
     *    the connection to the database should be working.
     */
    @Test
    fun enableSqlite_DataSourceConfiguration_ShouldStartUpAndCloseWithoutProblems() {
        // Arrange, Act, Assert
        try {
            val config = HikariConfig()
            config.driverClassName = "org.sqlite.JDBC"
            config.connectionTestQuery = "SELECT 1"
            config.jdbcUrl = "jdbc:sqlite:PetBlocks.db"
            config.maxLifetime = 60000
            config.idleTimeout = 45000
            config.maximumPoolSize = 50

            val ds = HikariDataSource(config)
            ds.close()
        } catch (e: Exception) {
            Assertions.fail("Failed to enable databaseource for sqllite.", e)
        }
    }

    /**
     * Given
     *   a mysql database.
     * When
     *    hikari connection is setup
     * Then
     *    the connection to the database should be working.
     */
    @Test
    fun enableMySql_DataSourceConfiguration_ShouldStartUpAndCloseWithoutProblems() {
        // Arrange, Act, Assert
        try {
            val database = DB.newEmbeddedDB(3306)
            database.start()

            DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=").use { conn ->
                conn.createStatement().use { statement ->
                    statement.executeUpdate("CREATE DATABASE db")
                }
            }

            val config = HikariConfig()
            config.driverClassName = "com.mysql.jdbc.Driver"
            config.connectionTestQuery = "SELECT 1"
            config.jdbcUrl = "jdbc:mysql://localhost:3306/db"
            config.maxLifetime = 60000
            config.idleTimeout = 45000
            config.maximumPoolSize = 50

            val ds = HikariDataSource(config)
            ds.close()
            database.stop()
        } catch (e: Exception) {
            Assertions.fail("Failed to enable databaseource for sqllite.", e)
        }
    }
}