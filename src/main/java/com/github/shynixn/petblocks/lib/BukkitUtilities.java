package com.github.shynixn.petblocks.lib;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

@SuppressWarnings({"ResultOfMethodCallIgnored", "SameParameterValue"})
@Deprecated
public final class BukkitUtilities {
    private BukkitUtilities() {
        super();
    }

    public static List<Player> getOnlinePlayers() {
        final List<Player> players = new ArrayList<>();
        for (final World world : Bukkit.getWorlds()) {
            players.addAll(world.getPlayers());
        }
        return players;
    }

    public static void sendColorMessage(String message, ChatColor color, String prefix) {
        Bukkit.getServer().getConsoleSender().sendMessage(prefix + color + message);
    }

    public static boolean tryParseInt(String value) {
        try {
            Integer.parseInt(value);
        } catch (final NumberFormatException nfe) {
            return false;
        }
        return true;
    }

    public static ItemStack nameItem(ItemStack item, String name, String[] lore) {
        if (item.getType() != Material.AIR) {
            final ItemMeta im = item.getItemMeta();
            if (name != null) {
                im.setDisplayName(name);
            }
            if (lore != null) {
                im.setLore(Arrays.asList(lore));
            } else {
                im.setLore(new ArrayList<>());
            }
            item.setItemMeta(im);
            return item;
        }
        return item;
    }

    public static ItemStack nameItemDisplay(ItemStack item, String name) {
        final ItemMeta im = item.getItemMeta();
        if (im != null) {
            if (name != null) {
                im.setDisplayName(name);
            }
            item.setItemMeta(im);
        }
        return item;
    }

    public static boolean copyFile(InputStream in, File file) {
        try {
            try(final OutputStream out = new FileOutputStream(file))
            {
                final byte[] buf = new byte[1024];
                int len;
                while ((len = in.read(buf)) > 0) {
                    out.write(buf, 0, len);
                }
            }
            in.close();
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, e.getMessage());
            return false;
        }
        return true;
    }

    public static int isNegative(Random rand) {
        if (rand.nextInt(2) == 0)
            return -1;
        return 1;
    }

    public static boolean compareItemName(ItemStack itemStack, String displayname) {
        try {
            if (itemStack != null) {
                if (itemStack.getItemMeta() != null) {
                    return itemStack.getItemMeta().getDisplayName().equals(displayname);
                }
            }
        } catch (final Exception ignored) {
        }
        return false;
    }

    public static String replaceServerVersion(String text) {
        return text.replace("VERSION", getServerVersion());
    }

    public static String getServerVersion() {
        try {
            return Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3];
        } catch (final Exception ex) {
            throw new RuntimeException("Version not found!");
        }
    }

    public static ItemStack activateHead(String name, ItemStack itemStack) {
        try {
            if (itemStack.getItemMeta() instanceof SkullMeta) {
                final SkullMeta meta = (SkullMeta) itemStack.getItemMeta();
                meta.setOwner(name);
                itemStack.setItemMeta(meta);
            }
        } catch (final Exception e) {
            Bukkit.getLogger().log(Level.WARNING, e.getMessage());
        }
        return itemStack;
    }
}
