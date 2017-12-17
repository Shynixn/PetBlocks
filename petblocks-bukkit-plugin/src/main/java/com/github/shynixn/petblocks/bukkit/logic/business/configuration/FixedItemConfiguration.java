package com.github.shynixn.petblocks.bukkit.logic.business.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.OtherGUIItemsController;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.entity.ItemContainer;
import org.bukkit.ChatColor;
import org.bukkit.configuration.MemorySection;
import org.bukkit.inventory.ItemStack;
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
public class FixedItemConfiguration implements OtherGUIItemsController {

    private Plugin plugin;
    private final Map<String, GUIItemContainer> items = new HashMap<>();

    /**
     * Initializes a new engine repository
     *
     * @param plugin plugin
     */
    public FixedItemConfiguration(Plugin plugin) {
        super();
        if (plugin == null)
            throw new IllegalArgumentException("Plugin cannot be null!");
        this.plugin = plugin;
    }

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
    public List<GUIItemContainer> getAll() {
        return new ArrayList<>(this.items.values());
    }

    /**
     * Reloads the content from the fileSystem
     */
    @Override
    public void reload() {
        this.items.clear();
        this.plugin.reloadConfig();
        final Map<String, Object> data = ((MemorySection) this.plugin.getConfig().get("gui.items")).getValues(false);
        for (final String key : data.keySet()) {
            try {
                final GUIItemContainer container = new ItemContainer(0, ((MemorySection) data.get(key)).getValues(false));
                if (key.equals("suggest-heads")) {
                    ((ItemContainer) container).setDisplayName(ChatColor.AQUA + "" + ChatColor.BOLD + "Suggest Heads");
                }
                this.items.put(key, container);
            } catch (final Exception e) {
                PetBlocksPlugin.logger().log(Level.WARNING, "Failed to load guiItem " + key + '.', e);
            }
        }
    }

    /**
     * Returns the guiItem by the given name
     *
     * @param name name
     * @return item
     */
    @Override
    @Deprecated
    public GUIItemContainer getGUIItemByName(String name) {
        if (this.items.containsKey(name))
            return this.items.get(name);
        return null;
    }

    /**
     * Returns the guiItem by the given name.
     *
     * @param name name
     * @return item
     */
    @Override
    public Optional<GUIItemContainer> getGUIItemFromName(String name) {
        if (this.items.containsKey(name)) {
            return Optional.of(this.items.get(name));
        }
        return Optional.empty();
    }

    /**
     * Returns if the given itemStack is a guiItemStack with the given name
     *
     * @param itemStack itemStack
     * @param name      name
     * @return itemStack
     */
    @Override
    public boolean isGUIItem(Object itemStack, String name) {
        if (itemStack == null || name == null)
            return false;
        final GUIItemContainer container = this.getGUIItemByName(name);
        final ItemStack mItemStack = (ItemStack) itemStack;
        return mItemStack.getItemMeta() != null && container.getDisplayName().isPresent()
                && mItemStack.getItemMeta().getDisplayName() != null
                && mItemStack.getItemMeta().getDisplayName().equalsIgnoreCase(container.getDisplayName().get());
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
