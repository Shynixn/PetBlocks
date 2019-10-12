@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.sponge.logic.business.service

import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
import com.google.inject.Inject
import ninja.leaping.configurate.ConfigurationNode
import ninja.leaping.configurate.yaml.YAMLConfigurationLoader
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.plugin.PluginContainer
import java.io.IOException
import java.io.InputStream
import java.nio.file.Files
import java.nio.file.Path

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
class ConfigurationServiceImpl @Inject constructor(
    @ConfigDir(sharedRoot = false) private val privateConfigDir: Path,
    private val loggingService: LoggingService,
    private val pluginContainer: PluginContainer
) : ConfigurationService {
    private var configurationNode: ConfigurationNode? = null

    /**
     * Init.
     */
    init {
        reload()
    }

    /**
     * Gets the path to the folder where the application is allowed to store
     * save data.
     */
    override val applicationDir: Path
        get() = privateConfigDir

    /**
     * Reloads the config.
     */
    override fun reload() {
        if (!this.privateConfigDir.toFile().exists()) {
            this.privateConfigDir.toFile().mkdir()
        }

        val configFile = privateConfigDir.resolve("config.yml")

        if (!Files.exists(configFile)) {
            try {
                pluginContainer.getAsset("config.yml").get().copyToFile(configFile)
            } catch (e: IOException) {
                loggingService.warn("Failed to create config.yml.", e)
            }
        }

        val loader = YAMLConfigurationLoader.builder().setPath(configFile).build()
        this.configurationNode = loader.load()
    }

    /**
     * Opens an inputStream to the given resource name.
     */
    override fun openResource(name: String): InputStream {
        val rootAsset = this.pluginContainer.getAsset(name)

        if (rootAsset.isPresent) {
            return rootAsset.get().url.openStream()
        }

        val assetName = name.replace("assets/petblocks/", "")
        return this.pluginContainer.getAsset(assetName).get().url.openStream()
    }

    /**
     * Checks if the given [path] contains a value.
     */
    override fun containsValue(path: String): Boolean {
        try {
            val items = path.split(".").toTypedArray()
            val targetNode = this.configurationNode!!.getNode(*items as Array<Any>)
            targetNode.value ?: return false
            return true
        } catch (e: Exception) {
            return false
        }
    }

    /**
     * Tries to load the config value from the given [path].
     * Throws a [IllegalArgumentException] if the path could not be correctly
     * loaded.
     */
    override fun <C> findValue(path: String): C {
        require(containsValue(path)) { "Path '$path' could not be found!" }

        val items = path.split(".").toTypedArray()
        val data = this.configurationNode!!.getNode(*items as Array<Any>).value

        if (data is String) {
            return data.translateChatColors() as C
        }

        return data as C
    }
}