package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.configuration.Config;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.configuration.Permission;
import com.github.shynixn.petblocks.business.logic.persistence.entity.PetData;
import com.github.shynixn.petblocks.lib.SimpleCommandExecutor;
import org.bukkit.Material;
import org.bukkit.configuration.MemorySection;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

class PetDataCommandExecutor extends SimpleCommandExecutor.UnRegistered {
    private final PetDataManager manager;
    private final Plugin plugin;

    PetDataCommandExecutor(PetDataManager petDataManager) throws Exception {
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
    public void onPlayerExecuteCommand(Player player, String[] args) {
        if (!Config.getInstance().allowPetSpawning(player.getLocation()))
            return;
        if (args.length == 1 && args[0].equalsIgnoreCase("call")) {
            final PetBlock petBlock;
            if ((petBlock = PetBlocksApi.getDefaultPetBlockController().getByPlayer(player)) != null)
                petBlock.teleport(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (PetBlocksApi.getDefaultPetBlockController().getByPlayer(player) != null)
                PetBlocksApi.getDefaultPetBlockController().remove(PetBlocksApi.getDefaultPetBlockController().getByPlayer(player));
            else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = PetDataCommandExecutor.this.manager.getPetMeta(player);
                    if (petMeta != null) {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                            final PetBlock petBlock = PetBlocksApi.getDefaultPetBlockController().create(player, petMeta);
                            PetBlocksApi.getDefaultPetBlockController().store(petBlock);
                        });
                    }
                });
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("rename") && player.hasPermission(Permission.RENAMEPET.get())) {
            this.handleNaming(player, args[1], false);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && player.hasPermission(Permission.RENAMESKULL.get())) {
            this.handleNaming(player, args[1], true);
        } else {
            this.manager.gui.open(player);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final PetMeta petMeta;
                if ((petMeta = this.manager.getPetMeta(player)) != null) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> {
                        this.manager.gui.setPage(player, GUIPage.MAIN, petMeta);
                    });
                }
            });
        }
    }
    private void handleNaming(Player player, String message, boolean skullNaming) {
        PetBlock petBlock;
        if ((petBlock = PetBlocksApi.getDefaultPetBlockController().getByPlayer(player)) != null) {
            if (skullNaming) {
                this.renameSkull(player, message, petBlock.getMeta(), petBlock);
            } else {
                this.renameName(player, message, petBlock.getMeta(), petBlock);

            }
        } else {
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = this.manager.getPetMeta(player);
                if (skullNaming) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.renameSkull(player, message, petMeta, null));
                } else {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.renameName(player, message, petMeta, null));
                }
            });
        }
    }

    private void renameName(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > ConfigPet.getInstance().getDesign_maxPetNameLength()) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
        } else {
            try {
                petMeta.setDisplayName(message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingSuccessMessage());
            } catch (final Exception e) {
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getNamingErrorMessage());
            }
        }
    }

    private void renameSkull(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > 20) {
            player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingErrorMessage());
        } else {
            try {
                ((PetData)petMeta).setSkin(Material.SKULL_ITEM, (short) 3, message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Config.getInstance().getPrefix() + Config.getInstance().getSkullNamingSuccessMessage());
            } catch (final Exception e) {
                player.sendMessage(Config.getInstance().getPrefix() +Config.getInstance().getSkullNamingErrorMessage());
            }
        }
    }

    private void persistAsynchronously(com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.persist(petMeta));
    }
}
