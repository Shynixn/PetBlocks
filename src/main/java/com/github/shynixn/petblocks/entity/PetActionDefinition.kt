package com.github.shynixn.petblocks.entity

class PetActionDefinition {
    /**
     * All actions defined by the holder.
     */
    var actions: List<PetAction> = emptyList()

    /**
     * Ticks until this action can or is executed again.
     */
    var cooldown: Int = 20
}
