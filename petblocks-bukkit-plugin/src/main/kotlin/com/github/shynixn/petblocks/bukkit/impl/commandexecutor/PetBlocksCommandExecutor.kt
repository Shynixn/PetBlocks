package com.github.shynixn.petblocks.bukkit.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.petblocks.bukkit.PetBlocksLanguage
import com.github.shynixn.petblocks.bukkit.PetBlocksPlugin
import com.github.shynixn.petblocks.bukkit.contract.Pet
import com.github.shynixn.petblocks.bukkit.contract.PetService
import com.github.shynixn.petblocks.bukkit.contract.PetTemplateRepository
import com.github.shynixn.petblocks.bukkit.entity.Permission
import com.github.shynixn.petblocks.bukkit.entity.PetTemplate
import com.github.shynixn.petblocks.bukkit.entity.PetVisibility
import com.github.shynixn.petblocks.bukkit.exception.PetBlocksException
import com.github.shynixn.petblocks.bukkit.extension.hasPermission
import com.github.shynixn.petblocks.bukkit.extension.toFirstLetterUpperCase
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*
import kotlin.collections.ArrayList

class PetBlocksCommandExecutor @Inject constructor(
    private val petService: PetService,
    private val templateRepository: PetTemplateRepository,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService
) : SuspendingCommandExecutor,
    SuspendingTabCompleter {
    private var allCommands = arrayListOf<CommandDefinition>(
        CommandDefinition("create", Permission.CREATE, "/petblocks create <name> <template> [player]"),
        CommandDefinition("delete", Permission.DELETE, "/petblocks delete <name> [player]"),
        CommandDefinition("call", Permission.CALL, "/petblocks call <name> [player]"),
        CommandDefinition("spawn", Permission.SPAWN, "/petblocks spawn <name> [player]"),
        CommandDefinition("despawn", Permission.DESPAWN, "/petblocks despawn <name> [player]"),
        CommandDefinition(
            "displayName",
            Permission.DISPLAYNAME,
            "/petblocks displayName <name> <displayName> [player]"
        ),
        CommandDefinition("visibility", Permission.VISIBILITY, "/petblocks visibility <name> <visibility> [player]"),
        CommandDefinition("reload", Permission.CREATE, "/petblocks reload")
    )

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
        if (args.size >= 3 && sender.hasPermission(Permission.CREATE) && args[0].equals("create", true)) {
            val petName = args[1]
            val templateId = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val template =
                findTemplate(templateId) ?: throw PetBlocksException(PetBlocksLanguage.templateNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)

            if (pet != null) {
                throw PetBlocksException(String.format(PetBlocksLanguage.petNameExistsMessage, petName))
            }

            petService.createPet(player, player.location.toVector3d().addRelativeFront(3.0).toLocation(), template.id)
            sender.sendMessage(String.format(PetBlocksLanguage.petCreatedMessage, petName))
            return true
        }

        if (args.size >= 2 && sender.hasPermission(Permission.DELETE) && args[0].equals("delete", true)) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            petService.deletePet(pet)
            sender.sendMessage(String.format(PetBlocksLanguage.petDeletedMessage, petName))
            return true
        }

        if (args.size >= 2 && sender.hasPermission(Permission.CALL) && args[0].equals(
                "call",
                true
            )
        ) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.call()
            sender.sendMessage(String.format(PetBlocksLanguage.petCalledMessage, petName))
            return true
        }

        if (args.size >= 2 && sender.hasPermission(Permission.SPAWN) && args[0].equals(
                "spawn",
                true
            )
        ) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.spawn()
            sender.sendMessage(String.format(PetBlocksLanguage.petSpawnedMessage, petName))
            return true
        }

        if (args.size >= 2 && sender.hasPermission(Permission.DESPAWN) && args[0].equals(
                "despawn",
                true
            )
        ) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.remove()
            sender.sendMessage(String.format(PetBlocksLanguage.petDespawnedMessage, petName))
            return true
        }

        if (args.size >= 3 && sender.hasPermission(Permission.DISPLAYNAME) && args[0].equals(
                "displayName",
                true
            )
        ) {
            val petName = args[1]
            val displayName = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.displayName = displayName
            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.petNameChangedMessage,
                    petName,
                    displayName.translateChatColors()
                )
            )
            return true
        }
        if (args.size >= 3 && sender.hasPermission(Permission.VISIBILITY) && args[0].equals("visibility", true)) {
            val petName = args[1]
            val visibilityName = args[2]
            val visibility = PetVisibility.values().firstOrNull { e -> e.name.equals(visibilityName, true) }
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.visibilityNotFoundMessage, visibilityName))
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet =
                findPetFromPlayer(player, petName)
                    ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.visibility = visibility
            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.petVisibilityChangedMessage,
                    petName
                )
            )
            return true
        }

        if (args.size == 1 && sender.hasPermission(Permission.RELOAD)) {
            configurationService.reload()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(
                language,
                PetBlocksLanguage::class.java,
                *PetBlocksPlugin.languageFiles.toTypedArray()
            )
            return true
        }

        if (args.size == 1 && args[0].equals(
                "help", true
            ) && sender.hasPermission(Permission.HELP)
        ) {
            sender.sendMessage("---------PetBlocks---------")
            for (commandDefinition in allCommands) {
                if (sender.hasPermission(commandDefinition.permission)) {
                    sender.sendMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendMessage("----------┌1/1┐----------")

            return true
        }

        return false
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
        if (args.size == 1) {
            return allCommands.filter { e -> sender.hasPermission(e.permission) }.map { e -> e.command }
        }

        if (args.size == 2) {
            if(sender is Player){
                return petService.getPetsFromPlayer(sender).map { e -> e.name }
            }

            return emptyList()
        }

        if(sender.hasPermission(Permission.CREATE) && args[0].equals("create", true)){
            return templateRepository.getAll().map { e -> e.id }.sortedBy { e -> e }
        }

        if(sender.hasPermission(Permission.VISIBILITY) && args[0].equals("visibility", true)){
            return PetVisibility.values().map { e -> e.name.toFirstLetterUpperCase() }
        }

        return null
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

    private class CommandDefinition(val command: String, val permission: Permission, val helpMessage: String)
}