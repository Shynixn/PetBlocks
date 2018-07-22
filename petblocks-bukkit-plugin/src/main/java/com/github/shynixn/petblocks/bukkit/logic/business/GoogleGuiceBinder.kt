package com.github.shynixn.petblocks.bukkit.logic.business

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController
import com.github.shynixn.petblocks.api.persistence.controller.EngineController
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController
import com.github.shynixn.petblocks.api.persistence.controller.ParticleController
import com.github.shynixn.petblocks.bukkit.logic.Factory
import com.github.shynixn.petblocks.bukkit.logic.business.helper.LoggingBridge
import com.github.shynixn.petblocks.bukkit.logic.business.service.ConfigurationServiceImpl
import com.github.shynixn.petblocks.core.logic.business.service.GUIScriptServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.GUIServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.ParticleServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.business.service.PersistenceServiceImpl
import com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.*
import com.google.inject.AbstractModule
import com.google.inject.name.Names
import org.bukkit.Bukkit
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.PluginManager
import org.slf4j.Logger


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
class GoogleGuiceBinder(private val plugin: Plugin) : AbstractModule() {

    override fun configure() {
        bind(Plugin::class.java).toInstance(plugin)
        Factory.initialize(plugin)

        bind(PluginManager::class.java).toInstance(Bukkit.getServer().pluginManager)
        bind(ParticleService::class.java).to(ParticleServiceImpl::class.java)
        bind(PersistenceService::class.java).toInstance(PersistenceServiceImpl(plugin, Factory.createPetBlockController(), Factory.createPetDataController()))

        val guiItems = BukkitStaticGUIItems()
        bind(BukkitStaticGUIItems::class.java).toInstance(guiItems) // Compatibility reasons.
        bind(OtherGUIItemsController::class.java).toInstance(guiItems)

        bind(ParticleController::class.java).toInstance(BukkitParticleConfiguration())
        bind(EngineController::class.java).toInstance(BukkitEngineConfiguration())
        bind(Logger::class.java).toInstance(LoggingBridge(plugin.logger))

        bind(CostumeController::class.java).annotatedWith(Names.named("ordinary")).toInstance(BukkitCostumeConfiguration("ordinary"))
        bind(CostumeController::class.java).annotatedWith(Names.named("color")).toInstance(BukkitCostumeConfiguration("color"))
        bind(CostumeController::class.java).annotatedWith(Names.named("rare")).toInstance(BukkitCostumeConfiguration("rare"))

        bind(Config::class.java).toInstance(Config)

        bind(GUIScriptService::class.java).to(GUIScriptServiceImpl::class.java)
        bind(ConfigurationService::class.java).to(ConfigurationServiceImpl::class.java)
        bind(GUIService::class.java).to(GUIServiceImpl::class.java)
    }
}