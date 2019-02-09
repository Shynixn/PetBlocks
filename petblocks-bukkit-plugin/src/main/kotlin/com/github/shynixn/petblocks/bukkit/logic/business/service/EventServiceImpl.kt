package com.github.shynixn.petblocks.bukkit.logic.business.service

import com.github.shynixn.petblocks.api.bukkit.event.*
import com.github.shynixn.petblocks.api.business.service.EventService
import com.github.shynixn.petblocks.api.persistence.entity.PetBlocksPostSave
import com.github.shynixn.petblocks.api.persistence.entity.PetBlocksPreSave
import com.github.shynixn.petblocks.api.persistence.entity.PetPostSpawn
import com.github.shynixn.petblocks.api.persistence.entity.PetPreSpawn
import org.bukkit.Bukkit
import org.bukkit.entity.Player

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
class EventServiceImpl : EventService {
    /**
     * Calls a framework event and returns if it was cancelled.
     */
    override fun callEvent(event: Any): Boolean {
        val cEvent: PetBlocksEvent = when (event) {
            is PetPreSpawn -> PetPreSpawnEvent(event.player as Player, event.petMeta)
            is PetPostSpawn -> PetPostSpawnEvent(event.player as Player, event.pet)
            is PetBlocksPreSave -> PetBlocksPreSaveEvent(event.player as Player, event.petMeta)
            is PetBlocksPostSave -> PetBlocksPostSaveEvent(event.player as Player, event.petMeta)
            else -> throw IllegalArgumentException("Event is not mapped to PetBlocks!")
        }

        Bukkit.getPluginManager().callEvent(cEvent)

        if (cEvent is PetBlocksCancelableEvent) {
            return cEvent.isCancelled
        }

        return false
    }
}