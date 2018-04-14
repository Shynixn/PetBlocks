package com.github.shynixn.petblocks.sponge.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.RideType
import com.github.shynixn.petblocks.core.logic.persistence.entity.EngineData
import org.spongepowered.api.entity.living.player.Player

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
class SpongeEngineData : EngineData<Player> {

    /**
     * Initializes a new engine data.
     *
     * @param id   id
     * @param data data
     * @throws Exception exception
     */
    constructor(id: Long, data: Map<String, Any>) : super(id) {
        this.id = id
        this.itemContainer = SpongeItemContainer(id.toInt(), data["gui"] as Map<String, Any>)
        this.entity = (data["behaviour"] as Map<String, Any>)["entity"] as String
        this.rideType = RideType.valueOf((data["behaviour"] as Map<String, Any>)["riding"] as String)

        val ambient = (data["sound"] as Map<String, Any>)["ambient"] as Map<String, Any>
        val walking = (data["sound"] as Map<String, Any>)["walking"] as Map<String, Any>

        this.ambientSound = SpongeSoundBuilder(ambient["name"] as String, ambient["volume"] as Double, ambient["pitch"] as Double)
        this.walkingSound = SpongeSoundBuilder(walking["name"] as String, walking["volume"] as Double, walking["pitch"] as Double)
    }
}