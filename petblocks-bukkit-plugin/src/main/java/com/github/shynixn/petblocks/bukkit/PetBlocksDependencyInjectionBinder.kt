package com.github.shynixn.petblocks.bukkit

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.database.impl.AutoSavePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.CachePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.PlayerDataSqlRepositoryImpl
import com.github.shynixn.mcutils.database.impl.SqliteConnectionServiceImpl
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.api.WorldSnapshotService
import com.github.shynixn.mcutils.pathfinder.impl.PathfinderServiceImpl
import com.github.shynixn.mcutils.pathfinder.impl.service.CubeWorldSnapshotServiceImpl
import com.github.shynixn.mcutils.physicobject.api.PhysicObjectService
import com.github.shynixn.mcutils.physicobject.impl.PhysicObjectServiceImpl
import com.github.shynixn.petblocks.bukkit.entity.PlayerInformation
import com.github.shynixn.petblocks.bukkit.service.*
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import com.google.inject.TypeLiteral
import org.bukkit.plugin.Plugin
import java.util.concurrent.TimeUnit

class PetBlocksDependencyInjectionBinder(private val plugin: PetBlocksPlugin) : AbstractModule() {

    override fun configure() {
        bind(Version::class.java).toInstance(Version.serverVersion)
        bind(Plugin::class.java).toInstance(plugin)

        // Build PetBlocks Module
        bind(PetActionExecutionService::class.java).to(PetActionExecutionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetEntityFactory::class.java).to(PetEntityFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetService::class.java).to(PetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PlaceHolderService::class.java).to(PlaceHolderServiceImpl::class.java).`in`(Scopes.SINGLETON)

        // Build Common Module
        bind(ConfigurationService::class.java).toInstance(ConfigurationServiceImpl(plugin))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())

        // Build Physic Module
        val physicObjectService = PhysicObjectServiceImpl(plugin)
        bind(PhysicObjectService::class.java).toInstance(physicObjectService)

        // Build Pathfinder Module
        val worldSnapshotService = CubeWorldSnapshotServiceImpl()
        val pathfinderService = PathfinderServiceImpl(worldSnapshotService)
        bind(PathfinderService::class.java).toInstance(pathfinderService)

        // Build Template Repository
        val templateRepositoryImpl =
            PetTemplateRepositoryImpl(plugin.dataFolder.resolve("pets").toPath(), plugin, "pet_hopping.yml")
        bind(PetTemplateRepository::class.java).toInstance(templateRepositoryImpl)

        // Build Player Storage Repository
        val sqliteConnectionServiceImpl =
            SqliteConnectionServiceImpl(plugin.dataFolder.toPath().resolve("PetBlocks.sqlite"), plugin.logger)
        bind(SqlConnectionService::class.java).toInstance(sqliteConnectionServiceImpl)
        val playerDataRepository = PlayerDataSqlRepositoryImpl<PlayerInformation>(
            "PetBlocks", object : TypeReference<PlayerInformation>() {}, sqliteConnectionServiceImpl
        )
        val cachePlayerDataRepository = CachePlayerDataRepositoryImpl(playerDataRepository, plugin)
        val autoSavePlayerDataRepository =
            AutoSavePlayerDataRepositoryImpl(TimeUnit.MINUTES.toMillis(20), cachePlayerDataRepository, plugin)
        bind(object :
            TypeLiteral<PlayerDataRepository<PlayerInformation>>() {}).toInstance(autoSavePlayerDataRepository)
        bind(object : TypeLiteral<CachePlayerRepository<PlayerInformation>>() {}).toInstance(
            autoSavePlayerDataRepository
        )
        bind(PlayerDataRepository::class.java).toInstance(autoSavePlayerDataRepository)
        bind(CachePlayerRepository::class.java).toInstance(autoSavePlayerDataRepository)
    }
}
