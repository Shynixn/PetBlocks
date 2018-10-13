package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.core.logic.business.extension.sync
import com.google.inject.Inject

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
class CombatPetServiceImpl @Inject constructor(private val petService: PetService, private val configurationService: ConfigurationService, private val concurrencyService: ConcurrencyService, private val proxyService: ProxyService) : CombatPetService {
    private val fleeCache = ArrayList<Any>()

    /**
     * Lets the pet flee when the given player is the owner of the pet and gets attacked or is the
     * source of a attack on another player.
     */
    override fun <P> flee(player: P) {
        if (player !is Any) {
            throw IllegalArgumentException("Player has to be Anything!")
        }

        if (fleeCache.contains(player)) {
            return
        }

        val playerProxy = proxyService.findPlayerProxyObject(player)

        if (!petService.hasPet(playerProxy.uniqueId)) {
            return
        }

        val fleeInCombat = configurationService.findValue<Boolean>("pet.flee.flee-in-combat")

        if (!fleeInCombat) {
            return
        }

        fleeCache.add(player)
        val reappearSeconds = configurationService.findValue<Int>("pet.flee.reappears-in-seconds") * 20L

        petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAccept { pet ->
            pet.remove()
        }

        sync(concurrencyService, reappearSeconds) {
            fleeCache.remove(player)
            petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId)
        }
    }
}