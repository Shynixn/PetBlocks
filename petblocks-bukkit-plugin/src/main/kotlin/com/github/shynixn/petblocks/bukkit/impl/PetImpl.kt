package com.github.shynixn.petblocks.bukkit.impl

import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.mcutils.common.translateChatColors
import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PetActionExecutionService
import com.github.shynixn.petblocks.bukkit.contract.PetEntityFactory
import com.github.shynixn.petblocks.bukkit.entity.PetMeta
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import com.github.shynixn.petblocks.bukkit.exception.PetBlocksPetDisposedException
import org.bukkit.Location
import org.bukkit.entity.Player

/**
 * A facade to handle a single pet.
 */
class PetImpl(
    /**
     * Gets the owner of the pet.
     */
    private var playerParam: Player?,
    private val petMeta: PetMeta,
    private val template: PetTemplate,
    private val petEntityFactory: PetEntityFactory,
    private val petActionExecutionService: PetActionExecutionService
) : Pet {
    private var petEntity: PetEntityImpl? = null
    private var disposed = false

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
            petEntity?.teleportInWorld(value.toVector3d())
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
     * Gets if the pet has been disposed. The pet can no longer be used then.
     */
    override val isDisposed: Boolean
        get() {
            return disposed
        }

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

        if (petEntity == null) {
            petMeta.lastStoredLocation = inFrontOfOwnerPosition
            petEntity = petEntityFactory.createPetEntity(this, petMeta, template)
        } else {
            petEntity!!.teleportInWorld(inFrontOfOwnerPosition)
        }
    }

    /**
     * Executes the actions defined by rightClicking the pet found in the template.
     */
    override fun rightClick() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petEntity == null) {
            return
        }

        petActionExecutionService.executeAction(this, template.rightClickDefinition)
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

        petEntity!!.remove()
        petEntity = null
    }

    /**
     * Shows the pet for the owner (and other players depending on the visibility) at the location
     * the pet remembers.
     */
    override fun spawn() {
        if (isDisposed) {
            throw PetBlocksPetDisposedException()
        }

        if (petEntity != null) {
            return
        }

        petEntity = petEntityFactory.createPetEntity(this, petMeta, template)
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
