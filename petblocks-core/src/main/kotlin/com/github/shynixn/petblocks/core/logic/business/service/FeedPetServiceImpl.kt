package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.legacy.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.legacy.business.service.*
import com.github.shynixn.petblocks.api.legacy.persistence.entity.AIFeeding
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.github.shynixn.petblocks.core.logic.persistence.entity.PositionEntity
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
class FeedPetServiceImpl @Inject constructor(
    private val concurrencyService: ConcurrencyService,
    private val soundService: SoundService,
    private val particleService: ParticleService,
    private val inventoryItemService: ItemTypeService
) : FeedingPetService {

    private val cache = HashSet<PetProxy>()

    /**
     * Feeds the given [pet] with the given [itemStack].
     * Returns true if [pet] can eat the [itemStack] otherwise false.
     */
    override fun <I> feedPet(pet: PetProxy, itemStack: I): Boolean {
        var feed = false

        for (aiGoal in pet.meta.aiGoals) {
            if (aiGoal is AIFeeding) {
                if (inventoryItemService.findItemDataValue(itemStack as Any) == aiGoal.dataValue
                    && inventoryItemService.findItemType<Any>(itemStack) == inventoryItemService.findItemType(aiGoal.typeName)
                ) {
                    playFeedEffects(pet, aiGoal)
                    feed = true
                }
            }
        }

        return feed
    }

    /**
     * Plays the feed effects to the players.
     */
    private fun playFeedEffects(pet: PetProxy, aiFeeding: AIFeeding) {
        soundService.playSound(pet.getLocation<Any>(), aiFeeding.clickSound, pet.getPlayer<Any>())
        particleService.playParticle(pet.getLocation<Any>(), aiFeeding.clickParticle, pet.getPlayer<Any>())

        if (cache.contains(pet)) {
            return
        }

        cache.add(pet)

        val vectorPosition = PositionEntity()
        with(vectorPosition) {
            x = 0.0
            y = 0.5
            z = 0.0
        }

        pet.setVelocity(vectorPosition)

        sync(concurrencyService, 20L) {
            cache.remove(pet)
        }
    }
}
