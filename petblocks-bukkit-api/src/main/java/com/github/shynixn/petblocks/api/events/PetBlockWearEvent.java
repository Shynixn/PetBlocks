package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockWearEvent extends PetBlockCancelAbleEvent {
    private final boolean wearing;

    public PetBlockWearEvent(PetBlock petBlock, boolean wearing) {
        super(petBlock);
        this.wearing = wearing;
    }

    public boolean isWearning() {
        return this.wearing;
    }
}
