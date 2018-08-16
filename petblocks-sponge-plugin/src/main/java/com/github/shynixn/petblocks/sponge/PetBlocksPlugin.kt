package com.github.shynixn.petblocks.sponge

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.business.entity.PetBlocksPlugin
import com.github.shynixn.petblocks.api.business.service.DependencyService
import com.github.shynixn.petblocks.api.business.service.EntityService
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController
import com.github.shynixn.petblocks.core.logic.business.helper.ChatColor
import com.github.shynixn.petblocks.core.logic.business.helper.ReflectionUtils
import com.github.shynixn.petblocks.sponge.logic.business.helper.*
import com.github.shynixn.petblocks.sponge.logic.business.listener.InventoryListener
import com.github.shynixn.petblocks.sponge.logic.business.service.DependencyServiceImpl
import com.github.shynixn.petblocks.sponge.logic.business.service.MessageServiceImpl
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
import java.io.IOException

/**
 * Main Sponge Plugin.
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
@Plugin(id = "petblocks", name = "PetBlocks", version = "7.2.1-SNAPSHOT", description = "PetBlocks is a spigot and also a sponge plugin to use blocks and custom heads as pets in Minecraft.")
class PetBlocksPlugin : com.github.shynixn.petblocks.api.business.entity.PetBlocksPlugin {

    companion object {
        /**
         * Common PetBlocks prefix.
         */
        val PREFIX_CONSOLE = ChatColor.AQUA.toString() + "[PetBlocks] "

        private const val SPIGOT_RESOURCEID: Long = 12056
        private const val PLUGIN_NAME = "PetBlocks"
    }

    private var disabled: Boolean = false

    private var childInjector: Injector? = null

    @Inject
    private lateinit var pluginContainer: PluginContainer

    @Inject
    private lateinit var metrics: Metrics

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var injector: Injector

    @Inject
    private lateinit var guice: PetBlocksDependencyInjectionBinder

    @Listener
    @Throws(IOException::class)
    fun onEnable(event: GameInitializationEvent) {
        if (!VersionSupport.isServerVersionSupported(PLUGIN_NAME, PREFIX_CONSOLE)) {
            this.disabled = true
            Sponge.getGame().unloadPlugin(this)
            return
        }

        Sponge.getGame().sendConsoleMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...")

        injector.createChildInjector(GoogleGuiceBinder())
        Config.reload()
        childInjector = injector.createChildInjector(guice)

        metrics.addCustomChart(Metrics.SimplePie("storage", {
            if (Config.getData<Boolean>("sql.enabled")!!) {
                "MySQL"
            }
            "SQLite"
        }))

        async(pluginContainer) {
            try {
                UpdateUtils.checkPluginUpToDateAndPrintMessage(SPIGOT_RESOURCEID, PREFIX_CONSOLE, PLUGIN_NAME, pluginContainer)
            } catch (e: IOException) {
                this.logger.warn("Failed to check for updates.")
            }
        }

        val dependencyService = resolve(DependencyService::class.java)
        dependencyService.checkForInstalledDependencies()

        // Register Listeners
        Sponge.getEventManager().registerListeners(pluginContainer, resolve(InventoryListener::class.java))

        val entityService = resolve(EntityService::class.java)

        entityService.registerEntitiesOnServer()

        try {
            ReflectionUtils.invokeMethodByClass<PetBlocksApi>(PetBlocksApi::class.java, "initialize"
                    , arrayOf<Class<*>>(PetMetaController::class.java, PetBlockController::class.java, PetBlocksPlugin::class.java)
                    , arrayOf<Any?>(guice.petBlocksManager!!.petMetaController, this.guice.petBlocksManager!!.petBlockController, this))
            Sponge.getGame().sendConsoleMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + pluginContainer.version.get() + " by Shynixn")
        } catch (e: Exception) {
            logger.error("Failed to enable plugin.", e)
        }
    }

    /**
     * Gets called on [event] [GameReloadEvent] and reloads resources.
     */
    @Listener
    fun onReload(event: GameReloadEvent) {
        if (disabled) {
            return
        }

        Config.reload()
        Sponge.getGame().sendConsoleMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Reloaded PetBlocks configuration.")
    }

    /**
     * Gets called on [event] [GameStoppingServerEvent] and closes remaining resources.
     */
    @Listener
    fun onDisable(event: GameStoppingServerEvent) {
        if (disabled) {
            return
        }

        try {
            NMSRegistry.unregisterCustomEntities()
        } catch (e: Exception) {
            logger.warn("Failed to disable petblocks.", e)
        }
    }

    /**
     * Gets a business logic from the PetBlocks plugin.
     * All types in the service package can be accessed.
     */
    override fun <S> resolve(service: Class<S>): S {
        return try {
            this.childInjector!!.getBinding(service).provider.get()
        } catch (e: Exception) {
            throw IllegalArgumentException("Service could not be resolved.", e)
        }
    }

    /**
     * Creates a new entity from the given class.
     * Throws a IllegalArgumentException if not found.
     *
     * @param entity entityClazz
     * @param <E>    type
     * @return entity.
    </E> */
    override fun <E> create(entity: Class<E>): E {
        try {
            val entityName = entity.simpleName + "Entity"
            return Class.forName("com.github.shynixn.petblocks.core.logic.persistence.entity.$entityName").newInstance() as E
        } catch (e: Exception) {
            throw IllegalArgumentException("Entity could not be created.", e)
        }
    }
}