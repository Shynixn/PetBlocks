package com.github.shynixn.petblocks.core.logic.persistence.repository

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.persistence.repository.PetRepository

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
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
class PetRunTimeRepository : PetRepository {
    private val pets = ArrayList<PetProxy>()

    /**
     * Removes the given petProxy from being managed.
     */
    override fun remove(petProxy: PetProxy) {
        if (pets.contains(petProxy)) {
            pets.remove(petProxy)
        }
    }

    /**
     * Saves the petProxy into the repository.
     */
    override fun save(petProxy: PetProxy) {
        if (!pets.contains(petProxy)) {
            pets.add(petProxy)
        }
    }

    /**
     * Returns [List] with a list of stored [PetProxy].
     */
    override fun getAll(): List<PetProxy> {
        return pets
    }

    /**
     * Gets the pet from the uuid. Throws exception if not exist.
     */
    override fun getFromPlayerUUID(uuid: String): PetProxy {
        return pets.find { p -> !p.isDead && p.meta.playerMeta.uuid == uuid }
            ?: throw IllegalArgumentException("Pet cannot be located of uuid $uuid.")
    }

    /**
     * Gets if the given player uniqueId has got an active pet.
     */
    override fun hasPet(uuid: String): Boolean {
        return pets.firstOrNull { p -> !p.isDead && p.meta.playerMeta.uuid == uuid } != null
    }
}