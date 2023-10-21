package com.github.shynixn.petblocks.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mcutils.common.*
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
            "skintype",
            3,
            Permission.SKIN,
            "/petblocks skintype <name> <material> [player]"
        ) { sender, player, args ->
            setSkinType(sender, player, args[1], args[2])
        },


        /*     CommandDefinition("despawn", 2, Permission.DESPAWN, "/petblocks despawn <name> [player]"),
             CommandDefinition("ride", 2, Permission.RIDE, "/petblocks ride <name> [player]"),
             CommandDefinition(
                 "displayName", 3, Permission.DISPLAYNAME, "/petblocks displayname <name> <displayName> [player]"
             ),
             CommandDefinition("visibility", 3, Permission.VISIBILITY, "/petblocks visibility <name> <visibility> [player]"),
             CommandDefinition("skintype", 3, Permission.SKIN, "/petblocks skintype <name> <material> [player]"),
             CommandDefinition("skinnbt", 3, Permission.SKIN, "/petblocks skinnbt <name> <nbt> [player]"),
             CommandDefinition("reload", 1, Permission.CREATE, "/petblocks reload")*/
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
                definition.function.invoke(sender, player, args)
                return true
            }
        }


        /*if (args.size >= 2 && args[0].equals("spawn", true) && sender.hasPermission(Permission.SPAWN.text)) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.spawn()
            sender.sendMessage(String.format(PetBlocksLanguage.petSpawnedMessage, petName))
            return true
        }

        if (args.size >= 2 && args[0].equals("despawn", true) && sender.hasPermission(Permission.DESPAWN.text)) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.remove()
            sender.sendMessage(String.format(PetBlocksLanguage.petDespawnedMessage, petName))
            return true
        }

        if (args.size >= 3 && args[0].equals(
                "displayName", true
            ) && sender.hasPermission(Permission.DISPLAYNAME.text)
        ) {
            val petName = args[1]
            val displayName = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.displayName = displayName
            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.petNameChangeMessage, petName, displayName.translateChatColors()
                )
            )
            return true
        }
        if (args.size >= 3 && args[0].equals("visibility", true) && sender.hasPermission(Permission.VISIBILITY.text)) {
            val petName = args[1]
            val visibilityName = args[2]
            val visibility = PetVisibility.values().firstOrNull { e -> e.name.equals(visibilityName, true) }
                ?: throw PetBlocksException(
                    String.format(
                        PetBlocksLanguage.visibilityTypeNotFoundMessage, visibilityName
                    )
                )
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.visibility = visibility
            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.visibilityChangedMessage, petName
                )
            )
            return true
        }

        if (args.size >= 3 && args[0].equals("skintype", true) && sender.hasPermission(Permission.SKIN.text)) {
            val petName = args[1]
            val skinType = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

            val item = pet.headItem.toItem()
            item.typeName = skinType
            pet.headItem = item.toItemStack()

            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.petSkinTypeChangedMessage, petName
                )
            )
        }

        if (args.size >= 3 && args[0].equals("skinnbt", true) && sender.hasPermission(Permission.SKIN.text)) {
            val petName = args[1]
            val skinNbt = args[2]
            val player =
                findPlayer(sender, 3, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

            val item = pet.headItem.toItem()
            item.nbt = skinNbt
            pet.headItem = item.toItemStack()

            sender.sendMessage(
                String.format(
                    PetBlocksLanguage.petSkinNbtChanged, petName
                )
            )
        }

        if (args.size >= 2 && args[0].equals("ride", true) && sender.hasPermission(Permission.RIDE.text)) {
            val petName = args[1]
            val player =
                findPlayer(sender, 2, args) ?: throw PetBlocksException(PetBlocksLanguage.playerNotFoundMessage)
            petService.getPetsFromPlayer(player).forEach { pet ->
                pet.umount()
            }
            val pet = findPetFromPlayer(player, petName)
                ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
            pet.ride()
            sender.sendMessage(String.format(PetBlocksLanguage.petRideMessage, petName))
            return true
        }


        if (args.size == 1 && args[0].equals("reload", true) && sender.hasPermission(Permission.RELOAD.text)) {
            configurationService.reload()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(
                language, PetBlocksLanguage::class.java, "en_us"
            )
            templateRepository.clearCache()
            return true
        }*/

        if (args.size == 2 && args[0].equals(
                "help", true
            ) && sender.hasPermission(Permission.HELP.text) && args[1] == "2"
        ) {
            sender.sendMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.stream().skip(10)) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendMessage(ChatColor.GREEN.toString() + "----------┌2/2┐----------")

            return true
        }

        if (args.isNotEmpty() && args[0].equals("help", true) && sender.hasPermission(Permission.HELP.text)) {
            sender.sendMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.take(10)) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendMessage(ChatColor.GREEN.toString() + "----------┌1/2┐----------")

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
            val item = pet.headItem
            item.typeName = material
            pet.headItem = item
            sender.sendMessage(String.format(PetBlocksLanguage.petSkinTypeChangedMessage, petName))
        } catch (e: Exception) {
            sender.sendMessage(String.format(PetBlocksLanguage.petSkinTypeNotFound, material))
        }
    }

    private suspend fun teleportPet(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.location = location
        sender.sendMessage(String.format(PetBlocksLanguage.petTeleportedMessage, petName))
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
        val function: suspend (CommandSender, Player, Array<out String>) -> Unit
    )
}
