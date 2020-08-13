package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.PetBlocksApi
import com.github.shynixn.petblocks.api.business.service.PetService
import com.github.shynixn.petblocks.api.business.service.PropertyTrackingService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta
import com.github.shynixn.petblocks.api.persistence.entity.Skin
import com.github.shynixn.petblocks.core.logic.business.service.PropertyTrackingServiceImpl

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
class PetMetaEntity(override val playerMeta: PlayerMeta, override val skin: Skin) : PetMeta {
    private var proxyService: ProxyService? = null
    private var petService: PetService? = null

    /**
     * Is the pet meta new?
     */
    override var new: Boolean = false

    /**
     * Gets a list of all ai goals of this pet.
     */
    override val aiGoals: ObserveableArrayList<AIBase> = ObserveableArrayList {
        propertyTracker.onPropertyChanged(this::aiGoals, true)

        try {
            if (proxyService == null) {
                proxyService = PetBlocksApi.resolve(ProxyService::class.java)
            }

            if (petService == null) {
                petService = PetBlocksApi.resolve(PetService::class.java)
            }

            val player = proxyService!!.getPlayerFromUUID<Any>(this.playerMeta.uuid)

            if (petService!!.hasPet(player)) {
                petService!!.getOrSpawnPetFromPlayer(player).ifPresent { pet ->
                    pet.triggerTick()
                }
            }
        } catch (e: Exception) {
            // We do not care if a problem appears here.
        }
    }

    /**
     * Database id.
     */
    override var id: Long = 0

    /**
     * Is the pet enabled. Should not get modified directly.
     */
    override var enabled: Boolean = false

    /**
     * Displayed name on top of the pet.
     */
    override var displayName: String = playerMeta.name + "'s Pet"
        set(value) {
            if (field != value) {
                propertyTracker.onPropertyChanged(this::displayName, true)
            }

            field = value
        }

    /**
     * Pet sounds enabled.
     */
    override var soundEnabled: Boolean = true

    /**
     * Pet particles enabled.
     */
    override var particleEnabled: Boolean = true

    /**
     * Gets the property tracker.
     */
    override val propertyTracker: PropertyTrackingService = PropertyTrackingServiceImpl()

    /**
     * Creates a shallow clone.
     */
    override fun clone(): PetMeta {
        val petMeta = PetMetaEntity(this.playerMeta, this.skin)
        petMeta.new = this.new
        petMeta.aiGoals.addAllWithoutChangeTrigger(this.aiGoals)
        petMeta.id = this.id
        petMeta.enabled = this.enabled
        petMeta.displayName = this.displayName
        petMeta.soundEnabled = this.soundEnabled
        petMeta.particleEnabled = this.particleEnabled
        return petMeta
    }
}