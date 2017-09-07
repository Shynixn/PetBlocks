package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.api.entities.PetMeta;
import com.github.shynixn.petblocks.api.events.PetBlockDeathEvent;
import com.github.shynixn.petblocks.business.bukkit.nms.NMSRegistry;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class PetBlockManager {
    private final Plugin plugin;
    Map<Player, PetBlock> petblocks = new HashMap<>();
    PetDataManager dataManager;
    List<Player> carryingPet = new ArrayList<>();
    Map<Player, Integer> timeBlocked = new HashMap<>();

    public PetBlockManager(PetDataManager dataManager, Plugin plugin) {
        super();
        this.plugin = plugin;
        this.dataManager = dataManager;
        if (plugin.getPluginLoader() != null) {
            new PetBlockListener(this, plugin);
            new PetBlockCommandExecutor(this);
            new PetBlockReloadCommandExecutor("petblockreload", plugin);
        }
    }

    public boolean hasPetBlock(Player player) {
        return this.petblocks.containsKey(player);
    }

    public PetBlock getPetBlock(Player player) {
        if (this.hasPetBlock(player))
            return this.petblocks.get(player);
        return null;
    }

    void setPetBlock(final Player player, final PetMeta petMeta) {
        this.setPetBlock(player, petMeta, 0);
    }

    public void setPetBlock(final Player player, final PetMeta petMeta, int delay) {
        if (!this.hasPetBlock(player) && !this.timeBlocked.containsKey(player)) {
            if (delay == 0) {
                this.petblocks.put(player, NMSRegistry.createPetBlock(player.getLocation(), petMeta));
            } else {
                this.plugin.getServer().getScheduler().runTaskLater(this.plugin, () -> PetBlockManager.this.petblocks.put(player, NMSRegistry.createPetBlock(player.getLocation(), petMeta)), 20 * delay);
            }
        }
    }

    public void removePetBlock(Player player) {
        if (this.hasPetBlock(player)) {
            final PetBlockDeathEvent event = new PetBlockDeathEvent(this.petblocks.get(player));
            Bukkit.getPluginManager().callEvent(event);
            final com.github.shynixn.petblocks.api.persistence.entity.PetMeta petMeta = (com.github.shynixn.petblocks.api.persistence.entity.PetMeta) this.petblocks.get(player).getPetMeta();
            this.plugin.getServer().getScheduler().runTaskAsynchronously(this.plugin, () -> this.dataManager.persist(petMeta));
            if (!event.isCanceled()) {
                this.petblocks.get(player).remove();
                this.petblocks.remove(player);
            }
        }
    }

    public void killAll() {
        for (final PetBlock petBlock : this.petblocks.values()) {
            petBlock.remove();
        }
        for (final Player player : this.carryingPet) {
            NMSRegistry.setItemInHand19(player, null, true);
        }
        this.carryingPet.clear();
        this.petblocks.clear();
    }
}
