package com.github.shynixn.petblocks.bukkit.event

import com.github.shynixn.petblocks.bukkit.contract.Pet

open class PetEvent(
    /**
     * The pet related to the event.
     */
    val pet: Pet
) : PetBlocksEvent()
