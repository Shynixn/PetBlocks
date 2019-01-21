package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.business.service.ConcurrencyService
import com.github.shynixn.petblocks.api.business.service.HealthService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.persistence.entity.AIHealth
import com.github.shynixn.petblocks.core.logic.business.extension.sync

/**
 * Created by Shynixn 2019.
 * <p>
 * Version 1.2
 * <p>
 * MIT License
 * <p>
 * Copyright (c) 2019 by Shynixn
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
class HealthServiceImpl @Inject constructor(private val concurrencyService: ConcurrencyService, private val persistenceService: PersistencePetMetaService) : HealthService {
    /**
     * Damages the given [pet] with the given [damage].
     * The pet needs a health ai otherwise this operation gets ignored.
     */
    override fun damagePet(pet: PetProxy, damage: Double) {
        val aiBase = pet.meta.aiGoals.firstOrNull { a -> a is AIHealth } ?: return
        val aiHealth = aiBase as AIHealth

        aiHealth.health = aiHealth.health - damage // Requires ingame storage.

        if (aiHealth.health <= 0) {
            pet.remove()

            sync(concurrencyService, (20 * aiHealth.respawningDelay).toLong()) {
                aiHealth.health = aiHealth.maxHealth
            }
        }
    }
}