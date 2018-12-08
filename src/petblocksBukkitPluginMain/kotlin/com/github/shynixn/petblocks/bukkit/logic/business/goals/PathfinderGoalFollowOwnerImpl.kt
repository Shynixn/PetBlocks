package com.github.shynixn.petblocks.bukkit.logic.business.goals

import org.bukkit.GameMode
import org.bukkit.Location
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player

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
class PathfinderGoalFollowOwnerImpl(private val player: Player, private val livingEntity: LivingEntity) : PathfinderBaseGoal() {
    private var lastLocation: Location? = null

    private val speed = 2.5
    private val distanceNearby = 3

    /**
     * Gets if the goal should be currently executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !livingEntity.isDead && player.gameMode != GameMode.SPECTATOR && player.location.distance(livingEntity.location) >= distanceNearby
    }

    /**
     * Gets the condition when the goal has been reached or cancelled.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        if (player.location.distance(livingEntity.location) < distanceNearby) {
            return false
        } else if (lastLocation != null && lastLocation!!.distance(player.location) > 2) {
            return false
        }

        return true
    }

    /**
     * Gets called when the goal stops getting executed.
     */
    override fun onStopExecuting() {
        this.clearNavigation(livingEntity)
    }

    /**
     * Gets called when the goal gets started.
     */
    override fun onStartExecuting() {
        lastLocation = player.location.clone()
        navigateTo(livingEntity, player.location, speed)
    }
}