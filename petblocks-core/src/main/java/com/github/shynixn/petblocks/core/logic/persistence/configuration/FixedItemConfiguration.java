package com.github.shynixn.petblocks.core.logic.persistence.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController;

import java.util.*;
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
public abstract class FixedItemConfiguration<Player> implements OtherGUIItemsController<GUIItemContainer<Player>> {
    protected final Map<String, GUIItemContainer<Player>> items = new HashMap<>();

    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    @Override
    public void store(GUIItemContainer item) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Removes an item from the repository
     *
     * @param item item
     */
    @Override
    public void remove(GUIItemContainer item) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Returns the amount of items in the repository
     *
     * @return size
     */
    @Override
    public int size() {
        return this.items.size();
    }

    /**
     * Returns all items from the repository as unmodifiableList
     *
     * @return items
     */
    @Override
    public List<GUIItemContainer<Player>> getAll() {
        return new ArrayList<>(this.items.values());
    }

    /**
     * Returns the guiItem by the given name.
     *
     * @param name name
     * @return item
     */
    @Override
    public Optional<GUIItemContainer<Player>> getGUIItemFromName(String name) {
        if (this.items.containsKey(name)) {
            return Optional.of(this.items.get(name));
        }
        return Optional.empty();
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
        this.items.clear();
    }
}
