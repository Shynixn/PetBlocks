package com.github.shynixn.petblocks.sponge

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager
import com.github.shynixn.petblocks.sponge.logic.business.helper.*
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.sponge.nms.NMSRegistry
import com.github.shynixn.petblocks.sponge.nms.VersionSupport
import com.google.inject.Inject
import com.google.inject.Injector
import org.bstats.sponge.Metrics
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.scheduler.Task
import java.io.IOException

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
@Plugin(id = "petblocks", name = "PetBlocks", version = "7.0.0-SNAPSHOT", description = "PetBlocks is a spigot and sponge plugin to use block as pets in minecraft.")
class PetBlocksPlugin {

    companion object {
        val PREFIX_CONSOLE = ChatColor.AQUA.toString() + "[PetBlocks] "
        private val SPIGOT_RESOURCEID: Long = 12056
        private val PLUGIN_NAME = "PetBlocks"
    }

    private var disabled: Boolean = false

    @Inject
    private lateinit var pluginContainer: PluginContainer

    @Inject
    private lateinit var metrics: Metrics

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var injector : Injector

    @Inject
    private lateinit var guice : GoogleGuiceSubBinder

    @Listener
    @Throws(IOException::class)
    fun onEnable(event: GameInitializationEvent) {
        if (!VersionSupport.isServerVersionSupported(PLUGIN_NAME, PREFIX_CONSOLE)) {
            this.disabled = true
            Sponge.getGame().unloadPlugin(this)
        } else {
            Sponge.getGame().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...")

            injector.createChildInjector(GoogleGuiceBinder())
            Config.reload()
            injector.createChildInjector(guice)

            metrics.addCustomChart(Metrics.SimplePie("storage", {
                if (Config.getData<Boolean>("sql.enabled")!!) {
                    "MySQL"
                }
                "SQLite"
            }))
            Task.builder().async().execute(Runnable {
                try {
                    UpdateUtils.checkPluginUpToDateAndPrintMessage(SPIGOT_RESOURCEID, PREFIX_CONSOLE, PLUGIN_NAME, pluginContainer)
                } catch (e: IOException) {
                    this.logger.warn("Failed to check for updates.")
                }
            }).submit(this.pluginContainer)
            try {
                ReflectionUtils.invokeMethodByClass<PetBlocksApi>(PetBlocksApi::class.java, "initialize", arrayOf<Class<*>>(PetMetaController::class.java, PetBlockController::class.java), arrayOf<Any>(guice.petBlocksManager!!.petMetaController, this.guice.petBlocksManager!!.petBlockController))
                Sponge.getGame().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + pluginContainer.version.get() + " by Shynixn")
            } catch (e: Exception) {
                logger.error("Failed to enable plugin.", e)
            }
        }
    }

    @Listener
    fun onReload(event: GameReloadEvent) {
        if (!this.disabled) {
            Config.reload()
            Sponge.getGame().sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Reloaded PetBlocks configuration.")
        }
    }//

    @Listener
    fun onDisable(event: GameStoppingServerEvent) {
        if (!this.disabled) {
            try {
                NMSRegistry.unregisterCustomEntities()
            } catch (e: Exception) {
                logger.warn("Failed to disable petblocks.", e)
            }
        }
    }
}