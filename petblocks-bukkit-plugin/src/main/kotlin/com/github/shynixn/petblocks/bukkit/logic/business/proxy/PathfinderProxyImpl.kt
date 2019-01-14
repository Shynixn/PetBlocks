package com.github.shynixn.petblocks.bukkit.logic.business.proxy

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
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
open class PathfinderProxyImpl(private val plugin: Plugin, override val aiBase: AIBase) : PathfinderProxy {
    /**
     * Should Goal executed function.
     */
    var shouldGoalBeExecuted: (() -> Boolean)? = null

    /**
     * Should Goal continue executing function.
     */
    var shouldGoalContinueExecuting: (() -> Boolean)? = null

    /**
     * On start executing function.
     */
    var onStartExecuting: (() -> Unit)? = null

    /**
     * On stop executing function.
     */
    var onStopExecuting: (() -> Unit)? = null

    /**
     * On execute function.
     */
    var onExecute: (() -> Unit)? = null

    /**
     * Is the pathfinder incorruptible.
     */
    override var isInteruptible: Boolean = true

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        if (shouldGoalBeExecuted != null) {
            return try {
                shouldGoalBeExecuted!!.invoke()
            } catch (e: Exception) {
                plugin.logger.log(Level.WARNING, "Failed shouldGoalBeExecuted.", e)
                false
            }
        }

        return false
    }

    /**
     * Should the goal continue executing.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        if (shouldGoalContinueExecuting != null) {
            return try {
                shouldGoalContinueExecuting!!.invoke()
            } catch (e: Exception) {
                plugin.logger.log(Level.WARNING, "Failed shouldGoalContinueExecuting.", e)
                false
            }
        }

        return false
    }

    /**
     * On start executing.
     */
    override fun onStartExecuting() {
        try {
            onStartExecuting?.invoke()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onStartExecuting.", e)
        }
    }

    /**
     * On stop executing.
     */
    override fun onStopExecuting() {
        try {
            onStopExecuting?.invoke()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onStopExecuting.", e)
        }
    }

    /**
     * On execute.
     */
    override fun onExecute() {
        try {
            onExecute?.invoke()
        } catch (e: Exception) {
            plugin.logger.log(Level.WARNING, "Failed onExecute.", e)
        }
    }
}