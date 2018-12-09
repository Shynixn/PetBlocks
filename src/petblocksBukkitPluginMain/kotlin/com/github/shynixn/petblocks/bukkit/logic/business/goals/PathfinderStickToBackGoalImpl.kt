package com.github.shynixn.petblocks.bukkit.logic.business.goals

import net.minecraft.server.v1_13_R2.EntityLiving
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

class PathfinderStickToBackGoalImpl(private val player: Player, private val livingEntity: LivingEntity) : PathfinderBaseGoal() {
    /**
     * Gets if the goal should be currently executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return !livingEntity.isDead && player.gameMode != GameMode.SPECTATOR
    }

    /**
     * Gets the condition when the goal has been reached or cancelled.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        return true
    }

    /**
     * Gets called every time the scheduler ticks this already started goal.
     */
    override fun onExecute() {
        val nmsEntity = getHandle(livingEntity) as EntityLiving
        val location = getBackLocation(player)

        nmsEntity.setPositionRotation(location.x, location.y, location.z, location.yaw, location.pitch)
    }

    /**
     * Gets the location of the player back.
     */
    private fun getBackLocation(player: Player): Location {
        val location = player.location
        return Location(location.world,
            (location.x + (-1 * Math.cos(Math.toRadians(location.yaw + 90.0)))),
            location.y,
            location.z + (-1 * Math.sin(Math.toRadians(location.yaw + 90.0))),
            location.yaw,
            location.pitch)
    }
}