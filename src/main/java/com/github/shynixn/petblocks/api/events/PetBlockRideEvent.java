package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockRideEvent extends PetBlockCancelAbleEvent {
    private boolean isRiding;

    public PetBlockRideEvent(PetBlock petBlock, boolean isRiding) {
        super(petBlock);
        this.isRiding = isRiding;
    }

    public boolean isRiding() {
        return this.isRiding;
    }
}

