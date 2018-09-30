@file:Suppress("unused")

package com.github.shynixn.petblocks.sponge.logic.compatibility

import com.github.shynixn.petblocks.core.logic.compatibility.GuiPageContainer
import com.google.inject.Inject
import com.google.inject.Singleton
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.item.inventory.Inventory
import java.util.*

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
    companion object {
        /** Compatibility **/
        var petBlocksManager: PetBlocksManager? = null
    }

    val timeBlocked: MutableMap<Player, Int> = HashMap()
    val inventories: MutableMap<Player, Inventory> = HashMap()
    val pages: MutableMap<Player, GuiPageContainer> = HashMap()

    init {
        petBlocksManager = this
    }

    @Inject
    lateinit var gui: GUI

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
        this.timeBlocked.clear()
        this.inventories.clear()
        this.pages.clear()
        this.petBlockController.close()
        this.petMetaController.close()
    }
}