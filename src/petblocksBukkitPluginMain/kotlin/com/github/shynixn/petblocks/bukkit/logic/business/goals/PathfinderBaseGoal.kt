package com.github.shynixn.petblocks.bukkit.logic.business.goals

import com.github.shynixn.petblocks.bukkit.logic.business.extension.getServerVersion
import org.bukkit.Location
import org.bukkit.entity.LivingEntity

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
abstract class PathfinderBaseGoal {
    companion object {
        private val version = getServerVersion()
        private val getHandleMethod = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!
        private val clearCurrentPath = findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod("q")
        private val navigationAbstractMethod = findClazz("net.minecraft.server.VERSION.EntityInsentient").getDeclaredMethod("getNavigation")
        private val goToEntityNavigationMethod =
            findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod("a", Double::class.java, Double::class.java, Double::class.java, Double::class.java)

        /**
         * Finds a version compatible class.
         */
        private fun findClazz(name: String): Class<*> {
            return Class.forName(name.replace("VERSION", version.bukkitId))
        }
    }

    /**
     * Can the goal be cancelled while not being achieved yet?
     */
    open val isInteruptible: Boolean
        get() = true

    /**
     * Gets called when the goal gets started.
     */
    open  fun onStartExecuting() {
    }

    /**
     * Gets called every time the scheduler ticks this already started goal.
     */
    open fun onExecute() {
    }

    /**
     * Gets called when the goal stops getting executed.
     */
    open fun onStopExecuting() {
    }

    /**
     * Lets the given [entity] navigate to the given location with the given [speed].
     */
    protected fun navigateTo(entity: LivingEntity, location: Location, speed: Double) {
        val navigation = getNavigation(entity)
        goToEntityNavigationMethod.invoke(navigation, location.x, location.y, location.z, speed)
    }

    /**
     * Stops the current navigation.
     */
    protected fun clearNavigation(entity: LivingEntity) {
        val navigation = getNavigation(entity)
        clearCurrentPath.invoke(navigation)
    }

    /**
     * Gets the navigation of an entity.
     */
    protected fun getNavigation(entity: LivingEntity): Any {
        val nmsEntity = getHandle(entity)
        return navigationAbstractMethod.invoke(nmsEntity)
    }

    /**
     * Gets the nms handle of an entity.
     */
    protected fun getHandle(entity: LivingEntity): Any {
        return getHandleMethod.invoke(entity)
    }

    /**
     * Finds a version compatible class.
     */
    protected fun findClazz(name: String): Class<*> {
        return Class.forName(name.replace("VERSION", version.bukkitId))
    }
}