package com.github.shynixn.petblocks.contract

import com.github.shynixn.petblocks.entity.PetSpawnResult
import org.bukkit.Location
import org.bukkit.entity.Player
import java.util.concurrent.CompletionStage

interface PetService : AutoCloseable {
    /**
     * Gets the pet cache.
     * Using this cache should be avoided and only for critical compatibility bridges.
     * e.g. DependencyPlaceHolderApi.
     */
    fun getCache(): Map<Player, MutableList<Pet>>

    /**
     * Gets all the pets a player owns.
     * The pets may or be not be spawned at the moment.
     */
    suspend fun getPetsFromPlayer(player: Player): List<Pet>

    /**
     * Gets all the pets a player owns.
     * The pets may or be not be spawned at the moment.
     */
    fun getPetsFromPlayerAsync(player: Player): CompletionStage<List<Pet>>

    /**
     * Creates a pet for the given player, at the given location, with the given template.
     * Throws an exception if template id does not exist.
     */
    suspend fun createPet(player: Player, location: Location, templateId: String, name: String): PetSpawnResult

    /**
     * Adds a pet for the given player, at the given location, with the given template.
     * Throws an exception if template id does not exist.
     */
    fun createPetAsync(
        player: Player,
        location: Location,
        templateId: String,
        name: String
    ): CompletionStage<PetSpawnResult>


    /**
     * Clears all currently cached pets for the player.
     * The pets are not deleted but removed from memory.
     */
    suspend fun clearCache(player: Player)

    /**
     *  Deletes the given pet.
     */
    suspend fun deletePet(pet: Pet)

    /**
     *  Deletes the given pet.
     */
    fun deletePetAsync(pet: Pet): CompletionStage<Void?>
}
