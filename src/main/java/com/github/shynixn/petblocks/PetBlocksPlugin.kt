package com.github.shynixn.petblocks

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.impl.service.ChatMessageServiceImpl
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.Language
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PluginDependency
import com.github.shynixn.petblocks.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.impl.listener.PetListener
import com.github.shynixn.petblocks.impl.provider.PetBlocksPlaceHolderApiProvider
import com.github.shynixn.petblocks.impl.provider.PetBlocksPlaceHolderProvider
import com.github.shynixn.shygui.ShyGUIDependencyInjectionModule
import com.github.shynixn.shygui.contract.GUIMenuService
import com.github.shynixn.shygui.entity.Settings
import com.github.shynixn.shygui.impl.commandexecutor.ShyGUICommandExecutor
import com.github.shynixn.shygui.impl.listener.GUIMenuListener
import com.github.shynixn.shygui.impl.provider.ShyGUIPlaceHolderProvider
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level

/**
 * Author Shynixn
 */
class PetBlocksPlugin : JavaPlugin() {
    companion object {
        var languageFiles = arrayOf("en_us", "es_es")
    }
    private val prefix: String = ChatColor.BLUE.toString() + "[PetBlocks] " + ChatColor.WHITE
    private lateinit var mainModule: PetBlocksDependencyInjectionModule
    private lateinit var shyGuiModule: ShyGUIDependencyInjectionModule
    private var immediateDisable = false

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
                Version.VERSION_1_20_R4,
                Version.VERSION_1_21_R1,
                Version.VERSION_1_21_R2,
            )
        } else {
            listOf(Version.VERSION_1_21_R2)
        }

        if (!Version.serverVersion.isCompatible(*versions.toTypedArray())) {
            immediateDisable = true
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "PetBlocks does not support your server version")
            logger.log(Level.SEVERE, "Install v" + versions[0].from + " - v" + versions[versions.size - 1].to)
            logger.log(Level.SEVERE, "Need support for a particular version? Go to https://www.patreon.com/Shynixn")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        logger.log(Level.INFO, "Loaded NMS version ${Version.serverVersion}.")

        // Load Language
        val language = PetBlocksLanguageImpl()
        language.chatMessageService = ChatMessageServiceImpl(this)
        language.placeHolderFun =
            { text, player ->
                val placeHolderService = mainModule.getService<PlaceHolderService>()
                if (player != null) {
                    placeHolderService.resolvePlaceHolder(player, text, hashMapOf())
                } else {
                    text
                }
            }
        reloadTranslation(language, PetBlocksLanguageImpl::class.java)
        logger.log(Level.INFO, "Loaded language file.")

        // ShyGUI Module
        initializeShyGUIModule(language)

        // Guice
        this.mainModule = PetBlocksDependencyInjectionModule(this, shyGuiModule, language).build()
        this.reloadConfig()

        // Register Packets
        val packetService = mainModule.getService<PacketService>()
        packetService.registerPacketListening(PacketInType.USEENTITY)
        packetService.registerPacketListening(PacketInType.STEERENTITY)

        // Register Listeners
        val petListener = mainModule.getService<PetListener>()
        Bukkit.getPluginManager().registerEvents(petListener, this)
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            val headDatabaseService = mainModule.getService<DependencyHeadDatabaseService>()
            Bukkit.getPluginManager().registerEvents(headDatabaseService, this)
            Bukkit.getServicesManager()
                .register(DependencyHeadDatabaseService::class.java, headDatabaseService, this, ServicePriority.Normal)
        }

        // Register CommandExecutor
        mainModule.getService<PetBlocksCommandExecutor>()

        // Register PlaceHolders
        val placeholderService = shyGuiModule.getService<PlaceHolderService>()
        placeholderService.registerProvider(ShyGUIPlaceHolderProvider(shyGuiModule.getService<Plugin>()))
        placeholderService.registerProvider(
            PetBlocksPlaceHolderProvider(
                mainModule.getService<CachePlayerRepository<PlayerInformation>>(),
                mainModule.getService<PetService>()
            )
        )
        placeholderService.registerPlaceHolderApiProvider {
            PetBlocksPlaceHolderApiProvider(
                mainModule.getService<Plugin>(),
                shyGuiModule.getService<PlaceHolderService>(),
            )
        }

        // Register Dependencies
        Bukkit.getServicesManager()
            .register(PetService::class.java, mainModule.getService<PetService>(), this, ServicePriority.Normal)

        val plugin = this
        plugin.launch {
            try {
                val playerDataRepository = mainModule.getService<PlayerDataRepository<PlayerInformation>>()
                playerDataRepository.createIfNotExist()
            } catch (e: Exception) {
                e.printStackTrace()
                immediateDisable = true
                Bukkit.getPluginManager().disablePlugin(plugin)
                return@launch
            }

            val templateRepository = mainModule.getService<Repository<PetTemplate>>()
            templateRepository.getAll()

            // Fix already online players.
            for (player in Bukkit.getOnlinePlayers()) {
                petListener.onPlayerJoinEvent(PlayerJoinEvent(player, null))
            }

            Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled PetBlocks " + plugin.description.version + " by Shynixn")
        }
    }

    /**
     * Called when this plugin is disabled.
     */
    override fun onDisable() {
        if (immediateDisable) {
            return
        }

        val petService = mainModule.getService<PetService>()
        petService.close()

        val playerDataRepository = mainModule.getService<CachePlayerRepository<PlayerInformation>>()
        runBlocking {
            playerDataRepository.saveAll()
            playerDataRepository.clearAll()
            playerDataRepository.close()
        }

        val physicService = mainModule.getService<PhysicObjectService>()
        physicService.close()
        var packetService = mainModule.getService<PacketService>()
        packetService.close()

        // ShyGUI Module
        val menuService = shyGuiModule.getService<GUIMenuService>()
        menuService.close()
        packetService = shyGuiModule.getService<PacketService>()
        packetService.close()
    }

    private fun initializeShyGUIModule(language : Language) {
        // Settings
        val settings = object : Settings() {
            override fun reload() {
                this.embedded = "GUI"
                this.guis = listOf(
                    "gui/petblocks_main_menu.yml" to "petblocks_main_menu.yml",
                    "gui/petblocks_skins_menu.yml" to "petblocks_skins_menu.yml",
                    "gui/petblocks_skins_blockskins_menu.yml" to "petblocks_skins_blockskins_menu.yml",
                    "gui/petblocks_skins_petskins_menu.yml" to "petblocks_skins_petskins_menu.yml",
                    "gui/petblocks_skins_plushieskins_menu.yml" to "petblocks_skins_plushieskins_menu.yml",
                    "gui/petblocks_skins_vehicleskins_menu.yml" to "petblocks_skins_vehicleskins_menu.yml",
                    "gui/simple_sample_menu.yml" to "simple_sample_menu.yml"
                )
                this.baseCommand = "petblocksgui"
                this.commandPermission = Permission.COMMAND.text
                this.otherPlayerPermission = Permission.MANIPULATE_OTHER.text
                this.guiPermission = Permission.DYN_GUI.text
                this.aliasesPath = "commands.petblocksgui.aliases"
                this.commandUsage = language.commandUsage.text
                this.commandDescription = language.commandDescription.text

                this.playerNotFoundMessage = language.playerNotFoundMessage.text
                this.commandSenderHasToBePlayerMessage = language.commandSenderHasToBePlayer.text
                this.manipulateOtherMessage = language.manipulateOtherMessage.text
                this.reloadMessage = language.reloadMessage.text
                this.noPermissionMessage = language.noPermissionCommand.text
                this.guiNotFoundMessage = language.guiMenuNotFoundMessage.text
                this.guiMenuNoPermissionMessage = language.guiMenuNoPermissionMessage.text

                this.reloadCommandHint = language.reloadCommandHint.text
                this.openCommandHint = language.openCommandHint.text
                this.nextCommandHint = language.nextCommandHint.text
                this.closeCommandHint = language.closeCommandHint.text
                this.backCommandHint = language.backCommandHint.text
                this.messageCommandHint = language.messageCommandHint.text
            }
        };
        settings.reload()
        this.shyGuiModule = ShyGUIDependencyInjectionModule(settings, this).build()

        // Register Packets
        val packetService = shyGuiModule.getService<PacketService>()
        packetService.registerPacketListening(PacketInType.CLICKINVENTORY)
        packetService.registerPacketListening(PacketInType.CLOSEINVENTORY)

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(shyGuiModule.getService<GUIMenuListener>(), this)

        // Register CommandExecutor
        val commandExecutor = shyGuiModule.getService<ShyGUICommandExecutor>()
        commandExecutor.registerShyGuiCommand()

        // Register Dependencies
        Bukkit.getServicesManager().register(
            GUIMenuService::class.java, shyGuiModule.getService<GUIMenuService>(), this, ServicePriority.Normal
        )

        val plugin = this
        runBlocking { // Needs to be runBlocking otherwise command is not registered.
            plugin.logger.log(Level.INFO, "Registering GUI commands...")
            commandExecutor.registerGuiCommands()
            plugin.logger.log(Level.INFO, "Registered GUI commands.")
        }
    }
}
