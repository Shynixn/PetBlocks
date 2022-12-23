package com.github.shynixn.petblocks.api.legacy.business.proxy

import com.github.shynixn.petblocks.api.legacy.persistence.entity.AIBase

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
interface AICreationProxy<A : AIBase> {
    /**
     *  Gets called when the the given ai Base should be serialized.
     */
    fun onSerialization(aiBase: A): Map<String, Any?>

    /**
     * Gets called when the given aiBase should be serialized.
     */
    fun onDeserialization(source: Map<String, Any?>): A

    /**
     * Gets called when a pathfinder needs to be created for the given pet.
     * ReturnType can be an instance of PathfinderProxy or any NMS pathfinder.
     *
     * If the pathfinder instance should not be created and is managed by something else (like events) return null.
     */
    fun onPathfinderCreation(pet: PetProxy, aiBase: A): Any?
}
