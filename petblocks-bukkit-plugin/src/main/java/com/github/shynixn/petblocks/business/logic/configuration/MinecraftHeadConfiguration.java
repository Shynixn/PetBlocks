package com.github.shynixn.petblocks.business.logic.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public class MinecraftHeadConfiguration extends CostumeConfiguration {

    /**
     * Initializes a new engine repository
     *
     * @param plugin plugin
     */
    public MinecraftHeadConfiguration(Plugin plugin) {
        super(null, plugin);
    }

    /**
     * Reloads the content from the fileSystem
     */
    @Override
    public void reload() {
        this.items.clear();
        try {
            final Cipher decipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
            decipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(Base64Coder.decode("vcnhus0kpQAIokFsEoT+0g=="), "AES"), new IvParameterSpec("RandomInitVector".getBytes("UTF-8")));
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(new CipherInputStream(JavaPlugin.getPlugin(PetBlocksPlugin.class).getResource("minecraftheads.db"), decipher)))) {
                String s;
                final String splitter = Pattern.quote(",");
                int i = 0;
                while ((s = reader.readLine()) != null) {
                    final String[] tags = s.split(splitter);
                    if (tags.length == 3 && tags[2].length() % 4 == 0) {
                        i++;
                        final String line = Base64Coder.decodeString(tags[2]).replace("{\"textures\":{\"SKIN\":{\"url\":\"", "");
                        final String url = line.substring(0, line.indexOf("\""));
                        GUIItemContainer container = new ItemContainer(true, i, GUIPage.MINECRAFTHEADS_COSTUMES, 397, 3, url, false, tags[1], new String[0]);
                        this.items.add(container);
                    }
                }
            }
        } catch (IOException | InvalidAlgorithmParameterException | InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to read minecraft-heads.com skins.");
        }
    }
}
