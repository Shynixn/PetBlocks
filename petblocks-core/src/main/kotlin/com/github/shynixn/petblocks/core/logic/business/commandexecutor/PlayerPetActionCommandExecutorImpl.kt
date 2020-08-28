package com.github.shynixn.petblocks.core.logic.business.commandexecutor

import com.github.shynixn.petblocks.api.business.command.PlayerCommand
import com.github.shynixn.petblocks.api.business.enumeration.Permission
import com.github.shynixn.petblocks.api.business.localization.Messages
import com.github.shynixn.petblocks.api.business.service.*
import com.github.shynixn.petblocks.core.logic.business.extension.mergeArgs
import com.google.inject.Inject

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
class PlayerPetActionCommandExecutorImpl @Inject constructor(
    private val petActionService: PetActionService,
    private val guiService: GUIService,
    private val proxyService: ProxyService,
    private val configurationService: ConfigurationService,
    private val persistencePetMetaService: PersistencePetMetaService
) : PlayerCommand {
    /**
     * Gets called when the given [player] executes the defined command with the given [args].
     */
    override fun <P> onPlayerExecuteCommand(player: P, args: Array<out String>): Boolean {
        if (!persistencePetMetaService.hasPetMeta(player)) {
            return true
        }

        if (args.size == 1 && args[0].equals("call", true)) {
            if (!checkForPermission(player, Permission.CALL)) {
                return true
            }

            petActionService.callPet(player)
            return true
        }

        if (args.size == 1 && args[0].equals("toggle", true)) {
            if (!checkForPermission(player, Permission.TOGGLE)) {
                return true
            }

            petActionService.togglePet(player)
            return true
        }

        if (args.size >= 2 && args[0].equals("rename", true)) {
            if (!checkForPermission(player, Permission.RENAME)) {
                return true
            }

            petActionService.renamePet(player, mergeArgs(args))
            return true
        }

        if (args.size == 2 && args[0].equals("skin", true)) {
            if (!checkForPermission(player, Permission.CUSTOMHEAD)) {
                return true
            }

            petActionService.changePetSkin(player, args[1])
            return true
        }

        if (args.size == 1) {
            if (!checkForPermission(player, Permission.GUI)) {
                return true
            }

            val pathPermission = configurationService.findValue<String>("commands.petblock.permission") + "." + args[0]

            if (!checkForPermission(player, pathPermission)) {
                return true
            }

            guiService.open(player, args[0])
            return true
        }

        if (!checkForPermission(player, Permission.GUI)) {
            return true
        }

        guiService.open(player)
        return true
    }

    /**
     * Permission check.
     */
    private fun <P> checkForPermission(player: P, permission: Permission): Boolean {
        return checkForPermission(player, permission.permission)
    }

    /**
     * Permission check.
     */
    private fun <P> checkForPermission(player: P, permission: String): Boolean {
        val hasPermission = proxyService.hasPermission(player, permission)

        if (!hasPermission) {
            proxyService.sendMessage(player, Messages.prefix + Messages.noPermissionMessage)
        }

        return hasPermission
    }
}