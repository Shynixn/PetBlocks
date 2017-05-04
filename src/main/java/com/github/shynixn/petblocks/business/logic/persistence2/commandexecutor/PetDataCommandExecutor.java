package com.github.shynixn.petblocks.business.logic.persistence2.commandexecutor;

import com.github.shynixn.petblocks.business.Config;
import com.github.shynixn.petblocks.business.Language;
import com.github.shynixn.petblocks.business.Permission;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigCommands;
import com.github.shynixn.petblocks.business.logic.configuration.ConfigPet;
import com.github.shynixn.petblocks.business.logic.persistence2.Factory;
import com.github.shynixn.petblocks.business.logic.persistence2.IPetDataController;
import com.github.shynixn.petblocks.business.logic.persistence2.PetData;
import com.github.shynixn.petblocks.lib.DynamicCommandHelper;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

/**
 * Created by Shynixn
 */
public class PetDataCommandExecutor extends DynamicCommandHelper {

    private Plugin plugin;

    public PetDataCommandExecutor(Plugin plugin) {
        super(ConfigCommands.getInstance().getPetblockGuiCommandContainer());
        this.plugin = plugin;
    }

    private void renamePet(final Player player, String name) {
        System.out.println("RUN");
        Bukkit.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> {
            System.out.println("EXECUTING");
            try (IPetDataController controller = Factory.createPetDataController()) {
                final PetData petData;
                if ((petData = controller.getByPlayer(player)) != null) {
                    petData.setName(name);
                    controller.store(petData);
                    player.sendMessage(Language.PREFIX + Language.NAME_SUCCES_MESSAGE);
                } else {
                    player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
                }
                System.out.println("SAVED");
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public void onCommandSend(CommandSender sender, final String[] args) {
        if (!(sender instanceof Player))
            return;
        System.out.println("1");
        final Player player = (Player) sender;
        if (player.getLocation() != null && !Config.getInstance().allowPetSpawning(player.getLocation())) {
            System.out.println("NOPE");
            return;
        }
        else if (args.length == 2 && args[0] != null && args[0].equalsIgnoreCase("rename") && player.hasPermission(Permission.RENAMEPET.get())) {
            System.out.println("2");
            if (args[1] == null || args[1].length() > ConfigPet.getInstance().getDesign_maxPetNameLength())
                player.sendMessage(Language.PREFIX + Language.NAME_ERROR_MESSAGE);
            else {
                System.out.println("3");
                this.renamePet(player, args[1]);
                //Missing RESPAWN
            }
        }
    }
}
