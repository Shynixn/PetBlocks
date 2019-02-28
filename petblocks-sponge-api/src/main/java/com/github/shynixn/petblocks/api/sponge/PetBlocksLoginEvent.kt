package com.github.shynixn.petblocks.api.sponge

import com.github.shynixn.petblocks.api.business.proxy.PetProxy
import com.github.shynixn.petblocks.api.persistence.entity.PetMeta
import org.spongepowered.api.entity.living.player.Player
import java.util.*

/**
 * Event which gets called once a user has login on the server and all
 * PetBlocks resources are ready for this user.
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
class PetBlocksLoginEvent(
    /**
     * Player which logs into the server.
     */
    val player: Player,
    /**
     *  PetMeta data which was created or restored for the given player.
     */
    val petMeta: PetMeta, private val petProxy: PetProxy? = null
) : PetBlocksEvent() {
    /**
     * Pet of the player if it is active on join.
     */
    val pet: Optional<PetProxy> get() = Optional.ofNullable(petProxy)
}