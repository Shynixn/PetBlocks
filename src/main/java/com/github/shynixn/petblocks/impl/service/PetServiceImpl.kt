package com.github.shynixn.petblocks.impl.service

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.ticks
import com.github.shynixn.mcutils.common.ConfigurationService
import com.github.shynixn.mcutils.common.item.Item
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
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.concurrent.CompletableFuture
import java.util.concurrent.CompletionStage
import java.util.logging.Level

class PetServiceImpl(
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
        val completeAbleFuture = CompletableFuture<List<Pet>>()

        plugin.launch {
            val pets = getPetsFromPlayer(player)
            completeAbleFuture.complete(pets)
        }

        return completeAbleFuture
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

                if (e.headItem.skinBase64.isNullOrBlank()) {
                    try {
                        // Ensures that skinBase64 is filled.
                        val originNbt = e.headItem.nbt
                        if (!e.headItem.nbt.isNullOrBlank()) {
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

                // Check if ItemStack can be parsed. If not, fall back to grass block.
                try {
                    itemService.toItemStack(e.headItem)
                } catch (ex: Exception) {
                    e.headItem = Item().also {
                        it.typeName = "minecraft:player_head,397"
                        it.nbt = "{SkullOwner:{Id:[I;-679733089,1513112343,-1218902292,1830955974],Name:\"PetBlocks\",Properties:{textures:[{Value:\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA4YWM1ZTI4ZGJkZmEyMjUwYzYwMjg3Njg2ZGIxNGNjYmViNzc2YzNmMDg2N2M5NTU1YjdlNDk1NmVmYmE3NyJ9fX0=\"}]}}}"
                        it.component = "{\"minecraft:profile\":{\"properties\":[{\"name\":\"textures\",\"value\":\"eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA4YWM1ZTI4ZGJkZmEyMjUwYzYwMjg3Njg2ZGIxNGNjYmViNzc2YzNmMDg2N2M5NTU1YjdlNDk1NmVmYmE3NyJ9fX0=\"}]}}"
                        it.skinBase64 = "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODA4YWM1ZTI4ZGJkZmEyMjUwYzYwMjg3Njg2ZGIxNGNjYmViNzc2YzNmMDg2N2M5NTU1YjdlNDk1NmVmYmE3NyJ9fX0="
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
        val completeAbleFuture = CompletableFuture<PetSpawnResult>()

        plugin.launch {
            val petSpawnResult = createPet(player, location, templateId, name)
            completeAbleFuture.complete(petSpawnResult)
        }

        return completeAbleFuture
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
        petMeta.displayName = placeHolderService.resolvePlaceHolder(
            template.pet.displayName,
            player
        ) // PlaceHolders have to be resolved right now, to not make trouble with the gui.
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
        pet.remove() // Invoke deSpawn command.
        delay(20.ticks)
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
        val completeAbleFuture = CompletableFuture<Void?>()

        plugin.launch {
            deletePet(pet)
            completeAbleFuture.complete(null)
        }

        return completeAbleFuture
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
