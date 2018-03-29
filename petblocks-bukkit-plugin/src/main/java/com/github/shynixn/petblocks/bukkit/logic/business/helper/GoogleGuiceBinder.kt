package com.github.shynixn.petblocks.bukkit.logic.business.helper

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController
import com.github.shynixn.petblocks.api.persistence.controller.EngineController
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController
import com.github.shynixn.petblocks.api.persistence.controller.ParticleController
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.logic.persistence.configuration.*
import com.google.inject.AbstractModule
import com.google.inject.TypeLiteral
import com.google.inject.name.Names
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin


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

    private val typeLiteral = TypeLiteral.get(EngineController::class.java) as TypeLiteral<EngineController<EngineContainer<GUIItemContainer<Player>>, GUIItemContainer<Player>>>


    /*  private final Key<T> key;
      private final Provider<T> provider;

      public MyModule(final Class<T> clazz, final String[] args) {
          this.key = Key.get(clazz); // Or add an annotation if you feel like it
          this.provider = new Provider<T>() {
              @Override public T get() {
                  try {
                      T instance = clazz.newInstance();
                      // etc.
                  } catch (ReflectiveOperationException ex) {
                      // throw a RuntimeException here
                  }
              }
          };
      }

      @Override protected void configure() {
          bind(key).toProvider(provider).in(Singleton.class);
      }
  }*/

    override fun configure() {




        bind(OtherGUIItemsController::class.java).toInstance(BukkitStaticGUIItems())
        bind(ParticleController::class.java).toInstance(BukkitParticleConfiguration())
        bind(EngineController::class.java).toInstance(BukkitEngineConfiguration())

        bind(CostumeController::class.java).annotatedWith(Names.named("ordinary")).toInstance(BukkitCostumeConfiguration("ordinary"))
        bind(CostumeController::class.java).annotatedWith(Names.named("color")).toInstance(BukkitCostumeConfiguration("color"))
        bind(CostumeController::class.java).annotatedWith(Names.named("rare")).toInstance(BukkitCostumeConfiguration("rare"))
        bind(CostumeController::class.java).annotatedWith(Names.named("minecraft-heads")).toInstance(BukkitMinecraftConfiguration())



        bind(Config::class.java).toInstance(Config)
    }
}