package com.github.shynixn.petblocks.bukkit.dependencies.worldguard;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.lib.ReflectionUtils;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.flags.DefaultFlag;
import com.sk89q.worldguard.protection.flags.StateFlag.State;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class WorldGuardConnection5 {
    private static final List<ProtectedRegion> flags = new ArrayList<>();
    private static final Map<Player, Collection<ProtectedRegion>> cache = new HashMap<>();

    private WorldGuardConnection5() {
        super();
    }

    /**
     * Modifies the worldguard regions at the given location to allow spawning for PetBlocks.
     *
     * @param location location
     * @throws NoSuchMethodException     exception
     * @throws IllegalAccessException    exception
     * @throws InvocationTargetException exception
     */
    public synchronized static void allowSpawn(Location location) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final WorldGuardPlugin worldGuard = getWorldGuard();
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final Iterable<?> set = ReflectionUtils.invokeMethodByObject(regionManager, "getApplicableRegions", new Class[]{location.getClass()}, new Object[]{location});
        for (final Object region1 : set) {
            final ProtectedRegion region = (ProtectedRegion) region1;
            if (region.getFlag(DefaultFlag.MOB_SPAWNING) == State.DENY) {
                region.setFlag(DefaultFlag.MOB_SPAWNING, State.ALLOW);
                flags.add(region);
            }
        }
    }

    /**
     * Checks if the player is riding his pet and entering a different region. Returns false if he isn't the owner of the region.
     *
     * @param player     player
     * @param cacheSpawn cacheSpawn
     * @return success
     * @throws InvocationTargetException exception
     * @throws IllegalAccessException    exception
     * @throws NoSuchMethodException     exception
     */
    public static boolean isAllowedToEnterRegionByRiding(Player player, boolean cacheSpawn) throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
        final Optional<PetBlock> optPetBlock;
        if (!Config.getInstance().allowRidingOnRegionChanging() && ((optPetBlock = PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player)).isPresent())) {
            if (((ArmorStand) optPetBlock.get().getArmorStand()).getPassenger() != null && ((ArmorStand) optPetBlock.get().getArmorStand()).getPassenger().equals(player) || cacheSpawn) {
                final Location location = player.getLocation();
                final WorldGuardPlugin worldGuard = getWorldGuard();
                final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
                final Iterable<?> set = ReflectionUtils.invokeMethodByObject(regionManager, "getApplicableRegions", new Class[]{location.getClass()}, new Object[]{location});
                List<ProtectedRegion> regionsList = null;
                if (cacheSpawn) {
                    regionsList = new ArrayList<>();
                }
                for (final Object region1 : set) {
                    final ProtectedRegion region = (ProtectedRegion) region1;
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

    /**
     * Checks if the player should be kicked off his pet.
     *
     * @param player   player
     * @param regionId regionId
     * @return kickOff
     */
    public static boolean shouldKickOffPet(Player player, String regionId) {
        if (cache.containsKey(player)) {
            for (final ProtectedRegion protectedRegion : cache.get(player)) {
                if (protectedRegion.getId().equals(regionId)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns a list of worldguard regions from the given location.
     *
     * @param location location
     * @return list
     * @throws NoSuchMethodException     exception
     * @throws IllegalAccessException    exception
     * @throws InvocationTargetException exception
     */
    public static List<String> getRegionsFromLocation(Location location) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        final List<String> regionList = new ArrayList<>();
        final WorldGuardPlugin worldGuard = getWorldGuard();
        final RegionManager regionManager = worldGuard.getRegionManager(location.getWorld());
        final Iterable<?> set = ReflectionUtils.invokeMethodByObject(regionManager, "getApplicableRegions", new Class[]{location.getClass()}, new Object[]{location});
        for (final Object region1 : set) {
            final ProtectedRegion region = (ProtectedRegion) region1;
            regionList.add(region.getId());
        }
        return regionList;
    }

    /**
     * Rolls all modified regions back to their original state.
     */
    public synchronized static void rollBack() {
        for (final ProtectedRegion region : flags.toArray(new ProtectedRegion[flags.size()])) {
            region.setFlag(DefaultFlag.MOB_SPAWNING, State.DENY);
        }
        flags.clear();
    }

    private static WorldGuardPlugin getWorldGuard() {
        return (WorldGuardPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    }
}
