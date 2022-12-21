@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic

import com.github.shynixn.petblocks.api.legacy.business.enumeration.PluginDependency
import com.github.shynixn.petblocks.api.legacy.business.enumeration.Version
import com.github.shynixn.petblocks.api.legacy.business.proxy.PluginProxy
import com.github.shynixn.petblocks.api.legacy.business.serializer.ItemStackSerializer
import com.github.shynixn.petblocks.api.legacy.business.service.*
import com.github.shynixn.petblocks.api.legacy.persistence.context.SqlDbContext
import com.github.shynixn.petblocks.api.legacy.persistence.repository.PetMetaRepository
import com.github.shynixn.petblocks.bukkit.logic.business.serializer.ItemStackSerializerImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.business.service.*
import com.github.shynixn.petblocks.core.logic.persistence.context.SqlDbContextImpl
import com.github.shynixn.petblocks.core.logic.persistence.repository.PetMetaSqlRepository
import com.google.inject.AbstractModule
import com.google.inject.Scopes
import org.bukkit.plugin.Plugin

/**
 * Builds the dependency tree.
 */
class PetBlocksDependencyInjectionBinderOld(private val plugin: PetBlocksPluginOld) : AbstractModule() {
    /**
     * Configures the business logic tree.
     */
    override fun configure() {
        val version = plugin.getServerVersion()
        val dependencyService = DependencyServiceImpl(plugin)

        bind(Plugin::class.java).toInstance(plugin)
        bind(Version::class.java).toInstance(version)
        bind(PluginProxy::class.java).toInstance(plugin)
        bind(LoggingService::class.java).toInstance(LoggingUtilServiceImpl(plugin.logger))

        // Repositories
        bind(PetMetaRepository::class.java).to(PetMetaSqlRepository::class.java).`in`(Scopes.SINGLETON)

        // Services
        bind(SqlDbContext::class.java).to(SqlDbContextImpl::class.java).`in`(Scopes.SINGLETON)
        bind(AIService::class.java).to(AIServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CarryPetService::class.java).to(CarryPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CombatPetService::class.java).to(CombatPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(FeedingPetService::class.java).to(FeedPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(HealthService::class.java).to(HealthServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PersistencePetMetaService::class.java).to(PersistencePetMetaServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetActionService::class.java).to(PetActionServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetService::class.java).to(PetServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(UpdateCheckService::class.java).to(UpdateCheckServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(YamlSerializationService::class.java).to(YamlSerializationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(PetDebugService::class.java).to(PetDebugServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CommandService::class.java).to(CommandServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ConcurrencyService::class.java).to(ConcurrencyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(DependencyHeadDatabaseService::class.java).to(DependencyHeadDatabaseServiceImpl::class.java)
            .`in`(Scopes.SINGLETON)
        bind(DependencyService::class.java).to(DependencyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EntityService::class.java).to(EntityServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(EventService::class.java).to(EventServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GUIService::class.java).to(GUIServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ItemTypeService::class.java).to(ItemTypeServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(MessageService::class.java).to(MessageServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(NavigationService::class.java).to(NavigationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ProxyService::class.java).to(ProxyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(SoundService::class.java).to(SoundServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GUIItemLoadService::class.java).to(GUIItemLoadServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(YamlService::class.java).to(YamlServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(GUIPetStorageService::class.java).to(GUIPetStorageServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ItemStackSerializer::class.java).to(ItemStackSerializerImpl::class.java).`in`(Scopes.SINGLETON)
        bind(LocalizationService::class.java).to(LocalizationServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ProtocolService::class.java).to(ProtocolServiceImpl::class.java).`in`(Scopes.SINGLETON)

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_17_R1) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration117R1ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            version.isVersionSameOrGreaterThan(Version.VERSION_1_16_R3) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration116R3ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            version.isVersionSameOrGreaterThan(Version.VERSION_1_15_R1) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration115R1ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            version.isVersionSameOrGreaterThan(Version.VERSION_1_14_R1) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration114R1ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R2) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration113R2ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            version.isVersionSameOrGreaterThan(Version.VERSION_1_11_R1) -> bind(EntityRegistrationService::class.java).to(
                EntityRegistration111R1ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            else -> bind(EntityRegistrationService::class.java).to(EntityRegistration18R1ServiceImpl::class.java)
                .`in`(Scopes.SINGLETON)
        }

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_9_R1) -> bind(HandService::class.java).to(
                Hand19R1ServiceImpl::class.java
            ).`in`(Scopes.SINGLETON)
            else -> bind(HandService::class.java).to(Hand18R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }

        when {
            version.isVersionSameOrGreaterThan(Version.VERSION_1_13_R2) -> bind(ParticleService::class.java).to(
                Particle113R2ServiceImpl::class.java
            ).`in`(
                Scopes.SINGLETON
            )
            else -> bind(ParticleService::class.java).to(Particle18R1ServiceImpl::class.java).`in`(Scopes.SINGLETON)
        }

        if (dependencyService.isInstalled(PluginDependency.PLACEHOLDERAPI)) {
            bind(DependencyPlaceholderApiService::class.java).to(DependencyPlaceholderApiServiceImpl::class.java)
        }
    }
}
