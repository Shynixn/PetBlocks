package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.business.logic.Factory;
import com.github.shynixn.petblocks.business.logic.business.commandexecutor.PetBlockCommandExecutor;
import com.github.shynixn.petblocks.business.logic.business.commandexecutor.PetBlockReloadCommandExecutor;
import com.github.shynixn.petblocks.business.logic.business.commandexecutor.PetDataCommandExecutor;
import com.github.shynixn.petblocks.business.logic.business.entity.GuiPageContainer;
import com.github.shynixn.petblocks.business.logic.business.filter.PetBlockFilter;
import com.github.shynixn.petblocks.business.logic.business.listener.PetBlockListener;
import com.github.shynixn.petblocks.business.logic.business.listener.PetDataListener;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

import java.io.Closeable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;

/**
 * Copyright 2017 Shynixn
 * <p>
 * Do not remove this header!
 * <p>
 * Version 1.0
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017
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
public class PetBlockManager implements AutoCloseable {

    public Set<Player> carryingPet = new HashSet<>();
    public Map<Player, Integer> timeBlocked = new HashMap<>();
    public Set<Player> headDatabasePlayers = new HashSet<>();
    public Map<Player, Inventory> inventories = new HashMap<>();
    public Map<Player, GuiPageContainer> pages = new HashMap<>();
    public GUI gui;



    private PetBlockFilter filter;
    private final PetBlockController petBlockController;
    private final PetMetaController petMetaController;

    public PetBlockManager(Plugin plugin) {
        super();

        Factory.initialize(plugin);
        this.petBlockController = Factory.createPetBlockController();
        this.petMetaController = Factory.createPetDataController();
        try {
            new PetDataCommandExecutor(this);
            new PetBlockCommandExecutor(this);
            new PetBlockReloadCommandExecutor(plugin);
            new PetDataListener(this, plugin);
            new PetBlockListener(this, plugin);
            this.filter = PetBlockFilter.create();
            this.gui = new GUI(this);
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to initialize petblockmanager.", e);
        }
    }

    public PetBlockController getPetBlockController() {
        return this.petBlockController;
    }

    public PetMetaController getPetMetaController() {
        return this.petMetaController;
    }

    /**
     * Closes this resource, relinquishing any underlying resources.
     * This method is invoked automatically on objects managed by the
     * {@code try}-with-resources statement.
     * <p>
     * <p>While this interface method is declared to throw {@code
     * Exception}, implementers are <em>strongly</em> encouraged to
     * declare concrete implementations of the {@code close} method to
     * throw more specific exceptions, or to throw no exception at all
     * if the close operation cannot fail.
     * <p>
     * <p> Cases where the close operation may fail require careful
     * attention by implementers. It is strongly advised to relinquish
     * the underlying resources and to internally <em>mark</em> the
     * resource as closed, prior to throwing the exception. The {@code
     * close} method is unlikely to be invoked more than once and so
     * this ensures that the resources are released in a timely manner.
     * Furthermore it reduces problems that could arise when the resource
     * wraps, or is wrapped, by another resource.
     * <p>
     * <p><em>Implementers of this interface are also strongly advised
     * to not have the {@code close} method throw {@link
     * InterruptedException}.</em>
     * <p>
     * This exception interacts with a thread's interrupted status,
     * and runtime misbehavior is likely to occur if an {@code
     * InterruptedException} is {@linkplain Throwable#addSuppressed
     * suppressed}.
     * <p>
     * More generally, if it would cause problems for an
     * exception to be suppressed, the {@code AutoCloseable.close}
     * method should not throw it.
     * <p>
     * <p>Note that unlike the {@link Closeable#close close}
     * method of {@link Closeable}, this {@code close} method
     * is <em>not</em> required to be idempotent.  In other words,
     * calling this {@code close} method more than once may have some
     * visible side effect, unlike {@code Closeable.close} which is
     * required to have no effect if called more than once.
     * <p>
     * However, implementers of this interface are strongly encouraged
     * to make their {@code close} methods idempotent.
     *
     * @throws Exception if this resource cannot be closed
     */
    @Override
    public void close() throws Exception {
        for (final Player player : this.carryingPet) {
            NMSRegistry.setItemInHand19(player, null, true);
        }
        this.timeBlocked.clear();
        this.headDatabasePlayers.clear();
        this.inventories.clear();
        this.pages.clear();
        this.petBlockController.close();
        this.petMetaController.close();
        this.filter.close();
        this.carryingPet.clear();
    }
}
