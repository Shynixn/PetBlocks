package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockDeathEvent extends PetBlockCancelAbleEvent {
    public PetBlockDeathEvent(PetBlock petBlock) {
        super(petBlock);
    }
}
