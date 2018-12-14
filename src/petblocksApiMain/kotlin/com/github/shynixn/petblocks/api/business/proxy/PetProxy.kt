package com.github.shynixn.petblocks.api.business.proxy

import com.github.shynixn.petblocks.api.business.service.LoggingService
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta

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
interface PetProxy {
    /**
     * Gets the meta data.
     */
    val meta: PetMeta

    /**
     * Gets if the pet is dead or was removed.
     */
    val isDead: Boolean

    /**
     * Gets the logger of the pet.
     */
    val logger: LoggingService

    /**
     * Runnable value which represents internal nbt changes of the design armorstand.
     * Gets automatically applied next pet tick.
     */
    val designNbtChange: Map<String, Any>

    /**
     * Runnable value which represents internal nbt changes of the hitboxEntity.
     * Gets automatically applied next pet tick.
     */
    val hitBoxNbtChange: Map<String, Any>

    /**
     * Gets the itemStack on the pet head.
     */
    fun <I> getHeadItemStack(): I

    /**
     * Sets the itemstack on the pet head.
     */
    fun <I> setHeadItemStack(itemStack: I)

    /**
     * Sets the velocity of the pet.
     */
    fun <V> setVelocity(vector: V)

    /**
     * Gets the velocity of the pet.
     */
    fun <V> getVelocity(): V

    /**
     * Teleports the pet to the given [location].
     */
    fun <L> teleport(location: L)

    /**
     * Gets the location of the pet.
     */
    fun <L> getLocation(): L

    /**
     * Gets the pet owner.
     */
    fun <P> getPlayer(): P

    /**
     * Gets the head armorstand.
     */
    fun <A> getHeadArmorstand(): A

    /**
     * Gets a living hitbox entity.
     */
    fun <L> getHitBoxLivingEntity(): L

    /**
     * Removes the pet.
     */
    fun remove()
}