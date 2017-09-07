package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;
import com.github.shynixn.petblocks.lib.SpigotEvent;
import org.bukkit.entity.Player;

/**
 * Created by Shynixn
 */
public class PetBlockEvent extends SpigotEvent {
    private final PetBlock petBlock;

    public PetBlockEvent(PetBlock petBlock) {
        super();
        if (petBlock == null)
            throw new IllegalArgumentException("PetBlock cannot be null!");
        this.petBlock = petBlock;
    }

    public PetBlock getPetBlock() {
        return this.petBlock;
    }

    public Player getPlayer() {
        return this.petBlock.getOwner();
    }
}
