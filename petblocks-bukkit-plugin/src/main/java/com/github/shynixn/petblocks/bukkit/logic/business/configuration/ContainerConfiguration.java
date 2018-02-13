package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.persistence.controller.ContainerController;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
public abstract class ContainerConfiguration<T> implements ContainerController<T> {
    protected Plugin plugin;
    protected final List<T> items = new ArrayList<>();

    /**
     * Initializes a new container repository.
     *
     * @param plugin plugin
     */
    ContainerConfiguration(Plugin plugin) {
        super();
        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be null!");
        this.plugin = plugin;
    }

    /**
     * Removes an item from the repository.
     *
     * @param item item
     */
    @Override
    public void remove(T item) {
        if (item != null && this.items.contains(item)) {
            this.items.remove(item);
        }
    }

    /**
     * Returns the amount of items in the repository.
     *
     * @return size
     */
    @Override
    public int size() {
        return this.items.size();
    }

    /**
     * Returns all items from the repository as unmodifiableList.
     *
     * @return items
     */
    @Override
    public List<T> getAll() {
        return Collections.unmodifiableList(this.items);
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
        this.plugin = null;
        this.items.clear();
    }
}
