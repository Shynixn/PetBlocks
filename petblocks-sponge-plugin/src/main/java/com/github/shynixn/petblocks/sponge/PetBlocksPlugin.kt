@file:Suppress("UNCHECKED_CAST", "unused", "UNUSED_PARAMETER")

package com.github.shynixn.petblocks.sponge

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.EditPetCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.PlayerPetActionCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import com.github.shynixn.petblocks.sponge.logic.business.extension.getServerVersion
import com.github.shynixn.petblocks.sponge.logic.business.extension.sendMessage
import com.github.shynixn.petblocks.sponge.logic.business.extension.toText
import com.github.shynixn.petblocks.sponge.logic.business.listener.*
import com.google.inject.Inject
import com.google.inject.Injector
import org.bstats.sponge.Metrics2
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.Cause
import org.spongepowered.api.event.game.GameReloadEvent
import org.spongepowered.api.event.game.state.GameInitializationEvent
import org.spongepowered.api.event.game.state.GameStoppingServerEvent
import org.spongepowered.api.event.message.MessageEvent
import org.spongepowered.api.event.network.ClientConnectionEvent
import org.spongepowered.api.plugin.Plugin
import org.spongepowered.api.plugin.PluginContainer
import org.spongepowered.api.text.Text
import org.spongepowered.api.text.channel.MessageChannel
import java.util.*

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
@Plugin(id = "petblocks",
    name = "PetBlocks",
    description = "PetBlocks is a spigot and also a sponge plugin to use blocks and custom heads as pets in Minecraft.")
class PetBlocksPlugin : PluginProxy {
    companion object {
        /** Final Prefix of PetBlocks in the console */
        val PREFIX_CONSOLE: String = ChatColor.AQUA.toString() + "[PetBlocks] "
    }

    private val configVersion = 1
    private var injector: Injector? = null
    private var immediateDisable: Boolean = false

    @Inject
    private lateinit var plugin: PluginContainer

    @Inject
    private lateinit var metrics: Metrics2

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var spongeInjector: Injector

    private lateinit var configurationService: ConfigurationService

    /**
     * Enables the plugin PetBlocks.
     */
    @Listener
    fun onEnable(event: GameInitializationEvent) {
        Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Loading PetBlocks ...")
        this.injector = spongeInjector.createChildInjector(PetBlocksDependencyInjectionBinder(plugin))

        if (!getServerVersion().isCompatible(
                Version.VERSION_1_12_R1)
        ) {
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "PetBlocks does not support your server version")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Install v" + Version.VERSION_1_12_R1.id + " - v" + Version.VERSION_1_12_R1.id)
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Plugin gets now disabled!")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")

            immediateDisable = true
            disablePlugin()

            return
        }

        configurationService = resolve(ConfigurationService::class.java)

        if (!configurationService.contains("config-version") || configurationService.findValue<Int>("config-version") != configVersion) {
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "PetBlocks config.yml config-version does not match")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "with your installed PetBlocks.jar.")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Carefully read the patch notes to get the correct config-version.")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "https://github.com/Shynixn/PetBlocks/releases")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "Plugin gets now disabled!")
            Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.RED + "================================================")

            immediateDisable = true
            disablePlugin()

            return
        }

        val dependencyService = resolve<DependencyService>(DependencyService::class.java)
        val updateCheckService = resolve<UpdateCheckService>(UpdateCheckService::class.java)
        val commandService = resolve<CommandService>(CommandService::class.java)
        val entityService = resolve<EntityService>(EntityService::class.java)

        dependencyService.checkForInstalledDependencies()
        updateCheckService.checkForUpdates()

        // GrievPrevention requires eager entity registration.
        entityService.registerEntitiesOnServer()

        // Register Listener
        Sponge.getEventManager().registerListeners(plugin, resolve(CarryPetListener::class.java))
        Sponge.getEventManager().registerListeners(plugin, resolve(DamagePetListener::class.java))
        Sponge.getEventManager().registerListeners(plugin, resolve(FeedingPetListener::class.java))
        Sponge.getEventManager().registerListeners(plugin, resolve(InventoryListener::class.java))
        Sponge.getEventManager().registerListeners(plugin, resolve(PetListener::class.java))

        // Register CommandExecutor
        commandService.registerCommandExecutor(configurationService.findValue<Map<String, Any>>("commands.petblock"), this.resolve(PlayerPetActionCommandExecutorImpl::class.java))
        commandService.registerCommandExecutor("petblocks", this.resolve(EditPetCommandExecutorImpl::class.java))
        commandService.registerCommandExecutor("petblockreload", this.resolve(ReloadCommandExecutorImpl::class.java))

        for (world in Sponge.getGame().server.worlds) {
            entityService.cleanUpInvalidEntities(world.entities)
        }

        startPlugin()
        Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Enabled PetBlocks " + plugin.version.get() + " by Shynixn")
    }

    /**
     * Gets called on [event] [GameReloadEvent] and reloads resources.
     */
    @Listener
    fun onReload(event: GameReloadEvent) {
        if (immediateDisable) {
            return
        }

        configurationService.refresh()
        Sponge.getServer().console.sendMessage(PREFIX_CONSOLE + ChatColor.GREEN + "Reloaded PetBlocks configuration.")
    }

    /**
     * OnDisable.
     */
    @Listener
    fun onDisable(event: GameStoppingServerEvent) {
        if (immediateDisable) {
            return
        }

        resolve<EntityRegistrationService>(EntityRegistrationService::class.java).clearResources()
        resolve<PersistencePetMetaService>(PersistencePetMetaService::class.java).close()

        for (world in Sponge.getGame().server.worlds) {
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

            for (world in Sponge.getGame().server.worlds) {
                for (player in world.players) {
                    resolve<PetListener>(PetListener::class.java).onPlayerJoinEvent(object : ClientConnectionEvent.Join {
                        override fun getOriginalChannel(): MessageChannel? {
                            return null
                        }

                        override fun getMessage(): Text {
                            return "PetBlocksRunTime".toText()
                        }

                        override fun setMessageCancelled(cancelled: Boolean) {
                        }

                        override fun getOriginalMessage(): Text? {
                            return null
                        }

                        override fun isMessageCancelled(): Boolean {
                            return false
                        }

                        override fun getTargetEntity(): Player {
                            return player
                        }

                        override fun setChannel(channel: MessageChannel?) {
                        }

                        override fun getChannel(): Optional<MessageChannel> {
                            return Optional.empty()
                        }

                        override fun getFormatter(): MessageEvent.MessageFormatter? {
                            return null
                        }

                        override fun getCause(): Cause? {
                            return null
                        }
                    })
                }
            }

            logger.info("Using NMS Connector " + getServerVersion().bukkitId + ".")
        } catch (e: Exception) {
            logger.warn("Failed to enable PetBlocks.", e)
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

    /**
     * Disables the plugin.
     */
    private fun disablePlugin() {
        Sponge.getGame().eventManager.unregisterPluginListeners(this)
        Sponge.getGame().commandManager.getOwnedBy(this).forEach { Sponge.getGame().commandManager.removeMapping(it) }
        Sponge.getGame().scheduler.getScheduledTasks(this).forEach { it.cancel() }
    }
}