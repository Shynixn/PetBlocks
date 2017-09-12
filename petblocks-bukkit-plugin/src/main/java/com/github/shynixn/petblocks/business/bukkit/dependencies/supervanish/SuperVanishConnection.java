package com.github.shynixn.petblocks.business.bukkit.dependencies.supervanish;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.lib.SimpleListener;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public final class SuperVanishConnection {
    private static VisibilityManager visibilityManager;

    public static void register(JavaPlugin plugin) {
        visibilityManager = new VisibilityManager();
        new SuperVanishListener(plugin);
    }

    private static void hideFromAll(PetBlock petBlock) {
        for (final Player player :getOnlinePlayers()) {
            if (!player.equals(petBlock.getPlayer())) {
                visibilityManager.hidePetBlock(petBlock, player);
            }
        }
        petBlock.getMeta().setVisible(false);
    }

    private static List<Player> getOnlinePlayers() {
        final List<Player> players = new ArrayList<>();
        for (final World world : Bukkit.getWorlds()) {
            players.addAll(world.getPlayers());
        }
        return players;
    }


    private static void showToAll(PetBlock petBlock) {
        for (final Player player : getOnlinePlayers()) {
            if (!player.equals(petBlock.getPlayer())) {
                visibilityManager.showPetBlock(petBlock, player);
            }
        }
        petBlock.getMeta().setVisible(true);
    }

    private static class SuperVanishListener extends SimpleListener {
        SuperVanishListener(JavaPlugin plugin) {
            super(plugin);
        }

        @EventHandler
        public void onPlayerHideEvent(PlayerHideEvent event) {
            final PetBlock petBlock;
            if ((petBlock = this.getPetBlock(event.getPlayer())) != null) {
                hideFromAll(petBlock);
            }
        }

        @EventHandler
        public void onPlayerShowEvent(PlayerShowEvent event) {
            final PetBlock petBlock;
            if ((petBlock = this.getPetBlock(event.getPlayer())) != null) {
                showToAll(petBlock);
            }
        }

        private PetBlock[] getPetBlocks() {
            final List<PetBlock> petBlocks = new ArrayList<>();
            PetBlock petBlock;
            for (final Player player : getOnlinePlayers()) {
                if ((petBlock = this.getPetBlock(player)) != null) {
                    petBlocks.add(petBlock);
                }
            }
            return petBlocks.toArray(new PetBlock[petBlocks.size()]);
        }

        @EventHandler
        public void onPlayerJoinEvent(PlayerJoinEvent event) {
            final Player player = event.getPlayer();
            for (final PetBlock petBlock : this.getPetBlocks()) {
                if (!petBlock.getMeta().isVisible()) {
                    visibilityManager.hidePetBlock(petBlock, player);
                }
            }
        }

        @EventHandler
        public void onPlayerQuitEvent(PlayerQuitEvent event) {
            final Player player = event.getPlayer();
            for (final PetBlock petBlock : this.getPetBlocks()) {
                visibilityManager.showPetBlock(petBlock, player);
            }
        }

        private PetBlock getPetBlock(Player player) {
            return PetBlocksApi.getDefaultPetBlockController().getByPlayer(player);
        }
    }
}
