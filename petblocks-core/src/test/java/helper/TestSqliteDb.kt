package helper

import com.github.shynixn.petblocks.api.persistence.context.SqlContext
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.*

class TestSqliteDb : SqlContext {
    private var databaseName = "test-db-" + UUID.randomUUID().toString() + ".sqlite"
    private val url = "jdbc:sqlite:${databaseName}"
    private val file = File(databaseName)

    init {
        DriverManager.getConnection(url).use {
            it.prepareStatement(
                "CREATE TABLE PETBLOCKS (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                        ", uuid CHAR(36) UNIQUE NOT NULL" +
                        ", name VARCHAR(16) NOT NULL" +
                        ", content TEXT)"
            ).execute()
        }
    }

    fun shutdown() {
        synchronized(file) {
            if (file.exists()) {
                file.delete()
            }
        }
    }

    /**
     * Gets a new connection to the database.
     * Caller is responsible for closing the connection after using.
     */
    override fun getConnection(): Connection {
        synchronized(file) {
            if (!file.exists()) {
                throw RuntimeException("File does not longer exist.")
            }

            return DriverManager.getConnection(url)
        }
    }
}
