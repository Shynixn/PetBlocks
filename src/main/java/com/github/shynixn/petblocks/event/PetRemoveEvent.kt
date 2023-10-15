package com.github.shynixn.petblocks.event

import com.github.shynixn.petblocks.contract.Pet

/**
 * This event is called when the pet tries to deSpawn.
 */
class PetRemoveEvent(pet: Pet) : PetEvent(pet) {
}
