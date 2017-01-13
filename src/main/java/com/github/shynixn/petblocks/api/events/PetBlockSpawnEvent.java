package com.github.shynixn.petblocks.api.events;

import com.github.shynixn.petblocks.api.entities.PetBlock;

/**
 * Created by Shynixn
 */
public class PetBlockSpawnEvent extends PetBlockCancelAbleEvent {
    public PetBlockSpawnEvent(PetBlock petBlock) {
        super(petBlock);
    }
}
