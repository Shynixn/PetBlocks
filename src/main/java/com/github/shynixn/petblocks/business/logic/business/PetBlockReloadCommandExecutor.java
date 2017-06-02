package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.lib.BukkitCommands;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

class PetBlockReloadCommandExecutor extends BukkitCommands {
    PetBlockReloadCommandExecutor(String command, Plugin plugin) {
        super(command, plugin);
    }

    @Override
    public void playerSendCommandEvent(Player player, String[] args) {
        Config.getInstance().reload();
        Language.reload(this.getPlugin());
        player.sendMessage(Language.PREFIX + "Reloaded PetBlocks.");
    }
}
