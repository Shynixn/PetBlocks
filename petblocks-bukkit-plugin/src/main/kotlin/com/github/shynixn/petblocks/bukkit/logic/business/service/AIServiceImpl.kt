@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import api.business.proxy.AICreationProxy
import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.deserializeToMap
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.PathfinderProxyImpl
import com.github.shynixn.petblocks.core.logic.business.proxy.AICreationProxyImpl
import com.google.inject.Inject
import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin

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
    private val plugin: Plugin,
    private val loggingService: LoggingService,
    private val yamlSerializationService: YamlSerializationService
) : AIService {

    private val getHandleMethod = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!

    private val registeredAIS = HashMap<String, AICreationProxy<AIBase>>()

    init {
        val afraidOfWaterService: AfraidOfWaterService = PetBlocksApi.resolve(AfraidOfWaterService::class)
        val navigationService: NavigationService = PetBlocksApi.resolve(NavigationService::class)
        val soundService: SoundService = PetBlocksApi.resolve(SoundService::class)

        this.register<AIFloatInWater>(AIType.FLOAT_IN_WATER) { pet, _ ->
            findClazz("net.minecraft.server.VERSION.PathfinderGoalFloat")
                .getDeclaredConstructor(findClazz("net.minecraft.server.VERSION.EntityInsentient"))
                .newInstance(getHandleMethod.invoke(pet.getHitBoxLivingEntity<LivingEntity>()))
        }

        this.register<AIAfraidOfWater>(AIType.AFRAID_OF_WATER) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(plugin, aiBase)
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>()
            val owner = pet.getPlayer<Player>()

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR && afraidOfWaterService.isPetInWater(pet)
            }

            pathfinder.onExecute = {
                afraidOfWaterService.escapeWater(pet, aiBase)
            }

            pathfinder
        }

        this.register<AIAmbientSound>(AIType.AMBIENT_SOUND) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(plugin, aiBase)
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>()
            val owner = pet.getPlayer<Player>()

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR
            }

            pathfinder.onExecute = {
                if (Math.random() > 0.99) {
                    soundService.playSound(hitBox.location, aiBase.sound, hitBox.world.players)
                }
            }

            pathfinder
        }

        this.register<AIFollowBack>(AIType.FOLLOW_BACK) { pet, aiBase ->
            val pathfinder = PathfinderProxyImpl(plugin, aiBase)
            val owner = pet.getPlayer<Player>()

            pathfinder.shouldGoalBeExecuted = {
                !pet.isDead && owner.gameMode != GameMode.SPECTATOR
            }

            pathfinder.onExecute = {
                val location = owner.location
                val targetLocation = Location(location.world,
                    (location.x + (-1 * Math.cos(Math.toRadians(location.yaw + 90.0)))),
                    location.y,
                    location.z + (-1 * Math.sin(Math.toRadians(location.yaw + 90.0))),
                    location.yaw,
                    location.pitch)

                pet.teleport(targetLocation)
            }

            pathfinder
        }

        this.register<AIFollowOwner>(AIType.FOLLOW_BACK) { pet, aiBase ->
            var lastLocation: Location? = null
            val pathfinder = PathfinderProxyImpl(plugin, aiBase)
            val owner = pet.getPlayer<Player>()
            val hitBox = pet.getHitBoxLivingEntity<LivingEntity>()

            pathfinder.shouldGoalContinueExecuting = {
                when {
                    owner.location.distance(hitBox.location) > aiBase.maxRange -> {
                        pet.teleport(owner.location)
                        false
                    }

                    owner.location.distance(hitBox.location) < aiBase.distanceToOwner -> false
                    else -> !(lastLocation != null && lastLocation!!.distance(owner.location) > 2)
                }
            }

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR && owner.location.distance(hitBox.location) >= aiBase.distanceToOwner
            }

            pathfinder.onStopExecuting = {
                navigationService.clearNavigation(pet)
            }

            pathfinder.onStartExecuting = {
                lastLocation = owner.location.clone()
                navigationService.navigateToLocation(pet, owner.location, aiBase.speed)
            }

            pathfinder
        }
    }

    /**
     * Generates pathfinders from the given ai bases depending on the
     * type specified in the [AIBase] and registers types of this service.
     */
    override fun convertPetAiBasesToPathfinders(petProxy: PetProxy, metas: List<AIBase>): List<Any> {
        val player = petProxy.getPlayer<Player>()
        val pathfinders = ArrayList<Any>()

        for (meta in metas) {
            if (!registeredAIS.containsKey(meta.type)) {
                loggingService.warn("Pet of ${player.name} tried to use ai type + " + meta.type + " which is not registered in the AI Service. Please register it first.")
            }

            val aiCreation = registeredAIS[meta.type]!!
            val pathfinder = aiCreation.onPathfinderCreation(petProxy, meta)

            pathfinders.add(pathfinder)
        }

        if (pathfinders.count { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } > 1) {
            loggingService.warn("Pet of ${player.name} tried to apply ai follow-back atleast twice. Please check your configuration!")

            val resultPathfinder = pathfinders.first { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            pathfinders.add(resultPathfinder)
        }

        if (pathfinders.count { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } > 1) {
            loggingService.warn("Pet of ${player.name} tried to apply ai follow-owner atleast twice. Please check your configuration!")

            val resultPathfinder = pathfinders.first { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner }
            pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner }
            pathfinders.add(resultPathfinder)
        }

        if (pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } != null) {
            if (pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } != null) {
                loggingService.warn("Pet of ${player.name} tried to apply both follow-owner and follow-back. Please check your configuration!")
                pathfinders.removeAll { p -> p is PathfinderProxy && p.aiBase is AIFollowBack }
            }
        }

        return pathfinders
    }

    /**
     * Registers a custom ai type with unique [type] and a proxy to create required AI actions.
     * Existing types can be overwritten if the given [type] already exists.
     */
    override fun <A : AIBase> register(type: String, creator: AICreationProxy<A>) {
        registeredAIS[type] = creator as AICreationProxy<AIBase>
    }

    /**
     * Generates an AIBase from the given yaml source string.
     */
    override fun <A : AIBase> deserializeAiBase(type: String, source: String): A {
        val yamlSerializer = YamlConfiguration()
        yamlSerializer.loadFromString(source)
        return deserializeAiBase(type, yamlSerializer.deserializeToMap("a")) as A
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
        val yamlSerializer = YamlConfiguration()
        yamlSerializer.set("a", serializeAiBase(aiBase))
        return yamlSerializer.saveToString()
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

    /**
     * Registers a default ai type.
     */
    private fun <A : AIBase> register(aiType: AIType, function: (PetProxy, A) -> Any) {
        this.register(aiType.type, AICreationProxyImpl(yamlSerializationService, aiType.aiClazz, function as (PetProxy, AIBase) -> Any))
    }
}