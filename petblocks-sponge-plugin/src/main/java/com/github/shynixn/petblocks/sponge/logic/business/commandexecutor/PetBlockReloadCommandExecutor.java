package com.github.shynixn.petblocks.sponge.logic.business.commandexecutor;

import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;

@Singleton
public final class PetBlockReloadCommandExecutor extends SimpleCommandExecutor {

    @Inject
    public PetBlockReloadCommandExecutor(PluginContainer plugin) {
        super(plugin);
        this.register("petblockreload", "Reloads the petblock configuration.", "petblocks.reload", "You don't have permission.", builder -> {

        });
    }

    /**
     * Can be overwritten to listen to player executed commands.
     *
     * @param player player
     * @param args   args
     */
    @Override
    public void onPlayerExecuteCommand(Player player, CommandContext args) {
        Config.INSTANCE.reload();
        ExtensionMethodsKt.sendMessage(player,Config.INSTANCE.getPrefix() + "Reloaded PetBlocks.");
    }
}
