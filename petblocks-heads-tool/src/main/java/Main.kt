import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import java.sql.Connection
import java.util.*
import java.util.logging.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

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

fun main(args: Array<String>) {
    val sourceFile =
        Paths.get("")
    val targetFile = Paths.get("")
    val targetEncryptedFile = Paths.get("")
    val targetYamlFile = Paths.get("")

    val mainLogger = Logger.getLogger("com.logicbig")
    mainLogger.useParentHandlers = false
    val handler = ConsoleHandler()
    handler.formatter = object : SimpleFormatter() {
        private val format = "[%1\$tF %1\$tT] [%2$-7s] %3\$s %n"

        @Synchronized
        override fun format(lr: LogRecord): String {
            return String.format(format,
                Date(lr.millis),
                lr.level.localizedName,
                lr.message
            )
        }
    }
    mainLogger.addHandler(handler)
    val logger = Logger.getLogger("MinecraftHeadDatabaseManager")

    var lines: List<String> = ArrayList()

    logger.log(Level.INFO, "Loading source file...")

    try {
        lines = Files.readAllLines(sourceFile)
    } catch (e: Exception) {
        logger.log(Level.WARNING, "Source file is not correct.", e)
    }

    logger.log(Level.INFO, "Completed.")

    logger.log(Level.INFO, "Inserting data into database...")

    var counter = 0
    var size = lines.size

    SqlProxyImpl(logger).use { proxy ->
        val sqlContext = SqlDbContextImpl(proxy, logger)

        sqlContext.transaction<Any, Connection> { connection ->

            lines.forEach { line ->

                val content = line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))

                if (content.size == 3) {
                    var found = false
                    var appendCounter = 1

                    while (!found) {
                        try {
                            sqlContext.insert(connection, "SHY_MCHEAD"
                                , "name" to content[1].replace("\"", "") + " " + appendCounter
                                , "skin" to content[2]
                                , "headtype" to content[0]
                            )
                            found = true
                            counter++
                        } catch (e: Exception) {
                        }

                        if (appendCounter > 100) {
                            break
                        }

                        appendCounter++
                    }
                } else {
                    logger.log(Level.WARNING, "Failure in line " + line + ".")
                }
            }

            var nameChanges = 0

            sqlContext.multiQuery(connection, "SELECT id, name FROM SHY_MCHEAD\n" +
                    "WHERE name like '%1'\n" +
                    "  and NOT(Replace(name, '1', '2') IN(SELECT name from SHY_MCHEAD))", { result ->
                val identifier = (result["id"] as Int)
                val target = (result["name"] as String).replace(" 1", "")

                sqlContext.update(connection, "SHY_MCHEAD", "WHERE id = $identifier", "name" to target)

                println(target)

                nameChanges++
            })

            logger.log(Level.INFO, "$nameChanges name changes were required.")
        }

        logger.log(Level.INFO, "Inserted $counter/$size new items into the database.")
        logger.log(Level.INFO, "Completed.")
        logger.log(Level.INFO, "Generating csv from stored data...")

        val result = sqlContext.transaction<List<String>, Connection> { connection ->
            sqlContext.multiQuery(connection, "SELECT * FROM SHY_MCHEAD ORDER BY headtype, id", { resultSet ->
                val stringBuilder = StringBuilder()

                stringBuilder.append(resultSet["id"] as Int)
                stringBuilder.append(";")
                stringBuilder.append(resultSet["headtype"] as String)
                stringBuilder.append(";")
                stringBuilder.append(resultSet["name"] as String)
                stringBuilder.append(";")
                stringBuilder.append(resultSet["skin"] as String)

                stringBuilder.toString()
            })
        }

        FileUtils.writeLines(targetFile.toFile(), result)
        logger.log(Level.INFO, "Completed.")

        logger.log(Level.INFO, "Generating encrypted csv from csv...")

        val key = UUID.randomUUID().toString().replace("-", "").toByteArray()
        val halfkey = ByteArray(16)

        for (i in halfkey.indices) {
            halfkey[i] = key[i]
        }

        val iv = IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8")))
        val skeySpec = SecretKeySpec(halfkey, "AES")

        val encipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        encipher.init(Cipher.ENCRYPT_MODE, skeySpec, iv)
        val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        decipher.init(Cipher.DECRYPT_MODE, skeySpec, iv)

        try {
            FileInputStream(targetFile.toFile()).use { inputStream ->
                CipherOutputStream(FileOutputStream(targetEncryptedFile.toFile()),
                    encipher).use { outputStream -> IOUtils.copy(inputStream, outputStream) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        logger.log(Level.INFO, "Completed.")

        logger.log(Level.INFO, "Generating yaml source...")

        var categories = arrayOf("Pet", "Puppet", "Vehicle")
        var builder = StringBuilder()


        categories.forEach { category ->
            val resultPet = sqlContext.transaction<List<String>, Connection> { connection ->
                sqlContext.multiQuery(connection, "SELECT * FROM SHY_MCHEAD WHERE headtype='$category' ORDER BY headtype, id", { resultSet ->
                    (resultSet["id"] as Int).toString()
                })
            }

            if (category == "Pet") {
                builder.appendln("  minecraft-heads-pet-skins:")
            }
            if (category == "Puppet") {
                builder.appendln("  minecraft-heads-puppet-skins:")
            }
            if (category == "Vehicle") {
                builder.appendln("  minecraft-heads-vehicle-skins:")
            }

            builder.appendln("    next-page:\n" +
                    "      row: 6\n" +
                    "      col: 9\n" +
                    "      fixed: true\n" +
                    "      script: 'scroll 2 0'\n" +
                    "      icon: \n" +
                    "       id: 397\n" +
                    "       damage: 3\n" +
                    "       skin: 'http://textures.minecraft.net/texture/1b6f1a25b6bc199946472aedb370522584ff6f4e83221e5946bd2e41b5ca13b'\n" +
                    "       name: '&aNext page'\n" +
                    "       script: 'hide-right-scroll'\n" +
                    "    previous-page:\n" +
                    "      row: 6\n" +
                    "      col: 1\n" +
                    "      fixed: true\n" +
                    "      script: 'scroll -2 0'\n" +
                    "      icon: \n" +
                    "       id: 397\n" +
                    "       damage: 3\n" +
                    "       skin: 'http://textures.minecraft.net/texture/3ebf907494a935e955bfcadab81beafb90fb9be49c7026ba97d798d5f1a23'\n" +
                    "       name: '&aPrevious page'\n" +
                    "       script: 'hide-left-scroll'\n" +
                    "    back:\n" +
                    "      row: 6\n" +
                    "      col: 5\n" +
                    "      fixed: true\n" +
                    "      script: 'close-gui'\n" +
                    "      icon:\n" +
                    "        id: 166\n" +
                    "        damage: 0\n" +
                    "        name: '&cBack'\n" +
                    "        lore:\n" +
                    "        - '&7Closes the current window.'")

            var counter = 0
            var amount = (resultPet.size / 5)
            var remaining = resultPet.size % 5

            for (i in 1..5) {
                for(j in 1..amount) {
                    var item = resultPet[counter]

                    builder.append("    block-").append(i).append("-").append(j).appendln(":")
                    builder.appendln("      row: $i")
                    builder.appendln("      col: $j")
                    builder.appendln("      icon:")
                    builder.appendln("        id: 397")
                    builder.appendln("        damage: 3")
                    builder.appendln("        name: 'minecraft-heads.com/$item'")
                    builder.appendln("        skin: 'minecraft-heads.com/$item'")
                    builder.appendln("      set-skin:")
                    builder.appendln("        id: 397")
                    builder.appendln("        damage: 3")
                    builder.appendln("        skin: 'minecraft-heads.com/$item'")
                    counter++
                }
            }

            var startCol = amount + 1
            var rowCounter = 1
            for(i in 0 until remaining){
                val item = resultPet[counter]

                builder.append("    block-").append(i).append("-").append(startCol).appendln(":")
                builder.appendln("      row: $rowCounter")
                builder.appendln("      col: $startCol")
                builder.appendln("      icon:")
                builder.appendln("        id: 397")
                builder.appendln("        damage: 3")
                builder.appendln("        name: 'minecraft-heads.com/$item'")
                builder.appendln("        skin: 'minecraft-heads.com/$item'")
                builder.appendln("      set-skin:")
                builder.appendln("        id: 397")
                builder.appendln("        damage: 3")
                builder.appendln("        skin: 'minecraft-heads.com/$item'")
                counter++
                rowCounter++
            }
        }


        FileUtils.write(targetYamlFile.toFile(), builder.toString())

        logger.log(Level.INFO, "Finished generation. Decryption Key: " + Base64.getEncoder().encodeToString(halfkey))
    }
}