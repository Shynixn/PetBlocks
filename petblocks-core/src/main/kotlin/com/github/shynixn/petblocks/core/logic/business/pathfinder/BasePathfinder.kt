package com.github.shynixn.petblocks.core.logic.business.pathfinder

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import com.github.shynixn.petblocks.api.persistence.entity.AIBase

abstract class BasePathfinder(
    /**
     * Gets the base ai.
     */
    override val aiBase: AIBase
) : PathfinderProxy {
    /**
     * Is the pathfinder incorruptible.
     */
    override var isInteruptible: Boolean = true

    /**
     * Should the goal be executed.
     */
    override fun shouldGoalBeExecuted(): Boolean {
        return false
    }

    /**
     * Should the goal continue executing.
     */
    override fun shouldGoalContinueExecuting(): Boolean {
        return false
    }

    /**
     * On start executing.
     */
    override fun onStartExecuting() {
    }

    /**
     * On stop executing.
     */
    override fun onStopExecuting() {
    }

    /**
     * On execute.
     */
    override fun onExecute() {
    }
}