package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

class PetDataCommandExecutor extends DynamicCommandHelper {
    private final PetDataManager manager;

    PetDataCommandExecutor(PetDataManager petDataManager) {
        super(ConfigCommands.getInstance().getPetblockGuiCommandContainer());
        this.manager = petDataManager;
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
            else if (PetBlocksApi.hasPetMeta(player))
                PetBlocksApi.setPetBlock(player, PetBlocksApi.getPetMeta(player));
        } else if (args.length == 2 && args[0].equalsIgnoreCase("rename") && player.hasPermission(Permission.RENAMEPET.get())) {
            if (args[1].length() > ConfigPet.getInstance().getDesign_maxPetNameLength())
                player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            else {
                try {
                    if (PetBlocksApi.hasPetMeta(player))
                        PetBlocksApi.getPetMeta(player).setDisplayName(args[1]);
                    final PetBlock petBlock;
                    if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                        petBlock.respawn();
                    player.sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
                } catch (final Exception e) {
                    player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("skin") && player.hasPermission(Permission.RENAMESKULL.get())) {
            if (args[1].length() > 20)
                player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
            else {
                try {
                    if (PetBlocksApi.hasPetMeta(player))
                        PetBlocksApi.getPetMeta(player).setSkin(Material.SKULL_ITEM, (short) 3, args[1]);
                    final PetBlock petBlock;
                    if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                        petBlock.respawn();
                    player.sendMessage(Language.PREFIX + Language.SNAME_SUCCES_MESSAGE);
                } catch (final Exception e) {
                    player.sendMessage(Language.PREFIX + Language.SNAME_ERROR_MESSAGE);
                }
            }
        } else {
            this.manager.gui.open(player);
            this.manager.gui.setMainItems(player);
            final PetMeta petMeta;
            if ((petMeta = this.manager.getPetMeta(player)) != null)
                this.manager.gui.setMainItems(player, petMeta.getType(), PetBlocksApi.hasPetBlock(player), true);
        }
    }
}
