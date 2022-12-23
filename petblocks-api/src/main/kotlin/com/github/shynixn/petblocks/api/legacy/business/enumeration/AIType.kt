package com.github.shynixn.petblocks.api.legacy.business.enumeration

import com.github.shynixn.petblocks.api.legacy.persistence.entity.*
import kotlin.reflect.KClass

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
enum class AIType(
    /**
     * Type identifier.
     */
    val type: String,

    /**
     * AiClazz.
     */
    val aiClazz: KClass<*>
) {

    /**
     * Afraid of water.
     */
    AFRAID_OF_WATER("afraid-of-water", AIAfraidOfWater::class),

    /**
     * Ambient sound.
     */
    AMBIENT_SOUND("ambient-sound", AIAmbientSound::class),

    /**
     * BuffEffect ai.
     */
    BUFF_EFFECT("buff-effect", AIBuffEffect::class),

    /**
     * Carry.
     */
    CARRY("carry", AICarry::class),

    /**
     * Entity Nbt.
     */
    ENTITY_NBT("entity-nbt", AIEntityNbt::class),

    /**
     * Feeding.
     */
    FEEDING("feeding", AIFeeding::class),

    /**
     * Flee in combat.
     */
    FLEE_IN_COMBAT("flee-in-combat", AIFleeInCombat::class),

    /**
     * Float in water.
     */
    FLOAT_IN_WATER("float-in-water", AIFloatInWater::class),

    /**
     * Flying.
     */
    FLYING("flying", AIFlying::class),

    /**
     * Fly riding.
     */
    FLY_RIDING("fly-riding", AIFlyRiding::class),

    /**
     * Follow back.
     */
    FOLLOW_BACK("follow-back", AIFollowBack::class),

    /**
     * Follow owner.
     */
    FOLLOW_OWNER("follow-owner", AIFollowOwner::class),

    /**
     * Ground riding.
     */
    GROUND_RIDING("ground-riding", AIGroundRiding::class),

    /**
     * AIHealth.
     */
    HEALTH("health", AIHealth::class),

    /**
     * Hopping.
     */
    HOPPING("hopping", AIHopping::class),

    /**
     * Inventory.
     */
    INVENTORY("inventory", AIInventory::class),

    /**
     * Particle.
     */
    PARTICLE("particle", AIParticle::class),

    /**
     * Walking.
     */
    WALKING("walking", AIWalking::class),

    /**
     * Wearing.
     */
    WEARING("wearing", AIWearing::class),
}
