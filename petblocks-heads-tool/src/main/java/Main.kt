import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.bukkit.Material
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

/**
 * This is a quick and dirty tool program for generating the config.yml.
 */
fun main(args: Array<String>) {
    val sourceFile = Paths.get(Thread.currentThread().contextClassLoader.getResource("sourcefile.csv").toURI())
    val targetFile = Paths.get(Thread.currentThread().contextClassLoader.getResource("minecraft-heads-generated.db").toURI())
    val targetEncryptedFile = Paths.get(Thread.currentThread().contextClassLoader.getResource("minecraft-heads-encrypted.db").toURI())
    val targetYamlFile = Paths.get(Thread.currentThread().contextClassLoader.getResource("target.yml").toURI())

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

        val blockSkins = arrayOf(1 to 0
            , 1 to 2
            , 1 to 3
            , 1 to 4
            , 1 to 5
            , 1 to 6
            , 2 to 0
            , 3 to 0
            , 3 to 1
            , 3 to 2
            , 4 to 0
            , 5 to 0
            , 5 to 1
            , 5 to 2
            , 5 to 3
            , 5 to 4
            , 5 to 5
            , 7 to 0
            , 12 to 1
            , 13 to 0
            , 14 to 0
            , 15 to 0
            , 16 to 0
            , 17 to 0
            , 17 to 1
            , 17 to 2
            , 17 to 3
            , 18 to 0
            , 18 to 1
            , 18 to 2
            , 18 to 3
            , 19 to 0
            , 19 to 1
            , 20 to 0
            , 21 to 0
            , 22 to 0
            , 24 to 0
            , 24 to 1
            , 24 to 2
            , 25 to 0
            , 29 to 0
            , 33 to 0
            , 41 to 0
            , 42 to 0
            , 44 to 0
            , 45 to 0
            , 57 to 0
            , 155 to 0
        )

        val coloredSkins = arrayOf(35 to 0
            , 35 to 1
            , 35 to 2
            , 35 to 3
            , 35 to 4
            , 35 to 5
            , 35 to 6
            , 35 to 7
            , 35 to 8
            , 35 to 9
            , 35 to 10
            , 35 to 11
            , 35 to 12
            , 35 to 13
            , 35 to 14
            , 35 to 15
            , 95 to 0
            , 95 to 1
            , 95 to 2
            , 95 to 3
            , 95 to 4
            , 95 to 5
            , 95 to 6
            , 95 to 7
            , 95 to 8
            , 95 to 9
            , 95 to 10
            , 95 to 11
            , 95 to 12
            , 95 to 13
            , 95 to 14
            , 95 to 15
            , 159 to 0
            , 159 to 1
            , 159 to 2
            , 159 to 3
            , 159 to 4
            , 159 to 5
            , 159 to 6
            , 159 to 7
            , 159 to 8
            , 159 to 9
            , 159 to 10
            , 159 to 11
            , 159 to 12
            , 159 to 13
            , 159 to 14
            , 159 to 15
        )

        val playerHeads = arrayOf("Pig" to "http://textures.minecraft.net/texture/621668ef7cb79dd9c22ce3d1f3f4cb6e2559893b6df4a469514e667c16aa4"
        , "Chicken" to "http://textures.minecraft.net/texture/1638469a599ceef7207537603248a9ab11ff591fd378bea4735b346a7fae893"
            , "Dog" to "http://textures.minecraft.net/texture/e95cbb4f75ea87617f2f713c6d49dac3209ba1bd4b9369654b1459ea15317"
        , "Cat" to "http://textures.minecraft.net/texture/5657cd5c2989ff97570fec4ddcdc6926a68a3393250c1be1f0b114a1db1"
        , "Cow" to "http://textures.minecraft.net/texture/5d6c6eda942f7f5f71c3161c7306f4aed307d82895f9d2b07ab4525718edc5"
        , "Sheep" to "http://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70"
        , "Horse" to "http://textures.minecraft.net/texture/61902898308730c4747299cb5a5da9c25838b1d059fe46fc36896fee662729"
        , "Bird" to "http://textures.minecraft.net/texture/f2542c19cbcd964274244af6bed1ac8560b3f36fd3b3268061f8a614a9b59e35"
        , "Bat" to "http://textures.minecraft.net/texture/4cf1b3b3f539d2f63c172e94cacfaa391e8b385cdd633f3b991c74e44b28"
        , "Zombie" to "http://textures.minecraft.net/texture/56fc854bb84cf4b7697297973e02b79bc10698460b51a639c60e5e417734e11"
        , "Skeleto" to "http://textures.minecraft.net/texture/2e5be6a3c0159d2c1f3b1e4e1d8384b6f7ebac993d58b10b9f8989c78a232"
        , "Creeper" to "http://textures.minecraft.net/texture/295ef836389af993158aba27ff37b6567185f7a721ca90fdfeb937a7cb5747"
        ,  "Spider" to "http://textures.minecraft.net/texture/cd541541daaff50896cd258bdbdd4cf80c3ba816735726078bfe393927e57f1"
        , "Enderman" to  "http://textures.minecraft.net/texture/7a59bb0a7a32965b3d90d8eafa899d1835f424509eadd4e6b709ada50b9cf"
        , "Slime" to "http://textures.minecraft.net/texture/16ad20fc2d579be250d3db659c832da2b478a73a698b7ea10d18c9162e4d9b5"
        , "Lavaslime" to "http://textures.minecraft.net/texture/38957d5023c937c4c41aa2412d43410bda23cf79a9f6ab36b76fef2d7c429"
        , "Pigzombie" to "http://textures.minecraft.net/texture/74e9c6e98582ffd8ff8feb3322cd1849c43fb16b158abb11ca7b42eda7743eb"
        , "Dragon" to "http://textures.minecraft.net/texture/c52264ac4e5621668c43e87aed7918ed6b883abc5018769c9536d27b77f"
        , "Human" to "http://textures.minecraft.net/texture/456eec1c2169c8c60a7ae436abcd2dc5417d56f8adef84f11343dc1188fe138"
        , "Villager" to "http://textures.minecraft.net/texture/822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b"
        , "Irongolem" to "http://textures.minecraft.net/texture/89091d79ea0f59ef7ef94d7bba6e5f17f2f7d4572c44f90f76c4819a714"
        , "Silverfish" to "http://textures.minecraft.net/texture/ce663a1949db253da3a6f2a69e35da9a524ee3d958a3807ce2bdabcd6f7f5"
        , "Ghast" to "http://textures.minecraft.net/texture/8b6a72138d69fbbd2fea3fa251cabd87152e4f1c97e5f986bf685571db3cc0"
        , "Blaze" to "http://textures.minecraft.net/texture/b78ef2e4cf2c41a2d14bfde9caff10219f5b1bf5b35a49eb51c6467882cb5f0"
        , "Wither" to "http://textures.minecraft.net/texture/233b41fa79cd53a230e2db942863843183a70404533bbc01fab744769bcb"
        , "Shulker" to "http://textures.minecraft.net/texture/b1d3534d21fe8499262de87affbeac4d25ffde35c8bdca069e61e1787ff2f"
        , "Polar Bear" to "http://textures.minecraft.net/texture/442123ac15effa1ba46462472871b88f1b09c1db467621376e2f71656d3fbc"
        , "Llama" to "http://textures.minecraft.net/texture/818cd457fbaf327fa39f10b5b36166fd018264036865164c02d9e5ff53f45"
        , "Vex" to "http://textures.minecraft.net/texture/c2ec5a516617ff1573cd2f9d5f3969f56d5575c4ff4efefabd2a18dc7ab98cd"
        , "Parrot" to "http://textures.minecraft.net/texture/707dab2cbebea539b64d5ad246f9ccc1fcda7aa94b88e59fc2829852f46071"
        , "Mario"  to "http://textures.minecraft.net/texture/a0c2549a893726988f3428bef799875ba871688ae64eb0cfdc43f7d6e24c6c"
        , "Yoshi" to "http://textures.minecraft.net/texture/5fc8b863995fb84685c273c859548c75d94f9b82cce41b1efff454fe03cc123"
        , "Pokeball" to "http://textures.minecraft.net/texture/4b62d1dbf94e8cbb3c5327d96aac121d20677338924a5ed6de4cbf578a73d"
        , "Pikachu" to "http://textures.minecraft.net/texture/f6c5ee57717f561fc12b9f8878fbe0d0d62c72facfad61c0d27cade54e818c14"
        , "Charmander" to "http://textures.minecraft.net/texture/538992fa71d5d98789d5061ddd68e2b7af9efc253b39e1b346343d7789f8dc"
        , "Bulbasaur" to "http://textures.minecraft.net/texture/c99ec943b48c6f82f32acd9e8626546de8416cce4da41cbaa02c69feefbea"
        , "Squirtle" to "http://textures.minecraft.net/texture/f53ebc976cb6771f3e95117b326842ff7812c740bece96bb8858346d841"
        , "Mewto" to "http://textures.minecraft.net/texture/d9e0b56996f494adabebac9ea2a52c64ac486a5e0b8a89e485945dcca2b"
        , "Food" to "http://textures.minecraft.net/texture/f06555706b641fdaf436c07663f923afc5ee72146f90195fb337b9de766588d"
        , "Burger 1" to "http://textures.minecraft.net/texture/b26f0b5e52337c76b5a80e3d971477a8e38b0d71390f13defe1316ad6a0a67c"
        , "Burger 2" to "http://textures.minecraft.net/texture/b0e38c176dbf7df9b0632c256eeb6c5aaca99e1c8c1a530656eaff0417aed22"
        , "Milk" to "http://textures.minecraft.net/texture/d7ab62fb77189352541dd95a8ee7e3631f7c1658f463f661680c283493d8a"
        , "Bread" to "http://textures.minecraft.net/texture/9a29335ffad5bdf825a96be8eb4c1a803dace929fc8e1178475f8f8d9c5668"
        , "Fruit 1" to "http://textures.minecraft.net/texture/f71cc917159f14565f6afe22b9e33d5a23e423ecd8cb5dfba0b3f66d825389ca"
        , "Fruit 2" to "http://textures.minecraft.net/texture/6837a48ee530cfe35aca37969e4ea71d875237d2cb7a81b1ae80a75dc76e5a"
        , "Sweet 1" to "http://textures.minecraft.net/texture/dfd71e20fc50abf0de2ef7decfc01ce27ad51955759e072ceaab96355f594f0"
        , "Sweet 2" to "http://textures.minecraft.net/texture/819f948d17718adace5dd6e050c586229653fef645d7113ab94d17b639cc466"
        , "Stormtrooper 1" to "http://textures.minecraft.net/texture/37ca2aa8a9f5d3246ee7f2d07e1d5fd4819125ecddf5a72e2f675355a3b9bcc"
        , "Stormtrooper 2" to "http://textures.minecraft.net/texture/52284e132bfd659bc6ada497c4fa3094cd93231a6b505a12ce7cd5135ba8ff93"
        , "Boba Fett" to "http://textures.minecraft.net/texture/c535ff84f4b6d85626b92ca5d2619fd5599a96a585d851727d6633227a654"
        , "Jabba The Hutt" to "http://textures.minecraft.net/texture/68f54244604274927974e579d222f0c931278e0eceead31872c81c8e6f441"
        , "Wookie" to "http://textures.minecraft.net/texture/c9c0b6aa32b5a2c2d77f15dad54af3c2f201f818c7fa3ef83760648264fee2"
        , "Yoda" to "http://textures.minecraft.net/texture/4251d1e46d4d5fda32665da1f6cce29fd1c113491451c955f7668de32d37ae6"
        , "R2D2" to "http://textures.minecraft.net/texture/b0bcbea8d2ecce2e43daf5997e597b02c605f1babc11971ce4870342e3e1551"
        , "C3PO" to "http://textures.minecraft.net/texture/9fec991891f4143d40f013b98dc38dc53ed14ca75ebd32866efb126a32842a1"
        , "Notch" to "http://textures.minecraft.net/texture/a116e69a845e227f7ca1fdde8c357c8c821ebd4ba619382ea4a1f87d4ae94"
        , "Shynixn" to "http://textures.minecraft.net/texture/3a22126c8187c352bf4dac5af2afe48dbf0d4e5f98542bdd9f6c5f52f5169b"
        , "Santa" to "http://textures.minecraft.net/texture/14e424b1676feec3a3f8ebade9e7d6a6f71f7756a869f36f7df0fc182d436e"
        , "Herobrine" to "http://textures.minecraft.net/texture/98b7ca3c7d314a61abed8fc18d797fc30b6efc8445425c4e250997e52e6cb"
        , "Miner" to "http://textures.minecraft.net/texture/d357444ade64ec6cea645ec57e775864d67c5fa62299786e03799317ee4ad"
        , "Sonic" to "http://textures.minecraft.net/texture/42bc2de782fd6d7c50e9b3edb133487191d8f5546644c25be357cca2685db"
        , "Reaper" to "http://textures.minecraft.net/texture/fb3329b3c82ddfcd19255c42712eca7bc24472c03f78668faff7d8431997d693"
        , "Crazy" to "http://textures.minecraft.net/texture/797884d451dc7b7729de2076cd6c4912865ade70391d1ccec3e95fb39f8c5e1"
        )

        printBlockSkins("  block-skins:", builder, blockSkins)
        printBlockSkins("  colored-block-skins:", builder, coloredSkins)
        printPlayerHeads("  player-head-skins:", builder, playerHeads )

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

            appendPageItems(builder)

            var counter = 0
            var amount = (resultPet.size / 5)
            var remaining = resultPet.size % 5

            for (i in 1..5) {
                for (j in 1..amount) {
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
            for (i in 0 until remaining) {
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

private fun printBlockSkins(name: String, builder: StringBuilder, blockSkins: Array<Pair<Int, Int>>) {
    builder.appendln(name)
    appendPageItems(builder)

    var counter = 0
    var amount = (blockSkins.size / 5)
    var remaining = blockSkins.size % 5

    for (i in 1..5) {
        for (j in 1..amount) {
            var item = blockSkins[counter]

            val material = Material.getMaterial(item.first)

            builder.append("    block-").append(i).append("-").append(j).appendln(":")
            builder.appendln("      row: $i")
            builder.appendln("      col: $j")
            builder.appendln("      icon:")
            builder.appendln("        id: ${item.first}")
            builder.appendln("        damage: ${item.second}")
            builder.appendln("        name: '${material.customName()}'")
            builder.appendln("      set-skin:")
            builder.appendln("        id: ${item.first}")
            builder.appendln("        damage: ${item.second}")
            counter++
        }
    }

    var startCol = amount + 1
    var rowCounter = 1
    for (i in 1 until remaining) {
        val item = blockSkins[counter]
        val material = Material.getMaterial(item.first)

        builder.append("    block-").append(i).append("-").append(startCol).appendln(":")
        builder.appendln("      row: $i")
        builder.appendln("      col: $startCol")
        builder.appendln("      icon:")
        builder.appendln("        id: ${item.first}")
        builder.appendln("        damage: ${item.second}")
        builder.appendln("        name: '${material.customName()}'")
        builder.appendln("      set-skin:")
        builder.appendln("        id: ${item.first}")
        builder.appendln("        damage: ${item.second}")
        counter++
        rowCounter++
    }
}

private fun printPlayerHeads(name: String, builder: StringBuilder, blockSkins: Array<Pair<String, String>>) {
    builder.appendln(name)
    appendPageItems(builder)

    var counter = 0
    var amount = (blockSkins.size / 5)
    var remaining = blockSkins.size % 5

    for (i in 1..5) {
        for (j in 1..amount) {
            var item = blockSkins[counter]

            builder.append("    block-").append(i).append("-").append(j).appendln(":")
            builder.appendln("      row: $i")
            builder.appendln("      col: $j")
            builder.appendln("      icon:")
            builder.appendln("        id: 397")
            builder.appendln("        damage: 3")
            builder.appendln("        name: '${item.first}'")
            builder.appendln("        skin: '${item.second}'")
            builder.appendln("      set-skin:")
            builder.appendln("        id: 397")
            builder.appendln("        damage: 3")
            builder.appendln("        skin: '${item.second}'")
            counter++
        }
    }

    var startCol = amount + 1
    var rowCounter = 1
    for (i in 1 until remaining) {
        val item = blockSkins[counter]

        builder.append("    block-").append(i).append("-").append(startCol).appendln(":")
        builder.appendln("      row: $i")
        builder.appendln("      col: $startCol")
        builder.appendln("      icon:")
        builder.appendln("        id: 397")
        builder.appendln("        damage: 3")
        builder.appendln("        name: '${item.first}'")
        builder.appendln("        skin: '${item.second}'")
        builder.appendln("      set-skin:")
        builder.appendln("        id: 397")
        builder.appendln("        damage: 3")
        builder.appendln("        skin: '${item.second}'")
        counter++
        rowCounter++
    }
}

fun Material.customName(): String {
    return this.name.toLowerCase().capitalize().replace("_", " ")
}

private fun appendPageItems(builder: StringBuilder) {
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
}