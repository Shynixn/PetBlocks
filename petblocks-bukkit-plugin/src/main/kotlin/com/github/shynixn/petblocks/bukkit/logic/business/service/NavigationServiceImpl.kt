package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.Version
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.NavigationService
import org.bukkit.Location

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
class NavigationServiceImpl(private val version: Version) : NavigationService {
    private val getHandleMethod = findClazz("org.bukkit.craftbukkit.VERSION.entity.CraftLivingEntity").getDeclaredMethod("getHandle")!!
    private val clearCurrentPath = findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod("q")
    private val navigationAbstractMethod = findClazz("net.minecraft.server.VERSION.EntityInsentient").getDeclaredMethod("getNavigation")
    private val goToEntityNavigationMethod =
        findClazz("net.minecraft.server.VERSION.NavigationAbstract").getDeclaredMethod("a", Double::class.java, Double::class.java, Double::class.java, Double::class.java)

    /**
     * Lets the given [petProxy] navigate to the given [location].
     */
    override fun <L> navigateToLocation(petProxy: PetProxy, location: L, speed: Double) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        val nmsEntity = getHandleMethod.invoke(petProxy.getHitBoxLivingEntity())
        val navigation = navigationAbstractMethod.invoke(nmsEntity)
        goToEntityNavigationMethod.invoke(navigation, location.x, location.y, location.z, speed)
    }

    /**
     * Clears the current navigation target from the petProxy.
     */
    override fun clearNavigation(petProxy: PetProxy) {
        val nmsEntity = getHandleMethod.invoke(petProxy.getHitBoxLivingEntity())
        val navigation = navigationAbstractMethod.invoke(nmsEntity)
        clearCurrentPath.invoke(navigation)
    }

    /**
     * Finds a version compatible class.
     */
    private fun findClazz(name: String): Class<*> {
        return Class.forName(name.replace("VERSION", version.bukkitId))
    }
}