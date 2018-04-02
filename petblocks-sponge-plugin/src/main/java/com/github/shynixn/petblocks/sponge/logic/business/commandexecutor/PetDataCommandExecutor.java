package com.github.shynixn.petblocks.sponge.logic.business.commandexecutor;

import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager;
import com.google.inject.Inject;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;

import java.util.Optional;

public final class PetDataCommandExecutor extends SimpleCommandExecutor {

    @Inject
    private PetBlocksManager manager;

    @Inject
    public PetDataCommandExecutor(PluginContainer pluginContainer) {
        super(pluginContainer);
    }

    /**
     * Can be overwritten to listen to player executed commands.
     *
     * @param player player
     * @param args   args
     */
    @Override
    public void onPlayerExecuteCommand(Player player, CommandContext args) {
        this.manager.gui.open(player);
        Task.builder().async().execute(() -> {
            Optional<PetMeta> petMeta;
            PetMeta meta;
            if (!(petMeta = this.manager.getPetMetaController().getFromPlayer(player)).isPresent()) {
                meta = this.manager.getPetMetaController().create(player);
                this.manager.getPetMetaController().store(meta);
            } else {
                meta = petMeta.get();
            }
            Task.builder().execute(() -> this.manager.gui.setPage(player, GUIPage.MAIN, meta)).submit(this.plugin);
        }).submit(this.plugin);
    }
}
