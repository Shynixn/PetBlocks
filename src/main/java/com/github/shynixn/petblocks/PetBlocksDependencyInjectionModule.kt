package com.github.shynixn.petblocks

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.di.DependencyInjectionModule
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.language.globalChatMessageService
import com.github.shynixn.mcutils.common.language.globalPlaceHolderService
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcher
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.common.repository.CachedRepositoryImpl
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.impl.AutoSavePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.CachedPlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.ConfigSelectedRepositoryImpl
import com.github.shynixn.mcutils.javascript.JavaScriptService
import com.github.shynixn.mcutils.javascript.JavaScriptServiceImpl
import com.github.shynixn.mcutils.packet.api.PacketService
import com.github.shynixn.mcutils.packet.api.RayTracingService
import com.github.shynixn.mcutils.packet.impl.service.ChatMessageServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.ItemServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.PacketServiceImpl
import com.github.shynixn.mcutils.packet.impl.service.RayTracingServiceImpl
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.impl.PathfinderServiceImpl
import com.github.shynixn.mcutils.pathfinder.impl.service.CubeWorldSnapshotServiceImpl
import com.github.shynixn.petblocks.contract.*
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PluginDependency
import com.github.shynixn.petblocks.impl.commandexecutor.PetBlocksCommandExecutor
import com.github.shynixn.petblocks.impl.listener.PetListener
import com.github.shynixn.petblocks.impl.service.*
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import java.util.logging.Level

class PetBlocksDependencyInjectionModule(
    private val plugin: PetBlocksPlugin,
    private val shyGuiModule: DependencyInjectionModule,
    private val language: PetBlocksLanguage
) {
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

    fun build(): DependencyInjectionModule {
        val module = DependencyInjectionModule()

        // Params
        module.addService<Plugin>(plugin)
        module.addService<PetBlocksLanguage>(language)

        // Repositories
        val templateRepositoryImpl = YamlFileRepositoryImpl<PetTemplate>(
            plugin, "pets", listOf(
                Pair("pets/pet_classic.yml", "pet_classic.yml"),
                Pair("pets/pet_mining.yml", "pet_mining.yml"),
                Pair("pets/pet_flying_dolphin.yml", "pet_flying_dolphin.yml")
            ), emptyList(), object : TypeReference<PetTemplate>() {})
        val cacheTemplateRepository = CachedRepositoryImpl(templateRepositoryImpl)
        module.addService<Repository<PetTemplate>>(cacheTemplateRepository)
        module.addService<CacheRepository<PetTemplate>>(cacheTemplateRepository)
        val configSelectedRepository = ConfigSelectedRepositoryImpl<PlayerInformation>(
            plugin,
            "PetBlocks",
            plugin.dataFolder.toPath().resolve("PetBlocks.sqlite"),
            object : TypeReference<PlayerInformation>() {}, plugin.minecraftDispatcher
        )
        val autoSaveMinutes = plugin.config.getInt("database.autoSaveIntervalMinutes")
        val playerDataRepository = AutoSavePlayerDataRepositoryImpl(
            1000 * 60L * autoSaveMinutes,
            CachedPlayerDataRepositoryImpl(configSelectedRepository, plugin.minecraftDispatcher),
            plugin.scope,
            plugin.minecraftDispatcher
        )
        module.addService<PlayerDataRepository<PlayerInformation>>(playerDataRepository)
        module.addService<CachePlayerRepository<PlayerInformation>>(playerDataRepository)

        // Library Services
        val chatMessageService = ChatMessageServiceImpl(plugin)
        module.addService<PlaceHolderService>(shyGuiModule.getService<PlaceHolderService>())
        module.addService<RayTracingService>(RayTracingServiceImpl())
        module.addService<PacketService>(PacketServiceImpl(plugin))
        module.addService<PhysicObjectDispatcher>(PhysicObjectDispatcherImpl(plugin))
        module.addService<ConfigurationService>(ConfigurationServiceImpl(plugin))
        module.addService<ChatMessageService>(chatMessageService)
        module.addService<PhysicObjectService> {
            PhysicObjectServiceImpl(plugin, module.getService())
        }
        module.addService<ItemService>(ItemServiceImpl())
        module.addService<PathfinderService>(PathfinderServiceImpl(CubeWorldSnapshotServiceImpl()))
        module.addService<JavaScriptService>(
            JavaScriptServiceImpl(
                plugin,
                this.plugin.config.getStringList("scriptEngine.options")
            )
        )

        // Services
        module.addService<PetListener> {
            PetListener(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<PetBlocksCommandExecutor> {
            PetBlocksCommandExecutor(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<BreakBlockService>() {
            BreakBlockServiceImpl(module.getService(), module.getService())
        }
        module.addService<PetService> {
            PetServiceImpl(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService()
            )
        }
        module.addService<PetEntityFactory> {
            PetEntityFactoryImpl(
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
                module.getService(),
            )
        }
        module.addService<PetActionExecutionService> {
            PetActionExecutionServiceImpl(module.getService(), module.getService(), module.getService())
        }
        if (Bukkit.getPluginManager().getPlugin(PluginDependency.HEADDATABASE.pluginName) != null) {
            module.addService<DependencyHeadDatabaseService> {
                DependencyHeadDatabaseServiceImpl(module.getService(), module.getService(), module.getService())
            }
            plugin.logger.log(Level.INFO, "Loaded dependency ${PluginDependency.HEADDATABASE.pluginName}.")
        }


        plugin.globalChatMessageService = chatMessageService
        plugin.globalPlaceHolderService = shyGuiModule.getService<PlaceHolderService>()
        return module
    }
}
