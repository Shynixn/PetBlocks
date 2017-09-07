package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.enumeration.GUIPage;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.bukkit.PetBlocksPlugin;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

class PetDataCommandExecutor extends DynamicCommandHelper {
    private final PetDataManager manager;
    private final Plugin plugin;

    PetDataCommandExecutor(PetDataManager petDataManager) {
        super(ConfigCommands.getInstance().getPetblockGuiCommandContainer());
        this.manager = petDataManager;
        this.plugin = JavaPlugin.getPlugin(PetBlocksPlugin.class);
    }

    @Override
    public void onCommandSend(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;
        final Player player = (Player) sender;
        if (!Config.getInstance().allowPetSpawning(player.getLocation()))
            return;
        if (args.length == 1 && args[0].equalsIgnoreCase("call")) {
            final PetBlock petBlock;
            if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                petBlock.teleport(player);
        } else if (args.length == 1 && args[0].equalsIgnoreCase("toggle")) {
            if (PetBlocksApi.hasPetBlock(player))
                PetBlocksApi.removePetBlock(player);
            else {
                this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                    final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = PetDataCommandExecutor.this.manager.getPetMeta(player);
                    if (petMeta != null) {
                        this.plugin.getServer().getScheduler().runTask(this.plugin, () -> PetBlocksApi.setPetBlock(player, petMeta));
                    }
                });
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("rename") && player.hasPermission(Permission.RENAMEPET.get())) {
            this.handleNaming(player, args[1], false);
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && player.hasPermission(Permission.RENAMESKULL.get())) {
            this.handleNaming(player, args[1], true);
        } else {
            this.manager.gui.open(player);
            this.manager.gui.setPetTypeItems(player);
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
                final PetMeta petMeta;
                if ((petMeta = this.manager.getPetMeta(player)) != null) {
                    this.plugin.getServer().getScheduler().runTask(this.plugin, () -> this.manager.gui.setItems(GUIPage.MAIN, player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), true, petMeta));
                }
            });
        }
    }

    private void handleNaming(Player player, String message, boolean skullNaming) {
        if (PetBlocksApi.hasPetBlock(player)) {
            final PetBlock petBlock = PetBlocksApi.getPetBlock(player);
            if (skullNaming) {
                this.renameSkull(player, message, (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);
            } else {
                this.renameName(player, message, (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) petBlock.getPetMeta(), petBlock);

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
            player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
        } else {
            try {
                petMeta.setDisplayName(message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
            } catch (final Exception e) {
                player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            }
        }
    }

    private void renameSkull(Player player, String message, com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta, PetBlock petBlock) {
        if (message.length() > 20) {
            player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
        } else {
            try {
                petMeta.setSkin(Material.SKULL_ITEM, (short) 3, message);
                this.persistAsynchronously(petMeta);
                if (petBlock != null)
                    petBlock.respawn();
                player.sendMessage(Language.PREFIX + Language.SNAME_SUCCES_MESSAGE);
            } catch (final Exception e) {
                player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
            }
        }
    }

    private void persistAsynchronously(com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta) {
        this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.manager.persist(petMeta));
    }
}
