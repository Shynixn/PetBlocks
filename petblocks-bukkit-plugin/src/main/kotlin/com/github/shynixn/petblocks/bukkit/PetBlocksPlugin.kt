@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.bukkit.logic.business.extension.getServerVersion
import com.github.shynixn.petblocks.bukkit.logic.business.extension.yamlMap
import com.github.shynixn.petblocks.bukkit.logic.business.listener.*
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.EditPetCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.PlayerPetActionCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.io.IOUtils
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.util.logging.Level

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
class PetBlocksPlugin : JavaPlugin(), PluginProxy {
    companion object {
        /** Final Prefix of PetBlocks in the console */
        val PREFIX_CONSOLE: String = ChatColor.AQUA.toString() + "[PetBlocks] "
    }

    private val configVersion = 1
    private var injector: Injector? = null
    private var immediateDisable: Boolean = false

    /**
     * Enables the plugin PetBlocks.
     */
    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...")
        this.saveDefaultConfig()

        if (!getServerVersion().isCompatible(
                Version.VERSION_1_8_R1,
                Version.VERSION_1_8_R2,
                Version.VERSION_1_8_R3,
                Version.VERSION_1_9_R1,
                Version.VERSION_1_9_R2,
                Version.VERSION_1_10_R1,
                Version.VERSION_1_11_R1,
                Version.VERSION_1_12_R1,
                Version.VERSION_1_13_R1,
                Version.VERSION_1_13_R2
            )
        ) {
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "PetBlocks does not support your server version")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Install v" + Version.VERSION_1_8_R1.id + " - v" + Version.VERSION_1_13_R2.id)
            Bukkit.getServer().consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Plugin gets now disabled!")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")

            immediateDisable = true
            Bukkit.getPluginManager().disablePlugin(this)

            return
        }

        this.injector = Guice.createInjector(PetBlocksDependencyInjectionBinder(this))

        if (!this.config.contains("config-version") || this.config.getInt("config-version") != configVersion) {
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "PetBlocks config.yml config-version does not match")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "with your installed PetBlocks.jar.")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Carefully read the patch notes to get the correct config-version.")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "https://github.com/Shynixn/PetBlocks/releases")
            Bukkit.getServer().consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Plugin gets now disabled!")
            Bukkit.getServer()
                .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")

            immediateDisable = true
            Bukkit.getPluginManager().disablePlugin(this)

            return
        }

        this.reloadConfig()

        val dependencyService = resolve<DependencyService>(DependencyService::class.java)
        val updateCheckService = resolve<UpdateCheckService>(UpdateCheckService::class.java)
        val commandService = resolve<CommandService>(CommandService::class.java)
        val entityService = resolve<EntityService>(EntityService::class.java)

        dependencyService.checkForInstalledDependencies()
        updateCheckService.checkForUpdates()

        // Register Listener
        Bukkit.getPluginManager().registerEvents(resolve(CarryPetCommonListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(DamagePetListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(FeedingPetListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(InventoryListener::class.java), this)
        Bukkit.getPluginManager().registerEvents(resolve(PetListener::class.java), this)

        if (getServerVersion().isVersionSameOrGreaterThan(Version.VERSION_1_9_R2)) {
            Bukkit.getPluginManager().registerEvents(resolve(CarryPet19R2Listener::class.java), this)
        }

        if (dependencyService.isInstalled(PluginDependency.HEADDATABASE)) {
            Bukkit.getPluginManager().registerEvents(resolve(DependencyHeadDatabaseListener::class.java), this)
        }

        // Register CommandExecutor
        commandService.registerCommandExecutor(
            this.config.get("commands.petblock")!!.yamlMap(),
            this.resolve(PlayerPetActionCommandExecutorImpl::class.java)
        )
        commandService.registerCommandExecutor("petblocks", this.resolve(EditPetCommandExecutorImpl::class.java))
        commandService.registerCommandExecutor("petblockreload", this.resolve(ReloadCommandExecutorImpl::class.java))

        if (config.getBoolean("metrics")) {
            val metrics = Metrics(this)

            metrics.addCustomChart(Metrics.SimplePie("storage") {
                if (config.getBoolean("sql.enabled")) {
                    "MySQL"
                } else {
                    "SQLite"
                }
            })
        }

        for (world in Bukkit.getWorlds()) {
            entityService.cleanUpInvalidEntities(world.entities)
        }

        startPlugin()
        Bukkit.getServer()
            .consoleSender.sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + this.description.version + " by Shynixn")
    }

    /**
     * OnDisable.
     */
    override fun onDisable() {
        if (immediateDisable) {
            return
        }

        resolve<EntityRegistrationService>(EntityRegistrationService::class.java).clearResources()
        resolve<PersistencePetMetaService>(PersistencePetMetaService::class.java).close()

        for (world in Bukkit.getWorlds()) {
            for (player in world.players) {
                resolve<PetDebugService>(PetDebugService::class.java).unRegister(player)
                resolve<DependencyHeadDatabaseService>(DependencyHeadDatabaseService::class.java).clearResources(player)
                resolve<GUIService>(GUIService::class.java).cleanResources(player)
                resolve<ProxyService>(ProxyService::class.java).cleanResources(player)
                resolve<CarryPetService>(CarryPetService::class.java).clearResources(player)

                val petService = resolve<PetService>(PetService::class.java)

                if (petService.hasPet(player)) {
                    val pet = petService.getOrSpawnPetFromPlayer(player).get()

                    pet.getHitBoxLivingEntity<EntityPetProxy>().ifPresent { p -> p.deleteFromWorld() }
                    pet.getHeadArmorstand<EntityPetProxy>().deleteFromWorld()

                    pet.remove()
                }
            }
        }

        resolve<SqlDbContext>(SqlDbContext::class.java).close()
    }

    /**
     * Starts the plugin.
     */
    private fun startPlugin() {
        try {
            val method = PetBlocksApi::class.java.getDeclaredMethod("initializePetBlocks", PluginProxy::class.java)
            method.isAccessible = true
            method.invoke(PetBlocksApi, this)

            for (world in Bukkit.getWorlds()) {
                for (player in world.players) {
                    resolve<PetListener>(PetListener::class.java).onPlayerJoinEvent(
                        PlayerJoinEvent(
                            player,
                            "PetBlocksRunTime"
                        )
                    )
                }
            }

            logger.log(Level.INFO, "Using NMS Connector " + getServerVersion().bukkitId + ".")
        } catch (e: Exception) {
            logger.log(Level.WARNING, "Failed to enable PetBlocks.", e)
        }
    }

    /**
     * Loads the default config and saves it to the plugin folder.
     */
    override fun saveDefaultConfig() {
        this.getResource("assets/petblocks/config.yml").use { inputStream ->
            if (!this.dataFolder.exists()) {
                this.dataFolder.mkdir()
            }

            val configFile = File(this.dataFolder, "config.yml")
            if (configFile.exists()) {
                return
            }

            FileOutputStream(configFile).use { outStream ->
                IOUtils.copy(inputStream, outStream)
            }
        }
    }

    /**
     * Gets a business logic from the PetBlocks plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     * @param S the type of service class.
     */
    override fun <S> resolve(service: Any): S {
        if (service !is Class<*>) {
            throw IllegalArgumentException("Service has to be a Class!")
        }

        try {
            return this.injector!!.getBinding(service).provider.get() as S
        } catch (e: Exception) {
            throw IllegalArgumentException("Service could not be resolved.", e)
        }
    }

    /**
     * Creates a new entity from the given [entity].
     * Throws a [IllegalArgumentException] if the entity could not be found.
     * @param E the type of entity class.
     */
    override fun <E> create(entity: Any): E {
        if (entity !is Class<*>) {
            throw IllegalArgumentException("Entity has to be a Class!")
        }

        try {
            val entityName = entity.simpleName + "Entity"
            return Class.forName("com.github.shynixn.petblocks.core.logic.persistence.entity.$entityName").newInstance() as E
        } catch (e: Exception) {
            throw IllegalArgumentException("Entity could not be created.", e)
        }
    }
}