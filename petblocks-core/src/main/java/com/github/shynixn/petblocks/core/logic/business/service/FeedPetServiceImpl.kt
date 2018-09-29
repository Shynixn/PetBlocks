package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.enumeration.MaterialType
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.proxy.PlayerProxy
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.Sound
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
import com.google.inject.Inject
import java.util.*

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
class FeedPetServiceImpl @Inject constructor(private val concurrencyService: ConcurrencyService, private val configurationService: ConfigurationService, private val soundService: SoundService, private val particleService: ParticleService, private val proxyService: ProxyService, private val petService: PetService, private val inventoryItemService: ItemService) : FeedingPetService {
    private val jumpCache = HashSet<UUID>()

    /**
     * Feeds the pet of the given [player] with the current item in hand.
     */
    override fun <P> feedPet(player: P): Boolean {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        val feedingPetEnabled = configurationService.findValue<Boolean>("pet.feeding.enabled")

        if (!feedingPetEnabled) {
            return false
        }

        if (!petService.hasPet(playerProxy.uniqueId)) {
            return false
        }

        val itemInHand = playerProxy.getItemInHand<Any>()

        if (!itemInHand.isPresent || inventoryItemService.getMaterialTypeOfItemstack(itemInHand.get()) != MaterialType.CARROT_ITEM) {
            return false
        }

        petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAccept { petblock ->
            feedPet(playerProxy, petblock, itemInHand.get())
        }

        return true
    }

    /**
     * Feeds the pet if the conditions are right.
     */
    private fun feedPet(playerProxy: PlayerProxy, pet: PetProxy, itemInHand: Any) {
        if (pet.meta.isSoundEnabled) {
            val feedingSound = configurationService.findValue<Sound>("pet.feeding.click-sound")
            this.soundService.playSound(pet.getLocation<Any>(), feedingSound, playerProxy.handle)
        }

        val feedingParticle = configurationService.findValue<Particle>("pet.feeding.click-particle")
        particleService.playParticle(pet.getLocation<Any>(), feedingParticle, playerProxy.handle)

        val amountInHand = inventoryItemService.getAmountOfItemStack(itemInHand)

        if (amountInHand == 1) {
            val airItemStack = inventoryItemService.createItemStack<Any>(MaterialType.AIR)
            playerProxy.setItemInHand(airItemStack)
        } else {
            inventoryItemService.setAmountOfItemStack(itemInHand, amountInHand - 1)
            playerProxy.setItemInHand(itemInHand)
        }

        if (!jumpCache.contains(playerProxy.uniqueId)) {
            jumpCache.add(playerProxy.uniqueId)

            val vectorPosition = PositionEntity()
            with(vectorPosition) {
                x = 0.0
                y = 0.5
                z = 0.0
            }

            pet.setVelocity(vectorPosition)

            sync(concurrencyService, 20L) {
                jumpCache.remove(playerProxy.uniqueId)
            }
        }
    }
}