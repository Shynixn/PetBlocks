package com.github.shynixn.petblocks.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.SuspendingCommandExecutor
import com.github.shynixn.mccoroutine.bukkit.SuspendingTabCompleter
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.PetBlocksDependencyInjectionModule
import com.github.shynixn.petblocks.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.DropType
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PetRotationType
import com.github.shynixn.petblocks.enumeration.PetVisibility
import com.github.shynixn.petblocks.exception.PetBlocksException
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.Plugin
import java.util.*
import java.util.logging.Level

class PetBlocksCommandExecutor @Inject constructor(
    private val petService: PetService,
    private val templateRepository: CacheRepository<PetTemplate>,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService,
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val itemService: ItemService
) : SuspendingCommandExecutor, SuspendingTabCompleter {
    private val random = Random()
    private val regexPath = "pet.name.regex"
    private val blackListPath = "pet.name.blacklist"
    private val minLengthPath = "pet.name.minLength"
    private val maxLengthPath = "pet.name.maxLength"

    private val dependencyHeadDatabaseService: DependencyHeadDatabaseService? by lazy {
        try {
            Bukkit.getServicesManager().load(DependencyHeadDatabaseService::class.java)
        } catch (e: Exception) {
            null
        }
    }

    // We can add a download/upload for player uuid data.
    private var allCommands = arrayListOf(CommandDefinition(
        "create", 3, Permission.CREATE, "/petblocks create <name> <template> [player]"
    ) { sender, player, args -> createPet(sender, player, args[1], args[2]) },
        CommandDefinition(
            "delete", 2, Permission.DELETE, "/petblocks delete <name> [player]"
        ) { sender, player, args -> deletePet(sender, player, args[1]) },
        CommandDefinition("list", 1, Permission.LIST, "/petblocks list [player]") { sender, player, _ ->
            listPet(sender, player)
        },
        CommandDefinition("call", 2, Permission.CALL, "/petblocks call <name> [player]") { sender, player, args ->
            callPet(sender, player, args[1])
        },
        CommandDefinition(
            "lookat", 5, Permission.LOOKAT, "/petblocks lookat <name> <x> <y> <z> [player]"
        ) { sender, player, args ->
            val location = findLocation(null, args[2], args[3], args[4])
            lookAtLocation(sender, player, args[1], location)
        },
        CommandDefinition(
            "lookatowner", 2, Permission.LOOKATOWNER, "/petblocks lookatowner <name> [player]"
        ) { sender, player, args ->
            lookAtLocation(sender, player, args[1], player.location.add(0.0, 1.0, 0.0))
        },
        CommandDefinition(
            "moveto", 6, Permission.MOVETO, "/petblocks moveto <name> <x> <y> <z> <speed> [player]"
        ) { sender, player, args ->
            val location = findLocation(null, args[2], args[3], args[4])
            walkToLocation(sender, player, args[1], location, args[5])
        },
        CommandDefinition(
            "movetoowner", 3, Permission.MOVETOOWNER, "/petblocks movetoowner <name> <speed> [player]"
        ) { sender, player, args ->
            walkToLocation(
                sender, player, args[1], player.location.toVector3d().addRelativeFront(3.0).toLocation(), args[2]
            )
        },
        CommandDefinition(
            "hat", 2, Permission.HAT, "/petblocks hat <name> [player]"
        ) { sender, player, args ->
            hat(sender, player, args[1])
        },
        CommandDefinition(
            "unmount", 2, Permission.UNMOUNT, "/petblocks unmount <name> [player]"
        ) { sender, player, args ->
            unmount(sender, player, args[1])
        },
        CommandDefinition(
            "teleport", 8, Permission.TELEPORT, "/petblocks teleport <name> <world> <x> <y> <z> <yaw> <pitch> [player]"
        ) { sender, player, args ->
            val location = findLocation(args[2], args[3], args[4], args[5], args[6], args[7])
            teleportPet(sender, player, args[1], location)
        },
        CommandDefinition(
            "velocity", 5, Permission.VELOCITY, "/petblocks velocity <name> <x> <y> <z> [player]"
        ) { sender, player, args ->
            val vector = findLocation(null, args[2], args[3], args[4]).toVector()
            setVelocityToPet(sender, player, args[1], vector)
        },
        CommandDefinition(
            "skintype", 3, Permission.SKIN, "/petblocks skintype <name> <material> [player]"
        ) { sender, player, args ->
            setSkinType(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "skinnbt", 3, Permission.SKIN, "/petblocks skinnbt <name> <nbt> [player]"
        ) { sender, player, args ->
            setSkinNbt(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "skincomponent", 3, Permission.SKIN, "/petblocks skincomponent <name> <datacomponent> [player]"
        ) { sender, player, args ->
            setSkinDataComponent(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "skinbase64", 3, Permission.SKIN, "/petblocks skinbase64 <name> <skin> [player]"
        ) { sender, player, args ->
            setSkinBase64(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "skinheaddatabase", 3, Permission.SKIN_HEADDATABASE, "/petblocks skinheaddatabase <name> <hdbId> [player]"
        ) { sender, player, args ->
            setSkinHeadDatabase(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "rename", 3, Permission.RENAME, "/petblocks rename <name> <displayname> [player]"
        ) { sender, player, args ->
            setDisplayName(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "ride", 2, Permission.RIDE, "/petblocks ride <name> [player]"
        ) { sender, player, args ->
            ridePet(sender, player, args[1])
        },
        CommandDefinition(
            "visibility", 3, Permission.VISIBILITY, "/petblocks visibility <name> <type> [player]"
        ) { sender, player, args ->
            setVisibility(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "loop", 3, Permission.LOOP, "/petblocks loop <name> <loop> [player]"
        ) { sender, player, args ->
            setPetLoop(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "template", 3, Permission.TEMPLATE, "/petblocks template <name> <template> [player]"
        ) { sender, player, args ->
            setPetTemplate(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "spawn", 2, Permission.SPAWN, "/petblocks spawn <name> [player]"
        ) { sender, player, args ->
            spawnPet(sender, player, args[1])
        },
        CommandDefinition(
            "despawn", 2, Permission.DESPAWN, "/petblocks despawn <name> [player]"
        ) { sender, player, args ->
            deSpawnPet(sender, player, args[1])
        },
        CommandDefinition(
            "toggle", 2, Permission.TOGGLE, "/petblocks toggle <name> [player]"
        ) { sender, player, args ->
            togglePet(sender, player, args[1])
        },
        CommandDefinition(
            "select", 2, Permission.SELECT, "/petblocks select <name> [player]"
        ) { sender, player, args ->
            selectPet(sender, player, args[1])
        },
        CommandDefinition(
            "openheaddatabase", 2, Permission.OPEN_HEADDATABSE, "/petblocks openheaddatabase <name> [player]"
        ) { sender, player, args ->
            openHeadDatabase(sender, player, args[1])
        },
        CommandDefinition(
            "breakblock", 4, Permission.BREAK_BLOCK, "/petblocks breakblock <name> <timeToBreak> <dropType> [player]"
        ) { sender, player, args ->
            if (args[2].toIntOrNull() != null) {
                breakBlock(player, args[1], args[3], args[2].toInt())
            }
        },
        CommandDefinition(
            "cancel", 2, Permission.CANCEL, "/petblocks cancel <name> [player]"
        ) { sender, player, args ->
            cancel(sender, player, args[1])
        },
        CommandDefinition(
            "snap", 2, Permission.SNAP, "/petblocks snap <name> [player]"
        ) { sender, player, args ->
            snap(sender, player, args[1])
        },
        CommandDefinition(
            "moveforward", 3, Permission.MOVEREL, "/petblocks moveforward <name> <speed> [player]"
        ) { sender, player, args ->
            moveForward(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "rotaterel", 4, Permission.ROTATEREL, "/petblocks rotaterel <name> <direction> <angle> [player]"
        ) { sender, player, args ->
            rotateRel(sender, player, args[1], args[2], args[3])
        },
        CommandDefinition(
            "entitytype", 3, Permission.ENTITYTYPE, "/petblocks entitytype <name> <entityType> [player]"
        ) { sender, player, args ->
            setEntityType(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "entityvisible", 3, Permission.ENTITYVISIBILITY, "/petblocks entityvisible <name> <true/false> [player]"
        ) { sender, player, args ->
            setEntityVisible(sender, player, args[1], args[2])
        },
        CommandDefinition(
            "groundoffset", 3, Permission.GROUNDOFFSET, "/petblocks groundoffset <name> <offset> [player]"
        ) { sender, player, args ->
            setGroundOffset(sender, player, args[1], args[2])
        }
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
            sender.sendPluginMessage(e.message!!)
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
                val player = findPlayer(sender, definition.minArgsCount, args) ?: throw PetBlocksException(
                    String.format(
                        PetBlocksLanguage.playerNotFoundMessage, args[args.size - 1]
                    )
                )
                val newArgs = args.map { e -> e.replace("###", " ") }.toTypedArray()
                definition.playerFunction.invoke(sender, player, newArgs)
                return true
            }
        }

        val commandsPerPage = 10
        val amountOfPages = (allCommands.size.toDouble() / commandsPerPage.toDouble()).toInt() + 1

        if (args.size == 1 && args[0].equals("reload", true) && sender.hasPermission(Permission.RELOAD.text)) {
            plugin.reloadConfig()
            val language = configurationService.findValue<String>("language")
            plugin.reloadTranslation(language, PetBlocksLanguage::class.java, "en_us")
            plugin.logger.log(Level.INFO, "Loaded language file $language.properties.")
            templateRepository.clearCache()

            try {
                val templates = templateRepository.getAll()
                for (pet in petService.getCache().values.flatten()) {
                    val matchingTemplate = templates.firstOrNull { e -> e.name.equals(pet.template.name, true) }

                    if (matchingTemplate != null) {
                        pet.template = matchingTemplate
                    }
                }
                sender.sendPluginMessage(PetBlocksLanguage.reloadMessage)
            } catch (e: Exception) {
                plugin.logger.log(Level.SEVERE, "Failed to load file", e)
                sender.sendPluginMessage(PetBlocksLanguage.errorLoadingTemplatesMessage)
            }
            return true
        }

        if (args.size == 2 && args[0].equals(
                "help", true
            ) && sender.hasPermission(Permission.HELP.text) && args[1].toIntOrNull() != null && args[1].toInt() > 1
        ) {
            val helpIndex = args[1].toInt()
            sender.sendPluginMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.drop(commandsPerPage * (helpIndex - 1)).take(commandsPerPage)) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendPluginMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendPluginMessage(ChatColor.GREEN.toString() + "----------┌${helpIndex}/${amountOfPages}┐----------")

            return true
        }

        if (args.isNotEmpty() && args[0].equals("help", true) && sender.hasPermission(Permission.HELP.text)) {
            sender.sendPluginMessage(ChatColor.GREEN.toString() + "---------PetBlocks---------")
            for (commandDefinition in allCommands.take(commandsPerPage)) {
                if (sender.hasPermission(commandDefinition.permission.text)) {
                    sender.sendPluginMessage(ChatColor.GRAY.toString() + commandDefinition.helpMessage)
                }
            }
            sender.sendPluginMessage(ChatColor.GREEN.toString() + "----------┌1/${amountOfPages}┐----------")

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

        if (args.size >= 2 && sender.hasPermission(Permission.ROTATEREL.text) && args[0].equals("rotaterel", true)) {
            return PetRotationType.values().map { e -> e.name.lowercase() }
        }

        if (args.size >= 2 && sender.hasPermission(Permission.ENTITYTYPE.text) && args[0].equals("entitytype", true)) {
            return EntityType.values().map { e -> "minecraft:${e.name.lowercase()}" }
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

        val pets = petService.getPetsFromPlayer(player)

        val petAmountPermission = "${Permission.DYN_AMOUNT.text}${pets.size + 1}"

        if (!player.hasPermission(petAmountPermission)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petAmountNotAllowed, (pets.size + 1).toString()))
            return
        }

        val templatePermission = Permission.DYN_TEMPLATE.text + templateId

        if (!player.hasPermission(templatePermission)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.templateNotAllowed, templateId))
            return
        }

        if (!PetBlocksDependencyInjectionModule.areLegacyVersionsIncluded && pets.isNotEmpty()) {
            sender.sendPluginMessage(PetBlocksLanguage.premiumMultiplePets)
            return
        }

        petService.createPet(
            player, player.location.toVector3d().addRelativeFront(3.0).toLocation(), template.name, petName
        )
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petCreatedMessage, petName))
    }

    private suspend fun setSkinType(sender: CommandSender, player: Player, petName: String, material: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            itemService.toItemStack(Item(material)) // Test if material is valid.
            val item = pet.headItem
            item.typeName = material
            pet.headItem = item
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinTypeChangedMessage, petName))
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinTypeNotFound, material))
        }
    }

    private suspend fun rotateRel(
        sender: CommandSender, player: Player, petName: String, directionName: String, angleRaw: String
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        val petRotationType = try {
            PetRotationType.values().first { t -> t.name.equals(directionName, true) }
        } catch (e: Exception) {
            player.sendMessage(
                PetBlocksLanguage.petRotationTypeNotFound.format(
                    PetRotationType.values().joinToString(",")
                )
            )
            return
        }

        if (angleRaw.toDoubleOrNull() == null) {
            player.sendMessage(PetBlocksLanguage.cannotParseNumberMessage)
            return
        }

        val location = pet.location
        val angle = angleRaw.toFloat()

        if (petRotationType == PetRotationType.RIGHT) {
            location.yaw += angle
        } else if (petRotationType == PetRotationType.LEFT) {
            location.yaw -= angle
        } else if (petRotationType == PetRotationType.UP) {
            location.pitch -= angle
        } else if (petRotationType == PetRotationType.DOWN) {
            location.pitch += angle
        }

        pet.location = location
        sender.sendPluginMessage(String.format(PetBlocksLanguage.rotationRelMessage))
    }

    private suspend fun setSkinBase64(
        sender: CommandSender, player: Player, petName: String, base64EncodedSkinUrl: String
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        val headItem = pet.headItem
        headItem.typeName = "minecraft:player_head,397"
        pet.headItem = headItem

        val dataComponent =
            "{\"minecraft:profile\":{\"properties\":[{\"name\":\"textures\",\"value\":\"$base64EncodedSkinUrl\"}]}}"
        val id1 = random.nextInt()
        val id2 = random.nextInt()
        val id3 = random.nextInt()
        val id4 = random.nextInt()
        val nbt =
            "{SkullOwner:{Id:[I;${id1},${id2},${id3},${id4}],Name:\"${id1}\",Properties:{textures:[{Value:\"${base64EncodedSkinUrl}\"}]}}}"

        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.nbt = nbt
            itemService.toItemStack(testItem)// Test if nbt and datacomponent is valid.
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseNbtMessage, nbt))
            return
        }

        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.component = dataComponent
            itemService.toItemStack(testItem)// Test if nbt and datacomponent is valid.
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseDataComponentMessage, dataComponent))
            return
        }

        val item = pet.headItem
        item.nbt = nbt
        item.component = dataComponent
        pet.headItem = item
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, petName))
    }

    private suspend fun setSkinHeadDatabase(sender: CommandSender, player: Player, petName: String, hdbId: String) {
        try {
            val itemStack = dependencyHeadDatabaseService!!.getItemStackFromId(hdbId)!!
            val item = itemService.toItem(itemStack)
            setSkinBase64(sender, player, petName, item.base64EncodedSkinUrl!!)
        } catch (e: Exception) {
            sender.sendPluginMessage(PetBlocksLanguage.headDatabasePluginNotLoaded)
            return
        }
    }

    private suspend fun setVisibility(
        sender: CommandSender, player: Player, petName: String, visibilityTypeName: String
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        val visibilityType = PetVisibility.values().firstOrNull { e -> e.name.equals(visibilityTypeName, true) }

        if (visibilityType == null) {
            sender.sendPluginMessage(
                String.format(
                    PetBlocksLanguage.visibilityTypeNotFoundMessage,
                    PetVisibility.values().map { e -> e.name.lowercase() }.joinToString(",")
                )
            )
            return
        }

        pet.visibility = visibilityType
        sender.sendPluginMessage(String.format(PetBlocksLanguage.visibilityChangedMessage, visibilityTypeName))
    }

    private suspend fun moveForward(sender: CommandSender, player: Player, petName: String, speed: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        if (speed.toDoubleOrNull() == null) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.speedCannotBeParsed, speed))
            return
        }

        pet.moveForward(speed.toDouble())
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petMoveForwardMessage))
    }

    private suspend fun setPetLoop(sender: CommandSender, player: Player, petName: String, loop: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        if (!pet.template.loops.containsKey(loop)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petLoopNotFound, loop))
            return
        }

        pet.loop = loop
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petLoopChangedMessage, petName, loop))
    }

    private suspend fun snap(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.snap()
        sender.sendPluginMessage(PetBlocksLanguage.snapMessage)
    }

    private suspend fun ridePet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.ride()

        val loop = "ride"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendPluginMessage(String.format(PetBlocksLanguage.petRideMessage, petName))
    }

    private suspend fun selectPet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        val playerInformation = petMetaRepository.getByPlayer(player) ?: return
        playerInformation.selectedPet = pet.name
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSelectedMessage, petName))
    }

    private suspend fun breakBlock(
        player: Player, petName: String, dropTypes: String, timeToBreak: Int
    ) {

        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        val block = pet.getBlockInFrontOf() ?: return

        val playerInteractEvent = BlockBreakEvent(block, player)
        Bukkit.getPluginManager().callEvent(playerInteractEvent)

        if (playerInteractEvent.isCancelled) {
            return
        }

        val actualDropTypes = try {
            dropTypes.split(",").map { e -> DropType.values().first { t -> t.name.equals(e, true) } }
        } catch (e: Exception) {
            player.sendMessage(PetBlocksLanguage.dropTypeNotFound.format(DropType.values().joinToString(",")))
            return
        }

        pet.breakBlock(timeToBreak, actualDropTypes)
    }

    private suspend fun cancel(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        pet.cancelAction()
        sender.sendPluginMessage(PetBlocksLanguage.cancelMessage)
    }

    private suspend fun setPetTemplate(sender: CommandSender, player: Player, petName: String, templateId: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        val template = findTemplate(templateId)

        if (template == null) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.templateNotFoundMessage, templateId))
            return
        }

        if (!player.hasPermission(Permission.DYN_TEMPLATE.text + templateId)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.templateNotAllowed, templateId))
            return
        }

        pet.template = template
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petTemplateChangeMessage, petName, templateId))
    }

    private suspend fun setSkinNbt(sender: CommandSender, player: Player, petName: String, nbt: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.nbt = nbt
            itemService.toItemStack(testItem)// Test if nbt is valid.

            val item = pet.headItem
            item.nbt = nbt
            pet.headItem = item
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, petName))
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseNbtMessage, nbt))
        }
    }

    private suspend fun setSkinDataComponent(
        sender: CommandSender,
        player: Player,
        petName: String,
        dataComponent: String
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.component = dataComponent
            itemService.toItemStack(testItem)// Test if dataComponent is valid.

            val item = pet.headItem
            item.component = dataComponent
            pet.headItem = item
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, petName))
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseDataComponentMessage, dataComponent))
        }
    }

    private suspend fun setDisplayName(sender: CommandSender, player: Player, petName: String, displayName: String) {

        val regex = configurationService.findValue<String>(regexPath)
        val blackList = (configurationService.findValue<List<String>>(blackListPath)).map { e -> e.lowercase() }
        val minLength = configurationService.findValue<Int>(minLengthPath)
        val maxLength = configurationService.findValue<Int>(maxLengthPath)

        if (displayName.length < minLength || displayName.length > maxLength) {
            sender.sendPluginMessage(PetBlocksLanguage.petCharacterNotAllowed)
            return
        }

        if (!regex.toRegex().matches(displayName)) {
            sender.sendPluginMessage(PetBlocksLanguage.petCharacterNotAllowed)
            return
        }

        val lowerDisplayName = displayName.lowercase()

        for (blackWord in blackList) {
            if (lowerDisplayName.contains(blackWord)) {
                sender.sendPluginMessage(PetBlocksLanguage.petCharacterNotAllowed)
                return
            }
        }

        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.displayName = displayName.replace("_", " ")
        sender.sendPluginMessage(
            String.format(
                PetBlocksLanguage.petNameChangeMessage, petName, pet.displayName.translateChatColors()
            )
        )
    }

    private suspend fun teleportPet(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.location = location
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petTeleportedMessage, petName))
    }

    private suspend fun setVelocityToPet(
        sender: CommandSender, player: Player, petName: String, vector: org.bukkit.util.Vector
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.velocity = vector
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petVelocityAppliedMessage, petName))
    }

    private suspend fun deletePet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        petService.deletePet(pet)
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petDeletedMessage, petName))
    }

    private suspend fun listPet(sender: CommandSender, player: Player) {
        val pets = petService.getPetsFromPlayer(player)
        val petString = pets.joinToString(", ") { e -> e.name }
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petListMessage, pets.size, petString))
    }

    private suspend fun lookAtLocation(sender: CommandSender, player: Player, petName: String, location: Location) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        location.y = location.y - pet.groundOffset
        pet.lookAt(location)
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petLookAtMessage))
    }

    private suspend fun unmount(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.umount()

        val loop = "idle"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendPluginMessage(String.format(PetBlocksLanguage.petUnmountMessage, petName))
    }

    private suspend fun hat(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.hat()
        val loop = "hat"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendPluginMessage(String.format(PetBlocksLanguage.petHatMessage, petName))
    }

    private suspend fun spawnPet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.spawn()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSpawnedMessage, petName))
    }

    private suspend fun togglePet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        if (pet.isSpawned) {
            deSpawnPet(sender, player, petName)
        } else {
            spawnPet(sender, player, petName)
        }
    }

    private suspend fun setEntityType(sender: CommandSender, player: Player, petName: String, entityType: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.entityType = entityType
        sender.sendPluginMessage(String.format(PetBlocksLanguage.entityTypeChangeMessage, entityType))
    }

    private suspend fun setEntityVisible(sender: CommandSender, player: Player, petName: String, flag: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            pet.isEntityVisible = flag.toBoolean()
            sender.sendPluginMessage(String.format(PetBlocksLanguage.entityVisibilityChangedMessage, flag))
        } catch (e: Exception) {
            // Ignored
        }
    }

    private suspend fun setGroundOffset(sender: CommandSender, player: Player, petName: String, offset: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        if (offset.toDoubleOrNull() == null) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.groundOffsetCannotBeParsed, offset))
            return
        }

        pet.groundOffset = offset.toDouble()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.groundOffsetChangedMessage, offset))
    }

    private suspend fun deSpawnPet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.remove()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petDespawnedMessage, petName))
    }


    private suspend fun walkToLocation(
        sender: CommandSender, player: Player, petName: String, location: Location, speed: String
    ) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        if (speed.toDoubleOrNull() == null) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.speedCannotBeParsed, speed))
            return
        }

        pet.moveTo(location, speed.toDouble())
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petWalkToLocationMessage))
    }

    private suspend fun callPet(sender: CommandSender, player: Player, petName: String) {
        val pet = findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))
        pet.call()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petCalledMessage, petName))
    }

    private suspend fun openHeadDatabase(sender: CommandSender, player: Player, petName: String) {
        findPetFromPlayer(player, petName)
            ?: throw PetBlocksException(String.format(PetBlocksLanguage.petNotFoundMessage, petName))

        try {
            dependencyHeadDatabaseService!!.registerPlayerForNextClick(player, petName)
            val configValue = "headDatabaseCommand"
            var command = if (configurationService.containsValue(configValue)) {
                configurationService.findValue("headDatabaseCommand")
            } else {
                // Compatibility to 9.0.3
                "/hdb"
            }

            command = if (command.startsWith("/")) {
                command.substring(1)
            } else {
                command
            }

            Bukkit.getServer().dispatchCommand(player, command)
        } catch (e: Exception) {
            sender.sendPluginMessage(PetBlocksLanguage.headDatabasePluginNotLoaded)
            return
        }
    }

    private suspend fun findPetFromPlayer(player: Player, petName: String): Pet? {
        val pets = petService.getPetsFromPlayer(player)
        return pets.firstOrNull { e -> e.name.equals(petName, true) }
    }

    private suspend fun findTemplate(templateId: String): PetTemplate? {
        return templateRepository.getAll().firstOrNull { e -> e.name == templateId }
    }

    private fun findLocation(
        worldName: String?, x: String, y: String, z: String, yaw: String = "0", pitch: String = "0"
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
        try {
            var player: Player? = null

            if (args.size == argIndex + 1 && sender.hasPermission(Permission.MANIPULATE_OTHER.text)) {
                val playerId = args[argIndex]
                player = Bukkit.getPlayer(playerId)

                if (player == null) {
                    player = Bukkit.getPlayer(UUID.fromString(playerId))
                }
            } else if (sender is Player) {
                player = sender
            }

            return player
        } catch (e: Exception) {
            return null
        }
    }

    private class CommandDefinition(
        val command: String,
        val minArgsCount: Int,
        val permission: Permission,
        val helpMessage: String,
        val playerFunction: suspend (CommandSender, Player, Array<String>) -> Unit
    )

    private fun CommandSender.sendPluginMessage(message: String) {
        if (message.isBlank()) {
            return
        }

        this.sendMessage(message)
    }
}
