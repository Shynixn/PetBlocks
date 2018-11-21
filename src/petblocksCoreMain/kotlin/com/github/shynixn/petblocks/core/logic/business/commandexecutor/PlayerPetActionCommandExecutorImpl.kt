package com.github.shynixn.petblocks.core.logic.business.commandexecutor

import com.github.shynixn.petblocks.api.business.annotation.Inject
import com.github.shynixn.petblocks.api.business.commandexecutor.PlayerPetActionCommandExecutor
import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.service.GUIService
import com.github.shynixn.petblocks.api.business.service.PetActionService
import com.github.shynixn.petblocks.api.business.service.ProxyService
import com.github.shynixn.petblocks.core.logic.business.extension.mergeArgs

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
class PlayerPetActionCommandExecutorImpl @Inject constructor(private val petActionService: PetActionService, private val guiService: GUIService, private val proxyService: ProxyService) : PlayerPetActionCommandExecutor {
    /**
     * Gets called when the given [player] executes the defined command with the given [args].
     */
    override fun <P> onPlayerExecuteCommand(player: P, args: Array<out String>): Boolean {
        val playerProxy = proxyService.findPlayerProxyObject(player)

        if (args.size == 1 && args[0].equals("call", true)) {
            petActionService.callPet(player)
        } else if (args.size == 1 && args[0].equals("toggle", true)) {
            petActionService.togglePet(player)
        } else if (args.size >= 2 && args[0].equals("rename", true) && playerProxy.hasPermission(Permission.ACTION_RENAME)) {
            petActionService.renamePet(player, mergeArgs(args))
        } else if (args.size == 2 && args[0].equals("skin", true) && playerProxy.hasPermission(Permission.ACTION_CUSTOMSKULL)) {
            petActionService.changeSkin(player, args[1])
        } else {
            guiService.open(player)
        }

        return true
    }
}