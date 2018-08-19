package com.github.shynixn.petblocks.sponge

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.controller.ParticleEffectMetaController
import com.github.shynixn.petblocks.core.logic.business.entity.DbContext
import com.github.shynixn.petblocks.core.logic.business.service.GUIScriptServiceImpl
import com.github.shynixn.petblocks.core.logic.persistence.controller.ParticleEffectDataRepository
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager
import com.github.shynixn.petblocks.sponge.logic.business.entity.SpongeDBContext
import com.github.shynixn.petblocks.sponge.logic.business.service.*
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.Scopes
import org.slf4j.Logger
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.plugin.PluginContainer
import java.nio.file.Path

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
class PetBlocksDependencyInjectionBinder : AbstractModule() {

    @Inject
    @ConfigDir(sharedRoot = false)
    private lateinit var privateConfigDir: Path

    @Inject
    private lateinit var logger: Logger

    @Inject
    private lateinit var plugin: PluginContainer

    var petBlocksManager: PetBlocksManager? = null

    override fun configure() {
        petBlocksManager = PetBlocksManager()
        bind(DbContext::class.java).toInstance(SpongeDBContext(plugin, logger, privateConfigDir))
        bind(PetBlocksManager::class.java).toInstance(petBlocksManager)

        //Bind Repositories
        bind(ParticleEffectMetaController::class.java).to(ParticleEffectDataRepository::class.java)

        // Bind Services
        bind(PersistenceService::class.java).to(PersistenceServiceImpl::class.java)
        bind(GUIScriptService::class.java).to(GUIScriptServiceImpl::class.java)
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java)
        bind(GUIService::class.java).to(GUIServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(ParticleService::class.java).to(ParticleServiceImpl::class.java)
        bind(SoundService::class.java).to(SoundServiceImpl::class.java)
        bind(UpdateCheckService::class.java).to(UpdateCheckServiceImpl::class.java)
        bind(EntityService::class.java).to(EntityServiceImpl::class.java)
        bind(MessageService::class.java).to(MessageServiceImpl::class.java)
        bind(FeedingPetService::class.java).to(FeedPetServiceImpl::class.java)
        bind(DependencyService::class.java).to(DependencyServiceImpl::class.java).`in`(Scopes.SINGLETON)
        bind(CarryPetService::class.java).to(CarryPetServiceImpl::class.java).`in`(Scopes.SINGLETON)
    }
}