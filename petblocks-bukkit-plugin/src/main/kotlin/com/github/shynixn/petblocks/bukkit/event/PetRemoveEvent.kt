package com.github.shynixn.petblocks.bukkit.event

import com.github.shynixn.petblocks.bukkit.contract.Pet

/**
 * This event is called when the pet tries to deSpawn.
 */
class PetRemoveEvent(pet: Pet) : PetEvent(pet) {
}
