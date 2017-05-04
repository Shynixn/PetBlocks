package com.github.shynixn.petblocks.business.logic.persistence2;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import com.github.shynixn.petblocks.lib.util.IDatabaseController;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by Shynixn
 */
public class PetDataCommandExecutor extends DynamicCommandHelper{

    private Plugin plugin;

    PetDataCommandExecutor(Plugin plugin) {
        super(ConfigCommands.getInstance().getPetblockGuiCommandContainer());
    }

    @Override
    public void onCommandSend(CommandSender sender, String[] args) {
        if (!(sender instanceof Player))
            return;
        final Player player = (Player) sender;
        if (!Config.getInstance().allowPetSpawning(player.getLocation()))
            return;
        else if (args.length == 2 && args[0].equalsIgnoreCase("rename") && player.hasPermission(Permission.RENAMEPET.get())) {
            if (args[1].length() > ConfigPet.getInstance().getDesign_maxPetNameLength())
                player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            else {
                Bukkit.getServer().getScheduler().runTaskAsynchronously(plugin, new Runnable() {
                    @Override
                    public void run() {
                        try(IDatabaseController<PetData> controller = Factory.createPetDataController())
                        {


                            player.sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
                        } catch (Exception e) {
                            player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
                        }
                    }
                });



                try {
                    if (PetBlocksApi.hasPetMeta(player))
                        PetBlocksApi.getPetMeta(player).setDisplayName(args[1]);
                    final PetBlock petBlock;
                    if ((petBlock = PetBlocksApi.getPetBlock(player)) != null)
                        petBlock.respawn();

                } catch (final Exception e) {

                }
            }
        }
    }
}
