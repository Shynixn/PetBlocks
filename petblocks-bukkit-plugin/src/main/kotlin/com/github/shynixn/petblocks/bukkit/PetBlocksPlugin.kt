package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.mccoroutine.bukkit.SuspendingJavaPlugin
import com.github.shynixn.mccoroutine.bukkit.setSuspendingExecutor
import com.github.shynixn.mccoroutine.bukkit.setSuspendingTabCompleter
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.reloadTranslation
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.petblocks.bukkit.contract.PetService
import com.github.shynixn.petblocks.bukkit.contract.PetTemplateRepository
import com.github.shynixn.petblocks.bukkit.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.bukkit.impl.listener.PetListener
import com.google.inject.Guice
import com.google.inject.Injector
import org.bukkit.Bukkit
import java.util.logging.Level

class PetBlocksPlugin : SuspendingJavaPlugin() {
    companion object {
        val prefix: String = ChatColor.BLUE.toString() + "[PetBlocks] " + ChatColor.WHITE
        val languageFiles = arrayListOf("en_us")
    }

    private var injector: Injector? = null

    /**
     * Called when this plugin is enabled
     */
    override suspend fun onEnableAsync() {
        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Loading PetBlocks ...")
        this.saveDefaultConfig()

        if (!Version.serverVersion.isCompatible(
                Version.VERSION_1_18_R2
            )
        ) {
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "================================================")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "PetBlocks does not support your server version")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "Install v" + Version.VERSION_1_18_R2.id + " - v" + Version.VERSION_1_18_R2.id)
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "Plugin gets now disabled!")
            Bukkit.getServer().consoleSender.sendMessage(ChatColor.RED.toString() + "================================================")
            Bukkit.getPluginManager().disablePlugin(this)
            return
        }

        // Guice
        this.injector = Guice.createInjector(PetBlocksDependencyInjectionBinder(this))
        this.reloadConfig()

        // Register Listeners
        Bukkit.getPluginManager().registerEvents(resolve(PetListener::class.java), this)

        // Register CommandExecutors
        val configurationService = resolve(ConfigurationService::class.java)
        val petBlocksCommandExecutor = resolve(PetBlocksCommandExecutor::class.java)
        val mcTennisCommand = this.getCommand("petblocks")!!
        mcTennisCommand.aliases = configurationService.findValue("commands.petblocks.aliases")
        mcTennisCommand.usage = configurationService.findValue("commands.petblocks.usage")
        mcTennisCommand.description = configurationService.findValue("commands.petblocks.description")
        mcTennisCommand.permissionMessage = configurationService.findValue("commands.petblocks.permission-message")
        mcTennisCommand.setSuspendingExecutor(petBlocksCommandExecutor)
        mcTennisCommand.setSuspendingTabCompleter(petBlocksCommandExecutor)

        val language = configurationService.findValue<String>("language")
        this.reloadTranslation(language, PetBlocksLanguage::class.java, *languageFiles.toTypedArray())
        logger.log(Level.INFO, "Loaded language file $language.properties.")

        val sqlConnectionService = resolve(SqlConnectionService::class.java)
        sqlConnectionService.connect()
        val playerDataRepository = resolve(PlayerDataRepository::class.java)
        playerDataRepository.createIfNotExist()
        val templateRepository = resolve(PetTemplateRepository::class.java)
        templateRepository.copyTemplatesIfNotExist()

        Bukkit.getServer().consoleSender.sendMessage(prefix + ChatColor.GREEN + "Enabled PetBlocks " + this.description.version + " by Shynixn")
    }

    /**
     * Called when this plugin is disabled.
     */
    override suspend fun onDisableAsync() {
        val petService = resolve(PetService::class.java)
        petService.close()

        val playerDataRepository = resolve(CachePlayerRepository::class.java)
        playerDataRepository.clearCache()

        val sqlConnectionService = resolve(SqlConnectionService::class.java)
        sqlConnectionService.close()
    }

    /**
     * Gets a business logic from the plugin.
     * All types in the service package can be accessed.
     * Throws a [IllegalArgumentException] if the service could not be found.
     */
    private fun <S> resolve(service: Class<S>): S {
        try {
            return this.injector!!.getBinding(service).provider.get() as S
        } catch (e: Exception) {
            throw IllegalArgumentException("Service ${service.name} could not be resolved.", e)
        }
    }
}
