@file:Suppress("KDocMissingDocumentation")

package com.github.shynixn.petblocks.bukkit.logic.business.nms.v1_9_R2

import com.github.shynixn.petblocks.api.business.proxy.EntityPetProxy
import net.minecraft.server.v1_9_R2.EntityInsentient
import org.bukkit.craftbukkit.v1_9_R2.CraftServer
import org.bukkit.craftbukkit.v1_9_R2.entity.CraftLivingEntity
import org.bukkit.entity.Slime

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
class CraftPet(server: CraftServer, nmsPet: EntityInsentient) : CraftLivingEntity(server, nmsPet), EntityPetProxy, Slime {
    /**
     * Boots marker.
     */
    override var bootsItemStack: Any? = null

    /**
     * Removes this entity.
     */
    override fun deleteFromWorld() {
        super.remove()
    }

    /**
     * Hides the true type of the pet from everyone else.
     */
    override fun getType(): org.bukkit.entity.EntityType {
        return org.bukkit.entity.EntityType.SLIME
    }

    /**
     * Ignore all other plugins trying to remove this entity. This is the entity of PetBlocks,
     * no one else is allowed to modify this!
     */
    override fun remove() {
    }

    /**
     * Custom type.
     */
    override fun toString(): String {
        return "PetBlocks{Entity}"
    }

    override fun getSize(): Int {
        return 1
    }

    override fun setSize(p0: Int) {
    }
}