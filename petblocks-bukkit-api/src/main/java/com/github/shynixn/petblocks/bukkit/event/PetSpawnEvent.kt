package com.github.shynixn.petblocks.bukkit.event

import com.github.shynixn.petblocks.bukkit.Pet

/**
 * This event is called when the pet tries to spawn.
 */
class PetSpawnEvent(pet: Pet) : PetEvent(pet)
