package com.github.shynixn.petblocks.bukkit.nms.v1_9_R1;

import com.github.shynixn.petblocks.bukkit.lib.SimpleListener;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Map;

public final class Listener19 extends SimpleListener {
    private final Map<Player, ItemStack> carryingPet;

    public Listener19(Map<Player, ItemStack> carryingPet, Plugin plugin) {
        super(plugin);
        this.carryingPet = carryingPet;
    }

    @EventHandler
    public void onPlayerSwapItems(PlayerSwapHandItemsEvent event) {
        if (this.carryingPet.containsKey(event.getPlayer())) {
            event.setCancelled(true);
        }
    }
}
