@file:Suppress("CAST_NEVER_SUCCEEDS", "unused")

package com.github.shynixn.petblocks.sponge.logic.business.listener

import com.flowpowered.math.vector.Vector3d
import com.github.shynixn.petblocks.api.business.service.CombatPetService
import com.github.shynixn.petblocks.api.business.service.HealthService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.api.persistence.entity.AIFleeInCombat
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth
import com.github.shynixn.petblocks.api.sponge.event.PetPreSpawnEvent
import com.github.shynixn.petblocks.sponge.logic.business.extension.toPosition
import com.github.shynixn.petblocks.sponge.logic.business.extension.toVector
import com.google.inject.Inject
import org.spongepowered.api.entity.Entity
import org.spongepowered.api.entity.living.player.Player
import org.spongepowered.api.event.Listener
import org.spongepowered.api.event.cause.entity.damage.DamageTypes
import org.spongepowered.api.event.cause.entity.damage.source.DamageSources
import org.spongepowered.api.event.entity.DamageEntityEvent

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
    private val combatPetService: CombatPetService,
    private val healthService: HealthService,
    private val persistencePetMetaService: PersistencePetMetaService
) {
    /**
     * The [event] gets called when a entity gets damaged. Redirects the damage to the PetBlocks implementation.
     */
    @Listener
    fun onEntityReceiveDamageEvent(event: DamageEntityEvent) {
        if (petService.findPetByEntity(event.targetEntity) != null) {
            val pet = petService.findPetByEntity(event.targetEntity)!!

            event.isCancelled = true

            healthService.damagePet(pet.meta, event.finalDamage)

            if (event.cause.containsType(DamageSources.FALLING::class.java)) {
                combatPetService.flee(pet.meta)
            }

            return
        }

        if (event.source != DamageTypes.FALL && event.targetEntity is Player) {
            val petMeta = persistencePetMetaService.getPetMetaFromPlayer(event.targetEntity)

            combatPetService.flee(petMeta)
        }
    }

    /**
     * The [event] gets called when a pet damages another entity. Cancels the damage per default.
     */
    @Listener
    fun onEntityDamageByEntityEvent(event: DamageEntityEvent) {
        if (!event.cause.first(Entity::class.java).isPresent) {
            return
        }

        val damager = event.cause.first(Entity::class.java).get()

        if (petService.findPetByEntity(damager) != null) {
            event.isCancelled = true
            return
        }

        if (petService.findPetByEntity(event.targetEntity) != null) {
            val pet = petService.findPetByEntity(event.targetEntity)!!
            combatPetService.flee(pet.meta)

            val vector = event.targetEntity.transform.toPosition().subtract(damager.transform.toPosition()).toVector().normalize().mul(0.5)

            pet.setVelocity(Vector3d(vector.x, 0.1, vector.z))
        }
    }

    /**
     * The [event] gets called when a pet tries to get spawned by a player. Checks if the pet is currently respawning blocked.
     */
    @Listener
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