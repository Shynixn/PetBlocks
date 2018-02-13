package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.entity.ItemContainer;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

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
public class CostumeConfiguration extends ContainerConfiguration<GUIItemContainer<Player>> implements CostumeController<GUIItemContainer<Player>> {
    private final String costumeCategory;

    /**
     * Initializes a new costume repository.
     *
     * @param costumeCategory costume
     * @param plugin          plugin
     */
    public CostumeConfiguration(String costumeCategory, Plugin plugin) {
        super(plugin);
        if (costumeCategory == null)
            throw new IllegalArgumentException("CostumeCategory cannot be null!");
        this.costumeCategory = costumeCategory;
    }

    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    @Override
    public void store(GUIItemContainer<Player> item) {
        if (item != null) {
            if (this.getContainerFromPosition(item.getPosition()).isPresent()) {
                throw new IllegalArgumentException("Item at this position already exists!");
            }
            this.items.add(item);
        }
    }

    /**
     * Reloads the content from the fileSystem
     */
    @Override
    public void reload() {
        this.items.clear();
        this.plugin.reloadConfig();
        final Map<String, Object> data = ((MemorySection) this.plugin.getConfig().get("wardrobe." + this.costumeCategory)).getValues(false);
        for (final String key : data.keySet()) {
            try {
                final GUIItemContainer<Player> container = new ItemContainer(Integer.parseInt(key), ((MemorySection) data.get(key)).getValues(true));
                this.items.add(container);
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load guiItem " + this.costumeCategory + '.' + key + '.');
            }
        }
    }

    /**
     * Returns the container by the given order id.
     *
     * @param id id
     * @return container
     */
    @Override
    public Optional<GUIItemContainer<Player>> getContainerFromPosition(int id) {
        for (final GUIItemContainer<Player> guiItemContainer : this.getAll()) {
            if (guiItemContainer.getPosition() == id) {
                return Optional.of(guiItemContainer);
            }
        }
        return Optional.empty();
    }
}
