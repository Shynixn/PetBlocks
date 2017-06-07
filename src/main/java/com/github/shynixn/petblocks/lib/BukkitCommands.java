package com.github.shynixn.petblocks.lib;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

@Deprecated
public abstract class BukkitCommands implements org.bukkit.command.CommandExecutor {
    private final Plugin plugin;

    protected Plugin getPlugin() {
        return this.plugin;
    }

    public void consoleSendCommandEvent(CommandSender sender, String[] args) {
    }

    public BukkitCommands(String command, Plugin plugin) {
        super();
        if(plugin instanceof JavaPlugin)
             ((JavaPlugin)plugin).getCommand(command).setExecutor(this);
        this.plugin = plugin;
    }

    @Override
    public final boolean onCommand(CommandSender arg0, Command arg1, String arg2, String[] arg3) {
        if (arg0 instanceof Player) {
            this.playerSendCommandEvent((Player) arg0, arg3);
        }
        this.consoleSendCommandEvent(arg0, arg3);
        return true;
    }

    public void playerSendCommandEvent(Player player, String[] args) {
        //
    }
}
