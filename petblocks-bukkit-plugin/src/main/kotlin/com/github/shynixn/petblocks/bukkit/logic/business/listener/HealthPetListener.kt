package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.business.service.PetService
import com.google.inject.Inject
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.EntityInteractEvent

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
class HealthPetListener @Inject constructor(private val petService: PetService) : Listener {
    /**
     * Cancels the entity interact event for pets.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityInteractEvent(event: EntityInteractEvent) {
        val optPet = petService.findPetByEntity(event.entity)

    }

    /**
     * Cancels the entity damage event for pets.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityDamageByBlockEvent(event: EntityDamageEvent) {
        val optPet = petService.findPetByEntity(event.entity)

        if (optPet != null) {
            event.isCancelled = true
        }
    }

    /**
     * Gets called when a pet damages another entity and cancels it. Also let's the pet flee if it is being attacked.
     *
     * @param event event
     */
    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        val optPet = petService.findPetByEntity(event.damager)

        if (optPet != null) {
            event.isCancelled = true
        }
    }
}