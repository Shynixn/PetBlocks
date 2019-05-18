package com.github.shynixn.petblocks.api.business.service

import com.github.shynixn.petblocks.api.business.proxy.AICreationProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIBase

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
interface AIService {
    /**
     * Registers a custom ai type with unique [type] and a proxy to create required AI actions.
     * Existing types can be overwritten if the given [type] already exists.
     */
    fun <A : AIBase> registerAI(type: String, creator: AICreationProxy<A>)

    /**
     * Generates an AIBase from the given yaml source string.
     */
    fun <A : AIBase> deserializeAiBase(type : String, source: String): A

    /**
     * Generates an AIBase from the given yaml map data.
     */
    fun <A : AIBase> deserializeAiBase(type : String, source: Map<String, Any?>): A

    /**
     *  Serializes the given [aiBase] to a yaml string.
     */
    fun serializeAiBaseToString(aiBase: AIBase): String

    /**
     *  Serializes the given [aiBase] to a yaml map.
     */
    fun serializeAiBase(aiBase: AIBase): Map<String, Any?>

    /**
     * Generates pathfinders from the given ai bases depending on the
     * type specified in the [AIBase] and registers types of this service.
     */
    fun convertPetAiBasesToPathfinders(petProxy: PetProxy, metas: List<AIBase>): List<Any>
}