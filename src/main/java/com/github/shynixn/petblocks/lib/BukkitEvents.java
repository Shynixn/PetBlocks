package com.github.shynixn.petblocks.lib;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class BukkitEvents implements Listener {
    private final JavaPlugin plugin;

    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    public BukkitEvents(JavaPlugin plugin) {
        super();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        this.plugin = plugin;
    }

    protected void unregister() {
        HandlerList.unregisterAll(this);
    }

    public void playerRightClickEvent(Player player, ItemStack itemStack, PlayerInteractEvent event) {
    }

    public void playerLeftClickEvent(Player player, ItemStack itemStack, PlayerInteractEvent event) {
    }
}
