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
import com.github.shynixn.mcutils.guice.DependencyInjectionModule
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
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetBlocksDependencyInjectionModule(private val plugin: PetBlocksPlugin) : DependencyInjectionModule() {
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
        addService<Plugin>(plugin)
        val autoSaveMinutes = plugin.config.getInt("database.autoSaveIntervalMinutes")

        // Repositories
        val templateRepositoryImpl = YamlFileRepositoryImpl<PetTemplate>(plugin, "pets", listOf(
            Pair("pets/pet_classic.yml", "pet_classic.yml"),
            Pair("pets/pet_mining.yml", "pet_mining.yml"),
            Pair("pets/pet_flying_dolphin.yml", "pet_flying_dolphin.yml")
        ), emptyList(), object : TypeReference<PetTemplate>() {})
        val cacheTemplateRepository = CachedRepositoryImpl(templateRepositoryImpl)
        addService<Repository<PetTemplate>>(cacheTemplateRepository)
        addService<CacheRepository<PetTemplate>>(cacheTemplateRepository)
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
        addService<PlayerDataRepository<PlayerInformation>>(playerDataRepository)
        addService<CachePlayerRepository<PlayerInformation>>(playerDataRepository)

        // Services
        val configurationService = ConfigurationServiceImpl(plugin)
        addService<EntityService>(EntityServiceImpl())
        addService<RayTracingService>(RayTracingServiceImpl())
        addService<PacketService>(PacketServiceImpl(plugin))
        addService<PhysicObjectDispatcher>(PhysicObjectDispatcherImpl(plugin))
        addService<ConfigurationService>(ConfigurationServiceImpl(plugin))
        addService<PhysicObjectService> {
            PhysicObjectServiceImpl(plugin, getService())
        }
        addService<ItemService>(ItemServiceImpl())
        addService<PathfinderService>(PathfinderServiceImpl(CubeWorldSnapshotServiceImpl()))
        addService<BreakBlockService, BreakBlockServiceImpl>()
        addService<PetService, PetServiceImpl>()
        addService<PetEntityFactory, PetEntityFactoryImpl>()
        addService<PetActionExecutionService, PetActionExecutionServiceImpl>()

        if (Bukkit.getPluginManager().getPlugin(PluginDependency.PLACEHOLDERAPI.pluginName) != null) {
            addService<PlaceHolderService, DependencyPlaceHolderApiServiceImpl>()
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.PLACEHOLDERAPI.pluginName}.")
        } else {
            addService<PlaceHolderService, PlaceHolderServiceImpl>()
        }

        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            addService<DependencyHeadDatabaseService, DependencyHeadDatabaseServiceImpl>()
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.HEADDATABASE.pluginName}.")
        }

        try {
            // Try Load Nashorn Implementation
            val nashornScriptEngine = ScriptNashornEngineServiceImpl(plugin, configurationService)
            addService<ScriptService>(nashornScriptEngine)
            plugin.logger.log(Level.INFO, "Loaded embedded NashornScriptEngine.")
        } catch (e: Error) {
            try {
                // Try Load JDK Implementation
                val jdkScriptEngine = ScriptJdkEngineServiceImpl(plugin, configurationService)
                addService<ScriptService>(jdkScriptEngine)
                plugin.logger.log(Level.INFO, "Loaded JDK NashornScriptEngine.")
            } catch (ex: Exception) {
                throw RuntimeException("Cannot find NashornScriptEngine implementation.", ex)
            }
        }
    }
}
