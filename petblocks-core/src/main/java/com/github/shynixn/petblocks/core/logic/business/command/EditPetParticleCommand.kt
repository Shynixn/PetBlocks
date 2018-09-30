package com.github.shynixn.petblocks.core.logic.business.command

import com.github.shynixn.petblocks.api.business.command.SourceCommand
import com.github.shynixn.petblocks.api.business.service.CommandService
import com.github.shynixn.petblocks.api.business.service.PersistencePetMetaService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
import com.github.shynixn.petblocks.core.logic.compatibility.Config
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
class EditPetParticleCommand @Inject constructor(private val proxyService: ProxyService, private val petMetaService: PersistencePetMetaService, private val commandService: CommandService) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        if (args.size < 2 || !args[0].equals("particle", true) || args[1].toIntOrNull() == null) {
            return false
        }

        val result = commandService.parseCommand<Any?>(source as Any, args, 1)

        if (result.first == null) {
            return false
        }

        val number = args[1].toInt()
        val playerProxy = proxyService.findPlayerProxyObject(result.first)

        petMetaService.getOrCreateFromPlayerUUID(playerProxy.uniqueId).thenAcceptSafely { petMeta ->
            val guiItemContainer = Config.getInstance<Any>().particleController.getContainerFromPosition(number)

            if (!guiItemContainer.isPresent) {
                playerProxy.sendMessage(Config.getInstance<Any>().prefix + "Particle not found.")
            } else {
                val transferOpt = Config.getInstance<Any>().particleController.getFromItem(guiItemContainer.get())

                if (transferOpt.isPresent) {
                    val sourceParticle = transferOpt.get()
                    val targetParticle = petMeta.particleEffectMeta

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
                petMetaService.save(petMeta)
            }
        }

        return true
    }
}