package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockMoveEvent extends PetBlockEvent {
    public PetBlockMoveEvent(PetBlock petBlock) {
        super(petBlock);
    }
}
