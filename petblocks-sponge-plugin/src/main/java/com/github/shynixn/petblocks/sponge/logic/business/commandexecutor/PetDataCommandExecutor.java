package com.github.shynixn.petblocks.sponge.logic.business.commandexecutor;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.controller.PetMetaController;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.core.logic.business.entity.PetRunnable;
import com.github.shynixn.petblocks.sponge.logic.business.PetBlocksManager;
import com.github.shynixn.petblocks.sponge.logic.business.helper.ExtensionMethodsKt;
import com.github.shynixn.petblocks.sponge.logic.persistence.configuration.Config;
import com.google.inject.Inject;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.scheduler.Task;
import org.spongepowered.api.text.Text;

import java.util.Optional;

public final class PetDataCommandExecutor extends SimpleCommandExecutor {

    @Inject
    private PetBlocksManager manager;

    @Inject
    public PetDataCommandExecutor(PluginContainer pluginContainer, ToggleCommandExecutor toggleCommandExecutor,
                                  RenameCommandExecutor renameCommandExecutor, RenameSkinCommandExecutor renameSkinCommandExecutor,
                                  CallCommandExecutor callCommandExecutor) {
        super(pluginContainer);
        this.register(Config.INSTANCE.getData("petblocks-gui"), builder -> {
            final CommandSpec.Builder toggleCommandBuilder = CommandSpec.builder()
                    .executor(toggleCommandExecutor);
            final CommandSpec.Builder callCommandBuilder = CommandSpec.builder()
                    .executor(callCommandExecutor);
            final CommandSpec.Builder renameCommandBuilder = CommandSpec.builder()
                    .permission(Permission.ACTION_RENAME.getPermission()[1])
                    .arguments(GenericArguments.remainingJoinedStrings(Text.of("name")))
                    .executor(renameCommandExecutor);
            final CommandSpec.Builder setSkinCommandBuilder = CommandSpec.builder()
                    .permission(Permission.ACTION_CUSTOMSKULL.getPermission()[1])
                    .arguments(GenericArguments.remainingJoinedStrings(Text.of("name")))
                    .executor(renameSkinCommandExecutor);
            builder.child(toggleCommandBuilder.build(), "toggle");
            builder.child(renameCommandBuilder.build(), "rename");
            builder.child(setSkinCommandBuilder.build(), "skin");
            builder.child(callCommandBuilder.build(), "call");
        });
    }

