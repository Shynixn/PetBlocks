package com.github.shynixn.petblocks.bukkit.logic.business.goals

import com.github.shynixn.petblocks.api.business.goal.Goal
import com.github.shynixn.petblocks.bukkit.logic.business.nms.VersionSupport

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
abstract class PathfinderBaseGoal : Goal {
    private val version = VersionSupport.getServerVersion()

    /**
     * Can the goal be cancelled while not being achieved yet?
     */
    override val isInteruptible: Boolean
        get() = true

    /**
     * Gets called when the goal gets started.
     */
    override fun onStartExecuting() {
    }

    /**
     * Gets called every time the scheduler ticks this already started goal.
     */
    override fun onExecute() {
    }

    /**
     * Gets called when the goal stops getting executed.
     */
    override fun onStopExecuting() {
    }

    /**
     * Finds a version compatible class.
     */
    protected fun findClazz(name : String) : Class<*>{
        return Class.forName(name.replace("VERSION",version.versionText))
    }
}