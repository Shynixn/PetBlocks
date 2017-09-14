package com.github.shynixn.petblocks.business.logic.business;

import com.github.shynixn.petblocks.api.business.entity.PetBlock;
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta;

@FunctionalInterface
public interface PetRunnable {
    void run(PetMeta petMeta, PetBlock petBlock);
}