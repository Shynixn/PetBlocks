package com.github.shynixn.petblocks.business.bukkit.nms;

import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.bukkit.dependencies.clearlag.ClearLagListener;
import com.github.shynixn.petblocks.business.bukkit.dependencies.supervanish.SuperVanishConnection;
import com.github.shynixn.petblocks.business.bukkit.dependencies.worldguard.WorldGuardConnection5;
import com.github.shynixn.petblocks.business.bukkit.dependencies.worldguard.WorldGuardConnection6;
import com.github.shynixn.petblocks.business.bukkit.nms.v1_9_R1.Listener19;
import com.github.shynixn.petblocks.lib.*;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public final class NMSRegistry {
    private NMSRegistry() {
        super();
    }

    /**
     * Creates a new petblock from the given location and meta
     *
     * @param location location
     * @param meta     meta
     * @return petblock
     */
    public static PetBlock createPetBlock(Location location, PetMeta meta) {
        try {
            return (PetBlock) ReflectionUtils.invokeConstructor(findClassFromVersion("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.CustomGroundArmorstand")
                    , new Class[]{location.getClass(), PetMeta.class}, new Object[]{location, meta});
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException | InstantiationException | ClassNotFoundException e) {
            Bukkit.getLogger().log(Level.WARNING, "Cannot create petblock.", e);
            return null;
        }
    }

    /**
     * Checks if the itemStack is unbreakable
     *
     * @param itemStack itemStack
     * @return isUnbreakable
     */
    @Deprecated
    public static boolean isUnbreakable(ItemStack itemStack) {
        try {
            return ReflectionUtils.invokeMethodByClass(
                    findClassFromVersion("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.OwnerPathfinder"),
                    "isUnbreakable", new Class[]{ItemStack.class}, new Object[]{itemStack});
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to check unbreakable stage of itemstack.", e);
            return false;
        }
    }

    /**
     * Sets the tags of the itemStack
     *
     * @param itemStack itemStack
     * @param tags      tags
     * @return itemStack
     */
    @Deprecated
    public static ItemStack setItemStackTag(ItemStack itemStack, Map<String, Object> tags) {
        try {
            return ReflectionUtils.invokeMethodByClass(
                    findClassFromVersion("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.OwnerPathfinder"),
                    "setItemstackTag", new Class[]{ItemStack.class, Map.class}, new Object[]{itemStack, tags});
        } catch (NoSuchMethodException | ClassNotFoundException | IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to set tags of the itemStack.", e);
            return null;
        }
    }

    /**
     * Registers a dynamic command
     *
     * @param command command
     * @param clazz   clazz
     */
    @Deprecated
    public static void registerDynamicCommand(String command, BukkitCommand clazz) {
        try {
            final Object craftServer = findClassFromVersion("org.bukkit.craftbukkit.VERSION.CraftServer").cast(Bukkit.getServer());
            final Object commandMap = ReflectionUtils.invokeMethodByObject(craftServer, "getCommandMap", new Class[]{}, new Object[]{});
            ReflectionUtils.invokeMethodByObject(commandMap, "register", new Class[]{command.getClass(), Command.class}, new Object[]{command, clazz});
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to register dynamic command.", e);
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
                return ReflectionUtils.invokeMethodByObject(player, "getItemInHand", new Class[]{}, new Object[]{});
            }
            if (offHand) {
                return ReflectionUtils.invokeMethodByObject(player.getInventory(), "getItemInOffHand", new Class[]{}, new Object[]{});
            }
            return ReflectionUtils.invokeMethodByObject(player.getInventory(), "getItemInMainHand", new Class[]{}, new Object[]{});
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to gather item in 19 hand.", e);
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
                ReflectionUtils.invokeMethodByObject(player, "setItemInHand", new Class[]{itemStack.getClass()}, new Object[]{itemStack});
            } else if (offHand) {
                ReflectionUtils.invokeMethodByObject(player.getInventory(), "setItemInOffHand", new Class[]{itemStack.getClass()}, new Object[]{itemStack});
            } else {
                ReflectionUtils.invokeMethodByObject(player.getInventory(), "setItemInMainHand", new Class[]{itemStack.getClass()}, new Object[]{itemStack});
            }
        } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
            Bukkit.getLogger().log(Level.WARNING, "Failed to set item in 19 hand.", e);
        }
    }

    public static void registerListener19(List<Player> players, Plugin plugin) {
        if (VersionSupport.getServerVersion().isVersionSameOrGreaterThan(VersionSupport.VERSION_1_9_R1)) {
            try {
                Class.forName("org.bukkit.event.player.PlayerSwapHandItemsEvent");
            } catch (final ClassNotFoundException e) {
                return;
            }
            new Listener19(players, plugin);
        }
    }

    public static void unregisterAll() {
        LightRegistry.unregister();
    }

    public static ItemStack changeSkullSkin(ItemStack itemStack, String skinUrl) {
        return SkullMetaRegistry.convertToSkinSkull(itemStack, skinUrl, "org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull".replace("VERSION", VersionSupport.getServerVersion().getVersionText()));
    }

    public static String getSkinUrl(ItemStack itemStack) {
        return SkullMetaRegistry.getLink(itemStack, BukkitUtilities.replaceServerVersion("org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull"));
    }

    public static void registerAll() {
        LightRegistry.RABBIT.register("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.CustomRabbit");
        LightRegistry.ZOMBIE.register("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.CustomZombie");
        RegisterHelper.PREFIX = PetBlocksPlugin.PREFIX_CONSOLE;
        RegisterHelper.register("WorldGuard", "com.sk89q.worldguard.protection.ApplicableRegionSet", '5');
        RegisterHelper.register("WorldGuard", "com.sk89q.worldguard.protection.ApplicableRegionSet", '6');
        if ((RegisterHelper.register("SuperVanish") || RegisterHelper.register("PremiumVanish"))) {
            try {
                SuperVanishConnection.register((JavaPlugin) Bukkit.getPluginManager().getPlugin("PetBlocks"));
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Manual hook failed. No interacting with [SuperVanish or PremiumVanish]");
            }
        }
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

    public static boolean canSpawnInRegion(String[] regionList, Location location) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    return WorldGuardConnection6.canSpawnInRegion(regionList, location);
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    return WorldGuardConnection5.canSpawnInRegion(regionList, location);
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
        return true;
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
    public static Class<?> findClassFromVersion(String path) throws ClassNotFoundException {
        return Class.forName(path.replace("VERSION", VersionSupport.getServerVersion().getVersionText()));
    }
}
