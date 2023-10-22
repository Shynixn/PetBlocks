package com.github.shynixn.petblocks.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.petblocks.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.exception.PetBlocksException
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import java.util.*

class PetBlocksCommandExecutor @Inject constructor(
    private val petService: PetService,
    private val templateRepository: CacheRepository<PetTemplate>,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService
) : SuspendingCommandExecutor, SuspendingTabCompleter {
    private val regexPath = "pet.name.regex"
    private val blackListPath = "pet.name.blacklist"
    private val minLengthPath = "pet.name.minLength"
    private val maxLengthPath = "pet.name.maxLength"

    // We can add a download/upload for player uuid data.
    private var allCommands = arrayListOf(
        CommandDefinition(
            "create", 3, Permission.CREATE, "/petblocks create <name> <template> [player]"
        ) { sender, player, args -> createPet(sender, player, args[1], args[2]) },
        CommandDefinition(
            "delete",
            2,
            Permission.DELETE,
            "/petblocks delete <name> [player]"
        ) { sender, player, args -> deletePet(sender, player, args[1]) },
        CommandDefinition("list", 1, Permission.LIST, "/petblocks list [player]") { sender, player, _ ->
            listPet(sender, player)
        },
        CommandDefinition("call", 2, Permission.CALL, "/petblocks call <name> [player]") { sender, player, args ->
            callPet(sender, player, args[1])
        },
        CommandDefinition(
            "lookat",
            5,
            Permission.LOOKAT,
            "/petblocks lookat <name> <x> <y> <z> [player]"
        ) { sender, player, args ->
            val location = findLocation(null, args[2], args[3], args[4])
            lookAtLocation(sender, player, args[1], location)
        },
        CommandDefinition(
            "lookatowner",
            2,
            Permission.LOOKATOWNER,
            "/petblocks lookatowner <name> [player]"
        ) { sender, player, args ->
            lookAtLocation(sender, player, args[1], player.location)
        },
        CommandDefinition(
            "moveto",
            5,
            Permission.MOVETO,
            "/petblocks moveto <name> <x> <y> <z> [player]"
        ) { sender, player, args ->
            val location = findLocation(null, args[2], args[3], args[4])
            walkToLocation(sender, player, args[1], location)
        },
        CommandDefinition(
            "movetoowner",
            2,
            Permission.MOVETOOWNER,
            "/petblocks movetoowner <name> [player]"
        ) { sender, player, args ->
            walkToLocation(sender, player, args[1], player.location.toVector3d().addRelativeFront(3.0).toLocation())
        },
        CommandDefinition(
            "hat",
            2,
            Permission.HAT,
            "/petblocks hat <name> [player]"
        ) { sender, player, args ->
            hat(sender, player, args[1])
        },
        CommandDefinition(
            "unmount",
            2,
            Permission.UNMOUNT,
            "/petblocks unmount <name> [player]"
        ) { sender, player, args ->
            unmount(sender, player, args[1])
        },
        CommandDefinition(
            "teleport",
            8,
            Permission.TELEPORT,
            "/petblocks teleport <name> <world> <x> <y> <z> <yaw> <pitch> [player]"
        ) { sender, player, args ->
            val location = findLocation(args[2], args[3], args[4], args[5], args[6], args[7])
            teleportPet(sender, player, args[1], location)
        },
        CommandDefinition(
            "velocity",
            5,
            Permission.VELOCITY,
            "/petblocks velocity <name> <x> <y> <z> [player]"
        ) { sender, player, args ->
            val vector = findLocation(null, args[2], args[3], args[4]).toVector()
            setVelocityToPet(sender, player, args[1], vector)
        },
        CommandDefinition(
            "skintype",
            3,
            Permission.SKIN,
            "/petblocks skintype <name> <material> [player]"
        ) { sender, player, args ->
            setSkinType(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "skinnbt",
            3,
            Permission.SKIN,
            "/petblocks skinnbt <name> <nbt> [player]"
        ) { sender, player, args ->
            setSkinNbt(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "displayname",
            3,
            Permission.DISPLAYNAME,
            "/petblocks displayname <name> <displayname> [player]"
        ) { sender, player, args ->
            setDisplayName(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "ride",
            2,
            Permission.DISPLAYNAME,
            "/petblocks ride <name> [player]"
        ) { sender, player, args ->
            ridePet(sender, player, args[1])
        },
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
            return executeCommands(sender, args)
        } catch (e: PetBlocksException) {
            sender.sendMessage(e.message!!)
            return true
        }
    }

    private suspend fun executeCommands(
        sender: CommandSender, args: Array<out String>
    ): Boolean {
        for (definition in allCommands) {
            if (args.size >= definition.minArgsCount && args[0].equals(
                    definition.command, true
                ) && sender.hasPermission(definition.permission.text)
            ) {
                val player = findPlayer(sender, definition.minArgsCount, args)
                    ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
                val newArgs = args.map { e -> e.replace("###", " ") }.toTypedArray()
                definition.function.invoke(sender, player, newArgs)
                return true
            }
        }

        val commandsPerPage = 10
        val amountOfPages = (allCommands.size.toDouble() / commandsPerPage.toDouble()).toInt() + 1

        if (args.size == 2 && args[0].equals("help", true)
            && sender.hasPermission(Permission.HELP.text) && args[1].toIntOrNull() != null && args[1].toInt() > 1
        ) {
            val helpIndex = args[1].toInt()
            sender.sendMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.stream().skip(commandsPerPage.toLong() * (helpIndex - 1))) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendMessage(ChatColor.GREEN.toString() + "----------┌${helpIndex}/${amountOfPages}┐----------")

            return true
        }

        if (args.isNotEmpty() && args[0].equals("help", true) && sender.hasPermission(Permission.HELP.text)) {
            sender.sendMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.take(commandsPerPage)) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendMessage(ChatColor.GREEN.toString() + "----------┌1/${amountOfPages}┐----------")

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
            return allCommands.filter { e -> sender.hasPermission(e.permission.text) }.map { e -> e.command }
        }

        if (args.size == 2) {
            if (sender is Player) {
                return petService.getPetsFromPlayer(sender).map { e -> e.name }
            }

            return emptyList()
        }

        if (sender.hasPermission(Permission.CREATE.text) && args[0].equals("create", true)) {
            return templateRepository.getAll().map { e -> e.name }.sortedBy { e -> e }
        }

        if (args.size >= 2 && sender.hasPermission(Permission.SKIN.text) && args[0].equals("skinType", true)) {
            return Material.values().map { e ->
                "minecraft:${e.name.lowercase()}"
            }
        }

        if (sender.hasPermission(Permission.VISIBILITY.text) && args[0].equals("visibility", true)) {
            return PetVisibility.values().map { e -> e.name.toFirstLetterUpperCase() }
        }

        return null
    }


    private suspend fun createPet(sender: CommandSender, player: Player, petName: String, templateId: String) {
        val template = findTemplate(templateId)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.templateNotFoundMessage, templateId))
        val pet = findPetFromPlayer(player, petName)

        if (pet != null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.petNameExistsMessage, petName))
        }

        petService.createPet(
            player, player.location.toVector3d().addRelativeFront(3.0).toLocation(), template.name, petName
        )
        sender.sendMessage(String.format(PetBlocksLanguage.petCreatedMessage, petName))
    }

    private suspend fun setSkinType(sender: CommandSender, player: Player, petName: String, material: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            Item(material).toItemStack() // Test if material is valid.
            val item = pet.headItem
            item.typeName = material
            pet.headItem = item
            sender.sendMessage(String.format(PetBlocksLanguage.petSkinTypeChangedMessage, petName))
        } catch (e: Exception) {
            sender.sendMessage(String.format(PetBlocksLanguage.petSkinTypeNotFound, material))
        }
    }

    private suspend fun ridePet(sender: CommandSender, player: Player, petName: String){
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.ride()
        sender.sendMessage(String.format(PetBlocksLanguage.petRideMessage, petName))
    }

    private suspend fun setSkinNbt(sender: CommandSender, player: Player, petName: String, nbt: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.nbt = nbt
            testItem.toItemStack()// Test if nbt is valid.

            val item = pet.headItem
            item.nbt = nbt
            pet.headItem = item
            sender.sendMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, petName))
        } catch (e: Exception) {
            sender.sendMessage(String.format(PetBlocksLanguage.cannotParseNbtMessage, nbt))
        }
    }

    private suspend fun setDisplayName(sender: CommandSender, player: Player, petName: String, displayName: String) {
        val regex = configurationService.findValue<String>(regexPath)
        val blackList = (configurationService.findValue<List<String>>(blackListPath)).map { e -> e.lowercase() }
        val minLength = configurationService.findValue<Int>(minLengthPath)
        val maxLength = configurationService.findValue<Int>(maxLengthPath)

        if (displayName.length < minLength || displayName.length > maxLength) {
            sender.sendMessage(PetBlocksLanguage.petCharacterNotAllowed)
            return
        }

        if (!regex.toRegex().matches(displayName)) {
            sender.sendMessage(PetBlocksLanguage.petCharacterNotAllowed)
            return
        }

        val lowerDisplayName = displayName.lowercase()

        for (blackWord in blackList) {
            if (lowerDisplayName.contains(blackWord)) {
                sender.sendMessage(PetBlocksLanguage.petCharacterNotAllowed)
                return
            }
        }

        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.displayName = displayName
        sender.sendMessage(String.format(PetBlocksLanguage.petNameChangeMessage, petName, displayName.translateChatColors()))
    }

    private suspend fun teleportPet(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.location = location
        sender.sendMessage(String.format(PetBlocksLanguage.petTeleportedMessage, petName))
    }

    private suspend fun setVelocityToPet(sender: CommandSender, player: Player, petName: String, vector: org.bukkit.util.Vector) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.velocity = vector
        sender.sendMessage(String.format(PetBlocksLanguage.petVelocityAppliedMessage, petName))
    }

    private suspend fun deletePet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        petService.deletePet(pet)
        sender.sendMessage(String.format(PetBlocksLanguage.petDeletedMessage, petName))
    }

    private suspend fun listPet(sender: CommandSender, player: Player) {
        val pets = petService.getPetsFromPlayer(player)
        val petString = pets.joinToString(", ") { e -> e.name }
        sender.sendMessage(String.format(PetBlocksLanguage.petListMessage, pets.size, petString))
    }

    private suspend fun lookAtLocation(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.lookAt(location)
        sender.sendMessage(String.format(PetBlocksLanguage.petLookAtMessage))
    }

    private suspend fun unmount(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.umount()
        sender.sendMessage(String.format(PetBlocksLanguage.petUnmountMessage, petName))
    }

    private suspend fun hat(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.hat()
        sender.sendMessage(String.format(PetBlocksLanguage.petHatMessage, petName))
    }

    private suspend fun walkToLocation(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.moveTo(location, 0.2)
        sender.sendMessage(String.format(PetBlocksLanguage.petWalkToLocationMessage))
    }

    private suspend fun callPet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.call()
        sender.sendMessage(String.format(PetBlocksLanguage.petCalledMessage, petName))
    }

    private suspend fun findPetFromPlayer(player: Player, petName: String): Pet? {
        val pets = petService.getPetsFromPlayer(player)
        return pets.firstOrNull { e -> e.name.equals(petName, true) }
    }

    private suspend fun findTemplate(templateId: String): PetTemplate? {
        return templateRepository.getAll().firstOrNull { e -> e.name.equals(templateId, true) }
    }

    private fun findLocation(
        worldName: String?,
        x: String,
        y: String,
        z: String,
        yaw: String = "0",
        pitch: String = "0"
    ): Location {
        var world: World? = null

        if (worldName != null) {
            world = Bukkit.getWorld(worldName)

            if (world == null) {
                throw PetBlocksException(String.format(PetBlocksLanguage.worldNotFoundMessage, worldName))
            }
        }

        if (x.toDoubleOrNull() == null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.cannotParseNumberMessage, x))
        }

        if (y.toDoubleOrNull() == null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.cannotParseNumberMessage, y))
        }

        if (z.toDoubleOrNull() == null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.cannotParseNumberMessage, z))
        }

        if (yaw.toDoubleOrNull() == null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.cannotParseNumberMessage, yaw))
        }

        if (pitch.toDoubleOrNull() == null) {
            throw PetBlocksException(String.format(PetBlocksLanguage.cannotParseNumberMessage, pitch))
        }

        return Location(world, x.toDouble(), y.toDouble(), z.toDouble(), yaw.toFloat(), pitch.toFloat())
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

    private class CommandDefinition(
        val command: String,
        val minArgsCount: Int,
        val permission: Permission,
        val helpMessage: String,
        val function: suspend (CommandSender, Player, Array<String>) -> Unit
    )
}
