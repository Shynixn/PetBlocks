@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import com.github.shynixn.petblocks.api.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.bukkit.logic.business.listener.*
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.EditPetCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.PlayerPetActionCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.extension.cast
import com.google.inject.Guice
import com.google.inject.Injector
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.Server
import org.bukkit.configuration.MemorySection
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.logging.Level

/**
 * Plugin created by Shynixn.
 */
class PetBlocksPlugin : JavaPlugin(), PluginProxy {
    companion object {
        /** Final Prefix of PetBlocks in the console */
        val PREFIX_CONSOLE: String = ChatColor.AQUA.toString() + "[PetBlocks] "
    }

    private val configVersion = 3
    private var injector: Injector? = null
    private var serverVersion: Version? = null
    private val bstatsPluginId = 1323

    /**
     * Gets the installed version of the plugin.
     */
    override val version: String
        get() {
            return description.version
        }

    /**
     * Enables the plugin PetBlocks.
     */
    override fun onEnable() {
        sendConsoleMessage(ChatColor.GREEN.toString() + "Loading PetBlocks ...")
        this.saveDefaultConfig()

        if (disableForVersion(Version.VERSION_1_8_R1, Version.VERSION_1_8_R3)) {
            return
        }

        if (disableForVersion(Version.VERSION_1_8_R2, Version.VERSION_1_8_R3)) {
            return
        }

        if (disableForVersion(Version.VERSION_1_9_R1, Version.VERSION_1_9_R2)) {
            return
        }

        if (disableForVersion(Version.VERSION_1_13_R1, Version.VERSION_1_13_R2)) {
            return
        }

        if (disableForVersion(Version.VERSION_1_16_R1, Version.VERSION_1_16_R3)) {
            return
        }

        val versions = arrayOf(
            Version.VERSION_1_8_R3,
            Version.VERSION_1_9_R2,
            Version.VERSION_1_10_R1,
            Version.VERSION_1_11_R1,
            Version.VERSION_1_12_R1,
            Version.VERSION_1_13_R2,
            Version.VERSION_1_14_R1,
            Version.VERSION_1_15_R1,
            Version.VERSION_1_16_R3,
            Version.VERSION_1_17_R1,
            Version.VERSION_1_18_R1,
            Version.VERSION_1_18_R2,
            Version.VERSION_1_19_R1
        )

        if (!getServerVersion().isCompatible(versions)) {
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")
            sendConsoleMessage(ChatColor.RED.toString() + "PetBlocks does not support your server version")
            sendConsoleMessage(ChatColor.RED.toString() + "Install v" + Version.VERSION_1_8_R3.id + " - v" + Version.VERSION_1_19_R1.id)
            sendConsoleMessage(ChatColor.RED.toString() + "Plugin gets now disabled!")
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")

            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        if (hasArmorstandTickingChanged()) {
            sendConsoleMessage(ChatColor.YELLOW.toString() + "================================================")
            sendConsoleMessage(ChatColor.YELLOW.toString() + "PetBlocks has automatically changed your paper.yml file.")
            sendConsoleMessage(ChatColor.YELLOW.toString() + "The setting armor-stand-tick: true has changed.")
            sendConsoleMessage(ChatColor.YELLOW.toString() + "Please restart the server.")
            sendConsoleMessage(ChatColor.YELLOW.toString() + "================================================")
            Bukkit.getServer().shutdown()
            return
        }

        if (!this.config.contains("config-version") || this.config.getInt("config-version") != configVersion) {
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")
            sendConsoleMessage(ChatColor.RED.toString() + "PetBlocks config.yml config-version does not match")
            sendConsoleMessage(ChatColor.RED.toString() + "with your installed PetBlocks.jar.")
            sendConsoleMessage(ChatColor.RED.toString() + "Carefully read the patch notes to get the correct config-version.")
            sendConsoleMessage(ChatColor.RED.toString() + "https://github.com/Shynixn/PetBlocks/releases")
            sendConsoleMessage(ChatColor.RED.toString() + "Plugin gets now disabled!")
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")

            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        this.injector = Guice.createInjector(PetBlocksDependencyInjectionBinder(this))
        this.reloadConfig()

        val dependencyService = resolve<DependencyService>(DependencyService::class.java)
        val updateCheckService = resolve<UpdateCheckService>(UpdateCheckService::class.java)
        val commandService = resolve<CommandService>(CommandService::class.java)
        val entityService = resolve<EntityService>(EntityService::class.java)
        val localizationService = resolve<LocalizationService>(LocalizationService::class.java)

        dependencyService.checkForInstalledDependencies()
        updateCheckService.checkForUpdates()
        localizationService.reload()

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

        if (getServerVersion().isVersionSameOrGreaterThan(Version.VERSION_1_17_R1)) {
            Bukkit.getPluginManager().registerEvents(resolve(EntityCleanUp117R1Listener::class.java), this)
        }

        // Register CommandExecutor
        commandService.registerCommandExecutor("petblocks", this.resolve(EditPetCommandExecutorImpl::class.java))
        commandService.registerCommandExecutor("petblockreload", this.resolve(ReloadCommandExecutorImpl::class.java))
        commandService.registerCommandExecutor(
            (config.get("commands.petblock") as MemorySection).getValues(false) as Map<String, String>,
            this.resolve(PlayerPetActionCommandExecutorImpl::class.java)
        )

        if (config.getBoolean("metrics")) {
            val metrics = Metrics(this, bstatsPluginId)

            metrics.addCustomChart(Metrics.SimplePie("storage") {
                if (config.getString("sql.type") == "mysql") {
                    "MySQL"
                } else {
                    "SQLite"
                }
            })
        }

        if (dependencyService.isInstalled(PluginDependency.PLACEHOLDERAPI)) {
            val placeHolderService =
                resolve<DependencyPlaceholderApiService>(DependencyPlaceholderApiService::class.java)
            placeHolderService.registerListener()
        }

        entityService.cleanUpInvalidEntitiesInAllWorlds()
        startPlugin()

        Bukkit.getPluginManager().registerEvents(resolve(ProtocolListener::class.java), this)
        sendConsoleMessage(ChatColor.GREEN.toString() + "Enabled PetBlocks " + this.description.version + " by Shynixn")
    }

    /**
     * OnDisable.
     */
    override fun onDisable() {
        if (injector == null) {
            return
        }

        resolve<ProtocolService>(ProtocolService::class.java).close()
        resolve<EntityRegistrationService>(EntityRegistrationService::class.java).clearResources()
        resolve<PersistencePetMetaService>(PersistencePetMetaService::class.java).close()

        for (world in Bukkit.getWorlds()) {
            for (player in world.players) {
                resolve<PetDebugService>(PetDebugService::class.java).unRegister(player)
                resolve<DependencyHeadDatabaseService>(DependencyHeadDatabaseService::class.java).clearResources(player)
                resolve<GUIService>(GUIService::class.java).cleanResources(player)
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
                    val protocolService = resolve<ProtocolService>(ProtocolService::class.java)
                    protocolService.registerPlayer(player)
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
     * Gets the server version this plugin is currently running on.
     */
    override fun getServerVersion(): Version {
        if (this.serverVersion != null) {
            return this.serverVersion!!
        }

        try {
            if (Bukkit.getServer().cast<Server?>() == null || Bukkit.getServer().javaClass.getPackage() == null) {
                this.serverVersion = Version.VERSION_UNKNOWN
                return this.serverVersion!!
            }

            val version = Bukkit.getServer().javaClass.getPackage().name.replace(".", ",").split(",")[3]

            for (versionSupport in Version.values()) {
                if (versionSupport.bukkitId == version) {
                    this.serverVersion = versionSupport
                    return versionSupport
                }
            }

        } catch (e: Exception) {
            // Ignore parsing exceptions.
        }

        this.serverVersion = Version.VERSION_UNKNOWN

        return this.serverVersion!!
    }

    /**
     * Gets a business logic from the PetBlocks plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     * @param S the type of service class.
     */
    override fun <S> resolve(service: Any): S {
        require(service is Class<*>) { "Service has to be a Class!" }

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
        require(entity is Class<*>) { "Entity has to be a Class!" }

        try {
            val entityName = entity.simpleName + "Entity"
            return Class.forName("com.github.shynixn.petblocks.core.logic.persistence.entity.$entityName")
                .getDeclaredConstructor().newInstance() as E
        } catch (e: Exception) {
            throw IllegalArgumentException("Entity could not be created.", e)
        }
    }

    /**
     * Sends a console message from this plugin.
     */
    private fun sendConsoleMessage(message: String) {
        Bukkit.getServer().consoleSender.sendMessage(PREFIX_CONSOLE + message)
    }

    /**
     * Disables the plugin for the given version and prints the supported version.
     */
    private fun disableForVersion(version: Version, supportedVersion: Version): Boolean {
        if (getServerVersion() == version) {
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")
            sendConsoleMessage(ChatColor.RED.toString() + "PetBlocks does not support this subversion")
            sendConsoleMessage(ChatColor.RED.toString() + "Please upgrade from v" + version.id + " to v" + supportedVersion.id)
            sendConsoleMessage(ChatColor.RED.toString() + "Plugin gets now disabled!")
            sendConsoleMessage(ChatColor.RED.toString() + "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return true
        }

        return false
    }

    /**
     * Checks if armorStand ticking is disabled when Paper(Spigot) is being used.
     */
    private fun hasArmorstandTickingChanged(): Boolean {
        if (config.getBoolean("global-configuration.ignore-ticking-settings")) {
            return false
        }

        val path = Paths.get("paper.yml")

        if (!Files.exists(path)) {
            return false
        }

        val text = FileUtils.readFileToString(path.toFile(), "UTF-8")

        if (text.contains("armor-stands-tick: false")) {
            FileUtils.writeStringToFile(
                path.toFile(),
                text.replace("armor-stands-tick: false", "armor-stands-tick: true"),
                "UTF-8"
            )
            return true
        }

        return false
    }
}
