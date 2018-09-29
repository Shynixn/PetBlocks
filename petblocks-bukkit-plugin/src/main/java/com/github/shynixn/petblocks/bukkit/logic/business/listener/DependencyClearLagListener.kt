package com.github.shynixn.petblocks.bukkit.logic.business.listener

import me.minebuilders.clearlag.events.EntityRemoveEvent
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.Rabbit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener

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
class DependencyClearLagListener : Listener {
    /**
     * Gets called from clear lag when an entity gets removed.
     */
    @EventHandler
    fun onEntityRemoveEvent(event: EntityRemoveEvent) {
        for (entity in event.entityList.toTypedArray()) {
            if (this.isPet(entity)) {
                event.entityList.remove(entity)
            }
        }
    }

    /**
     * Checks if the given [entity] is a petblock pet.
     */
    private fun isPet(entity: Entity): Boolean {
        if (entity is ArmorStand) {
            val xidentifier = entity.bodyPose.z.toInt()
            val identifier = entity.rightArmPose.x.toInt()

            if (xidentifier == 2877 && identifier == 2877) {
                return true
            }
        } else if (entity is Rabbit && entity.getCustomName() != null && entity.getCustomName() == "PetBlockIdentifier") {
            return true
        }

        return false
    }
}