package com.github.shynixn.petblocks.bukkit.logic.business;

import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.Factory;
import com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor.PetBlockCommandExecutor;
import com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor.PetBlockReloadCommandExecutor;
import com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor.PetDataCommandExecutor;
import com.github.shynixn.petblocks.bukkit.logic.business.entity.GuiPageContainer;
import com.github.shynixn.petblocks.bukkit.logic.business.filter.PetBlockFilter;
import com.github.shynixn.petblocks.bukkit.logic.business.listener.PetBlockListener;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import com.github.shynixn.petblocks.bukkit.logic.business.listener.PetDataListener;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;

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

    public final Set<Player> carryingPet = new HashSet<>();
    public final Map<Player, Integer> timeBlocked = new HashMap<>();
    public final Set<Player> headDatabasePlayers = new HashSet<>();
    public final Map<Player, Inventory> inventories = new HashMap<>();
    public final Map<Player, GuiPageContainer> pages = new HashMap<>();
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
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to initialize petblockmanager.", e);
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
