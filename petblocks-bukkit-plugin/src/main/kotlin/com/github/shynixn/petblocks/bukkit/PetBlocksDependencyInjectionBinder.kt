@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit

import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.SqlConnectionPoolProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.bukkit.logic.business.extension.getServerVersion
import com.github.shynixn.petblocks.bukkit.logic.business.listener.PetListener
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.SqlProxyImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.business.service.LoggingUtilServiceImpl
import com.github.shynixn.petblocks.core.logic.business.command.*
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.EditPetCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.PlayerPetActionCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.commandexecutor.ReloadCommandExecutorImpl
import com.github.shynixn.petblocks.core.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.bukkit.plugin.Plugin

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
        val version = getServerVersion()

        // General
        bind(Plugin::class.java).toInstance(plugin)
        bind(Version::class.java).toInstance(version)
        bind(LoggingService::class.java).toInstance(LoggingUtilServiceImpl(plugin.logger))

        // Commands
        bind(EditPetAICommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetDisableCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetEnableCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetKillNextCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetRenameCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetResetCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetSkinCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetToggleCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetToggleParticleCommand::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetToggleSoundCommand::class.java).`in`(Scopes.SINGLETON)

        // CommandExecutors
        bind(ReloadCommandExecutorImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PlayerPetActionCommandExecutorImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EditPetCommandExecutorImpl::class.java).`in`(Scopes.SINGLETON)

        // Listener
        bind(PetListener::class.java).`in`(Scopes.SINGLETON)

        // Contexts
        bind(SqlConnectionPoolProxy::class.java).to(SqlProxyImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SqlDbContext::class.java).to(SqlDbContextImpl::class.java).`in`(Scopes.SINGLETON)

        // Repositories
        bind(PetMetaRepository::class.java).to(PetMetaSqlRepository::class.java).`in`(Scopes.SINGLETON)

        // Core Services
        bind(AIService::class.java).to(AIServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CarryPetService::class.java).to(CarryPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CombatPetService::class.java).to(CombatPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(FeedingPetService::class.java).to(FeedPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GUIScriptService::class.java).to(GUIScriptServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(HealthService::class.java).to(HealthServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PersistencePetMetaService::class.java).to(PersistencePetMetaServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetActionService::class.java).to(PetActionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetService::class.java).to(PetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(UpdateCheckService::class.java).to(UpdateCheckServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(YamlSerializationService::class.java).to(YamlSerializationServiceImpl::class.java).`in`(Scopes.SINGLETON)

        // Bukkit Services
        bind(AfraidOfWaterService::class.java).to(AfraidOfWaterServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CommandService::class.java).to(CommandServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ConcurrencyService::class.java).to(ConcurrencyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DependencyHeadDatabaseService::class.java).to(DependencyHeadDatabaseServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DependencyService::class.java).to(DependencyServiceImpl::class.java).`in`(Scopes.SINGLETON)

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R2) -> bind(EntityRegistrationService::class.java).to(EntityRegistration113R2ServiceImpl::class.java).`in`(Scopes.SINGLETON)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R1) -> bind(EntityRegistrationService::class.java).to(EntityRegistration113R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
            version.isVersionSameOrGreaterThan(Version.VERSION_1_11_R1) -> bind(EntityRegistrationService::class.java).to(EntityRegistration111R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
            else -> bind(EntityRegistrationService::class.java).to(EntityRegistration18R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }

        bind(EntityService::class.java).to(EntityServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EventService::class.java).to(EventServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GUIService::class.java).to(GUIServiceImpl::class.java).`in`(Scopes.SINGLETON)

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R1) -> bind(ItemService::class.java).to(Item113R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
            else -> bind(ItemService::class.java).to(Item18R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }

        bind(MessageService::class.java).to(MessageServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(NavigationService::class.java).to(NavigationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ParticleService::class.java).to(ParticleServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ProxyService::class.java).to(ProxyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SoundService::class.java).to(SoundServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(YamlConfigurationService::class.java).to(YamlConfigurationServiceImpl::class.java).`in`(Scopes.SINGLETON)

        // Dependency resolving
        val dependencyService = DependencyServiceImpl(plugin)
        dependencyService.checkForInstalledDependencies()
    }
}