package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import net.minecraft.world.entity.ai.goal.Goal

/**
 * Pathfinder NMS proxy to handle native calls.
 */
class Pathfinder(private val pathfinderProxy: PathfinderProxy) : Goal() {
    /**
     * Override ShouldExecute.
     */
    override fun canUse(): Boolean {
        return pathfinderProxy.shouldGoalBeExecuted()
    }

    /**
     * Override continue executing.
     */
    override fun canContinueToUse(): Boolean {
        return pathfinderProxy.shouldGoalContinueExecuting()
    }

    /**
     * Override isInterrupting.
     */
    override fun isInterruptable(): Boolean {
        return pathfinderProxy.isInteruptible
    }

    /**
     * Override startExecuting.
     */
    override fun start() {
        this.pathfinderProxy.onStartExecuting()
    }

    /**
     * Override reset.
     */
    override fun stop() {
        this.pathfinderProxy.onStopExecuting()
    }

    /**
     * Override update.
     */
    override fun tick() {
        this.pathfinderProxy.onExecute()
    }
}
