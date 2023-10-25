package com.github.shynixn.petblocks.contract

import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.enumeration.PetVisibility
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

interface Pet {
    /**
     * Identifier. Each pet of a player needs to have a unique identifier how to interact with it.
     */
    val name: String

    /**
     * Gets or sets the displayName of the pet. ChatColors are automatically translated to be visible in game.
     */
    var displayName: String

    /**
     * Gets if the pet is currently atleast visible to the owner.
     * Check [visibility] to see if other players can see the pet as well.
     */
    val isSpawned: Boolean

    /**
     * Gets the owner of the pet.
     */
    val player: Player

    /**
     * Gets or sets the current location of the pet.
     * Can also be used while the pet is not spawned.
     */
    var location: Location

    /**
     * Movement Velocity vector.
     */
    var velocity: Vector

    /**
     * Gets or sets the visibility of the pet.
     * Should be used together with [isSpawned] to check if a pet can really be seen by a player at the moment.
     */
    var visibility: PetVisibility

    /**
     * Gets or sets the itemStack being rendered.
     */
    var headItemStack: ItemStack

    /**
     * Gets or sets the itemStack.
     */
    var headItem: Item

    /**
     * Gets or sets the execution loop of the pet.
     */
    var loop : String

    /**
     * Gets if the pet has been disposed. The pet can no longer be used then.
     */
    val isDisposed: Boolean

    /**
     * Gets or sets the template.
     */
    var template: PetTemplate

    /**
     * Calls the pet to the player. Spawns the pet if it is not spawned, and places the pet
     * right in front of the player.
     */
    fun call()

    /**
     * DeSpawns the pet for the owner and other players.
     * The current location of the pet is stored.
     */
    fun remove()

    /**
     * Shows the pet for the owner (and other players depending on the visibility) at the location
     * the pet remembers.
     */
    fun spawn()

    /**
     * Starts riding the pet on the ground.
     * Spawns the pet if it is not spawned.
     */
    fun ride()

    /**
     * Starts wearing the pet as a hat.
     * Spawns the pet if it is not spawned.
     */
    fun hat()

    /**
     * Stops riding, flying or hat if the pet currently performs it.
     */
    fun umount()

    /**
     * Turns the pet to look at the given location.
     *  The world property is ignored.
     */
    fun lookAt(location: Location)

    /**
     * Letss the pet path find to the given location.
     * If the pet is too far away from the given location, the call will be ignored and false will be returned.
     * True if the moving has been accepted.
     *  The world property is ignored.
     */
    fun moveTo(location: Location, speed: Double): Boolean

    /**
     * Is the owner riding on the pet.
     */
    fun isRiding(): Boolean

    /**
     * Is owner wearing the pet on its head?
     */
    fun isHat(): Boolean

    /**
     * Is the pet mounted as a hat or is someone riding or flying it?
     */
    fun isMounted(): Boolean

    /**
     * Permanently disposes this pet. Once disposed, this instance can no longer be used.
     * The pet is not deleted however and can be retrieved again from the [PetService].
     */
    fun dispose()
}
