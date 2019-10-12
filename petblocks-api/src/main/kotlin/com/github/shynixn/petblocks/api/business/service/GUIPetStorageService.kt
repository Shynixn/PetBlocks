package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.persistence.entity.PetMeta

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
interface GUIPetStorageService {
    /**
     * Opens the storage of the given [petMeta] for the given player.
     * If the petMeta belongs to a different player than the given player, the
     * inventory gets opened in readOnlyMode.
     */
    fun <P> openStorage(player: P, petMeta: PetMeta, from: Int, to: Int)

    /**
     * Returns if the given [inventory] matches the storage inventory of this service.
     */
    fun <I> isStorage(inventory: I): Boolean

    /**
     * Saves the storage inventory to the database and clears all resources.
     */
    fun <P> saveStorage(player: P)
}