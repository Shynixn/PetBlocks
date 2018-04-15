package com.github.shynixn.petblocks.sponge.logic.business.helper

import com.github.shynixn.petblocks.api.persistence.controller.CostumeController
import com.github.shynixn.petblocks.api.persistence.controller.EngineController
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController
import com.github.shynixn.petblocks.api.persistence.controller.ParticleController
import com.github.shynixn.petblocks.core.logic.business.helper.ExtensionHikariConnectionContext
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.*
import com.google.inject.AbstractModule
import com.google.inject.Inject
import com.google.inject.name.Names
import ninja.leaping.configurate.ConfigurationNode
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.plugin.PluginContainer
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.sql.SQLException
import java.util.regex.Pattern

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
class GoogleGuiceBinder : AbstractModule() {
    override fun configure() {

        bind(OtherGUIItemsController::class.java).toInstance(SpongeStaticGUIItems())
        bind(ParticleController::class.java).toInstance(SpongeParticleConfiguration())
        bind(EngineController::class.java).toInstance(SpongeEngineConfiguration())

        bind(CostumeController::class.java).annotatedWith(Names.named("ordinary")).toInstance(SpongeCostumeConfiguration("ordinary"))
        bind(CostumeController::class.java).annotatedWith(Names.named("color")).toInstance(SpongeCostumeConfiguration("color"))
        bind(CostumeController::class.java).annotatedWith(Names.named("rare")).toInstance(SpongeCostumeConfiguration("rare"))
        bind(CostumeController::class.java).annotatedWith(Names.named("minecraft-heads")).toInstance(SpongeMinecraftConfiguration())

        bind(Config::class.java).toInstance(Config)
    }
}