package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.*
import com.github.shynixn.petblocks.bukkit.logic.business.extension.findClazz
import com.github.shynixn.petblocks.bukkit.logic.business.proxy.PathfinderProxyImpl
import com.google.inject.Inject
import org.bukkit.GameMode
import org.bukkit.Location
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
    private val soundService: SoundService,
    private val loggingService: LoggingService,
    private val afraidOfWaterService: AfraidOfWaterService,
    private val navigationService: NavigationService
) : AIService {

    private val getHandleMethod = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!

    /**
     * Applies an ai goal to a pet and performs all checking actions.
     */
    override fun applyAIGoalToPet(petProxy: PetProxy, goal: AIBase) {
        if (goal is AIFloatInWater) {
            val pathfinderGoal = findClazz("net.minecraft.server.VERSION.PathfinderGoalFloat")
                .getDeclaredConstructor(findClazz("net.minecraft.server.VERSION.EntityInsentient"))
                .newInstance(getHandleMethod.invoke(petProxy.getHitBoxLivingEntity<LivingEntity>()))

            petProxy.addPathfinder(pathfinderGoal)
            return
        }

        val pathfinder = PathfinderProxyImpl(plugin, goal)
        val hitBox = petProxy.getHitBoxLivingEntity<LivingEntity>()
        val owner = petProxy.getPlayer<Player>()

        if (goal is AIAfraidOfWater) {
            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR && afraidOfWaterService.isPetInWater(petProxy)
            }

            pathfinder.onExecute = {
                afraidOfWaterService.escapeWater(petProxy, goal)
            }
        }

        if (goal is AIAmbientSound) {
            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR
            }

            pathfinder.onExecute = {
                if (Math.random() > 0.99) {
                    soundService.playSound(hitBox.location, goal.sound, hitBox.world.players)
                }
            }
        }

        if (goal is AIFollowBack) {
            if (petProxy.pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } != null) {
                loggingService.warn("The pet of player " + owner.name + " tried to apply AI [follow-back] again.")
                loggingService.warn("Remove the duplicate definition in your configuration.")
                return
            }

            if (petProxy.pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } != null) {
                loggingService.warn("The pet of player " + owner.name + " tried to apply AI [follow-back] even though [follow-owner] was already applied.")
                loggingService.warn("Remove one of the definitions in your configuration.")
                return
            }

            pathfinder.shouldGoalBeExecuted = {
                !petProxy.isDead && owner.gameMode != GameMode.SPECTATOR
            }

            pathfinder.onExecute = {
                val location = owner.location
                val targetLocation = Location(location.world,
                    (location.x + (-1 * Math.cos(Math.toRadians(location.yaw + 90.0)))),
                    location.y,
                    location.z + (-1 * Math.sin(Math.toRadians(location.yaw + 90.0))),
                    location.yaw,
                    location.pitch)

                petProxy.teleport(targetLocation)
            }
        }

        if (goal is AIFollowOwner) {
            if (petProxy.pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowOwner } != null) {
                loggingService.warn("The pet of player " + owner.name + " tried to apply AI [follow-owner] again.")
                loggingService.warn("Remove the duplicate definition in your configuration.")
                return
            }

            if (petProxy.pathfinders.singleOrNull { p -> p is PathfinderProxy && p.aiBase is AIFollowBack } != null) {
                loggingService.warn("The pet of player " + owner.name + " tried to apply AI [follow-owner] even though [follow-back] was already applied.")
                loggingService.warn("Remove one of the definitions in your configuration.")
                return
            }

            var lastLocation: Location? = null

            pathfinder.shouldGoalContinueExecuting = {
                when {
                    owner.location.distance(hitBox.location) > goal.maxRange -> {
                        petProxy.teleport(owner.location)
                        false
                    }

                    owner.location.distance(hitBox.location) < goal.distanceToOwner -> false
                    else -> !(lastLocation != null && lastLocation!!.distance(owner.location) > 2)
                }
            }

            pathfinder.shouldGoalBeExecuted = {
                !hitBox.isDead && owner.gameMode != GameMode.SPECTATOR && owner.location.distance(hitBox.location) >= goal.distanceToOwner
            }

            pathfinder.onStopExecuting = {
                navigationService.clearNavigation(petProxy)
            }

            pathfinder.onStartExecuting = {
                lastLocation = owner.location.clone()
                navigationService.navigateToLocation(petProxy, owner.location, goal.speed)
            }
        }

        petProxy.addPathfinder(pathfinder)
    }
}