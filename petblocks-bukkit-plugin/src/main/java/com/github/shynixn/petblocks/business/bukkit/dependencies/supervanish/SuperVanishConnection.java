package com.github.shynixn.petblocks.business.bukkit.dependencies.supervanish;

import com.github.shynixn.petblocks.api.PetBlocksApi;
import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.lib.BukkitUtilities;
import com.github.shynixn.petblocks.lib.SimpleListener;
import de.myzelyam.api.vanish.PlayerHideEvent;
import de.myzelyam.api.vanish.PlayerShowEvent;
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
        for (final Player player : BukkitUtilities.getOnlinePlayers()) {
            if (!player.equals(petBlock.getOwner())) {
                visibilityManager.hidePetBlock(petBlock, player);
            }
        }
        PetBlocksApi.getPetMeta(petBlock.getOwner()).setVisible(false);
    }

    private static void showToAll(PetBlock petBlock) {
        for (final Player player : BukkitUtilities.getOnlinePlayers()) {
            if (!player.equals(petBlock.getOwner())) {
                visibilityManager.showPetBlock(petBlock, player);
            }
        }
        PetBlocksApi.getPetMeta(petBlock.getOwner()).setVisible(true);
    }

    private static class SuperVanishListener extends SimpleListener {
        SuperVanishListener(JavaPlugin plugin) {
            super(plugin);
        }

        @EventHandler
        public void onPlayerHideEvent(PlayerHideEvent event) {
            if (PetBlocksApi.hasPetBlock(event.getPlayer())) {
                hideFromAll(PetBlocksApi.getPetBlock(event.getPlayer()));
            }
        }

        @EventHandler
        public void onPlayerShowEvent(PlayerShowEvent event) {
            if (PetBlocksApi.hasPetBlock(event.getPlayer())) {
                showToAll(PetBlocksApi.getPetBlock(event.getPlayer()));
            }
        }

        private PetBlock[] getPetBlocks() {
            final List<PetBlock> petBlocks = new ArrayList<>();
            for (final Player player : BukkitUtilities.getOnlinePlayers()) {
                if (PetBlocksApi.hasPetBlock(player)) {
                    petBlocks.add(PetBlocksApi.getPetBlock(player));
                }
            }
            return petBlocks.toArray(new PetBlock[petBlocks.size()]);
        }


        @EventHandler
        public void onPlayerJoinEvent(PlayerJoinEvent event) {
            final Player player = event.getPlayer();
            for (final PetBlock petBlock : this.getPetBlocks()) {
                if (!petBlock.getPetMeta().isVisible()) {
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
    }
}
