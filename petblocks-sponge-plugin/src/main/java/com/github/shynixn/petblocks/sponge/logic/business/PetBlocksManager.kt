package com.github.shynixn.petblocks.sponge.logic.business

import com.github.shynixn.petblocks.api.business.controller.PetBlockController
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController
import com.github.shynixn.petblocks.core.logic.business.entity.GuiPageContainer
import com.github.shynixn.petblocks.core.logic.business.helper.ExtensionHikariConnectionContext
import com.github.shynixn.petblocks.sponge.logic.business.commandexecutor.PetBlockCommandExecutor
import com.github.shynixn.petblocks.sponge.logic.business.commandexecutor.PetBlockReloadCommandExecutor
import com.github.shynixn.petblocks.sponge.logic.business.commandexecutor.PetDataCommandExecutor
import com.github.shynixn.petblocks.sponge.logic.business.controller.SpongePetBlockRepository
import com.github.shynixn.petblocks.sponge.logic.business.listener.SpongePetBlockListener
import com.github.shynixn.petblocks.sponge.logic.business.listener.SpongePetDataListener
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config
import com.github.shynixn.petblocks.sponge.logic.persistence.controller.SpongePetDataRepository
import com.google.inject.Inject
import com.google.inject.Singleton
import org.slf4j.Logger
import org.spongepowered.api.Sponge
import org.spongepowered.api.config.ConfigDir
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.Inventory
import org.spongepowered.api.item.inventory.ItemStack
import org.spongepowered.api.plugin.PluginContainer
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Path
import java.sql.SQLException
import java.util.*
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
@Singleton
class PetBlocksManager : AutoCloseable {

    val carryingPet: MutableMap<Player, ItemStack> = HashMap()
    val timeBlocked: MutableMap<Player, Int> = HashMap()
    val inventories: MutableMap<Player, Inventory> = HashMap()
    val pages: MutableMap<Player, GuiPageContainer> = HashMap()

    @Inject
    lateinit var gui: GUI

    @Inject
    private lateinit var petDataCommandExecutor: PetDataCommandExecutor

    @Inject
    private lateinit var petReloadCommandExecutor: PetBlockReloadCommandExecutor

    @Inject
    private lateinit var petblockCommandExecutor: PetBlockCommandExecutor

    @Inject
    private lateinit var petDataListener: SpongePetDataListener

    @Inject
    private lateinit var petBlockListener: SpongePetBlockListener

    @Inject
    lateinit var petBlockController: SpongePetBlockRepository
        private set

    @Inject
    lateinit var petMetaController: SpongePetDataRepository
        private set

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * `try`-with-resources statement.
     * However, implementers of this interface are strongly encouraged
     * to make their `close` methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    override fun close() {
        for (player in this.carryingPet.keys) {
            //  NMSRegistry.setItemInHand19(player, null, true)
        }
        this.timeBlocked.clear()
        this.inventories.clear()
        this.pages.clear()
        this.petBlockController.close()
        this.petMetaController.close()
        this.carryingPet.clear()
    }
}