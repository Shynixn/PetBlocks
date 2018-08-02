package com.github.shynixn.petblocks.bukkit.logic.business.commandexecutor;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.business.enumeration.Permission;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager;
import com.github.shynixn.petblocks.bukkit.logic.business.helper.PetBlockModifyHelper;
import com.github.shynixn.petblocks.bukkit.nms.v1_13_R1.MaterialCompatibility13;
import com.github.shynixn.petblocks.core.logic.business.entity.PetRunnable;
import com.github.shynixn.petblocks.core.logic.persistence.configuration.Config;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public final class PetDataCommandExecutor extends SimpleCommandExecutor.UnRegistered {
    private final PetBlockManager manager;
    private final Plugin plugin;

    public PetDataCommandExecutor(PetBlockManager petDataManager) throws Exception {
        super(((MemorySection) JavaPlugin.getPlugin(PetBlocksPlugin.class).getConfig().get("petblocks-gui")).getValues(false)
                , JavaPlugin.getPlugin(PetBlocksPlugin.class));
        this.manager = petDataManager;
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin.class);
    }

    /**
     * Can be overwritten to listen to player executed commands
     *
     * @param player player
     * @param args   args
     */
    @Override
    protected void onPlayerExecuteCommand(Player player, String[] args) {
        if (!Config.getInstance().allowPetSpawning(player.getLocation()))
            return;
        if (args.length == 1 && args[0].equalsIgnoreCase("call")) {
            final Optional<PetBlock> optPetBlock;
            if ((optPetBlock = PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player)).isPresent()) {
                optPetBlock.get().teleport(player.getLocation());
            } else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final Optional<PetMeta> optPetMeta = PetDataCommandExecutor.this.manager.getPetMetaController().getFromPlayer(player);
                    optPetMeta.ifPresent(petMeta -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMeta);
                        PetBlocksApi.getDefaultPetBlockController().store(petBlock);
                    }));
                });
            }

            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getCallPetSuccessMessage());

        } else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player).isPresent()) {
                PetBlocksApi.getDefaultPetBlockController().remove(PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player).get());
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getToggleDeSpawnMessage());
            } else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final Optional<PetMeta> optPetMeta = PetDataCommandExecutor.this.manager.getPetMetaController().getFromPlayer(player);
                    optPetMeta.ifPresent(petMeta -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMeta);
                        PetBlocksApi.getDefaultPetBlockController().store(petBlock);
                        player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getToggleSpawnMessage());
                    }));
                });
            }
        } else if (args.length >= 2 && args[0].equalsIgnoreCase("rename") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_RENAME)) {
            this.renameNameCommand(player, args);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && PetBlockModifyHelper.hasPermission(player, Permission.ACTION_CUSTOMSKULL)) {
            this.handleSkinSetting(player, args[1]);
        } else {
            this.manager.gui.open(player);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final PetMeta meta;
                final Optional<PetMeta> optPetMeta;
                if (!(optPetMeta = this.manager.getPetMetaController().getFromPlayer(player)).isPresent()) {
                    final PetMeta petMeta = this.manager.getPetMetaController().create(player);
                    this.manager.getPetMetaController().store(petMeta);
                    meta = petMeta;
                } else {
                    meta = optPetMeta.get();
                }
                this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.manager.gui.setPage(player, GUIPage.MAIN, meta));

            });
        }
    }

    private void handleSkinSetting(Player player, String message) {
        final Optional<PetBlock> optPetBlock;
        if ((optPetBlock = PetBlocksApi.getDefaultPetBlockController().getFromPlayer(player)).isPresent()) {
            this.renameSkull(player, message, optPetBlock.get().getMeta(), optPetBlock.get());
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final Optional<PetMeta> optPetMeta = this.manager.getPetMetaController().getFromPlayer(player);
                optPetMeta.ifPresent(petMeta -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.renameSkull(player, message, petMeta, null)));
            });
        }
    }

    private void renameNameCommand(Player player, String[] args) {
        try {
            final String message = this.mergeArgs(args);
            if (message.length() > Config.getInstance().getDesign_maxPetNameLength()) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
            } else {
                this.providePetblock(player, (meta, petBlock) -> {
                    PetBlockModifyHelper.rename(meta, petBlock, message);
                    player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingSuccessMessage());
                    this.persistAsynchronously(meta);
                });
            }
        } catch (final Exception e) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
        }
    }

    private void renameSkull(Player player, String message, PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > 20) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
        } else {
            try {
                petMeta.setSkin(MaterialCompatibility13.getIdFromMaterial(Material.SKULL_ITEM), 3, message, false);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingSuccessMessage());
            } catch (final Exception e) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
            }
        }
    }

    private void providePetblock(Player player, PetRunnable runnable) {
        final Optional<PetBlock> optPetBlock;
        if ((optPetBlock = this.manager.getPetBlockController().getFromPlayer(player)).isPresent()) {
            runnable.run(optPetBlock.get().getMeta(), optPetBlock.get());
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                if (!this.manager.getPetMetaController().hasEntry(player))
                    return;
                final Optional<PetMeta> optPetMeta = this.manager.getPetMetaController().getFromPlayer(player);
                optPetMeta.ifPresent(petMeta -> this.plugin.getServer().getScheduler().runTask(this.plugin, () -> runnable.run(petMeta, null)));
            });
        }
    }

    private String mergeArgs(String[] args) {
        final StringBuilder builder = new StringBuilder();
        for (int i = 1; i < args.length; i++) {
            if (builder.length() != 0) {
                builder.append(' ');
            }
            builder.append(args[i]);
        }
        return builder.toString();
    }

    private void persistAsynchronously(PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.getPetMetaController().store(petMeta));
    }
}
