package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.scope
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.Repository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetEntityFactory
import com.github.shynixn.petblocks.contract.PetService
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
import java.util.concurrent.CompletionStage
import java.util.logging.Level

class PetServiceImpl @Inject constructor(
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val plugin: Plugin,
    private val petEntityFactory: PetEntityFactory,
    private val placeHolderService: PlaceHolderService,
    private val templateRepository: Repository<PetTemplate>,
    private val configurationService: ConfigurationService,
    private val itemService: ItemService
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
            playerData.playerName = player.name
            petMetaRepository.save(playerData)
            plugin.logger.log(Level.FINE, "Saved pets of player ${player.name}.")

            val uuids = HashSet(playerData.retrievedUuids)
            uuids.add(player.uniqueId)

            for (uuid in uuids) {
                petMetaRepository.clearByPlayerUUID(uuid)
            }
        }
    }

    /**
     * Gets the pet cache.
     * Using this cache should be avoided and only for critical compatibility bridges.
     * e.g. DependencyPlaceHolderApi.
     */
    override fun getCache(): Map<Player, MutableList<Pet>> {
        return cache
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

                // TODO: Fix compatibility with < 1.20.5. Will be removed in >= year 2025.
                if (e.headItem.skinBase64.isNullOrBlank()) {
                    try {
                        // Ensures that skinBase64 is filled.
                        val originNbt = e.headItem.nbt
                        if(!e.headItem.nbt.isNullOrBlank()){
                            val selector = "{textures:[{Value:\""
                            val nbt = e.headItem.nbt!!
                            val rawSelection = nbt.substring(nbt.indexOf(selector) + selector.length)
                            e.headItem.skinBase64 = rawSelection.replace("}", "").replace("]", "").replace("\"", "")
                        }
                        e.headItem = itemService.toItem(itemService.toItemStack(e.headItem))
                        e.headItem.nbt = originNbt
                    } catch (e: Exception) {
                        e.printStackTrace()
                        // Ignored
                    }
                }

                val pet = createPetInstance(player, e, template, false)

                // Fix pet state.
                if (e.isSpawned) {
                    pet.spawn()
                } else {
                    pet.unmount()
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
        petMeta.displayName = placeHolderService.resolvePlaceHolder(template.pet.displayName, player) // PlaceHolders have to be resolved right now, to not make trouble with the gui.
        petMeta.isSpawned = template.pet.spawned
        petMeta.visibility = template.pet.visibility
        petMeta.ridingState = template.pet.ridingState
        petMeta.isSpawned = template.pet.spawned
        petMeta.loop = template.pet.loop
        petMeta.entityType = template.pet.entityType
        petMeta.isEntityVisible = template.pet.entityVisible
        petMeta.physics.groundOffset = template.pet.physics.groundOffset
        petMeta.headItem =
            itemService.toItem(itemService.toItemStack(template.pet.item)) // Ensures that skinBase64 is filled.

        // Create pet instance.
        val pet = createPetInstance(player, petMeta, template, true)

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
            playerInformation = PlayerInformation().also {
                it.playerUUID = player.uniqueId.toString()
                it.playerName = player.name
            }
            plugin.logger.log(Level.FINE, "Creating database entry for ${player.name} (${player.uniqueId})...")
            petMetaRepository.save(playerInformation)
            plugin.logger.log(Level.FINE, "Created database entry for ${player.name} (${player.uniqueId}).")
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
     * Creates a new pet instance.
     */
    private fun createPetInstance(
        player: Player,
        petMeta: PetMeta,
        petTemplate: PetTemplate,
        applyTemplatePhysics: Boolean
    ): Pet {
        val maxPathfinderDistance = configurationService.findValue<Double>("pet.pathFinderDistance")
        val pet = PetImpl(player, petMeta, petEntityFactory, maxPathfinderDistance, plugin, itemService)
        pet.template = petTemplate

        if (applyTemplatePhysics) {
            petMeta.physics = petTemplate.pet.physics
        }

        return pet
    }
}
