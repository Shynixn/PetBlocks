package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.lib.SimpleCommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

class PetBlockReloadCommandExecutor extends SimpleCommandExecutor.Registered {
    /**
     * Initializes a new commandExecutor by command, plugin
     *
     * @param command command
     * @param plugin  plugin
     */
    PetBlockReloadCommandExecutor(String command, Plugin plugin) {
        super(command, (JavaPlugin) plugin);
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
        Language.reload(this.plugin);
        player.sendMessage(Language.PREFIX + "Reloaded PetBlocks.");
    }
}
