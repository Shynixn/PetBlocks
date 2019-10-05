@file:Suppress("UNCHECKED_CAST")

package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.AIType
import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AICarry
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject

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
class CarryPetServiceImpl @Inject constructor(
    private val petService: PetService,
    private val proxyService: ProxyService,
    private val loggingService: LoggingService,
    private val itemTypeService: ItemTypeService,
    private val concurrencyService: ConcurrencyService
) : CarryPetService {
    private val carryingPet: MutableMap<String, Any> = HashMap()

    /**
     * Starts the given [player] carry his pet if is is currently spawned.
     * Does nothing if the pet is already getting carried.
     */
    override fun <P> carryPet(player: P) {
        if (!petService.hasPet(player)) {
            return
        }

        val pet = petService.getOrSpawnPetFromPlayer(player).get()

        val count = pet.meta.aiGoals.count { p -> p is AICarry }

        if (count == 0) {
            return
        }

        if (count > 1) {
            loggingService.warn("Player ${pet.meta.playerMeta.name} has registered multiple ${AIType.HEALTH.type}. Please check your configuration.")
        }

        val itemInHand = proxyService.getPlayerItemInHand<P, Any>(player, true)

        if (itemInHand == null || itemTypeService.findItemType<Any>(itemInHand) ==  itemTypeService.findItemType(MaterialType.AIR)) {
            val cachePet = pet.getHeadArmorstandItemStack<Any>()
            val playerUUID = proxyService.getPlayerUUID(player)

            carryingPet[playerUUID] = cachePet
            proxyService.setPlayerItemInHand(player, cachePet, true)

            pet.remove()
        }
    }

    /**
     * Lets the given [player] drop his pet if he is currently carrying it.
     * Does nothing if the player isn't carrying it.
     */
    override fun <P> dropPet(player: P) {
        val playerUUID = proxyService.getPlayerUUID(player)

        if (!carryingPet.containsKey(playerUUID)) {
            return
        }

        carryingPet.remove(playerUUID)
        proxyService.setPlayerItemInHand(player, null, true)

        petService.getOrSpawnPetFromPlayer(player)
    }

    /**
     * Lets the given [player] throw his pet if he is currently carrying.
     * Does automatically drop it and does nothing if the player isn't carrying it.
     */
    override fun <P> throwPet(player: P) {
        val playerUUID = proxyService.getPlayerUUID(player)

        if (!carryingPet.containsKey(playerUUID)) {
            return
        }

        dropPet(player)

        val pet = petService.getOrSpawnPetFromPlayer(player).get()

        sync(concurrencyService, 1L) {
            val direction = proxyService.getDirectionVector(player)
            pet.setVelocity(direction.multiply(1.2))
        }
    }

    /**
     * Gets the itemstack from the carrying pet.
     * Returns an empty optional if the player is carrying anything.
     */
    override fun <P, I> getCarryPetItemStack(player: P): I? {
        val playerUUID = proxyService.getPlayerUUID(player)

        return if (isCarryingPet(player)) {
            this.carryingPet[playerUUID]!! as I
        } else {
            null
        }
    }

    /**
     * Gets if the given player is carrying a pet.
     */
    override fun <P> isCarryingPet(player: P): Boolean {
        val playerUUID = proxyService.getPlayerUUID(player)
        return carryingPet.containsKey(playerUUID)
    }

    /**
     * Clears all resources the given [player] may have allocated.
     */
    override fun <P> clearResources(player: P) {
        dropPet(player)
    }
}