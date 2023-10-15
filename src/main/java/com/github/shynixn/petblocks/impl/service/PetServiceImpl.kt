package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.mcutils.database.api.PlayerDataRepository
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetEntityFactory
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.contract.PlaceHolderService
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetSpawnResult
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.PetSpawnResultType
import com.github.shynixn.petblocks.event.PetSpawnEvent
import com.github.shynixn.petblocks.impl.PetImpl
import com.google.inject.Inject
import kotlinx.coroutines.future.future
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.UUID
import java.util.concurrent.CompletionStage
import java.util.logging.Level

class PetServiceImpl @Inject constructor(
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val plugin: Plugin,
    private val petEntityFactory: PetEntityFactory,
    private val placeHolderService: PlaceHolderService,
    private val templateRepository: Repository<PetTemplate>
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
    override suspend fun clearCache(player: Player) {
        if (!cache.containsKey(player)) {
            return
        }

        val pets = cache.remove(player)!!

        for (pet in pets) {
            pet.dispose()
        }

        val playerData = petMetaRepository.getByPlayer(player)

        if (playerData != null) {
            plugin.logger.info("Saving pets of player ${player.name}...")
            petMetaRepository.save(player, playerData)
            plugin.logger.info("Saved pets of player ${player.name}...")

            val uuids = HashSet(playerData.retrievedUuids)
            uuids.add(player.uniqueId)

            for (uuid in uuids) {
                plugin.logger.info("Removing cache of $uuid...")
                if (petMetaRepository.getCache().containsKey(uuid)) {
                    petMetaRepository.getCache().remove(uuid)!!.await()
                }
                plugin.logger.info("Removed cache of $uuid.")
            }
        }
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
                val template = templates.firstOrNull { inner -> inner.name.equals(templateId, true) }
                    ?: throw IllegalArgumentException("Player '${player.name}' has a pet, which references a template '${templateId}' which  does not exist!")

                if (template.fileHashCode != e.templateHashCode) {
                    applyPetTemplateToPetMeta(player, template, e)
                    plugin.logger.log(
                        Level.INFO,
                        "Pet '${e.name}' uses template version '${e.templateHashCode}' but template has been changed to version '${template.fileHashCode}'. The latest template version has been applied to this pet."
                    )
                }

                val pet = createPetInstance(player, e, template)

                // Fix pet state.
                if (e.isSpawned) {
                    pet.spawn()
                } else {
                    pet.umount()
                }

                pet
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
        player: Player, location: Location, templateId: String, name: String
    ): CompletionStage<PetSpawnResult> {
        return plugin.scope.future {
            createPet(player, location, templateId, name)
        }
    }

    /**
     * Creates a pet for the given player, at the given location, with the given template.
     * Throws an exception if template id does not exist.
     */
    override suspend fun createPet(
        player: Player,
        location: Location,
        templateId: String,
        name: String
    ): PetSpawnResult {
        // Call to make sure the cache is filled.
        val pets = getPetsFromPlayer(player)

        if (pets.firstOrNull { e -> e.name.equals(name, true) } != null) {
            throw IllegalArgumentException("Pet with the same name already exists!")
        }

        // Retrieve template and build petMeta out of it.
        val templates = templateRepository.getAll()
        val template = templates.firstOrNull { e -> e.name.equals(templateId, true) }

        if (template == null) {
            throw IllegalArgumentException("Template '${templateId}' does not exist!")
        }

        val petMeta = PetMeta()
        petMeta.name = name
        petMeta.template = templateId
        applyPetTemplateToPetMeta(player, template, petMeta)

        // Create pet instance.
        val pet = createPetInstance(player, petMeta, template)

        if (!cache.containsKey(player)) {
            cache[player] = arrayListOf()
        }

        val petSpawnEvent = PetSpawnEvent(pet)
        Bukkit.getPluginManager().callEvent(petSpawnEvent)

        if (petSpawnEvent.isCancelled) {
            return PetSpawnResult(PetSpawnResultType.EVENT_CANCELLED, null)
        }

        // Retrieve existing stored pets and add petMeta to it.
        var playerInformation = petMetaRepository.getByPlayer(player)

        if (playerInformation == null) {
            playerInformation = PlayerInformation()
            plugin.logger.info("Creating database entry for ${player.name} (${player.uniqueId})...")
            petMetaRepository.save(player, playerInformation)
            plugin.logger.info("Created database entry for ${player.name} (${player.uniqueId}).")
        }

        playerInformation.pets.add(petMeta)

        cache[player]!!.add(pet)

        if (petMeta.isSpawned) {
            pet.spawn()
        }

        return PetSpawnResult(PetSpawnResultType.SUCCESS, pet)
    }

    /**
     *  Deletes the given pet.
     */
    override suspend fun deletePet(pet: Pet) {
        val player = pet.player
        pet.dispose()
        val playerInformation = petMetaRepository.getByPlayer(player) ?: return
        val petMetaToDelete = playerInformation.pets.firstOrNull { e -> e.name == pet.name } ?: return
        playerInformation.pets.remove(petMetaToDelete)

        if (cache.containsKey(player)) {
            cache[player]!!.remove(pet)
        }
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
     * Applies data to the petMeta.
     */
    private fun applyPetTemplateToPetMeta(player: Player, template: PetTemplate, petMeta: PetMeta) {
        petMeta.displayName = placeHolderService.replacePlaceHolders(player, template.displayName, null)
        petMeta.templateHashCode = template.fileHashCode
        petMeta.isSpawned = template.isSpawned
        petMeta.visibility = template.visibility
        petMeta.ridingState = template.ridingState
    }

    /**
     * Creates a new pet instance.
     */
    private fun createPetInstance(player: Player, petMeta: PetMeta, petTemplate: PetTemplate): Pet {
        val pet = PetImpl(player, petMeta, petTemplate, petEntityFactory, plugin)
        return pet
    }
}