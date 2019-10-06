@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.proxy.AICreationProxy
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.AIService
import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.business.service.YamlService
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowBack
import com.github.shynixn.petblocks.api.persistence.entity.AIFollowOwner
import com.google.inject.Inject

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
class AIServiceImpl @Inject constructor(
    private val loggingService: LoggingService,
    private val proxyService: ProxyService,
    private val yamlService: YamlService
) : AIService {

    private val registeredAIS = HashMap<String, AICreationProxy<AIBase>>()

    /**
     * Generates pathfinders from the given ai bases depending on the
     * type specified in the [AIBase] and registers types of this service.
     */
    override fun convertPetAiBasesToPathfinders(petProxy: PetProxy, metas: List<AIBase>): List<Any> {
        val player = petProxy.getPlayer<Any>()
        val playerName = proxyService.getPlayerName(player)
        val pathfinders = ArrayList<Any>()

        if (!petProxy.getHitBoxLivingEntity<Any>().isPresent) {
            return pathfinders
        }

        for (meta in metas) {
            if (!registeredAIS.containsKey(meta.type)) {
                loggingService.warn("Pet of $playerName tried to use ai type + " + meta.type + " which is not registered in the AI Service. Please registerAI it first.")
            }

            val aiCreation = registeredAIS[meta.type]!!
            val pathfinder = aiCreation.onPathfinderCreation(petProxy, meta)

            if (pathfinder != null) {
                pathfinders.add(pathfinder)
            }
        }

        if (pathfinders.count { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } > 1) {
            loggingService.warn("Pet of $playerName} tried to apply ai follow-back atleast twice. Please check your configuration!")

            val resultPathfinder = pathfinders.first { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            pathfinders.add(resultPathfinder)
        }

        if (pathfinders.count { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } > 1) {
            loggingService.warn("Pet of $playerName tried to apply ai follow-owner atleast twice. Please check your configuration!")

            val resultPathfinder = pathfinders.first { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner }
            pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner }
            pathfinders.add(resultPathfinder)
        }

        if (pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } != null) {
            if (pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } != null) {
                loggingService.warn("Pet of $playerName tried to apply both follow-owner and follow-back. Please check your configuration!")
                pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            }
        }

        return pathfinders
    }

    /**
     * Registers a custom ai type with unique [type] and a proxy to create required AI actions.
     * Existing types can be overwritten if the given [type] already exists.
     */
    override fun <A : AIBase> registerAI(type: String, creator: AICreationProxy<A>) {
        registeredAIS[type] = creator as AICreationProxy<AIBase>
    }

    /**
     * Generates an AIBase from the given yaml source string.
     */
    override fun <A : AIBase> deserializeAiBase(type: String, source: String): A {
        val serializedContent = yamlService.readFromString(source)

        // Compatibility to 8.0.1.
        return if (serializedContent.containsKey("a")) {
            deserializeAiBase(type, serializedContent["a"] as Map<String, Any?>)
        } else {
            deserializeAiBase(type, serializedContent)
        }
    }

    /**
     * Generates an AIBase from the given yaml map data.
     */
    override fun <A : AIBase> deserializeAiBase(type: String, source: Map<String, Any?>): A {
        if (!registeredAIS.containsKey(type)) {
            throw IllegalArgumentException("AIBase $type is not registered.")
        }

        val creationProxy = registeredAIS[type]!!
        return creationProxy.onDeserialization(source) as A
    }

    /**
     *  Serializes the given [aiBase] to a yaml string.
     */
    override fun serializeAiBaseToString(aiBase: AIBase): String {
        val serializedContent = serializeAiBase(aiBase)
        return yamlService.writeToString(serializedContent)
    }

    /**
     *  Serializes the given [aiBase] to a yaml map.
     */
    override fun serializeAiBase(aiBase: AIBase): Map<String, Any?> {
        if (!registeredAIS.containsKey(aiBase.type)) {
            throw IllegalArgumentException("AIBase " + aiBase.type + " is not registered.")
        }

        val creationProxy = registeredAIS[aiBase.type]!!
        return creationProxy.onSerialization(aiBase)
    }
}