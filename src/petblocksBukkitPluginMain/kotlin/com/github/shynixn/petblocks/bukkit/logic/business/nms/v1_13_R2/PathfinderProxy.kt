package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_13_R2

import com.github.shynixn.petblocks.api.business.goal.Goal
import net.minecraft.server.v1_13_R2.PathfinderGoal
import org.bukkit.plugin.Plugin
import java.util.logging.Level

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
class PathfinderProxy(private val goal: Goal, private val plugin: Plugin) : PathfinderGoal() {
    /**
     * Override ShouldExecute.
     */
    override fun a(): Boolean {
        return try {
            goal.shouldGoalBeExecuted()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed shouldGoalBeExecuted.", e)
            false
        }
    }

    /**
     * Override continue executing.
     */
    override fun b(): Boolean {
        return try {
            goal.shouldGoalContinueExecuting()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed shouldGoalContinueExecuting.", e)
            false
        }
    }

    /**
     * Override isInterruptible.
     */
    override fun f(): Boolean {
        return try {
            goal.isInteruptible
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed isInteruptible.", e)
            false
        }
    }

    /**
     * Override startExecuting.
     */
    override fun c() {
        try {
            this.goal.onStartExecuting()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onStartExecuting.", e)
        }
    }

    /**
     * Override reset.
     */
    override fun d() {
        try {
            this.goal.onStopExecuting()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onStopExecuting.", e)
        }
    }

    /**
     * Override update.
     */
    override fun e() {
        try {
            this.goal.onExecute()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onExecute.", e)
        }
    }
}