package com.github.shynixn.petblocks.api.persistence.controller;

import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;

import java.util.Optional;
import java.util.UUID;

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
/**
 * @deprecated Use the PersistencePetMetaService instead.
 */
@Deprecated
public interface PetMetaController<Player> extends IDatabaseController<PetMeta> {

    /**
     * Creates a petMeta for the given player.
     *
     * @param player player
     * @return petMeta
     */
    PetMeta create(Player player);

    /**
     * Returns the petData from the given player.
     *
     * @param player player
     * @return petData
     */
    Optional<PetMeta> getFromPlayer(Player player);

    /**
     * Checks if the player has got an entry in the database.
     *
     * @param player player
     * @return hasEntry
     */
    boolean hasEntry(Player player);

    /**
     * Removes the petMeta of the given player.
     *
     * @param player player
     */
    void removeByPlayer(Player player);

    /**
     * Create from uniqueId.
     * @param uuid uniqueId.
     * @return petMeta
     */
    PetMeta createFromUUID(UUID uuid);

    /**
     * Returns the petMeta of the given uniqueId.
     *
     * @param uuid uniqueId
     * @return playerMeta
     */
    Optional<PetMeta> getFromUUID(UUID uuid);
}
