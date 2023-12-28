package com.github.shynixn.petblocks.contract

import com.github.shynixn.mcutils.common.CancellationToken
import com.github.shynixn.petblocks.enumeration.DropType
import com.github.shynixn.petblocks.impl.PetEntityImpl

interface BreakBlockService {
    /**
     * Breaks the given block.
     */
    fun breakBlock(petEntity: PetEntityImpl, timeToBreakTicks: Int, dropTypes: List<DropType>, token: CancellationToken)
}
