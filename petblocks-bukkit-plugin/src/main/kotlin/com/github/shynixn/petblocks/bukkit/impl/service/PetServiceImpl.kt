package com.github.shynixn.petblocks.bukkit.impl.service

import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.petblocks.bukkit.contract.*
import com.github.shynixn.petblocks.bukkit.entity.*
import com.github.shynixn.petblocks.bukkit.impl.PetImpl
import com.google.inject.Inject
import kotlinx.coroutines.future.future
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.concurrent.CompletionStage
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class PetServiceImpl @Inject constructor(
    private val petMetaRepository: PlayerDataRepository<PlayerInformation>,
    private val plugin: Plugin,
    private val templateRepository: PetTemplateRepository,
    private val petEntityFactory: PetEntityFactory
) : PetService {
    private val cache = HashMap<Player, MutableList<Pet>>()

    /**
     * Gets all the pets a player owns.
     * The pets may or be not be spawned at the moment.
     */
    override fun getPetsFromPlayerAsync(player: Player): CompletionStage<List<Pet>> {
        return plugin.scope.future {
            getPetsFromPlayer(player)
        }
    }

    /**
     * Clears all currently cached pets for the player.
     * The pets are not deleted but removed from memory.
     */
    override fun clearCache(player: Player) {
        if (!cache.containsKey(player)) {
            return
        }

        val pets = cache.remove(player)!!

        for (pet in pets) {
            pet.dispose()
        }
    }

    /**
     * Gets all pets which are currently loaded in memory
     */
    override fun getActivePets(): List<Pet> {
        val allPets = ArrayList<Pet>()
        for (items in cache.values) {
            allPets.addAll(items)
        }

        return allPets
    }

    /**
     * Gets all the pets a player owns.
     * The pets may or be not be spawned at the moment.
     */
    override suspend fun getPetsFromPlayer(player: Player): List<Pet> {
        if (!cache.containsKey(player)) {
            val playerInformation = petMetaRepository.getByPlayer(player) ?: return emptyList()
            val templates = templateRepository.getAll()
            val pets = playerInformation.pets.map { e ->
                val templateId = e.template
                val template = templates.firstOrNull { inner -> inner.id.equals(templateId, true) }
                    ?: throw IllegalArgumentException("Player '${player.name}' has a pet, which references a template '${templateId}' which  does not exist!")
                createPetInstance(player, e, template)
            }
            cache[player] = pets.toMutableList()
        }

        return cache[player]!!
    }

    /**
     * Adds a pet for the given player, at the given location, with the given template.
     * Throws an exception if template id does not exist.
     */
    override fun createPetAsync(
        player: Player, location: Location, templateId: String
    ): CompletionStage<PetSpawnResult> {
        return plugin.scope.future {
            createPet(player, location, templateId)
        }
    }

    /**
     * Creates a pet for the given player, at the given location, with the given template.
     * Throws an exception if template id does not exist.
     */
    override suspend fun createPet(player: Player, location: Location, templateId: String): PetSpawnResult {
        // Call to make sure the cache is filled.
        getPetsFromPlayer(player)

        // Retrieve template and build petMeta out of it.
        val templates = templateRepository.getAll()
        val template = templates.firstOrNull { e -> e.id.equals(templateId, true) }

        if (template == null) {
            throw IllegalArgumentException("Template '${templateId}' does not exist!")
        }

        val petMeta = PetMeta().also {
            it.name = template.displayName
        }

        // Create pet instance.
        val pet = createPetInstance(player, petMeta, template)

        if (!cache.containsKey(player)) {
            cache[player] = arrayListOf()
        }

        // TODO: Add PetSpawnEvent here.

        // Retrieve existing stored pets and add petMeta to it.
        var playerInformation = petMetaRepository.getByPlayer(player)

        if (playerInformation == null) {
            playerInformation = PlayerInformation()
            petMetaRepository.save(player, playerInformation)
        }

        playerInformation.pets.add(petMeta)

        cache[player]!!.add(pet)
        return PetSpawnResult(PetSpawnResultType.SUCCESS, pet)
    }

    /**
     *  Deletes the given pet.
     */
    override suspend fun deletePet(pet: Pet) {
        pet.dispose()
        val playerInformation = petMetaRepository.getByPlayer(pet.player) ?: return
        val petMetaToDelete = playerInformation.pets.firstOrNull { e -> e.name == pet.name } ?: return
        playerInformation.pets.remove(petMetaToDelete)
    }

    /**
     *  Deletes the given pet.
     */
    override fun deletePetAsync(pet: Pet): CompletionStage<Void?> {
        return plugin.scope.future {
            deletePet(pet)
        }.thenApply { null }
    }

    /**
     * Closes the resource.
     */
    override fun close() {
        for (petList in cache.values) {
            for (pet in petList) {
                pet.dispose()
            }
        }

        cache.values.clear()
    }

    /**
     * Creates a new pet instance.
     */
    private fun createPetInstance(player: Player, petMeta: PetMeta, petTemplate: PetTemplate): Pet {
        val pet = PetImpl(player, petMeta, petTemplate, petEntityFactory)
        return pet
    }
}
