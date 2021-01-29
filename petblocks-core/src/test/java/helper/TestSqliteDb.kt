package helper

import com.github.shynixn.petblocks.api.persistence.context.SqlContext
import java.io.File
import java.sql.Connection
import java.sql.DriverManager

class TestSqliteDb : SqlContext {
    private val url = "jdbc:sqlite:test-db.sqlite"

    init {
        val file = File("test-db.sqlite")

        if (file.exists()) {
            file.delete()
        }

        getConnection().use {
            it.prepareStatement(
                "CREATE TABLE PETBLOCKS (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT" +
                        ", uuid CHAR(36) UNIQUE NOT NULL" +
                        ", name VARCHAR(16) NOT NULL" +
                        ", content TEXT)"
            ).execute()
        }
    }

    /**
     * Gets a new connection to the database.
     * Caller is responsible for closing the connection after using.
     */
    override fun getConnection(): Connection {
        return DriverManager.getConnection(url)
    }
}
