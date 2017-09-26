package com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor;

import com.github.shynixn.petblocks.bukkit.logic.business.configuration.Config;
import com.github.shynixn.petblocks.bukkit.lib.SimpleCommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class PetBlockReloadCommandExecutor extends SimpleCommandExecutor.Registered {
    /**
     * Initializes a new commandExecutor by command, plugin
     *
     * @param plugin  plugin
     */
    public PetBlockReloadCommandExecutor(Plugin plugin) {
        super("petblockreload", (JavaPlugin) plugin);
    }

    /**
     * Can be overwritten to listen to player executed commands
     *
     * @param player player
     * @param args   args
     */
    @Override
    public void onPlayerExecuteCommand(Player player, String[] args) {
        Config.getInstance().reload();
        player.sendMessage(Config.getInstance().getPrefix() + "Reloaded PetBlocks.");
    }
}
