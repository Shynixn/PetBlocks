@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.commandexecutor.EditPetCommandExecutor
import com.github.shynixn.petblocks.api.business.commandexecutor.PlayerPetActionCommandExecutor
import com.github.shynixn.petblocks.api.business.commandexecutor.ReloadCommandExecutor
import com.github.shynixn.petblocks.api.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.SqlConnectionPoolProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository
import com.github.shynixn.petblocks.bukkit.logic.business.extension.getServerVersion
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.SqlProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.*
import com.github.shynixn.petblocks.core.jvm.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.service.UpdateCheckServiceImpl
import com.github.shynixn.petblocks.core.jvm.logic.persistence.service.YamlSerializationServiceImpl
import com.github.shynixn.petblocks.core.logic.business.command.*
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.EditPetCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.PlayerPetActionCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetRunTimeRepository
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.bukkit.plugin.Plugin
import java.lang.reflect.Constructor

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class PetBlocksDependencyInjectionBinder(private val plugin: Plugin) : AbstractModule() {
    /**
     * Configures the business logic tree.
     */
    override fun configure() {
        val version = plugin.getServerVersion()

        bindInstance<Plugin>(plugin)
        bindInstance<Version>(version)
        bindInstance<LoggingService>(LoggingUtilServiceImpl(plugin.logger))

        // Commands
        bindClass<EditPetCostumeCommand>()
        bindClass<EditPetDisableCommand>()
        bindClass<EditPetEnableCommand>()
        bindClass<EditPetItemLoreCommand>()
        bindClass<EditPetItemNameCommand>()
        bindClass<EditPetKillNextCommand>()
        bindClass<EditPetParticleCommand>()
        bindClass<EditPetRenameCommand>()
        bindClass<EditPetRideCommand>()
        bindClass<EditPetSkinCommand>()
        bindClass<EditPetToggleCommand>()
        bindClass<EditPetToggleSoundCommand>()
        bindClass<EditPetWearCommand>()

        // CommandExecutors
        bind<ReloadCommandExecutor, ReloadCommandExecutorImpl>()
        bind<PlayerPetActionCommandExecutor, PlayerPetActionCommandExecutorImpl>()
        bind<EditPetCommandExecutor, EditPetCommandExecutorImpl>()

        // Context
        bind<SqlConnectionPoolProxy, SqlProxyImpl>()
        bind<SqlDbContext, SqlDbContextImpl>()

        // Repository
        bind<PetMetaRepository, PetMetaSqlRepository>()
        bind<PetRepository, PetRunTimeRepository>()

        // Services
        bind<ParticleService, ParticleServiceImpl>()
        bind<SoundService, SoundServiceImpl>()
        bind<GUIScriptService, GUIScriptServiceImpl>()
        bind<UpdateCheckService, UpdateCheckServiceImpl>()
        bind<MessageService, MessageServiceImpl>()
        bind<PetActionService, PetActionServiceImpl>()
        bind<ProxyService, ProxyServiceImpl>()
        bind<EntityService, EntityServiceImpl>()
        bind<ConcurrencyService, ConcurrencyServiceImpl>()
        bind<CommandService, CommandServiceImpl>()
        bind<PersistencePetMetaService, PersistencePetMetaServiceImpl>()
        bind<ConfigurationService, ConfigurationServiceImpl>()
        bind<GUIService, GUIServiceImpl>()
        bind<FeedingPetService, FeedPetServiceImpl>()
        bind<PetService, PetServiceImpl>()
        bind<DependencyService, DependencyServiceImpl>()
        bind<CarryPetService, CarryPetServiceImpl>()
        bind<CombatPetService, CombatPetServiceImpl>()
        bind<AIService, AIServiceImpl>()
        bind<YamlSerializationService, YamlSerializationServiceImpl>()
        bind<AfraidOfWaterService, AfraidOfWaterServiceImpl>()
        bind<NavigationService, NavigationServiceImpl>()
        bind<DependencyHeadDatabaseService, DependencyHeadDatabaseServiceImpl>()

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R2) -> bind<EntityRegistrationService, EntityRegistration113R2ServiceImpl>()
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R1) -> bind<EntityRegistrationService, EntityRegistration113R1ServiceImpl>()
            version.isVersionSameOrGreaterThan(Version.VERSION_1_11_R1) -> bind<EntityRegistrationService, EntityRegistration111R1ServiceImpl>()
            else -> bind<EntityRegistrationService, EntityRegistration18R1ServiceImpl>()
        }

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R1) -> bind<ItemService, Item113R1ServiceImpl>()
            else -> bind<ItemService, Item18R1ServiceImpl>()
        }

        // Dependency resolving
        val dependencyService = DependencyServiceImpl(plugin)
        dependencyService.checkForInstalledDependencies()

        if (dependencyService.isInstalled(PluginDependency.WORLDGUARD)) {
            val version = dependencyService.getVersion(PluginDependency.WORLDGUARD)

            if (version.startsWith("5")) {
                bind<DependencyWorldGuardService, DependencyWorldGuard5Impl>()
            } else {
                bind<DependencyWorldGuardService, DependencyWorldGuard6Impl>()
            }
        }
    }

    /**
     * Binds an instance.
     */
    private inline fun <reified I> bindInstance(instance: Any) {
        this.bind(I::class.java).toInstance(instance as I)
    }

    /**
     * Binds an interface to its implementation via the custom annotation Inject.
     */
    private inline fun <reified I> bindClass() {
        this.bind<I, I>()
    }

    /**
     * Binds an interface to its implementation via the custom annotation Inject.
     */
    private inline fun <reified I, reified T> bind() {
        T::class.java.declaredConstructors.forEach { constructor ->
            constructor.declaredAnnotations.forEach { annotation ->
                if (annotation is Inject) {
                    bind(I::class.java).toConstructor(constructor as Constructor<I>).`in`(Scopes.SINGLETON)
                    return
                }
            }
        }

        if (T::class.java.declaredConstructors.size == 1) {
            bind(I::class.java).toConstructor(T::class.java.declaredConstructors[0] as Constructor<I>).`in`(Scopes.SINGLETON)
            return
        }

        throw RuntimeException("Failed to bind PetBlocks dependency! Does the binding " + T::class.java + " contain the Inject annotation?")
    }
}