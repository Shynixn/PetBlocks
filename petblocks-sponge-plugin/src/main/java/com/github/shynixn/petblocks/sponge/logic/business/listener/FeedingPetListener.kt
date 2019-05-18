@file:Suppress("CAST_NEVER_SUCCEEDS", "unused")

package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.FeedingPetService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.google.inject.Inject
import org.spongepowered.api.data.type.HandTypes
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.Order
import org.spongepowered.api.event.entity.InteractEntityEvent
import org.spongepowered.api.event.filter.cause.First
import org.spongepowered.api.item.ItemTypes

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
class FeedingPetListener @Inject constructor(private val feedingPetService: FeedingPetService, private val petService: PetService) {
    /**
     * Gets called when a player interacts at the given entity.
     */
    @Listener(order = Order.EARLY)
    fun entityRightClickEvent(event: InteractEntityEvent.Secondary, @First(typeFilter = [Player::class]) player: Player) {
        if (!event.cause.first(Entity::class.java).isPresent) {
            return
        }

        val pet = petService.findPetByEntity(event.targetEntity) ?: return

        if (pet.getPlayer<Player>() == player) {
            val itemStack = player.getItemInHand(HandTypes.MAIN_HAND)

            if (itemStack.isPresent && itemStack.get().type != ItemTypes.AIR) {
                val feed = feedingPetService.feedPet(pet, itemStack.get())

                if (feed) {
                    if (itemStack.get().quantity == 1) {
                        pet.getPlayer<Player>().setItemInHand(HandTypes.MAIN_HAND, null)
                    } else {
                        itemStack.get().quantity = itemStack.get().quantity - 1
                    }
                }

                event.isCancelled = feed
            }
        }
    }
}