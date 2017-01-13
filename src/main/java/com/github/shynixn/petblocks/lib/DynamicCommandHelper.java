package com.github.shynixn.petblocks.lib;

import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.ArrayList;

/**
 * Created by Shynixn
 */
public abstract class DynamicCommandHelper extends BukkitCommand {
    private final CommandContainer c;

    public DynamicCommandHelper(CommandContainer c) {
        super(c.getCommand());
        this.c = c;
        this.description = c.getDescription();
        this.usageMessage = c.getUseage();
        this.setPermission(c.getPermission());
        this.setAliases(new ArrayList<String>());
        NMSRegistry.registerDynamicCommand(c.getCommand(), this);
    }

    @Override
    public boolean execute(CommandSender sender, String alias, String[] args) {
        if (!sender.hasPermission(this.getPermission())) {
            sender.sendMessage(this.c.getPermissionMessage());
        } else if (sender instanceof Player) {
            this.onCommandSend(sender, args);
        }
        return true;
    }

    public abstract void onCommandSend(CommandSender sender, String[] args);


    public String getText(String[] args) {
        String s = "";
        for (String k : args) {
            if (!s.equalsIgnoreCase(""))
                s += " ";
            s += k;
        }
        return s;
    }

    public static class CommandContainer {
        private final String command;
        private final boolean enabled;
        private final String useage;
        private final String description;
        private final String permission;
        private final String permissionMessage;

        public CommandContainer(String upper, FileConfiguration c) {
            super();
            this.command = c.getString(upper + ".command");
            this.enabled = c.getBoolean(upper + ".enabled");
            this.useage = c.getString(upper + ".useage");
            this.description = c.getString(upper + ".description");
            this.permission = c.getString(upper + ".permission");
            this.permissionMessage = c.getString(upper + ".permission-message");
        }

        public String getCommand() {
            return this.command;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public String getUseage() {
            return this.useage;
        }

        public String getDescription() {
            return this.description;
        }

        public String getPermission() {
            return this.permission;
        }

        public String getPermissionMessage() {
            return this.permissionMessage;
        }
    }
}
