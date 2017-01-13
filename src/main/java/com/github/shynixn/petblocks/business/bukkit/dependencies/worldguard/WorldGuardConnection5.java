package com.github.shynixn.petblocks.business.bukkit.dependencies.worldguard;

import com.github.shynixn.petblocks.lib.ReflectionLib;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;

public final class WorldGuardConnection5 {
    private WorldGuardConnection5() {
        super();
    }

    private static final ArrayList<ProtectedRegion> flags = new ArrayList<>();

    public synchronized static void allowSpawn(Location location, Plugin plugin) {
        final WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final Iterable<?> set = (Iterable<?>) ReflectionLib.invokeMethodByObject(regionManager, "getApplicableRegions", location);
        for (final Object region1 : set) {
            final ProtectedRegion region = (ProtectedRegion) region1;
            if (region.getFlag(DefaultFlag.MOB_SPAWNING) == State.DENY) {
                region.setFlag(DefaultFlag.MOB_SPAWNING, State.ALLOW);
                flags.add(region);
            }
        }
    }

    public static boolean canSpawnInRegion(String[] regionList, Location location, Plugin plugin) {
        final WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final Iterable<?> set = (Iterable<?>) ReflectionLib.invokeMethodByObject(regionManager, "getApplicableRegions", location);
        for (final Object region1 : set) {
            final ProtectedRegion region = (ProtectedRegion) region1;
            for (final String s : regionList) {
                if (region.getId().equalsIgnoreCase(s))
                    return true;
            }
        }
        return false;
    }

    public synchronized static void rollBack() {
        for (final ProtectedRegion region : flags.toArray(new ProtectedRegion[flags.size()])) {
            region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
        }
        flags.clear();
    }
}
