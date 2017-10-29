package com.github.shynixn.petblocks.api.business.entity;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;

import java.util.Optional;

/**
 * Holds general info about the UI items.
 * <p>
 * Version 1.1
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2017 by Shynixn
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
public interface GUIItemContainer {

    /**
     * Returns if the itemContainer is enabled.
     *
     * @return enabled
     */
    boolean isEnabled();

    /**
     * Sets the itemContainer enabled.
     *
     * @param enabled enabled
     */
    void setEnabled(boolean enabled);

    /**
     * Generates a new itemStack for the player and his permissions.
     *
     * @param player      player
     * @param permissions permission
     * @return itemStack
     */
    Object generate(Object player, String... permissions);

    /**
     * Returns the displayName of the itemStack if present.
     *
     * @return displayName
     */
    Optional<String> getDisplayName();

    /**
     * Returns the lore of the itemStack if present.
     *
     * @return lore
     */
    Optional<String[]> getLore();

    /**
     * Returns the position of the itemStack in the ui.
     *
     * @return position
     */
    int getPosition();

    /**
     * Returns the guiPage of this container.
     *
     * @return guiPage
     */
    GUIPage getPage();

    /**
     * Returns the skin of the itemStack.
     * @return skin
     */
    String getSkin();

    /**
     * Returns the id of the item.
     * @return itemId
     */
    int getItemId();

    /**
     * Returns the damage of the item.
     * @return itemDamage
     */
    int getItemDamage();

    /**
     * Returns if the item is unbreakable.
     * @return unbreakable
     */
    boolean isItemUnbreakable();
}
