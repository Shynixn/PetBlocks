package com.github.shynixn.petblocks.bukkit.logic.business.controller;

import com.github.shynixn.petblocks.api.bukkit.event.PetBlockDeathEvent;
import com.github.shynixn.petblocks.api.business.controller.PetBlockController;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.nms.NMSRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;

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
public final class PetBlockRepository implements PetBlockController {
    private final Map<Player, PetBlock> petblocks = new HashMap<>();

    /**
     * Creates a new petblock for the given player and meta.
     *
     * @param player  player
     * @param petMeta meta
     * @return petblock
     */
    @Override
    public PetBlock create(Object player, PetMeta petMeta) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        if(petMeta == null)
            throw new IllegalArgumentException("PetMeta cannot be null!");
        final Player mPlayer = (Player) player;
        return NMSRegistry.createPetBlock(mPlayer.getLocation(), petMeta);
    }

    /**
     * Returns the petblock of the given player.
     *
     * @param player player
     * @return petblock
     */
    @Override
    public PetBlock getByPlayer(Object player) {
        final Player mPlayer = (Player) player;
        if (this.petblocks.containsKey(mPlayer)) {
            return this.petblocks.get(mPlayer);
        }
        return null;
    }

    /**
     * Returns the petblock of the given player.
     *
     * @param player player
     * @return petblock
     */
    @Override
    public Optional<PetBlock> getFromPlayer(Object player) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        final Player mPlayer = (Player) player;
        if (this.petblocks.containsKey(mPlayer)) {
            return Optional.of(this.petblocks.get(mPlayer));
        }
        return Optional.empty();
    }

    /**
     * Removes the petblock of the given player.
     *
     * @param player player
     */
    @Override
    public void removeByPlayer(Object player) {
        if(player == null)
            throw new IllegalArgumentException("Player cannot be null!");
        this.remove(this.getByPlayer(player));
    }

    /**
     * Stores a new a item in the repository.
     *
     * @param item item
     */
    @Override
    public void store(PetBlock item) {
        if(item == null)
            throw new IllegalArgumentException("Item cannot be null!");
        final Player mPlayer = (Player) item.getPlayer();
        if (!this.petblocks.containsKey(mPlayer)) {
            this.petblocks.put(mPlayer, item);
        }
    }

    /**
     * Removes an item from the repository.
     *
     * @param item item
     */
    @Override
    public void remove(PetBlock item) {
        if (item == null)
            return;
        final Player player = (Player) item.getPlayer();
        if (this.petblocks.containsKey(player)) {
            final PetBlockDeathEvent event = new PetBlockDeathEvent(this.petblocks.get(player));
            Bukkit.getPluginManager().callEvent(event);
            if (!event.isCanceled()) {
                this.petblocks.get(player).remove();
                this.petblocks.remove(player);
            }
        }
    }

    /**
     * Returns the amount of items in the repository.
     *
     * @return size
     */
    @Override
    public int size() {
        return this.petblocks.size();
    }

    /**
     * Returns all items from the repository as unmodifiableList.
     *
     * @return items
     */
    @Override
    public List<PetBlock> getAll() {
        return new ArrayList<>(this.petblocks.values());
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
        for (final Player player : this.petblocks.keySet()) {
            this.petblocks.get(player).remove();
        }
        this.petblocks.clear();
    }
}
