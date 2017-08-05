package com.github.shynixn.petblocks.business.bukkit.dependencies.worldguard;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.lib.ReflectionLib;
import org.bukkit.Location;
import org.bukkit.entity.Player;
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
    private static final Map<Player, Collection<ProtectedRegion>> cache = new HashMap<>();

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

    /**
     * Checks if the player is riding his pet and entering a different region. Returns false if he isn't the owner of the region
     *
     * @param player player
     * @param plugin plugin
     * @return success
     * @throws InvocationTargetException exception
     * @throws IllegalAccessException    exception
     */
    public static boolean isAllowedToEnterRegionByRiding(Player player, boolean cacheSpawn, Plugin plugin) throws InvocationTargetException, IllegalAccessException {
        if (!Config.getInstance().allowRidingOnRegionChanging() && PetBlocksApi.hasPetBlock(player)) {
            final PetBlock petBlock = PetBlocksApi.getPetBlock(player);
            if ((petBlock.getArmorStand().getPassenger() != null && petBlock.getArmorStand().getPassenger().equals(player)) || cacheSpawn) {
                final Location location = player.getLocation();
                final WorldGuardPlugin worldGuard = (WorldGuardPlugin) plugin;
                final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
                final ApplicableRegionSet set = (ApplicableRegionSet) ReflectionLib.invokeMethodByObject(regionManager, "getApplicableRegions", location);
                final Iterable<ProtectedRegion> regions = (Iterable<ProtectedRegion>) getMethod(set.getClass(), "getRegions").invoke(set);
                List<ProtectedRegion> regionsList = null;
                if (cacheSpawn) {
                    regionsList = new ArrayList<>();
                }
                for (final ProtectedRegion region : regions) {
                    if (!cacheSpawn && region.getOwners().size() > 0 && !region.getOwners().contains(player.getUniqueId())) {
                        if (cache.containsKey(player) && !cache.get(player).contains(region)) {
                            return false;
                        }
                    }
                    if (cacheSpawn) {
                        regionsList.add(region);
                    }
                }
                if (cacheSpawn) {
                    cache.put(player, regionsList);
                }
            }
        }
        return true;
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
