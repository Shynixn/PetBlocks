package com.github.shynixn.petblockstools.logic.service

import com.github.shynixn.petblockstools.contract.ConfigService
import com.github.shynixn.petblockstools.logic.entity.SkinDescription
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.util.*
import java.util.logging.*
import javax.crypto.Cipher
import javax.crypto.CipherOutputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.ArrayList

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class ConfigServiceImpl : ConfigService {
    private val logger = generateLogger()

    /**
     * Generates an encrypted, an decrypted minecraft-heads.com db and
     * the yaml file content for minecraft-heads.com into the given output path.
     */
    override fun generateFiles(minecraftHeadsSource: Path, ouputFolder: Path) {
        logger.log(Level.INFO, "Folder ${ouputFolder.toAbsolutePath()}.")

        val dbFile = ouputFolder.resolve("minecraft-heads-generated.db")
        val encryptedFile = ouputFolder.resolve("minecraft-heads-encrypted.db")
        val yamlFile = ouputFolder.resolve("target.yml")

        val sourceSkins = loadSourceFile(minecraftHeadsSource)
        val existingSkins = loadExistingSkins(dbFile)

        existingSkins.addAll(sourceSkins.filter { s -> existingSkins.firstOrNull { e -> e.skin == s.skin } == null })
        val skins = existingSkins.distinctBy { s -> s.skin }.toList()

        fixMapping(skins)

        generateDecryptedDbFile(dbFile, skins)
        generateEncryptedDbFile(dbFile, encryptedFile)
        generateYamlFile(yamlFile, skins)
    }

    /**
     * Generates the yaml file for the config.yml.
     */
    private fun generateYamlFile(outputFile: Path, skins: List<SkinDescription>) {
        val builder = StringBuilder()

        generateSection(builder, "minecraft-heads-pet-skins:", skins.filter { s -> s.headType == "Pet" })
        generateSection(builder, "minecraft-heads-puppet-skins:", skins.filter { s -> s.headType == "Puppet" })
        generateSection(builder, "minecraft-heads-vehicle-skins:", skins.filter { s -> s.headType == "Vehicle" })

        FileUtils.write(outputFile.toFile(), builder.toString())
        logger.log(Level.INFO, "Generated yaml config file.")
    }

    /**
     * Generates a new section in the config.yml.
     */
    private fun generateSection(builder: StringBuilder, sectionTitle: String, skins: List<SkinDescription>) {
        builder.appendln(sectionTitle)

        appendPageItems(builder)

        var counter = 0
        val amount = (skins.size / 5)
        val remaining = skins.size % 5

        for (i in 1..5) {
            for (j in 1..amount) {
                val item = skins[counter]

                builder.append("    block-").append(i).append("-").append(j).appendln(":")
                builder.appendln("      row: $i")
                builder.appendln("      col: $j")
                builder.appendln("      icon:")
                builder.appendln("        id: 397")
                builder.appendln("        damage: 3")
                builder.appendln("        name: 'minecraft-heads.com/${item.index}'")
                builder.appendln("        skin: 'minecraft-heads.com/${item.index}'")
                builder.appendln("      set-skin:")
                builder.appendln("        id: 397")
                builder.appendln("        damage: 3")
                builder.appendln("        skin: 'minecraft-heads.com/${item.index}'")
                counter++
            }
        }

        val startCol = amount + 1
        var rowCounter = 1
        for (i in 0 until remaining) {
            val item = skins[counter]

            builder.append("    block-").append(i).append("-").append(startCol).appendln(":")
            builder.appendln("      row: $rowCounter")
            builder.appendln("      col: $startCol")
            builder.appendln("      icon:")
            builder.appendln("        id: 397")
            builder.appendln("        damage: 3")
            builder.appendln("        name: 'minecraft-heads.com/${item.index}'")
            builder.appendln("        skin: 'minecraft-heads.com/${item.index}'")
            builder.appendln("      set-skin:")
            builder.appendln("        id: 397")
            builder.appendln("        damage: 3")
            builder.appendln("        skin: 'minecraft-heads.com/${item.index}'")
            counter++
            rowCounter++
        }
    }

    /**
     * Writes an encrypted db file.
     */
    private fun generateEncryptedDbFile(inputFile: Path, outputFile: Path) {
        val key = UUID.randomUUID().toString().replace("-", "").toByteArray()
        val secretKey = ByteArray(16)

        for (i in secretKey.indices) {
            secretKey[i] = key[i]
        }

        val resultKey = Base64.getEncoder().encodeToString(secretKey)
        logger.log(Level.INFO, "Decryption key: $resultKey")

        val iv = IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8")))
        val keySpec = SecretKeySpec(secretKey, "AES")
        val encipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
        encipher.init(Cipher.ENCRYPT_MODE, keySpec, iv)

        try {
            FileInputStream(inputFile.toFile()).use { inputStream ->
                CipherOutputStream(FileOutputStream(outputFile.toFile()),
                    encipher).use { outputStream -> IOUtils.copy(inputStream, outputStream) }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    /**
     * Writes an decrypted db file.
     */
    private fun generateDecryptedDbFile(outputFile: Path, skins: List<SkinDescription>) {
        FileUtils.writeLines(outputFile.toFile(), "UTF-8", skins.map { s -> s.index.toString() + ";" + s.headType + ";" + s.name + ";" + s.skin })
        logger.log(Level.INFO, "Generated decrypted db file.")
    }

    /**
     * Fixes the duplicate names and indexed skins.
     */
    private fun fixMapping(skins: List<SkinDescription>) {
        for (skin in skins) {
            if (skins.count { s -> s.name == skin.name } == 1 && skins.count { s -> s.name == (skin.name + " 1") } == 0) {
                continue
            }

            var counter = 1

            while (true) {
                val skinName = skin.name + " " + counter

                if (skins.count { s -> s.name == skinName } != 0) {
                    counter++
                    continue
                }

                skin.name = skinName

                break
            }
        }

        var maxIndex = skins.maxBy { p -> p.index }!!.index

        for (skin in skins) {
            if (skin.index == 0) {
                maxIndex++
                skin.index = maxIndex
            }
        }
    }

    /**
     * Loads the existing contents.
     */
    private fun loadExistingSkins(sourceFile: Path): MutableList<SkinDescription> {
        val skins = ArrayList<SkinDescription>()

        logger.log(Level.INFO, "Loading existing skins file...")

        if (!Files.exists(sourceFile)) {
            logger.log(Level.INFO, "Existing file does not exist.")
            return skins
        }

        try {
            val lines = Files.readAllLines(sourceFile)

            for (line in lines) {
                val content = line.split(";")
                val skin = SkinDescription(content[0].toInt(), content[2], content[3], content[1])

                skins.add(skin)
            }

            val dateTime = Date().time

            Files.copy(sourceFile, sourceFile.parent.resolve("old_minecraft-heads-$dateTime.db"))
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Existing file is not correct.", e)
        }

        logger.log(Level.INFO, "Completed.")

        return skins
    }

    /**
     * Loads the new source contents.
     */
    private fun loadSourceFile(sourceFile: Path): List<SkinDescription> {
        val skins = ArrayList<SkinDescription>()

        logger.log(Level.INFO, "Loading source file...")

        try {
            val lines = Files.readAllLines(sourceFile)

            for (line in lines) {
                val content = line.split(Regex(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)"))
                val skin = SkinDescription(0, content[1].replace("\"", ""), content[2], content[0])

                skins.add(skin)
            }
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Source file is not correct.", e)
        }

        logger.log(Level.INFO, "Completed.")

        return skins
    }

    /**
     * Generates a nicely formatted console logger.
     */
    private fun generateLogger(): Logger {
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
        return Logger.getLogger("MinecraftHeadDatabaseManager")
    }

    /**
     * Appends the default page items.
     */
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

}