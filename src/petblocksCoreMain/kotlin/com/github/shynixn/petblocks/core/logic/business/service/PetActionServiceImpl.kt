package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.AIBase
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors

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
class PetActionServiceImpl @Inject constructor(
    private val petService: PetService,
    private val persistencePetMetaService: PersistencePetMetaService,
    private val configurationService: ConfigurationService,
    private val proxyService: ProxyService
) : PetActionService {


    private val maxSkinLength = 20

    /**
     * Calls the pet to the given player. If the pet is not enabled, it will be enabled after calling.
     */
    override fun <P> callPet(player: P) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAccept { pet ->
            pet.teleport<Any>(playerProxy.getLocation())

            val message = configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.called-success")
            playerProxy.sendMessage(message)
        }
    }

    /**
     * Toggles the pet of the given [player]. If the pet is disabled it will be enabled and when enabled it will be
     * disabled.
     */
    override fun <P> togglePet(player: P) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        if (petService.hasPet(playerProxy.uniqueId)) {
            petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAccept { pet ->
                pet.remove()

                val message = configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.toggle-despawn")
                playerProxy.sendMessage(message)
            }
        } else {
            petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAccept {
                val message = configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.toggle-spawn")
                playerProxy.sendMessage(message)
            }
        }
    }

    /**
     * Renames the pet of the given [player] to the given [name].
     */
    override fun <P> renamePet(player: P, name: String) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        val prefix = configurationService.findValue<String>("messages.prefix")
        val maxPetNameLength = configurationService.findValue<Int>("pet.design.max-petname-length")
        val namingErrorMessage = configurationService.findValue<String>("messages.naming-error")

        if (name.length > maxPetNameLength) {
            playerProxy.sendMessage(prefix + namingErrorMessage)
            return
        }

        val petNameBlackList = configurationService.findValue<List<String>>("pet.design.petname-blacklist").map { s -> s.toUpperCase() }

        petNameBlackList.forEach { blackName ->
            if (name.toUpperCase().contains(blackName.toUpperCase())) {
                playerProxy.sendMessage(prefix + namingErrorMessage)
                return
            }
        }

        persistencePetMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAccept { petMeta ->
            val namingSuccessMessage = configurationService.findValue<String>("messages.naming-success")

            petMeta.displayName = name.translateChatColors()
            persistencePetMetaService.save(petMeta)

            playerProxy.sendMessage(prefix + namingSuccessMessage)
        }
    }

    /**
     * Changes the ai of the pet to the given target ai goals. The boolean flag sets
     * if the ai goals with the same type should get replaced.
     */
    override fun <P> changeAI(player: P, targetAIGoals: Map<AIBase, Boolean>) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}