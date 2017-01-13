package com.github.shynixn.petblocks.api.entities;

import com.github.shynixn.petblocks.business.Permission;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Created by Shynixn
 */
public interface ItemContainer {
    MoveType getMoveType();

    ItemStack generate();

    String[] getLore();

    void setEnabled(boolean enabled);

    int getPosition();

    String[] getLore(Player player, Permission... permission);

    String[] getLore(Player player, String... permission);

    String getSkullName();

    int getId();

    int getDamage();

    Movement getMovement();
}
