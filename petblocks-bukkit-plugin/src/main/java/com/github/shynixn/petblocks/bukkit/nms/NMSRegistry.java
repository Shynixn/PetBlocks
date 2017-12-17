package com.github.shynixn.petblocks.bukkit.nms;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.dependencies.clearlag.ClearLagListener;
import com.github.shynixn.petblocks.bukkit.dependencies.worldguard.WorldGuardConnection5;
import com.github.shynixn.petblocks.bukkit.dependencies.worldguard.WorldGuardConnection6;
import com.github.shynixn.petblocks.bukkit.lib.ReflectionUtils;
import com.github.shynixn.petblocks.bukkit.lib.RegisterHelper;
import com.github.shynixn.petblocks.bukkit.nms.v1_9_R1.Listener19;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;

public final class NMSRegistry {

    private static CustomEntityType.WrappedRegistry wrappedRegistry;
    private static final Class<?> rabbitClazz;
    private static final Class<?> zombieClazz;

    static {
        try {
            rabbitClazz = findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomRabbit");
            zombieClazz = findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomZombie");
        } catch (final ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private NMSRegistry() {
        super();
    }

    /**
     * Creates a new petblock from the given location and meta.
     *
     * @param location location
     * @param meta     meta
     * @return petblock
     */
    public static PetBlock createPetBlock(Location location, PetMeta meta) {
        try {
            if (wrappedRegistry == null) {
                if (VersionSupport.getServerVersion().isVersionLowerThan(VersionSupport.VERSION_1_11_R1)) {
                    wrappedRegistry = new CustomEntityType.Registry10();
                } else {
                    wrappedRegistry = new CustomEntityType.Registry11();
                }
            }
            if (!wrappedRegistry.isRegistered(rabbitClazz)) {
                wrappedRegistry.register(rabbitClazz, CustomEntityType.RABBIT);
                wrappedRegistry.register(zombieClazz, CustomEntityType.ZOMBIE);
            }
            return (PetBlock) ReflectionUtils.invokeConstructor(findClassFromVersion("com.github.shynixn.petblocks.bukkit.nms.VERSION.CustomGroundArmorstand")
                    , new Class[]{location.getClass(), PetMeta.class}, new Object[]{location, meta});
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | ClassNotFoundException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Cannot create petblock.", e);
            return null;
        } catch (final Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Unregisters all custom entities.
     *
     * @throws Exception exception
     */
    public static void unregisterCustomEntities() throws Exception {
        if (wrappedRegistry != null) {
            wrappedRegistry.unregister(rabbitClazz, CustomEntityType.RABBIT);
            wrappedRegistry.unregister(zombieClazz, CustomEntityType.ZOMBIE);
            wrappedRegistry = null;
        }
    }

    /**
     * Checks if the player is allowed to enter a region when he is riding his pet
     *
     * @param player player
     * @param spawn  spawn
     * @return isAllowed
     */
    public static boolean canEnterRegionOnPetRiding(Player player, boolean spawn) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6')) {
                    return WorldGuardConnection6.isAllowedToEnterRegionByRiding(player, spawn);
                } else if (RegisterHelper.isRegistered("WorldGuard", '5')) {
                    return WorldGuardConnection5.isAllowedToEnterRegionByRiding(player, spawn);
                }
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
        return true;
    }

    /**
     * Checks if the player should be kicked off his pet
     *
     * @param player   player
     * @param regionId regionId
     * @return kickOff
     */
    public static boolean shouldKickOffPet(Player player, String regionId) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    return WorldGuardConnection6.shouldKickOffPet(player, regionId);
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    return WorldGuardConnection5.shouldKickOffPet(player, regionId);
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
        return false;
    }

    /**
     * Returns the item in hand by being compatible to lower than 1.9
     *
     * @param player  player
     * @param offHand offHand
     * @return itemStack
     */
    public static ItemStack getItemInHand19(Player player, boolean offHand) {
        try {
            if (!VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
                return ReflectionUtils.invokeMethodByObject(player, "getItemInHand", new Class[]{}, new Object[]{}, HumanEntity.class);
            }
            if (offHand) {
                return ReflectionUtils.invokeMethodByObject(player.getInventory(), "getItemInOffHand", new Class[]{}, new Object[]{});
            }
            return ReflectionUtils.invokeMethodByObject(player.getInventory(), "getItemInMainHand", new Class[]{}, new Object[]{});
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to gather item in 19 hand.", e);
            return null;
        }
    }

    /**
     * Sets the item in hand by being compatible to lower than 1.9
     *
     * @param player    player
     * @param itemStack itemStack
     * @param offHand   offHand
     */
    public static void setItemInHand19(Player player, ItemStack itemStack, boolean offHand) {
        try {
            if (!VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
                ReflectionUtils.invokeMethodByObject(player, "setItemInHand", new Class[]{ItemStack.class}, new Object[]{itemStack}, HumanEntity.class);
            } else if (offHand) {
                ReflectionUtils.invokeMethodByObject(player.getInventory(), "setItemInOffHand", new Class[]{ItemStack.class}, new Object[]{itemStack});
            } else {
                ReflectionUtils.invokeMethodByObject(player.getInventory(), "setItemInMainHand", new Class[]{ItemStack.class}, new Object[]{itemStack});
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            PetBlocksPlugin.logger().log(Level.WARNING, "Failed to set item in 19 hand.", e);
        }
    }

    public static void registerListener19(Set<Player> players, Plugin plugin) {
        if (VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
            try {
                Class.forName("org.bukkit.event.player.PlayerSwapHandItemsEvent");
            } catch (final ClassNotFoundException e) {
                return;
            }
            new Listener19(players, plugin);
        }
    }

    public static void registerAll() {
        RegisterHelper.PREFIX = PetBlocksPlugin.PREFIX_CONSOLE;
        RegisterHelper.register("WorldGuard", "com.sk89q.worldguard.protection.ApplicableRegionSet", '5');
        RegisterHelper.register("WorldGuard", "com.sk89q.worldguard.protection.ApplicableRegionSet", '6');
        if (RegisterHelper.register("ClearLag")) {
            try {
                new ClearLagListener((JavaPlugin) Bukkit.getPluginManager().getPlugin("PetBlocks"));
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Manual hook failed. No interacting with [ClearLag.]");
            }
        }
    }

    public static void accessWorldGuardSpawn(Location location) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    WorldGuardConnection6.allowSpawn(location);
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    WorldGuardConnection5.allowSpawn(location);
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
    }

    public static List<String> getWorldGuardRegionsFromLocation(Location location) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    return WorldGuardConnection6.getRegionsFromLocation(location);
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    return WorldGuardConnection5.getRegionsFromLocation(location);
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
        return new ArrayList<>();
    }

    public static void rollbackWorldGuardSpawn(Location location) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    WorldGuardConnection6.rollBack();
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    WorldGuardConnection5.rollBack();
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
    }

    /**
     * Returns the class managed by version
     *
     * @param path path
     * @return class
     * @throws ClassNotFoundException exception
     */
    private static Class<?> findClassFromVersion(String path) throws ClassNotFoundException {
        return Class.forName(path.replace("VERSION", VersionSupport.getServerVersion().getVersionText()));
    }
}
