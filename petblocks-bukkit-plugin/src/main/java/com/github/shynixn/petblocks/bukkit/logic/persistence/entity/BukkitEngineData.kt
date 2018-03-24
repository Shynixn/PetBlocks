package com.github.shynixn.petblocks.bukkit.logic.persistence.entity

import com.github.shynixn.petblocks.api.business.enumeration.RideType
import com.github.shynixn.petblocks.bukkit.logic.business.entity.ItemContainer
import com.github.shynixn.petblocks.core.logic.persistence.entity.EngineData
import org.bukkit.configuration.MemorySection
import org.bukkit.entity.Player

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
class BukkitEngineData : EngineData<Player> {

    /**
     * Initializes a new engine data.
     *
     * @param id   id
     * @param data data
     * @throws Exception exception
     */
    constructor(id: Long, data: Map<String, Any>) : super(id) {
        this.id = id
        this.itemContainer = ItemContainer(id.toInt(), (data["gui"] as MemorySection).getValues(false))
        this.entity = data["behaviour.entity"] as String
        this.rideType = RideType.valueOf(data["behaviour.riding"] as String)
        this.ambientSound = SoundBuilder(data["sound.ambient.name"] as String, data["sound.ambient.volume"] as Double, data["sound.ambient.pitch"] as Double)
        this.walkingSound = SoundBuilder(data["sound.walking.name"] as String, data["sound.walking.volume"] as Double, data["sound.walking.pitch"] as Double)
    }
}