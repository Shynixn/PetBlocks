import ch.vorburger.mariadb4j.DB
import ch.vorburger.mariadb4j.DBConfigurationBuilder
import org.junit.jupiter.api.Test
import java.sql.DriverManager

class MockMySqlServer {
    // Allows to boot a MySql server locally to test PetBlocks.
    // @Test
    fun boot() {
        val config = DBConfigurationBuilder.newBuilder()
        config.setPort(3306)
        config.addArg("--user=root")
        val database = DB.newEmbeddedDB(config.build())
        database!!.start()

        DriverManager.getConnection("jdbc:mysql://localhost:3306/?user=root&password=").use { conn ->
            conn.createStatement().use { statement ->
                statement.executeUpdate("CREATE DATABASE PetBlocks")
            }
        }

        while (true) {
            Thread.sleep(50)
        }
    }
}
