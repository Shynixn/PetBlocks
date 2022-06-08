package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_19_R1

import com.github.shynixn.petblocks.api.business.proxy.PathfinderProxy
import net.minecraft.world.entity.ai.goal.PathfinderGoal

/**
 * Pathfinder NMS proxy to handle native calls.
 */
class Pathfinder(private val pathfinderProxy: PathfinderProxy) : PathfinderGoal() {
    /**
     * Override ShouldExecute.
     */
    override fun a(): Boolean {
        return pathfinderProxy.shouldGoalBeExecuted()
    }

    /**
     * Override continue executing.
     */
    override fun b(): Boolean {
        return pathfinderProxy.shouldGoalContinueExecuting()
    }

    /**
     * Override isInterrupting.
     */
    override fun D_(): Boolean {
        return pathfinderProxy.isInteruptible
    }

    /**
     * Override startExecuting.
     */
    override fun c() {
        this.pathfinderProxy.onStartExecuting()
    }

    /**
     * Override reset.
     */
    override fun d() {
        this.pathfinderProxy.onStopExecuting()
    }

    /**
     * Override update.
     */
    override fun e() {
        this.pathfinderProxy.onExecute()
    }
}
