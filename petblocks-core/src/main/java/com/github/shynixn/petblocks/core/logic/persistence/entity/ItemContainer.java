package com.github.shynixn.petblocks.core.logic.persistence.entity;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.business.enumeration.ChatColor;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;

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
public abstract class ItemContainer<Player> implements GUIItemContainer<Player> {
    private boolean enabled;
    private int position = -1;
    private GUIPage page;
    private int id;
    private int damage;
    private String skin;
    private boolean unbreakable;
    private String name;
    private String[] lore;
    private String executable;

    /**
     * Initializes a new itemContainer
     *
     * @param enabled     enabled
     * @param position    position
     * @param page        page
     * @param id          id
     * @param damage      damage
     * @param skin        skin
     * @param unbreakable unbreakablöe
     * @param name        name
     * @param lore        lore
     */
    public ItemContainer(boolean enabled, int position, GUIPage page, int id, int damage, String skin, boolean unbreakable, String name, String[] lore) {
        super();
        this.enabled = enabled;
        this.position = position;
        this.page = page;
        this.id = id;
        this.damage = damage;
        this.skin = skin;
        this.unbreakable = unbreakable;
        this.name = name;
        this.lore = lore.clone();
    }

    /**
     * Initializes a new itemContainer
     *
     * @param orderNumber orderNumber
     * @param data        data
     * @throws Exception exception
     */
    public ItemContainer(int orderNumber, Map<String, Object> data) throws Exception {
        super();
        this.position = orderNumber;
        this.enabled = !data.containsKey("enabled") || (boolean) data.get("enabled");
        if (data.containsKey("position"))
            this.position = (int) data.get("position");
        if (data.containsKey("page"))
            this.page = GUIPage.valueOf((String) data.get("page"));
        if (data.containsKey("id"))
            this.id = (int) data.get("id");
        if (data.containsKey("damage"))
            this.damage = (int) data.get("damage");
        if (data.containsKey("skin") && !data.get("skin").equals("none"))
            this.skin = (String) data.get("skin");
        if (data.containsKey("name")) {
            if (data.get("name").equals("default")) {
                this.name = null;
            } else if (data.get("name").equals("none")) {
                this.name = " ";
            } else {
                this.name = ChatColor.Companion.translateChatColorCodes('&', (String) data.get("name"));
            }
        }

        if (data.containsKey("script")) {
            this.executable = (String) data.get("script");
        }

        if (data.containsKey("unbreakable"))
            this.unbreakable = (boolean) data.get("unbreakable");
        if (data.containsKey("lore")) {
            final List<String> m = (List<String>) data.get("lore");
            if (m != null) {
                final List<String> lore = new ArrayList<>();
                for (final String s : m) {
                    if (!s.equals("none"))
                        lore.add(ChatColor.Companion.translateChatColorCodes('&', s));
                }
                this.lore = lore.toArray(new String[lore.size()]);
            }
        }
    }

    public void setDisplayName(String displayName) {
        this.name = displayName;
    }

    /**
     * Returns if the itemContainer is enabled
     *
     * @return enabled
     */
    @Override
    public boolean isEnabled() {
        return this.enabled;
    }

    /**
     * Sets the itemContainer enabled
     *
     * @param enabled enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the guiPage of this container
     *
     * @return guiPage
     */
    @Override
    public GUIPage getPage() {
        return this.page;
    }

    /**
     * Returns the skin of the itemStack
     *
     * @return skin
     */
    @Override
    public String getSkin() {
        return this.skin;
    }

    /**
     * Returns the id of the item
     *
     * @return itemId
     */
    @Override
    public int getItemId() {
        return this.id;
    }

    /**
     * Returns the damage of the item
     *
     * @return itemDamage
     */
    @Override
    public int getItemDamage() {
        return this.damage;
    }

    /**
     * Returns if the item is unbreakable
     *
     * @return unbreakable
     */
    @Override
    public boolean isItemUnbreakable() {
        return this.unbreakable;
    }

    /**
     * Returns the displayName of the itemStack if present
     *
     * @return displayName
     */
    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(this.name);
    }

    /**
     * Returns the lore of the itemStack if present.
     *
     * @return lore
     */
    @Override
    public Optional<String[]> getLore() {
        return Optional.ofNullable(this.lore);
    }

    /**
     * Returns the position of the itemStack in the ui
     *
     * @return position
     */
    @Override
    public int getPosition() {
        return this.position;
    }

    /**
     * Returns the optional executable script.
     *
     * @return script
     */
    @Override
    public Optional<String> getExecuteableScript() {
        return Optional.ofNullable(this.executable);
    }
}
