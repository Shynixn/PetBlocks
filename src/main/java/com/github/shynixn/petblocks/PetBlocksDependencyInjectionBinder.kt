package com.github.shynixn.petblocks

import com.fasterxml.jackson.core.type.TypeReference
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.ConfigurationServiceImpl
import com.github.shynixn.mcutils.common.Version
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.item.ItemServiceImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectDispatcherImpl
import com.github.shynixn.mcutils.common.physic.PhysicObjectService
import com.github.shynixn.mcutils.common.physic.PhysicObjectServiceImpl
import com.github.shynixn.mcutils.common.repository.YamlFileRepositoryImpl
import com.github.shynixn.mcutils.database.api.SqlConnectionService
import com.github.shynixn.mcutils.database.impl.SqliteConnectionServiceImpl
import com.github.shynixn.mcutils.pathfinder.api.PathfinderService
import com.github.shynixn.mcutils.pathfinder.impl.PathfinderServiceImpl
import com.github.shynixn.mcutils.pathfinder.impl.service.CubeWorldSnapshotServiceImpl
import com.github.shynixn.petblocks.entity.PetTemplate
import com.google.inject.AbstractModule
import org.bukkit.plugin.Plugin

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
        bind(Version::class.java).toInstance(Version.serverVersion)
        bind(Plugin::class.java).toInstance(plugin)

        // Build Common Module
        bind(ConfigurationService::class.java).toInstance(ConfigurationServiceImpl(plugin))
        bind(ItemService::class.java).toInstance(ItemServiceImpl())

        // Build Physic Module
        val physicObjectService = PhysicObjectServiceImpl(plugin, PhysicObjectDispatcherImpl(plugin))
        bind(PhysicObjectService::class.java).toInstance(physicObjectService)

        // Build Pathfinder Module
        val worldSnapshotService = CubeWorldSnapshotServiceImpl()
        val pathfinderService = PathfinderServiceImpl(worldSnapshotService)
        bind(PathfinderService::class.java).toInstance(pathfinderService)

        // Build PetBlocks Module
        val templateRepositoryImpl =
            YamlFileRepositoryImpl<PetTemplate>(plugin, "pets", "asd", "asd", object : TypeReference<PetTemplate>() {})

        // Build Player Storage Repository
        val sqliteConnectionServiceImpl =
            SqliteConnectionServiceImpl(plugin.dataFolder.toPath().resolve("PetBlocks.sqlite"), plugin.logger)
        bind(SqlConnectionService::class.java).toInstance(sqliteConnectionServiceImpl)
    }
}
