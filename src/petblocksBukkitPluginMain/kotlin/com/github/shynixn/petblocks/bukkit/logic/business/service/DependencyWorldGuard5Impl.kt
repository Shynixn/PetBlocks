package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.service.DependencyWorldGuardService
import com.sk89q.worldguard.bukkit.WorldGuardPlugin
import com.sk89q.worldguard.protection.flags.DefaultFlag
import com.sk89q.worldguard.protection.flags.StateFlag
import com.sk89q.worldguard.protection.regions.ProtectedRegion
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.*

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
class DependencyWorldGuard5Impl @Inject constructor(plugin: Plugin) : DependencyWorldGuardService {
    private val flags = ArrayList<ProtectedRegion>()
    private val worldGuardPlugin = plugin.server.pluginManager.getPlugin("WorldGuard") as WorldGuardPlugin

    /**
     * Prepares the given [location] for spawning a pet inside of it. All worldguard regions get modified
     * to correctly adjust the spawning. Does nothing if already prepared.
     */
    override fun <L> prepareSpawningRegion(location: L) {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        val regionManager = worldGuardPlugin.getRegionManager(location.world)

        val regions = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager")
                .getDeclaredMethod("getApplicableRegions", Location::class.java)
                .invoke(regionManager, location) as Iterable<*>

        regions.forEach { region ->
            if (region is ProtectedRegion && region.getFlag(DefaultFlag.MOB_SPAWNING) == StateFlag.State.DENY) {
                region.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.ALLOW)
                flags.add(region)
            }
        }
    }

    /**
     * Resets the cached spawning adjustments which where made by [prepareSpawningRegion]. If no caches
     * are present it does nothing.
     */
    override fun <L> resetSpawningRegion(location: L) {
        for (region in flags.toTypedArray()) {
            region.setFlag(DefaultFlag.MOB_SPAWNING, StateFlag.State.DENY)
        }

        flags.clear()
    }

    /**
     * Returns all region names at the given [location].
     */
    override fun <L> getRegionNames(location: L): List<String> {
        if (location !is Location) {
            throw IllegalArgumentException("Location has to be a BukkitLocation!")
        }

        val regionManager = worldGuardPlugin.getRegionManager(location.world)

        val regions = Class.forName("com.sk89q.worldguard.protection.managers.RegionManager")
                .getDeclaredMethod("getApplicableRegions", Location::class.java)
                .invoke(regionManager, location) as Iterable<*>

        return regions.map { p -> (p as ProtectedRegion).id }.toSet().toList()
    }
}