    private static void providePetblockData(PluginContainer pluginContainer, PetBlocksManager manager, Player player, PetRunnable runnable) {
        final Optional<PetBlock> petBlock;
        if ((petBlock = manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
            runnable.run(petBlock.get().getMeta(), petBlock.get());
        } else {
            Task.builder().async().execute(() -> {
                if (!manager.getPetMetaController().hasEntry(player))
                    return;
                final Optional<PetMeta> petMeta = manager.getPetMetaController().getFromPlayer(player);
                Task.builder().execute(() -> runnable.run(petMeta.get(), null)).submit(pluginContainer);
            }).submit(pluginContainer);
        }
    }

    private static void storeAsynchronly(PluginContainer pluginContainer, PetMetaController<Player> controller, PetMeta meta) {
        Task.builder().async().execute(() -> controller.store(meta)).submit(pluginContainer);
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
            Optional<PetMeta> optPetMeta;
            PetMeta petMeta;
            if (!(optPetMeta = this.manager.getPetMetaController().getFromPlayer(player)).isPresent()) {
                petMeta = this.manager.getPetMetaController().create(player);
                this.manager.getPetMetaController().store(petMeta);
            } else {
                petMeta = optPetMeta.get();
            }
            Task.builder().execute(() -> this.manager.gui.setPage(player, GUIPage.MAIN, petMeta)).submit(this.plugin);
        }).submit(this.plugin);
    }

    private static class CallCommandExecutor extends SimpleCommandExecutor {
        @Inject
        private PetBlocksManager manager;

        @Inject
        public CallCommandExecutor(PluginContainer plugin) {
            super(plugin);
        }

        /**
         * Can be overwritten to listen to player executed commands.
         *
         * @param player player
         * @param args   args
         */
        @Override
        public void onPlayerExecuteCommand(Player player, CommandContext args) {
            final Optional<PetBlock> petBlock;
            if ((petBlock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
                petBlock.get().teleport(player.getTransform());
            }
        }
    }

    private static class ToggleCommandExecutor extends SimpleCommandExecutor {
        @Inject
        private PetBlocksManager manager;

        @Inject
        public ToggleCommandExecutor(PluginContainer plugin) {
            super(plugin);
        }

        /**
         * Can be overwritten to listen to player executed commands.
         *
         * @param player player
         * @param args   args
         */
        @Override
        public void onPlayerExecuteCommand(Player player, CommandContext args) {
            if (!Config.getInstance().allowPetSpawning(player.getTransform()))
                return;
            final Optional<PetBlock> optPetBlock;
            if ((optPetBlock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
                this.manager.getPetBlockController().remove(optPetBlock.get());
            } else {
                Task.builder().async().execute(() -> {
                    final Optional<PetMeta> optPetMeta = this.manager.getPetMetaController().getFromPlayer(player);
                    optPetMeta.ifPresent(petMeta -> Task.builder().execute(() -> {
                        final PetBlock petBlock = this.manager.getPetBlockController().create(player, petMeta);
                        this.manager.getPetBlockController().store(petBlock);
                    }).submit(this.plugin));
                }).submit(this.plugin);
            }
        }
    }

    private static class RenameCommandExecutor extends SimpleCommandExecutor {

        @Inject
        private PetBlocksManager manager;

        @Inject
        public RenameCommandExecutor(PluginContainer plugin) {
            super(plugin);
        }

        /**
         * Can be overwritten to listen to player executed commands.
         *
         * @param player player
         * @param args   args
         */
        @Override
        public void onPlayerExecuteCommand(Player player, CommandContext args) {
            try {
                final String message = args.<String>getOne("name").get();
                if (message.length() > Config.INSTANCE.getDesign_maxPetNameLength()) {
                    player.sendMessage(ExtensionMethodsKt.translateToText(Config.INSTANCE.getPrefix().concat(Config.getInstance().getNamingErrorMessage())));
                } else {
                    PetDataCommandExecutor.providePetblockData(plugin, manager, player, (meta, petBlock) -> {
                        ExtensionMethodsKt.rename(meta, petBlock, message);
                        player.sendMessage(ExtensionMethodsKt.translateToText(Config.INSTANCE.getPrefix().concat(Config.getInstance().getNamingSuccessMessage())));
                        PetDataCommandExecutor.storeAsynchronly(plugin, manager.getPetMetaController(), meta);
                    });
                }
            } catch (final Exception e) {
                player.sendMessage(ExtensionMethodsKt.translateToText(Config.INSTANCE.getPrefix().concat(Config.getInstance().getNamingErrorMessage())));
            }
        }
    }

    private static class RenameSkinCommandExecutor extends SimpleCommandExecutor {

        private static int MAX_PETSKINLENGTH = 20;

        @Inject
        private PetBlocksManager manager;

        @Inject
        public RenameSkinCommandExecutor(PluginContainer plugin) {
            super(plugin);
        }

        /**
         * Can be overwritten to listen to player executed commands.
         *
         * @param player player
         * @param args   args
         */
        @Override
        public void onPlayerExecuteCommand(Player player, CommandContext args) {
            try {
                final String message = args.<String>getOne("name").get();
                if (message.length() > MAX_PETSKINLENGTH) {
                    player.sendMessage(ExtensionMethodsKt.translateToText(Config.getInstance().getPrefix().concat(Config.getInstance().getSkullNamingErrorMessage())));
                } else {
                    PetDataCommandExecutor.providePetblockData(plugin, manager, player, (meta, petBlock) -> {
                        ExtensionMethodsKt.setSkin(meta, petBlock, message);
                        player.sendMessage(ExtensionMethodsKt.translateToText(Config.getInstance().getPrefix().concat(Config.getInstance().getSkullNamingSuccessMessage())));
                        PetDataCommandExecutor.storeAsynchronly(this.plugin, manager.getPetMetaController(), meta);
                    });
                }
            } catch (final Exception e) {
                player.sendMessage(ExtensionMethodsKt.translateToText(Config.getInstance().getPrefix().concat(Config.getInstance().getSkullNamingErrorMessage())));
            }
        }
    }
}
