package com.github.shynixn.petblocks.api.business.controller;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.controller.IController;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;

import java.util.Optional;

/**
 * Registers petblocks to manage their action and behavior.
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
public interface PetBlockController extends IController<PetBlock> {

    /**
     * Creates a new petblock for the given player and meta.
     *
     * @param player  player
     * @param petMeta meta
     * @return petblock
     */
    PetBlock create(Object player, PetMeta petMeta);

    /**
     * Returns the petblock of the given player.
     *
     * @param player player
     * @return petblock
     */
    @Deprecated
    PetBlock getByPlayer(Object player);

    /**
     * Returns the petblock of the given player.
     *
     * @param player player
     * @return petblock
     */
    Optional<PetBlock> getFromPlayer(Object player);

    /**
     * Removes the petblock of the given player.
     *
     * @param player player
     */
    void removeByPlayer(Object player);
}
