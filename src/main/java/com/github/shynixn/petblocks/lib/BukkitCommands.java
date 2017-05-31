package com.github.shynixn.petblocks.lib;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public abstract class BukkitCommands implements org.bukkit.command.CommandExecutor {
    private final JavaPlugin plugin;

    protected JavaPlugin getPlugin() {
        return this.plugin;
    }

    public void consoleSendCommandEvent(CommandSender sender, String[] args) {
    }

    public BukkitCommands(String command, JavaPlugin plugin) {
        super();
        plugin.getCommand(command).setExecutor(this);
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
    }
}
