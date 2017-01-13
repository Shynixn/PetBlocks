package com.github.shynixn.petblocks.business.bukkit.dependencies.worldguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import com.github.shynixn.petblocks.lib.ReflectionLib;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.ApplicableRegionSet;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;

public final class WorldGuardConnection6 {
    private WorldGuardConnection6() {
        super();
    }

    private static final ArrayList<ProtectedRegion> flags = new ArrayList<>();

    public synchronized static void allowSpawn(Location location, Plugin plugin) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        final WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final ApplicableRegionSet set = (ApplicableRegionSet) ReflectionLib.invokeMethodByObject(regionManager, "getApplicableRegions", location);
        final Iterable<ProtectedRegion> regions = (Iterable<ProtectedRegion>) getMethod(set.getClass(), "getRegions").invoke(set);
        for (final ProtectedRegion region : regions) {
            if (region.getFlag(DefaultFlag.MOB_SPAWNING) == State.DENY) {
                region.setFlag(DefaultFlag.MOB_SPAWNING, State.ALLOW);
                flags.add(region);
            }
        }
    }

    public static boolean canSpawnInRegion(String[] regionList, Location location, Plugin plugin) throws InvocationTargetException, IllegalAccessException {
        final WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final ApplicableRegionSet set = (ApplicableRegionSet) ReflectionLib.invokeMethodByObject(regionManager, "getApplicableRegions", location);
        final Iterable<ProtectedRegion> regions = (Iterable<ProtectedRegion>) getMethod(set.getClass(), "getRegions").invoke(set);
        for (final Object region1 : regions) {
            final ProtectedRegion region = (ProtectedRegion) region1;
            for (final String s : regionList) {
                if (region.getId().equalsIgnoreCase(s))
                    return true;
            }
        }
        return false;
    }

    private static Method getMethod(Class<?> class1, String name) {
        for (final Method method : class1.getDeclaredMethods()) {
            if (method.getName().equalsIgnoreCase(name))
                return method;
        }
        throw new RuntimeException("Cannot hook into WorldGuard");
    }

    public synchronized static void rollBack() {
        for (final ProtectedRegion region : flags.toArray(new ProtectedRegion[flags.size()])) {
            region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
        }
        flags.clear();
    }
}
