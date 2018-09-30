package com.github.shynixn.petblocks.sponge.logic.compatibility

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage
import com.github.shynixn.petblocks.core.logic.compatibility.CostumeConfiguration
import com.github.shynixn.petblocks.sponge.logic.business.extension.getResource
import com.google.inject.Inject
import org.slf4j.Logger
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.plugin.PluginContainer
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.regex.Pattern
import javax.crypto.Cipher
import javax.crypto.CipherInputStream
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

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
class SpongeMinecraftConfiguration : CostumeConfiguration<Player>("minecraftheads") {

    @Inject
    private lateinit var plugin : PluginContainer

    @Inject
    private lateinit var logger: Logger

    /**
     * Reloads the content from the fileSystem
     */
    override fun reload() {
        this.items.clear()
        try {
            val decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
            decipher.init(Cipher.DECRYPT_MODE, SecretKeySpec(Base64Coder.decode("NTk50mqoZMw9ZTxcQJlVhA=="), "AES"), IvParameterSpec("RandomInitVector".toByteArray(charset("UTF-8"))))
            BufferedReader(InputStreamReader(CipherInputStream(plugin.getResource("minecraftheads.db"), decipher))).use { reader ->
                var s: String?
                val splitter = Pattern.quote(",")
                var i = 0
                while (true) {
                    s = reader.readLine()
                    if (s == null) {
                        break
                    }
                    val tags = s.split(splitter.toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                    if (tags.size == 2 && tags[1].length % 4 == 0) {
                        i++
                        try {
                            val line = Base64Coder.decodeString(tags[1]).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "")
                            val url = line.substring(0, line.indexOf("\""))
                            val texture = url.substring(7, url.length)
                            val container = SpongeItemContainer(true, i, GUIPage.MINECRAFTHEADS_COSTUMES, 397, 3, texture, false, tags[0].replace("\"", ""), emptyArray())
                            this.items.add(container)
                        } catch (ignored: Exception) {
                            logger.error("Failed parsing minecraftheads.com head.", ignored)
                        }

                    }
                }
            }
        } catch (e: Exception) {
            logger.error("Failed to read minecraft-heads.com skins.",e)
        }
    }
}