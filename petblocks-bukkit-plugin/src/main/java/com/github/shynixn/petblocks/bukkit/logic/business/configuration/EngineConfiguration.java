package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.EngineController;
import com.github.shynixn.petblocks.api.persistence.entity.EngineContainer;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.persistence.entity.EngineData;
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
public class EngineConfiguration extends ContainerConfiguration<EngineContainer<GUIItemContainer<Player>>> implements EngineController<EngineContainer<GUIItemContainer<Player>>, GUIItemContainer<Player>> {
    /**
     * Initializes a new engine repository
     *
     * @param plugin plugin
     */
    public EngineConfiguration(Plugin plugin) {
        super(plugin);
    }

    /**
     * Stores a new a item in the repository
     *
     * @param item item
     */
    @Override
    public void store(EngineContainer<GUIItemContainer<Player>> item) {
        if (item != null && !this.items.contains(item)) {
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
        final Map<String, Object> data = ((MemorySection) this.plugin.getConfig().get("engines")).getValues(false);
        for (final String key : data.keySet()) {
            final Map<String, Object> content = ((MemorySection) this.plugin.getConfig().get("engines." + key)).getValues(true);
            try {
                this.items.add(new EngineData(Integer.parseInt(key), content));
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to add content " + key + '.', e);
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
    public Optional<EngineContainer<GUIItemContainer<Player>>> getContainerFromPosition(int id) {
        for (final EngineContainer<GUIItemContainer<Player>> container : this.getAll()) {
            if (container.getId() == id) {
                return Optional.of(container);
            }
        }
        return Optional.empty();
    }

    /**
     * Returns all gui items.
     *
     * @return gui items
     */
    @Override
    public List<GUIItemContainer<Player>> getAllGUIItems() {
        final List<GUIItemContainer<Player>> items = new ArrayList<>();
        for (final EngineContainer<GUIItemContainer<Player>> container : this.getAll()) {
            items.add(container.getGUIItem());
        }
        return items;
    }
}
