package com.github.shynixn.petblocks.core.logic.persistence.configuration;

import com.github.shynixn.petblocks.api.business.entity.GUIItemContainer;
import com.github.shynixn.petblocks.api.persistence.controller.CostumeController;

import java.util.Optional;

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
public abstract class CostumeConfiguration<Player> extends ContainerConfiguration<GUIItemContainer<Player>> implements CostumeController<GUIItemContainer<Player>> {
    private final String costumeCategory;

    /**
     * Initializes a new costume repository.
     *
     * @param costumeCategory costume
     */
    public CostumeConfiguration(String costumeCategory) {
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
     * Returns the costume category.
     * @return category
     */
    public String getCostumeCategory() {
        return this.costumeCategory;
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
