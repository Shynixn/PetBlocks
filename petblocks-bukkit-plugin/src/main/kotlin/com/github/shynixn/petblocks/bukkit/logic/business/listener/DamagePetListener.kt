package com.github.shynixn.petblocks.bukkit.logic.business.listener

import com.github.shynixn.petblocks.api.bukkit.event.PetPreSpawnEvent
import com.github.shynixn.petblocks.api.business.service.CombatPetService
import com.github.shynixn.petblocks.api.business.service.HealthService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.api.persistence.entity.AIFleeInCombat
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth
import com.google.inject.Inject
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent

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
class DamagePetListener @Inject constructor(
    private val petService: PetService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val combatPetService: CombatPetService,
    private val healthService: HealthService
) :
    Listener {
    /**
     * The [event] gets called when a entity gets damaged. Redirects the damage to the PetBlocks implementation.
     */
    @EventHandler
    fun onEntityReceiveDamageEvent(event: EntityDamageEvent) {
        if (petService.findPetByEntity(event.entity) != null) {
            val pet = petService.findPetByEntity(event.entity)!!

            event.isCancelled = true

            healthService.damagePet(pet.meta, event.finalDamage)

            if (event.cause != EntityDamageEvent.DamageCause.FALL && event.cause != EntityDamageEvent.DamageCause.SUFFOCATION) {
                combatPetService.flee(pet.meta)
            }

            return
        }

        if (event.cause != EntityDamageEvent.DamageCause.FALL && event.entity is Player) {
            if (!persistencePetMetaService.hasPetMeta(event.entity)) {
                return
            }

            val petMeta = persistencePetMetaService.getPetMetaFromPlayer(event.entity)

            combatPetService.flee(petMeta)
        }
    }

    /**
     * The [event] gets called when a pet damages another entity. Cancels the damage per default.
     */
    @EventHandler
    fun onEntityDamageByEntityEvent(event: EntityDamageByEntityEvent) {
        if (petService.findPetByEntity(event.damager) != null) {
            event.isCancelled = true
            return
        }

        if (petService.findPetByEntity(event.entity) != null) {
            val pet = petService.findPetByEntity(event.entity)!!
            combatPetService.flee(pet.meta)

            val vector =
                event.entity.location.toVector().subtract(event.damager.location.toVector()).normalize().multiply(0.5)
            vector.y = 0.1

            pet.setVelocity(vector)
        }
    }

    /**
     * The [event] gets called when a pet tries to get spawned by a player. Checks if the pet is currently respawning blocked.
     */
    @EventHandler
    fun onPetSpawnEvent(event: PetPreSpawnEvent) {
        if (event.petMeta.aiGoals.firstOrNull { a -> a is AIHealth } != null) {
            val aiHealth = event.petMeta.aiGoals.firstOrNull { a -> a is AIHealth } as AIHealth

            if (aiHealth.currentRespawningDelay > 0) {
                event.isCancelled = true
            }

            return
        }

        if (event.petMeta.aiGoals.firstOrNull { a -> a is AIFleeInCombat } != null) {
            val aiFleeInCombat = event.petMeta.aiGoals.firstOrNull { a -> a is AIFleeInCombat } as AIFleeInCombat

            if (aiFleeInCombat.currentAppearsInSeconds > 0) {
                event.isCancelled = true
            }

            return
        }
    }
}