package com.github.shynixn.petblocks.event

import com.github.shynixn.petblocks.contract.Pet


open class PetEvent(
    /**
     * The pet related to the event.
     */
    val pet: Pet
) : PetBlocksEvent()
