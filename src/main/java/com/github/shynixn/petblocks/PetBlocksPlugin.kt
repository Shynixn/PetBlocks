package com.github.shynixn.petblocks

import com.github.shynixn.mccoroutine.bukkit.*
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.reloadTranslation
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.impl.listener.PetListener
import com.google.inject.Guice
import com.google.inject.Injector
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

class PetBlocksPlugin : JavaPlugin() {
    private val prefix: String = ChatColor.BLUE.toString() + "[PetBlocks] " + ChatColor.WHITE
    private var injector: Injector? = null

    /**
     * Called when this plugin is enabled.
     */
    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading PetBlocks ...")
        this.saveDefaultConfig()

        val versions = if (PetBlocksDependencyInjectionBinder.areLegacyVersionsIncluded) {
            listOf(
                Version.VERSION_1_17_R1,
                Version.VERSION_1_18_R1,
                Version.VERSION_1_18_R2,
                Version.VERSION_1_19_R1,
                Version.VERSION_1_19_R2,
                Version.VERSION_1_19_R3,
                Version.VERSION_1_20_R1,
                Version.VERSION_1_20_R2
            )
        } else {
            listOf(Version.VERSION_1_20_R2)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())
        ) {
            Bukkit.getServer().consoleSender.sendMessage(org.bukkit.ChatColor.RED.toString() + "================================================")
            Bukkit.getServer().consoleSender.sendMessage(org.bukkit.ChatColor.RED.toString() + "PetBlocks does not support your server version")
            Bukkit.getServer().consoleSender.sendMessage(org.bukkit.ChatColor.RED.toString() + "Install v" + versions[0].id + " - v" + versions[versions.size - 1].id)
            Bukkit.getServer().consoleSender.sendMessage(org.bukkit.ChatColor.RED.toString() + "Plugin gets now disabled!")
            Bukkit.getServer().consoleSender.sendMessage(org.bukkit.ChatColor.RED.toString() + "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // Guice
        this.injector = Guice.createInjector(PetBlocksDependencyInjectionBinder(this))
        this.reloadConfig()

        // Register Packets
        val packetService = resolve(PacketService::class.java)
        packetService.registerPacketListening(PacketInType.USEENTITY)
        packetService.registerPacketListening(PacketInType.STEERENTITY)

        // Register Listeners
        val petListener = resolve(PetListener::class.java)
        Bukkit.getPluginManager().registerSuspendingEvents(petListener, this)

        // Register CommandExecutors
        val configurationService = resolve(ConfigurationService::class.java)
        val petBlocksCommandExecutor = resolve(PetBlocksCommandExecutor::class.java)
        val mcTennisCommand = this.getCommand("petblocks")!!
        mcTennisCommand.usage = configurationService.findValue("commands.petblocks.usage")
        mcTennisCommand.description = configurationService.findValue("commands.petblocks.description")
        mcTennisCommand.permissionMessage = configurationService.findValue("commands.petblocks.permission-message")
        mcTennisCommand.setSuspendingExecutor(petBlocksCommandExecutor)
        mcTennisCommand.setSuspendingTabCompleter(petBlocksCommandExecutor)

        // Register Language
        val plugin = this
        this.launch {
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, PetBlocksLanguage::class.java, "en_us")
            logger.log(Level.INFO, "Loaded language file $language.properties.")

            // Connect
            val sqlConnectionService = resolve(SqlConnectionService::class.java)
            sqlConnectionService.connect()
            val playerDataRepository = resolve(PlayerDataRepository::class.java)
            playerDataRepository.createIfNotExist()
            val templateRepository = resolve(Repository::class.java)
            templateRepository.getAll()

            // Fix already online players.
            for (player in Bukkit.getOnlinePlayers()) {
                petListener.onPlayerJoinEvent(PlayerJoinEvent(player, null))
            }

            // Register Dependencies
            Bukkit.getServicesManager()
                .register(PetService::class.java, resolve(PetService::class.java), plugin, ServicePriority.Normal)

            Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled PetBlocks " + plugin.description.version + " by Shynixn")
        }
    }

    /**
     * Called when this plugin is disabled.
     */
    override fun onDisable() {
        val petService = resolve(PetService::class.java)
        petService.close()

        val playerDataRepository = resolve(CachePlayerRepository::class.java)
        runBlocking {
            playerDataRepository.saveCache()
        }
        playerDataRepository.clearCache()

        val sqlConnectionService = resolve(SqlConnectionService::class.java)
        sqlConnectionService.close()

        val physicService = resolve(PhysicObjectService::class.java)
        physicService.close()

        val packetService = resolve(PacketService::class.java)
        packetService.close()
    }

    private fun <S> resolve(service: Class<S>): S {
        try {
            return this.injector!!.getBinding(service).provider.get() as S
        } catch (e: Exception) {
            throw IllegalArgumentException("Service ${service.name} could not be resolved.", e)
        }
    }
}
