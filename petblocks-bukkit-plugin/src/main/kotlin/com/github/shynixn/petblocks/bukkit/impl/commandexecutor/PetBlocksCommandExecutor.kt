package com.github.shynixn.petblocks.bukkit.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mcutils.common.ChatColor
import com.github.shynixn.mcutils.common.toLocation
import com.github.shynixn.mcutils.common.toVector3d
import com.github.shynixn.petblocks.bukkit.PetBlocksLanguage
import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PetService
import com.github.shynixn.petblocks.bukkit.contract.PetTemplateRepository
import com.github.shynixn.petblocks.bukkit.entity.Permission
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.exception.PetBlocksException
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import java.util.*

class PetBlocksCommandExecutor @Inject constructor(
    private val petService: PetService,
    private val templateRepository: PetTemplateRepository
) : SuspendingCommandExecutor,
    SuspendingTabCompleter {
    /**
     * Executes the given command, returning its success.
     * If false is returned, then the "usage" plugin.yml entry for this command (if defined) will be sent to the player.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param label - Alias of the command which was used.
     * @param args - Passed command arguments.
     * @return True if a valid command, otherwise false.
     */
    override suspend fun onCommand(
        sender: CommandSender, command: Command, label: String, args: Array<out String>
    ): Boolean {
        try {
            return executeCommands(sender, command, label, args)
        } catch (e: PetBlocksException) {
            sender.sendMessage(e.message)
        }
        return false
    }

    private suspend fun executeCommands(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>
    ): Boolean {
        if (args.size >= 3 && sender.hasPermission(Permission.CREATE.text) && args[0].equals("create", true)) {
            val petName = args[1]
            val templateId = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val template =
                findTemplate(templateId) ?: throw PetBlocksException(PetBlocksLanguage.templateNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName) ?: throw PetBlocksException(PetBlocksLanguage.petNotFoundMessage)

            petService.createPet(player, player.location.toVector3d().addRelativeFront(3.0).toLocation(), template.id)


        }

        if (args.size == 1 && args[0].equals(
                "help", true
            ) && sender.hasPermission(Permission.HELP.text)
        ) {
            sender.sendMessage("---------PetBlocks---------")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks create <name> <template> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks delete <name> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks displayName <name> <displayName> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks call <name> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks spawn <name> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks despawn <name> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks visibility <name> <visibility> [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks uuid [player]")
            sender.sendMessage(ChatColor.GRAY.toString() + "/petblocks reload")
            sender.sendMessage("----------┌1/1┐----------")
            return true
        }
    }

    /**
     * Requests a list of possible completions for a command argument.
     * If the call is suspended during the execution, the returned list will not be shown.
     * @param sender - Source of the command.
     * @param command - Command which was executed.
     * @param alias - Alias of the command which was used.
     * @param args - The arguments passed to the command, including final partial argument to be completed and command label.
     * @return A List of possible completions for the final argument, or null to default to the command executor
     */
    override suspend fun onTabComplete(
        sender: CommandSender, command: Command, alias: String, args: Array<out String>
    ): List<String>? {
        TODO("Not yet implemented")
    }

    private suspend fun findPetFromPlayer(player: Player, petName: String): Pet? {
        return petService.getPetsFromPlayer(player).firstOrNull { e -> e.name.equals(petName, true) }
    }

    private suspend fun findTemplate(templateId: String): PetTemplate? {
        return templateRepository.getAll().firstOrNull { e -> e.id.equals(templateId, true) }
    }

    private fun findPlayer(sender: CommandSender, argIndex: Int, args: Array<out String>): Player? {
        var player: Player? = null

        if (args.size == argIndex + 1) {
            val playerId = args[argIndex]
            player = Bukkit.getPlayer(playerId)

            if (player == null) {
                player = Bukkit.getPlayer(UUID.fromString(playerId))
            }
        } else if (sender is Player) {
            player = sender
        }

        return player
    }
}
