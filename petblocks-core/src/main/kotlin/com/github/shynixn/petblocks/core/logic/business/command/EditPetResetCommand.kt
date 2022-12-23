package com.github.shynixn.petblocks.core.logic.business.command

import com.github.shynixn.petblocks.api.legacy.business.command.SourceCommand
import com.github.shynixn.petblocks.api.legacy.business.service.*
import com.github.shynixn.petblocks.api.legacy.persistence.entity.AIInventory
import com.github.shynixn.petblocks.core.logic.business.extension.thenAcceptSafely
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
class EditPetResetCommand @Inject constructor(
    private val proxyService: ProxyService,
    private val petService: PetService,
    private val petMetaService: PersistencePetMetaService,
    private val configurationService: ConfigurationService,
    private val commandService: CommandService,
    private val loadService: GUIItemLoadService,
    private val messageService: MessageService,
    private val itemTypeService: ItemTypeService
) : SourceCommand {
    /**
     * Gets called when the given [source] executes the defined command with the given [args].
     */
    override fun <S> onExecuteCommand(source: S, args: Array<out String>): Boolean {
        if (args.isEmpty() || !args[0].equals("reset", true)) {
            return false
        }

        val result = commandService.parseCommand<Any?>(source as Any, args, 1)

        if (result.first == null) {
            return false
        }

        val player = result.first
        val playerName = proxyService.getPlayerName(player)
        val playerUuid = proxyService.getPlayerUUID(player)

        if (petService.hasPet(player)) {
            val pet = petService.getOrSpawnPetFromPlayer(player).get()
            pet.remove()
        }

        val petMeta = petMetaService.getPetMetaFromPlayer(player)

        val dropLocation = if (proxyService.isPlayer(source)) {
            proxyService.getPlayerLocation<Any, Any>(source)
        } else {
            proxyService.getPlayerLocation(player!!)
        }

        val dropPosition = proxyService.toPosition(dropLocation)

        petMeta.aiGoals.filterIsInstance<AIInventory>().forEach { storage ->
            for (item in storage.items) {
                if (item != null) {
                    proxyService.dropInventoryItem(dropLocation, item)
                }
            }
        }

        loadService.reload()
        configurationService.reload()

        messageService.sendSourceMessage(source, "Resetting the pet of player $playerName...")
        messageService.sendSourceMessage(
            source,
            "Dropped item(s) at ${dropPosition.worldName} ${dropPosition.x.toInt()},${dropPosition.y.toInt()},${dropPosition.z.toInt()}"
        )

        val newPetMeta = loadService.generateDefaultPetMeta(playerUuid, playerName)
        petMetaService.save(newPetMeta).thenAcceptSafely {
            petMetaService.refreshPetMetaFromRepository(player).thenAcceptSafely {
                messageService.sendSourceMessage(source, "Finished resetting the pet of player $playerName.")
            }
        }

        return true
    }
}
