package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_17_R1

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import net.minecraft.world.entity.ai.goal.Goal

/**
 * This pathfinder is a solution to Paper causing normal pathfinders to sometimes get ignored.
 */
class CombinedPathfinder(val pathfinderProxy: Map<PathfinderProxy, Cache>) : Goal() {
    /**
     * Override ShouldExecute.
     */
    override fun canUse(): Boolean {
        for (proxy in pathfinderProxy.keys) {
            val cache = pathfinderProxy.getValue(proxy)

            if (cache.isExecuting) {
                val shouldContinue = proxy.shouldGoalContinueExecuting()

                if (!shouldContinue) {
                    proxy.onStopExecuting()
                    cache.isExecuting = false
                } else {
                    proxy.onExecute()
                }
            } else {
                val shouldExecute = proxy.shouldGoalBeExecuted()

                if (shouldExecute) {
                    proxy.onStartExecuting()
                    cache.isExecuting = true
                    proxy.onExecute()
                }
            }
        }

        return true
    }

    /**
     * Override continue executing.
     */
    override fun canContinueToUse(): Boolean {
        return false
    }

    /**
     * Override isInterrupting.
     */
    override fun isInterruptable(): Boolean {
        return true
    }

    /**
     * Override startExecuting.
     */
    override fun start() {}

    /**
     * Override reset.
     */
    override fun stop() {}

    /**
     * Override update.
     */
    override fun tick() {}

    class Cache {
        var isExecuting = false
    }
}

