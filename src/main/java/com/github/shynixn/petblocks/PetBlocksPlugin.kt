package com.github.shynixn.petblocks

import com.github.shynixn.mccoroutine.folia.entityDispatcher
import com.github.shynixn.mccoroutine.folia.globalRegionDispatcher
import com.github.shynixn.mccoroutine.folia.isFoliaLoaded
import com.github.shynixn.mccoroutine.folia.launch
import com.github.shynixn.mccoroutine.folia.mcCoroutineConfiguration
import com.github.shynixn.mccoroutine.folia.regionDispatcher
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.CoroutinePlugin
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.checkIfFoliaIsLoadable
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderServiceImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.packet.api.PacketInType
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PlaceHolder
import com.github.shynixn.petblocks.enumeration.PluginDependency
import com.github.shynixn.petblocks.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.impl.listener.PetListener
import com.github.shynixn.shygui.ShyGUIDependencyInjectionModule
import com.github.shynixn.shygui.contract.GUIMenuService
import com.github.shynixn.shygui.entity.ShyGUISettings
import com.github.shynixn.shygui.impl.commandexecutor.ShyGUICommandExecutor
import com.github.shynixn.shygui.impl.listener.GUIMenuListener
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.ServicePriority
import org.bukkit.plugin.java.JavaPlugin
import pluginMainThreadId
import java.util.*
import java.util.logging.Level
import kotlin.coroutines.CoroutineContext

/**
 * Author Shynixn
 */
class PetBlocksPlugin : JavaPlugin(), CoroutinePlugin {
    companion object {
        val eventPlayer = "eventPlayer"
        val index = "[index]"

        fun formatDoubleIfNotNull(value: Double?): String? {
            if (value == null) {
                return null
            }

            return String.format(
                Locale.ENGLISH, "%.2f", value
            )
        }

        fun formatFloatIfNotNull(value: Float?): String? {
            if (value == null) {
                return null
            }

            return String.format(
                Locale.ENGLISH, "%.2f", value
            )
        }
    }

