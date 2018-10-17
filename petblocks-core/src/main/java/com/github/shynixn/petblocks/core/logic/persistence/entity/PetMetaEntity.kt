package com.github.shynixn.petblocks.core.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.EntityType
import com.github.shynixn.petblocks.api.persistence.entity.Particle
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import com.github.shynixn.petblocks.api.persistence.entity.PlayerMeta

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
class PetMetaEntity(override val playerMeta: PlayerMeta, override val particle: Particle) : PetMeta {
    /**
     * Climbing height.
     */
    override val climbingHeight: Double = 1.0
    /**
     * Movement speed modifier.
     */
    override val movementSpeedModifier: Double = 1.0
    /**
     * Type of the hitbox entity.
     */
    override val hitBoxEntityType: EntityType = EntityType.RABBIT
    /**
     * Database id.
     */
    override var id: Long = 0

    /**
     * Is the pet using sounds.
     */
    override var sound: Boolean = true

    /**
     * Displayed name on top of the pet.
     */
    override var displayName: String = playerMeta.name + "'s Pet"

    /**
     * Skin of the pet head.
     */
    override var skin: String = ""
    /**
     * ItemId of the pet head.
     */
    override var itemId: Int = 2
    /**
     * ItemDamage of the pet head.
     */
    override var itemDamage: Int = 0
    /**
     * Unbreakable Tag of the pet head.
     */
    override var unbreakable: Boolean = false
}