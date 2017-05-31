package com.github.shynixn.petblocks.business.bukkit.nms;

import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
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
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

public final class NMSRegistry {
    private NMSRegistry() {
        super();
    }

    public static PetBlock createPetBlock(Location location, PetMeta meta) {
        final Class<?> clazz = ReflectionLib.getClassFromName("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.CustomGroundArmorstand");
        return (PetBlock) ReflectionLib.invokeConstructor(clazz, location, meta);
    }

    public static boolean isUnbreakable(ItemStack itemStack) {
        final Class<?> clazz = ReflectionLib.getClassFromName("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.OwnerPathfinder");
        return (boolean) ReflectionLib.invokeMethodByClazz(clazz, "isUnbreakable", itemStack);
    }

    public static ItemStack setItemStackTag(ItemStack itemStack, Map<String, Object> tags) {
        final Class<?> clazz = ReflectionLib.getClassFromName("com.github.shynixn.petblocks.business.bukkit.nms.VERSION.OwnerPathfinder");
        return (ItemStack) ReflectionLib.invokeMethodByClazz(clazz, "setItemstackTag", itemStack, tags);
    }

    public static void registerListener19(List<Player> players, JavaPlugin plugin) {
        if (BukkitUtilities.getServerVersion().equalsIgnoreCase("v1_9_R1")
                || BukkitUtilities.getServerVersion().equalsIgnoreCase("v1_9_R2")
                || BukkitUtilities.getServerVersion().equalsIgnoreCase("v1_10_R1")
                || BukkitUtilities.getServerVersion().equalsIgnoreCase("v1_11_R1")
                || BukkitUtilities.getServerVersion().equalsIgnoreCase("v1_12_R1")) {
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

    public static void registerDynamicCommand(String command, BukkitCommand clazz) {
        Object obj = ReflectionLib.getClassFromName("org.bukkit.craftbukkit.VERSION.CraftServer").cast(Bukkit.getServer());
        obj = ReflectionLib.invokeMethodByObject(obj, "getCommandMap");
        ReflectionLib.invokeMethodByObject(obj, "register", command, clazz);
    }

    public static ItemStack changeSkullSkin(ItemStack itemStack, String skinUrl) {
        return SkullMetaRegistry.convertToSkinSkull(itemStack, skinUrl, BukkitUtilities.replaceServerVersion("org.bukkit.craftbukkit.VERSION.inventory.CraftMetaSkull"));
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
                    WorldGuardConnection6.allowSpawn(location, getWorldGuard());
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    WorldGuardConnection5.allowSpawn(location, getWorldGuard());
            } catch (final Exception ex) {
                Bukkit.getServer().getConsoleSender().sendMessage(PetBlocksPlugin.PREFIX_CONSOLE + ChatColor.DARK_RED + "Crashed while connecting to worldguard." + ex.getMessage());
            }
        }
    }

    public static boolean canSpawnInRegion(String[] regionList, Location location) {
        if (RegisterHelper.isRegistered("WorldGuard")) {
            try {
                if (RegisterHelper.isRegistered("WorldGuard", '6'))
                    return WorldGuardConnection6.canSpawnInRegion(regionList, location, getWorldGuard());
                else if (RegisterHelper.isRegistered("WorldGuard", '5'))
                    return WorldGuardConnection5.canSpawnInRegion(regionList, location, getWorldGuard());
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

    private static Plugin getWorldGuard() {
        return Bukkit.getServer().getPluginManager().getPlugin("WorldGuard");
    }
}
