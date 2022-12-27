package com.github.shynixn.petblocks.bukkit

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.database.impl.AutoSavePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.CachePlayerDataRepositoryImpl
import com.github.shynixn.mcutils.database.impl.PlayerDataSqlRepositoryImpl
import com.github.shynixn.mcutils.database.impl.SqliteConnectionServiceImpl
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

        bind(PetActionExecutionService::class.java).to(PetActionExecutionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetEntityFactory::class.java).to(PetEntityFactoryImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetService::class.java).to(PetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PlaceHolderService::class.java).to(PlaceHolderServiceImpl::class.java).`in`(Scopes.SINGLETON)

        // Build Template Repository
        val templateRepositoryImpl =
            PetTemplateRepositoryImpl(plugin.dataFolder.resolve("template").toPath(), plugin, "pet_hopping.yml")
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