    private val prefix: String = ChatColor.BLUE.toString() + "[PetBlocks] " + ChatColor.WHITE
    private lateinit var module: DependencyInjectionModule
    private lateinit var shyGuiModule: DependencyInjectionModule
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
                Version.VERSION_1_21_R3,
                Version.VERSION_1_21_R4,
                Version.VERSION_1_21_R5,
            )
        } else {
            listOf(Version.VERSION_1_21_R5)
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

        if (mcCoroutineConfiguration.isFoliaLoaded && !checkIfFoliaIsLoadable()) {
            logger.log(Level.SEVERE, "================================================")
            logger.log(Level.SEVERE, "BlockBall for Folia requires BlockBall-Premium-Folia.jar")
            logger.log(Level.SEVERE, "Go to https://www.patreon.com/Shynixn to download it.")
            logger.log(Level.SEVERE, "Plugin gets now disabled!")
            logger.log(Level.SEVERE, "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        if (isFoliaLoaded()) {
            logger.log(Level.INFO, "Loading Folia components.")
        }

        launch {
            pluginMainThreadId = Thread.currentThread().id
        }

        // Register Language
        val language = PetBlocksLanguageImpl()
        reloadTranslation(language)
        logger.log(Level.INFO, "Loaded language file.")

        // Module
        val placeHolderService = PlaceHolderServiceImpl(this)
        this.shyGuiModule = loadShyGuiModule(language, placeHolderService)
        this.module = PetBlocksDependencyInjectionModule(this, language, placeHolderService).build()

        // Register PlaceHolder
        PlaceHolder.registerAll(
            module.getService<PlaceHolderService>(),
            module.getService<CachePlayerRepository<PlayerInformation>>(),
            module.getService<PetService>()
        )

        // Register Packets
        val packetService = module.getService<PacketService>()
        packetService.registerPacketListening(PacketInType.USEENTITY)
        packetService.registerPacketListening(PacketInType.STEERENTITY)

        // Register Listeners
        val petListener = module.getService<PetListener>()
        Bukkit.getPluginManager().registerEvents(petListener, this)
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            val headDatabaseService = module.getService<DependencyHeadDatabaseService>()
            Bukkit.getPluginManager().registerEvents(headDatabaseService, this)
            Bukkit.getServicesManager()
                .register(DependencyHeadDatabaseService::class.java, headDatabaseService, this, ServicePriority.Normal)
        }

        // Register CommandExecutor
        module.getService<PetBlocksCommandExecutor>()

        // Register Dependencies
        Bukkit.getServicesManager()
            .register(PetService::class.java, module.getService<PetService>(), this, ServicePriority.Normal)

        val plugin = this
        plugin.launch {
            try {
                val playerDataRepository = module.getService<PlayerDataRepository<PlayerInformation>>()
                playerDataRepository.createIfNotExist()
            } catch (e: Exception) {
                e.printStackTrace()
                immediateDisable = true
                Bukkit.getPluginManager().disablePlugin(plugin)
                return@launch
            }

            val templateRepository = module.getService<Repository<PetTemplate>>()
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

        val petService = module.getService<PetService>()
        petService.close()

        val playerDataRepository = module.getService<CachePlayerRepository<PlayerInformation>>()
        runBlocking {
            playerDataRepository.saveAll()
            playerDataRepository.clearAll()
            playerDataRepository.close()
        }

        module.close()
        shyGuiModule.close()
    }

    private fun loadShyGuiModule(
        language: PetBlocksLanguage,
        placeHolderService: PlaceHolderService
    ): DependencyInjectionModule {
        val module = ShyGUIDependencyInjectionModule(this, ShyGUISettings().also {
            it.guis = listOf(
                "gui/petblocks_main_menu.yml" to "petblocks_main_menu.yml",
                "gui/petblocks_skins_menu.yml" to "petblocks_skins_menu.yml",
                "gui/petblocks_skins_blockskins_menu.yml" to "petblocks_skins_blockskins_menu.yml",
                "gui/petblocks_skins_petskins_menu.yml" to "petblocks_skins_petskins_menu.yml",
                "gui/petblocks_skins_plushieskins_menu.yml" to "petblocks_skins_plushieskins_menu.yml",
                "gui/petblocks_skins_vehicleskins_menu.yml" to "petblocks_skins_vehicleskins_menu.yml",
                "gui/simple_sample_menu.yml" to "simple_sample_menu.yml"
            )
            it.guiPermission = Permission.DYN_GUI.text
            it.aliasesPath = "commands.petblocksgui.aliases"
            it.baseCommand = "petblocksgui"
            it.commandPermission = Permission.COMMAND.text
            it.otherPlayerPermission = Permission.MANIPULATE_OTHER.text
        }, language, placeHolderService).build()

        // Register PlaceHolders
        com.github.shynixn.shygui.enumeration.PlaceHolder.registerAll(
            this,
            module.getService<PlaceHolderService>(),
            module.getService<GUIMenuService>()
        )

        // Register Packets
        val packetService = module.getService<PacketService>()
        packetService.registerPacketListening(PacketInType.CLICKINVENTORY)
        packetService.registerPacketListening(PacketInType.CLOSEINVENTORY)

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(module.getService<GUIMenuListener>(), this)

        // Register CommandExecutor
        val commandExecutor = module.getService<ShyGUICommandExecutor>()
        commandExecutor.registerShyGuiCommand()

        // Register Dependencies
        Bukkit.getServicesManager().register(
            GUIMenuService::class.java, module.getService<GUIMenuService>(), this, ServicePriority.Normal
        )

        val plugin = this
        runBlocking { // Needs to be runBlocking otherwise command is not registered.
            plugin.logger.log(Level.INFO, "Registering GUI commands...")
            commandExecutor.registerGuiCommands()
            plugin.logger.log(Level.INFO, "Registered GUI commands.")
        }
        return module
    }

    override fun execute(
        coroutineContext: CoroutineContext,
        f: suspend () -> Unit
    ): Job {
        return launch(coroutineContext) {
            f.invoke()
        }
    }

    override fun execute(f: suspend () -> Unit): Job {
        return launch {
            f.invoke()
        }
    }

    override fun fetchEntityDispatcher(entity: Entity): CoroutineContext {
        return entityDispatcher(entity)
    }

    override fun fetchGlobalRegionDispatcher(): CoroutineContext {
        return globalRegionDispatcher
    }

    override fun fetchLocationDispatcher(location: Location): CoroutineContext {
        return regionDispatcher(location)
    }
}
