package com.github.shynixn.petblocks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.reloadTranslation
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.guice.DependencyInjectionModule
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PluginDependency
import com.github.shynixn.petblocks.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.impl.listener.PetListener
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import java.util.logging.Level

/**
 * Author Shynixn
 */
class PetBlocksPlugin : JavaPlugin() {
    private val prefix: String = ChatColor.BLUE.toString() + "[PetBlocks] " + ChatColor.WHITE
    private lateinit var module : DependencyInjectionModule
    private var immidiateDisable = false
    private val isLoggingEnabled by lazy {
        val configurationService = module.getService<ConfigurationService>()
        val loggingKey = "isLoggingEnabled"
        if (configurationService.containsValue(loggingKey)) {
            configurationService.findValue(loggingKey)
        } else {
            true
        }
    }

    /**
     * Called when this plugin is enabled.
     */
    override fun onEnable() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading PetBlocks ...")
        this.saveDefaultConfig()

        val versions = if (PetBlocksDependencyInjectionModule.areLegacyVersionsIncluded) {
            listOf(
                Version.VERSION_1_8_R3,
                Version.VERSION_1_9_R2,
                Version.VERSION_1_10_R1,
                Version.VERSION_1_11_R1,
                Version.VERSION_1_12_R1,
                Version.VERSION_1_13_R1,
                Version.VERSION_1_13_R2,
                Version.VERSION_1_14_R1,
                Version.VERSION_1_15_R1,
                Version.VERSION_1_16_R1,
                Version.VERSION_1_16_R2,
                Version.VERSION_1_16_R3,
                Version.VERSION_1_17_R1,
                Version.VERSION_1_18_R1,
                Version.VERSION_1_18_R2,
                Version.VERSION_1_19_R1,
                Version.VERSION_1_19_R2,
                Version.VERSION_1_19_R3,
                Version.VERSION_1_20_R1,
                Version.VERSION_1_20_R2,
                Version.VERSION_1_20_R3,
                Version.VERSION_1_20_R4
            )
        } else {
            listOf(Version.VERSION_1_20_R4)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())) {
            immidiateDisable = true
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "PetBlocks does not support your server version")
            logger.log(Level.SEVERE, "Install v" + versions[0].id + " - v" + versions[versions.size - 1].id)
            logger.log(Level.SEVERE, "Need support for a particular version? Go to https://www.patreon.com/Shynixn")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        logger.log(Level.INFO, "Loaded NMS version ${Version.serverVersion.bukkitId}.")

        // Guice
        this.module = PetBlocksDependencyInjectionModule(this).build()
        this.reloadConfig()

        // Register Packets
        val packetService = module.getService<PacketService>()
        packetService.registerPacketListening(PacketInType.USEENTITY)
        packetService.registerPacketListening(PacketInType.STEERENTITY)

        // Register Listeners
        val petListener = module.getService<PetListener>()
        Bukkit.getPluginManager().registerEvents(petListener, this)
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            Bukkit.getPluginManager().registerEvents(module.getService<DependencyHeadDatabaseService>(), this)
        }

        // Register CommandExecutors
        val configurationService = module.getService<ConfigurationService>()
        val petBlocksCommandExecutor = module.getService<PetBlocksCommandExecutor>()
        val mcTennisCommand = this.getCommand("petblocks")!!
        mcTennisCommand.usage = configurationService.findValue("commands.petblocks.usage")
        mcTennisCommand.description = configurationService.findValue("commands.petblocks.description")
        mcTennisCommand.permissionMessage = configurationService.findValue("commands.petblocks.permission-message")
        mcTennisCommand.setSuspendingExecutor(petBlocksCommandExecutor)
        mcTennisCommand.setSuspendingTabCompleter(petBlocksCommandExecutor)

        // Copy Third Party
        copyResourceToTarget("thirdparty/DeluxeMenu/config.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_menu.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_skins_overview_menu.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_skins_blockskins_menu.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_skins_petskins_menu.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_skins_vehicleskins_menu.yml")
        copyResourceToTarget("thirdparty/DeluxeMenu/gui_menus/petblocks_skins_puppetskins_menu.yml")

        // Register Language
        val plugin = this
        runBlocking {
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, PetBlocksLanguage::class.java, "en_us")
            logger.log(Level.INFO, "Loaded language file $language.properties.")
            // Connect
            try {
                val playerDataRepository = module.getService<PlayerDataRepository<PlayerInformation>>()
                playerDataRepository.createIfNotExist()
            } catch (e: Exception) {
                e.printStackTrace()
                immidiateDisable = true
                Bukkit.getPluginManager().disablePlugin(plugin)
                return@runBlocking
            }

            val templateRepository = module.getService<Repository<PetTemplate>>()
            templateRepository.getAll()

            // Register Dependencies
            Bukkit.getServicesManager()
                .register(PetService::class.java, module.getService<PetService>(), plugin, ServicePriority.Normal)

            Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled PetBlocks " + plugin.description.version + " by Shynixn")
        }

        plugin.launch {
            // Fix already online players.
            for (player in Bukkit.getOnlinePlayers()) {
                petListener.onPlayerJoinEvent(PlayerJoinEvent(player, null))
            }
        }
    }

    /**
     * Called when this plugin is disabled.
     */
    override fun onDisable() {
        if (immidiateDisable) {
            return
        }

        val petService = module.getService<PetService>()
        petService.close()

        val playerDataRepository = module.getService<CachePlayerRepository<PlayerInformation>>()
        runBlocking {
            playerDataRepository.saveAll()
            playerDataRepository.clearAll()
            playerDataRepository.close()
        }

        val physicService = module.getService<PhysicObjectService>()
        physicService.close()

        val packetService = module.getService<PacketService>()
        packetService.close()
    }

    private fun copyResourceToTarget(resourcePath: String) {
        val fullTargetFile = File(dataFolder, resourcePath)
        Files.createDirectories(fullTargetFile.parentFile.toPath())
        getResource(resourcePath).use { resourceStream ->
            Files.copy(resourceStream, fullTargetFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        }
    }
}
