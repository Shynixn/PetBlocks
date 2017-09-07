package com.github.shynixn.petblocks.api.bukkit.event;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockCancelAbleEvent extends PetBlockEvent {
    private boolean isCanceled;

    public PetBlockCancelAbleEvent(PetBlock petBlock) {
        super(petBlock);
    }

    public boolean isCanceled() {
        return this.isCanceled;
    }

    public void setCanceled(boolean isCanceled) {
        this.isCanceled = isCanceled;
    }
}
