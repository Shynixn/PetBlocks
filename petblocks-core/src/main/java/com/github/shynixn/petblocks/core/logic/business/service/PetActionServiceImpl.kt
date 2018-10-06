package com.github.shynixn.petblocks.core.logic.business.service

import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.api.persistence.entity.Engine
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.core.logic.business.extension.translateChatColors
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
class PetActionServiceImpl @Inject constructor(private val petService: PetService, private val persistencePetMetaService: PersistencePetMetaService, private val configurationService: ConfigurationService, private val proxyService: ProxyService) : PetActionService {
    private val maxSkinLength = 20

    /**
     * Calls the pet to the given player. If the pet is not enabled, it will be enabled after calling.
     */
    override fun <P> callPet(player: P) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { pet ->
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
            petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { pet ->
                pet.remove()

                val message = configurationService.findValue<String>("messages.prefix") + configurationService.findValue<String>("messages.toggle-despawn")
                playerProxy.sendMessage(message)
            }
        } else {
            petService.getOrSpawnPetFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely {
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

        persistencePetMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { petMeta ->
            val namingSuccessMessage = configurationService.findValue<String>("messages.naming-success")

            petMeta.displayName = name.translateChatColors()
            persistencePetMetaService.save(petMeta)

            playerProxy.sendMessage(prefix + namingSuccessMessage)
        }
    }

    /**
     * Changes the engine of the given [player] pet to the given [engine].
     */
    override fun <P> changeEngine(player: P, engine: Engine) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        persistencePetMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { petMeta ->
            val copySkin = configurationService.findValue<Boolean>("gui.settings.copy-skin")

            if (copySkin) {
                with(petMeta) {
                    itemId = engine.type
                    itemDamage = engine.data
                    skin = engine.skin
                    unbreakable = engine.unbreakable
                }
            }

            if (engine.petName.isPresent) {
                petMeta.displayName = engine.petName.get().translateChatColors()
            }

            if (engine.particle.isPresent) {
                val targetParticle = petMeta.particle
                val sourceParticle = engine.particle.get()

                with(targetParticle) {
                    type = sourceParticle.type
                    amount = sourceParticle.amount
                    speed = sourceParticle.speed
                    offSetX = sourceParticle.offSetX
                    offSetY = sourceParticle.offSetY
                    offSetZ = sourceParticle.offSetZ
                    materialName = sourceParticle.materialName
                    data = sourceParticle.data
                }
            }
        }
    }

    /**
     * Changes the skin of the given [player] pet to the given [name].
     */
    override fun <P> changeSkin(player: P, name: String) {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        val prefix = configurationService.findValue<String>("messages.prefix")
        val skinNamingErrorMessage = configurationService.findValue<String>("messages.skullnaming-error")

        if (name.length > maxSkinLength) {
            playerProxy.sendMessage(prefix + skinNamingErrorMessage)
            return
        }

        persistencePetMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { petMeta ->
            val namingSuccessMessage = configurationService.findValue<String>("messages.skullnaming-success")

            petMeta.skin = name
            petMeta.unbreakable = false
            petMeta.itemId = 397
            petMeta.itemDamage = 3

            persistencePetMetaService.save(petMeta)

            playerProxy.sendMessage(prefix + namingSuccessMessage)
        }
    }
}