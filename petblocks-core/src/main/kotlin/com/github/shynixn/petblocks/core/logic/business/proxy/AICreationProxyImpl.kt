package com.github.shynixn.petblocks.core.logic.business.proxy

import com.github.shynixn.petblocks.api.business.proxy.AICreationProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.YamlSerializationService
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import kotlin.reflect.KClass

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
class AICreationProxyImpl(
    private val yamlSerializationService: YamlSerializationService,
    private val clazz: KClass<*>,
    private val function: ((PetProxy, AIBase) -> Any)?
) :
    AICreationProxy<AIBase> {
    /**
     * Gets called when a pathfinder needs to be created for the given pet.
     * ReturnType can be an instance of PathfinderProxy or any NMS pathfinder.
     */
    override fun onPathfinderCreation(pet: PetProxy, aiBase: AIBase): Any? {
        if (function != null) {
            return function.invoke(pet, aiBase)
        }

        return null
    }

    /**
     *  Gets called when the the given ai Base should be serialized.
     */
    override fun onSerialization(aiBase: AIBase): Map<String, Any?> {
        return yamlSerializationService.serialize(aiBase)
    }

    /**
     * Gets called when the given aiBase should be serialized.
     */
    override fun onDeserialization(source: Map<String, Any?>): AIBase {
        return yamlSerializationService.deserialize(clazz, source)
    }
}