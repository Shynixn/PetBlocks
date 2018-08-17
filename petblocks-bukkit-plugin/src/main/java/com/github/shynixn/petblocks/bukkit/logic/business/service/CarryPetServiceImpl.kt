@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.business.service.CarryPetService
import com.github.shynixn.petblocks.api.business.service.ConfigurationService
import com.github.shynixn.petblocks.bukkit.logic.business.PetBlockManager
import com.github.shynixn.petblocks.bukkit.logic.business.helper.*
import com.google.inject.Inject
import org.bukkit.Material
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.*
import java.util.concurrent.CompletableFuture

/**
 * Created by Shynixn 2018.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2018 by Shynixn
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * <p>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
class CarryPetServiceImpl @Inject constructor(private val plugin: Plugin, private val configurationService: ConfigurationService) : CarryPetService {
    private val carryingPet: MutableMap<Player, ItemStack> = HashMap()

    /**
     * Starts the given [player] carry his pet if is is currently spawned.
     * Does nothing if the pet is already getting carried.
     */
    override fun <P> carryPet(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val allowCarry = configurationService.findValue<Boolean>("pet.follow.carry")

        if (!allowCarry) {
            return
        }

        val pet = PetBlockManager.instance.petBlockController.getFromPlayer(player)
        val itemInHand = player.inventory.getItemStackInHand(true)

        if (!pet.isPresent) {
            return
        }

        if (!itemInHand.isPresent || itemInHand.get().type == Material.AIR) {
            val cachePet = (pet.get().armorStand as ArmorStand).helmet.clone()
            carryingPet[player] = cachePet
            player.inventory.setItemStackInHand(cachePet, true)
            PetBlockManager.instance.petBlockController.remove(pet.get())
            player.inventory.updateInventory()
        }
    }

    /**
     * Lets the given [player] drop his pet if he is currently carrying it.
     * Does nothing if the player isn't carrying it.
     */
    override fun <P> dropPet(player: P): CompletableFuture<Void> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val completableFuture = CompletableFuture<Void>()

        if (!carryingPet.containsKey(player)) {
            return completableFuture
        }

        carryingPet.remove(player)
        player.inventory.setItemStackInHand(null, true)

        async(plugin) {
            val petMeta = PetBlockManager.instance.petMetaController.getFromPlayer(player)

            sync(plugin) {
                val petBlock = PetBlockManager.instance.petBlockController.create(player, petMeta.get())
                PetBlockManager.instance.petBlockController.store(petBlock)

                completableFuture.complete(null)
            }
        }

        return completableFuture
    }

    /**
     * Lets the given [player] throw his pet if he is currently carrying.
     * Does automatically drop it and does nothing if the player isn't carrying it.
     */
    override fun <P> throwPet(player: P): CompletableFuture<Void> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        val completableFuture = CompletableFuture<Void>()

        if (!carryingPet.containsKey(player)) {
            return completableFuture
        }

        dropPet(player).thenAccept {
            val pet = PetBlockManager.instance.petBlockController.getFromPlayer(player).get()
            (pet.engineEntity as LivingEntity).velocity = this.generateLaunchingVector(player)

            completableFuture.complete(null)
        }

        return completableFuture
    }

    /**
     * Gets the itemstack from the carrying pet.
     * Returns an empty optional if the player is carrying anything.
     */
    override fun <P, I> getCarryPetItemStack(player: P): Optional<I> {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return if (isCarryingPet(player)) {
            Optional.of(this.carryingPet[player]!!) as Optional<I>
        } else {
            Optional.empty()
        }
    }

    /**
     * Gets if the given player is carrying a pet.
     */
    override fun <P> isCarryingPet(player: P): Boolean {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        return carryingPet.containsKey(player)
    }

    /**
     * Clears all resources the given [player] may have allocated.
     */
    override fun <P> clearResources(player: P) {
        if (player !is Player) {
            throw IllegalArgumentException("Player has to be a BukkitPlayer!")
        }

        dropPet(player)
    }


    /**
     * Returns the launch direction for holding pets.
     *
     * @param player player
     * @return launchDirection
     */
    private fun generateLaunchingVector(player: Player): Vector {
        val vector = Vector()
        val rotX = player.location.yaw.toDouble()
        val rotY = player.location.pitch.toDouble()
        vector.y = -Math.sin(Math.toRadians(rotY))
        val h = Math.cos(Math.toRadians(rotY))
        vector.x = -h * Math.sin(Math.toRadians(rotX))
        vector.z = h * Math.cos(Math.toRadians(rotX))
        return vector.multiply(1.2)
    }
}