package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.FeedingPetService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.bukkit.logic.business.extension.itemStackInMainHand
import com.google.inject.Inject
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerInteractAtEntityEvent

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
class FeedingPetListener @Inject constructor(private val feedingPetService: FeedingPetService, private val petService: PetService) :
    Listener {
    /**
     * Gets called when a player interacts at the given entity.
     */
    @EventHandler(priority = EventPriority.LOWEST)
    fun entityRightClickEvent(event: PlayerInteractAtEntityEvent) {
        val pet = petService.findPetByEntity(event.rightClicked) ?: return

        if (pet.getPlayer<Player>() == event.player) {
            val itemStack = event.player.itemStackInMainHand

            if (itemStack != null && itemStack.type != Material.AIR) {
                val feed = feedingPetService.feedPet(pet, itemStack)

                if (feed) {
                    if (itemStack.amount == 1) {
                        event.player.itemStackInMainHand = null
                    } else {
                        itemStack.amount = itemStack.amount - 1
                    }
                }

                event.isCancelled = feed
            }
        }
    }
}