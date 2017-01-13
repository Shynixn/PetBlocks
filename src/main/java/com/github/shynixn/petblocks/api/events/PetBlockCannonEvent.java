package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockCannonEvent extends PetBlockCancelAbleEvent {
    public PetBlockCannonEvent(PetBlock petBlock) {
        super(petBlock);
    }
}
