package com.github.shynixn.petblocks.impl

import com.github.shynixn.mccoroutine.bukkit.CoroutineTimings
import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mccoroutine.bukkit.minecraftDispatcher
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetEntityFactory
import com.github.shynixn.petblocks.entity.PetMeta
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PetRidingState
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.event.PetRemoveEvent
import com.github.shynixn.petblocks.event.PetSpawnEvent
import com.github.shynixn.petblocks.exception.PetBlocksPetDisposedException
import kotlinx.coroutines.delay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector

/**
 * A facade to handle a single pet.
 */
class PetImpl(
    /**
     * Gets the owner of the pet.
     */
    private var playerParam: Player?,
    private val petMeta: PetMeta,
    private val petEntityFactory: PetEntityFactory,
    plugin: Plugin
) : Pet {
    private var petEntity: PetEntityImpl? = null
    private var disposed = false
    private var templateCache: PetTemplate? = null

    init {
        plugin.launch(plugin.minecraftDispatcher + object : CoroutineTimings() {}) {
            // Remove pet if the player does not have any spawn or call permission.
            while (!isDisposed) {
                if (!player.hasPermission(Permission.SPAWN.text) && !player.hasPermission(Permission.CALL.text)) {
                    petEntity?.remove()
                    petEntity = null
                    petMeta.isSpawned = false
                }

                delay(5000)
            }
        }

        plugin.launch {
            if (petMeta.ridingState == PetRidingState.HAT) {
                delay(200)
                hat()
            } else if (petMeta.ridingState == PetRidingState.GROUND) {
                delay(200)
                ride()
            }
        }
    }

    /**
     * Identifier. Each pet of a player needs to have a unique identifier how to interact with it.
     */
    override var name: String
        get() {
            return petMeta.name
        }
        set(value) {
            if (isDisposed) {
                throw PetBlocksPetDisposedException()
            }

            petMeta.name = value
        }

    /**
     * Gets or sets the displayName of the pet. ChatColors are automatically translated to be visible in game.
     */
    override var displayName: String
        get() {
            return petMeta.displayName
        }
        set(value) {
            if (isDisposed) {
                throw PetBlocksPetDisposedException()
            }

            petMeta.displayName = value.translateChatColors()
            petEntity?.updateDisplayName(petMeta.displayName)
        }

    /**
     * Gets if the pet is currently atleast visible to the owner.
     * Check [visibility] to see if other players can see the pet as well.
     */
    override val isSpawned: Boolean
        get() {
            return petEntity != null
        }

    /**
     * Gets the owner of the pet.
     */
    override val player: Player
        get() {
            if (isDisposed) {
                throw PetBlocksPetDisposedException()
            }

            return playerParam!!
        }

    /**
     * Gets or sets the current location of the pet.
     * Can also be used while the pet is not spawned.
     */
    override var location: Location
        get() {
            if (petEntity != null) {
                return petEntity!!.getLocation().toLocation()
            }

            return petMeta.lastStoredLocation.toLocation()
        }
        set(value) {
            if (isDisposed) {
                throw PetBlocksPetDisposedException()
            }

            petMeta.lastStoredLocation = value.toVector3d()

            if (petEntity != null) {
                if (value.world?.name != petMeta.lastStoredLocation.world) {
                    remove()
                    spawn()
                } else {
                    petEntity?.teleportInWorld(value.toVector3d())
                }
            }
        }


    /**
     * Movement Velocity vector.
     */
    override var velocity: Vector
        get() {
            if (petEntity != null) {
                return petEntity!!.getVelocity().toVector()
            }

            return Vector(0.0, 0.0, 0.0)
        }
        set(value) {
            if (petEntity != null) {
                petEntity!!.setVelocity(value.toVector3d())
            }
        }

    /**
     * Gets or sets the visibility of the pet.
     */
    override var visibility: PetVisibility
        get() {
            return petMeta.visibility
        }
        set(value) {
            if (isDisposed) {
                throw PetBlocksPetDisposedException()
            }

            val previousVisibility = petMeta.visibility
            petMeta.visibility = value

            if (previousVisibility != value && petEntity != null) {
                petEntity!!.updateVisibility(visibility, player, location)
            }
        }

    /**
     * Gets or sets the itemStack being rendered.
     */
    override var headItemStack: ItemStack
        get() {
            return petMeta.headItem.toItemStack()
        }
        set(value) {
            petMeta.headItem = value.toItem()
            petEntity?.updateHeadItemStack(petMeta.headItem.toItemStack())
        }

    /**
     * Gets or sets the itemStack in ItemFormat.
     */
    override var headItem: Item
        get() {
            return petMeta.headItem
        }
        set(value) {
            petMeta.headItem = value
            petEntity?.updateHeadItemStack(petMeta.headItem.toItemStack())
        }

    /**
     * Gets or sets the execution loop of the pet.
     */
    override var loop: String
        get() {
            return petMeta.loop
        }
        set(value) {
            petMeta.loop = value
        }

    /**
     * Gets if the pet has been disposed. The pet can no longer be used then.
     */
    override val isDisposed: Boolean
        get() {
            return disposed
        }

    /**
     * Gets or sets the template.
     */
    override var template: PetTemplate
        get() {
            return templateCache!!
        }
        set(value) {
            templateCache = value
            petMeta.template = value.name
        }

    /**
     * Calculated variables which can be used in subsequent operations by placeholders.
     */
    override var javaScriptMemory: MutableMap<String, String> = HashMap()

    /**
     * Calls the pet to the player. Spawns the pet if it is not spawned, and places the pet
     * right in front of the player.
     */
    override fun call() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petEntity != null) {
            // Cross World Call.
            val playerWorld = player.location.world
            val petWorld = location.world

            if (playerWorld != petWorld) {
                petEntity!!.remove()
                petEntity = null
            }
        }

        val inFrontOfOwnerPosition = player.location.toVector3d().addRelativeFront(3.0)
        inFrontOfOwnerPosition.yaw += 180
        inFrontOfOwnerPosition.pitch *= -1

        if (petEntity == null) {
            petMeta.lastStoredLocation = inFrontOfOwnerPosition
            spawn()
        } else {
            petEntity!!.teleportInWorld(inFrontOfOwnerPosition)
        }
    }

    /**
     * Hides the pet for the owner and other players.
     * The current location of the pet is stored.
     */
    override fun remove() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petEntity == null) {
            return
        }

        val event = PetRemoveEvent(this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        petEntity!!.remove()
        petEntity = null
        petMeta.isSpawned = false
    }

    /**
     * Shows the pet for the owner (and other players depending on the visibility) at the location the pet remembers.
     */
    override fun spawn() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petEntity != null) {
            return
        }

        val event = PetSpawnEvent(this)
        Bukkit.getPluginManager().callEvent(event)

        if (event.isCancelled) {
            return
        }

        petEntity = petEntityFactory.createPetEntity(this, petMeta)
        petMeta.isSpawned = true
    }

    /**
     * Starts riding the pet on the ground.
     * Spawns the pet if it is not spawned.
     */
    override fun ride() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        call()
        petMeta.ridingState = PetRidingState.GROUND
        petEntity?.updateRidingState(player)
    }

    /**
     * Starts wearing the pet as a hat.
     * Spawns the pet if it is not spawned.
     */
    override fun hat() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        call()
        val currentLocation = location
        val playerLocation = player.location
        currentLocation.yaw = playerLocation.yaw
        currentLocation.pitch = 0.0F
        location = currentLocation
        petMeta.ridingState = PetRidingState.HAT
        petEntity?.updateRidingState(player)
    }

    /**
     * Stops riding or flying if the pet currently performs it.
     */
    override fun umount() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petMeta.ridingState == PetRidingState.NO) {
            return
        }

        petMeta.ridingState = PetRidingState.NO

        if (petEntity != null) {
            petEntity!!.updateRidingState(player)
            call()
        }
    }

    /**
     * Turns the pet to look at the given location.
     */
    override fun lookAt(location: Location) {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        val sourceLocation = this.location
        location.world = sourceLocation.world
        val targetLocation = location.toVector()
        val directionVector = targetLocation.subtract(sourceLocation.toVector())
        sourceLocation.setDirection(directionVector)
        this.location = sourceLocation
    }

    /**
     * Letss the pet path find to the given location.
     * If the pet is too far away from the given location, the call will be ignored.
     */
    override fun moveTo(location: Location, speed: Double): Boolean {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (!isSpawned) {
            return false
        }

        val sourceLocation = this.location
        location.world = sourceLocation.world
        val distanceBetweenLocations = location.distance(sourceLocation)

        if (distanceBetweenLocations > 20) {
            return false
        }

        petEntity!!.moveToLocation(location, speed)
        return true
    }

    /**
     * Is the owner riding on the pet.
     */
    override fun isRiding(): Boolean {
        return petEntity != null && petMeta.ridingState == PetRidingState.GROUND
    }

    /**
     * Is owner wearing the pet on its head?
     */
    override fun isHat(): Boolean {
        return petEntity != null && petMeta.ridingState == PetRidingState.HAT
    }

    /**
     * Is the pet mounted as a hat or is someone riding or flying it?
     */
    override fun isMounted(): Boolean {
        return petEntity != null && petMeta.ridingState != PetRidingState.NO
    }

    /**
     * Permanently disposes this pet. Once disposed, this instance can no longer be used.
     */
    override fun dispose() {
        if (isDisposed) {
            return
        }

        disposed = true
        playerParam = null
        petEntity?.remove()
        petEntity = null
    }
}
