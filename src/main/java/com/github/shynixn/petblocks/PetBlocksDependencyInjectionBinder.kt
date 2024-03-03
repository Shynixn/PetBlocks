package com.github.shynixn.petblocks

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.item.ItemServiceImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.repository.CachedRepositoryImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.impl.AutoSavePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.CachePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.ConfigSelectedRepositoryImpl
import com.github.shynixn.mcutils.packet.api.EntityService
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.EntityServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.RayTracingServiceImpl
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.impl.PathfinderServiceImpl
import com.github.shynixn.mcutils.pathfinder.impl.service.CubeWorldSnapshotServiceImpl
import com.github.shynixn.petblocks.contract.*
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PluginDependency
import com.github.shynixn.petblocks.impl.service.*
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetBlocksDependencyInjectionBinder(private val plugin: PetBlocksPlugin) : AbstractModule() {
    companion object {
        val areLegacyVersionsIncluded: Boolean by lazy {
            try {
                Class.forName("com.github.shynixn.petblocks.lib.com.github.shynixn.mcutils.packet.nms.v1_8_R3.PacketSendServiceImpl")
                true
            } catch (e: ClassNotFoundException) {
                false
            }
        }
    }

    override fun configure() {
        bind(Plugin::class.java).toInstance(plugin)
        bind(PetBlocksPlugin::class.java).toInstance(plugin)

        val autoSaveMinutes = plugin.config.getInt("database.autoSaveIntervalMinutes")

        // Repositories
        val templateRepositoryImpl = YamlFileRepositoryImpl<PetTemplate>(plugin, "pets", listOf(
            Pair("pets/pet_classic.yml", "pet_classic.yml"),
            Pair("pets/pet_mining.yml", "pet_mining.yml"),
            Pair("pets/pet_flying_dolphin.yml", "pet_flying_dolphin.yml")
        ), emptyList(), object : TypeReference<PetTemplate>() {})
        val cacheTemplateRepository = CachedRepositoryImpl(templateRepositoryImpl)
        bind(object : TypeLiteral<Repository<PetTemplate>>() {}).toInstance(cacheTemplateRepository)
        bind(object : TypeLiteral<CacheRepository<PetTemplate>>() {}).toInstance(cacheTemplateRepository)
        bind(Repository::class.java).toInstance(cacheTemplateRepository)
        val configSelectedRepository = ConfigSelectedRepositoryImpl<PlayerInformation>(plugin,
            "PetBlocks",
            plugin.dataFolder.toPath().resolve("PetBlocks.sqlite"),
            object : TypeReference<PlayerInformation>() {})
        val playerDataRepository = AutoSavePlayerDataRepositoryImpl(
            "pets",
            1000 * 60L * autoSaveMinutes,
            CachePlayerDataRepositoryImpl(configSelectedRepository, plugin),
            plugin
        )
        bind(object : TypeLiteral<PlayerDataRepository<PlayerInformation>>() {}).toInstance(playerDataRepository)
        bind(object : TypeLiteral<CachePlayerRepository<PlayerInformation>>() {}).toInstance(playerDataRepository)
        bind(PlayerDataRepository::class.java).toInstance(playerDataRepository)
        bind(CachePlayerRepository::class.java).toInstance(playerDataRepository)

        // Services
        val configurationService = ConfigurationServiceImpl(plugin)
        val physicObjectDispatcher = PhysicObjectDispatcherImpl(plugin)
        bind(EntityService::class.java).toInstance(EntityServiceImpl())
        bind(RayTracingService::class.java).toInstance(RayTracingServiceImpl())
        bind(PacketService::class.java).toInstance(PacketServiceImpl(plugin))
        bind(PhysicObjectDispatcher::class.java).toInstance(physicObjectDispatcher)
        bind(ConfigurationService::class.java).toInstance(configurationService)
        bind(PhysicObjectService::class.java).toInstance(PhysicObjectServiceImpl(plugin, physicObjectDispatcher))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())
        bind(PathfinderService::class.java).toInstance(PathfinderServiceImpl(CubeWorldSnapshotServiceImpl()))

        bind(BreakBlockService::class.java).to(BreakBlockServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetService::class.java).to(PetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetEntityFactory::class.java).to(PetEntityFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetActionExecutionService::class.java).to(PetActionExecutionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.PLACEHOLDERAPI.pluginName) != null) {
            bind(PlaceHolderService::class.java).to(DependencyPlaceHolderApiServiceImpl::class.java)
                .`in`(Scopes.SINGLETON)
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.PLACEHOLDERAPI.pluginName}.")
        } else {
            bind(PlaceHolderService::class.java).to(PlaceHolderServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            bind(DependencyHeadDatabaseService::class.java).to(DependencyHeadDatabaseServiceImpl::class.java)
                .`in`(Scopes.SINGLETON)
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.HEADDATABASE.pluginName}.")
        }

        try {
            // Try Load Nashorn Implementation
            val nashornScriptEngine = ScriptNashornEngineServiceImpl(plugin, configurationService)
            bind(ScriptService::class.java).toInstance(nashornScriptEngine)
            plugin.logger.log(Level.INFO, "Loaded embedded NashornScriptEngine.")
        } catch (e: Error) {
            try {
                // Try Load JDK Implementation
                val jdkScriptEngine = ScriptJdkEngineServiceImpl(plugin, configurationService)
                bind(ScriptService::class.java).toInstance(jdkScriptEngine)
                plugin.logger.log(Level.INFO, "Loaded JDK NashornScriptEngine.")
            } catch (ex: Exception) {
                throw RuntimeException("Cannot find NashornScriptEngine implementation.", ex)
            }
        }
    }
}
