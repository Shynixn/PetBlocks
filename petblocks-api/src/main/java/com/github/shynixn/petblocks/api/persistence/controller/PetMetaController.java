package com.github.shynixn.petblocks.api.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;

import java.util.Optional;

/**
 * Controller for the pet properties and meta data.
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
public interface PetMetaController extends IDatabaseController<PetMeta> {

    /**
     * Creates a petMeta for the given player.
     *
     * @param player player
     * @return petMeta
     */
    PetMeta create(Object player);

    /**
     * Returns the petData from the given player.
     *
     * @param player player
     * @param <T>    type
     * @return petData
     */
    @Deprecated
    <T> PetMeta getByPlayer(T player);

    /**
     * Returns the petData from the given player.
     *
     * @param player player
     * @param <T>    type
     * @return petData
     */
    <T> Optional<PetMeta> getFromPlayer(T player);

    /**
     * Checks if the player has got an entry in the database.
     *
     * @param player player
     * @param <T>    type
     * @return hasEntry
     */
    <T> boolean hasEntry(T player);

    /**
     * Removes the petMeta of the given player.
     *
     * @param player player
     */
    void removeByPlayer(Object player);
}
