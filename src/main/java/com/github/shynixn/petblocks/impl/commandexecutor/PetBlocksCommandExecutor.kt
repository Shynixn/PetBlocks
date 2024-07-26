package com.github.shynixn.petblocks.impl.commandexecutor

import com.github.shynixn.mccoroutine.bukkit.launch
import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.command.*
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
import com.google.inject.Inject
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.plugin.Plugin
import org.bukkit.util.Vector
import java.util.*

class PetBlocksCommandExecutor @Inject constructor(
    private val petService: PetService,
    private val templateRepository: CacheRepository<PetTemplate>,
    private val plugin: Plugin,
    private val configurationService: ConfigurationService,
    chatMessageService: ChatMessageService,
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val itemService: ItemService
) {
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
    private val coroutineExecutor = object : CoroutineExecutor {
        override fun execute(f: suspend () -> Unit) {
            plugin.launch {
                f.invoke()
            }
        }
    }
    private val manipulateOtherPermission: () -> String = {
        Permission.MANIPULATE_OTHER.text
    }
    private val manipulateOtherPermissionMessage: () -> String = {
        PetBlocksLanguage.manipulateOtherMessage
    }
    private val onlinePlayerTabs: (suspend (CommandSender) -> List<String>) = {
        Bukkit.getOnlinePlayers().map { e -> e.name }
    }
    private val worldTabs: (suspend (CommandSender) -> List<String>) = {
        Bukkit.getWorlds().map { e -> e.name }
    }
    private val templateTabs: (suspend (CommandSender) -> List<String>) = {
        templateRepository.getAll().map { e -> e.name }
    }
    private val booleanTabs: (suspend (CommandSender) -> List<String>) = {
        listOf(true, false).map { e -> e.toString() }
    }
    private val petNamesTabs: (suspend (CommandSender) -> List<String>) = { sender ->
        if (sender is Player) {
            petService.getPetsFromPlayer(sender).map { e -> e.name }
        } else {
            emptyList()
        }
    }
    private val materialTabs: (suspend (CommandSender) -> List<String>) = { _ ->
        Material.values().map { e -> "minecraft:${e.name}" }
    }
    private val visibilityTabs: (suspend (CommandSender) -> List<String>) = { _ ->
        PetVisibility.values().map { e -> e.name }
    }
    private val rotationTypeTabs: (suspend (CommandSender) -> List<String>) = { _ ->
        PetRotationType.values().map { e -> e.name }
    }

    private val senderHasToBePlayer: () -> String = {
        PetBlocksLanguage.commandSenderHasToBePlayer
    }
    private val worldMustExist = object : Validator<World> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): World? {
            try {
                return Bukkit.getWorld(openArgs[0])
            } catch (e: Exception) {
                return null
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.worldNotFoundMessage
        }
    }

    private val mustBeDouble = object : Validator<Double> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Double? {
            return openArgs[0].toDoubleOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.cannotParseNumberMessage.format(openArgs[0])
        }
    }

    private val mustBeInt = object : Validator<Int> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Int? {
            return openArgs[0].toIntOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.cannotParseNumberMessage.format(openArgs[0])
        }
    }

    private val mustBeBoolean = object : Validator<Boolean> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Boolean? {
            return openArgs[0].toBooleanStrictOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.cannotParseBoolean.format(openArgs[0])
        }
    }

    private val visibilityMustExist = object : Validator<PetVisibility> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetVisibility? {
            return PetVisibility.values().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return String.format(PetBlocksLanguage.visibilityTypeNotFoundMessage,
                PetVisibility.values().joinToString(",") { e -> e.name.lowercase() })
        }
    }

    private val rotationTypeMustExist = object : Validator<PetRotationType> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetRotationType? {
            return PetRotationType.values().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return String.format(PetBlocksLanguage.petRotationTypeNotFound,
                PetRotationType.values().joinToString(",") { e -> e.name.lowercase() })
        }
    }

    private val templateMustExist = object : Validator<PetTemplate> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetTemplate? {
            return templateRepository.getAll().firstOrNull { e -> e.name == openArgs[0] }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.templateNotFoundMessage.format(openArgs[0])
        }
    }
    private val playerMustExist = object : Validator<Player> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): Player? {
            try {
                val playerId = openArgs[0]
                val player = Bukkit.getPlayer(playerId)

                if (player != null) {
                    return player
                }
                return Bukkit.getPlayer(UUID.fromString(playerId))
            } catch (e: Exception) {
                return null
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.playerNotFoundMessage.format(openArgs[0])
        }
    }
    private val templateMustHavePermission = object : Validator<PetTemplate> {
        override suspend fun validate(
            sender: CommandSender, prevArgs: List<Any>, argument: PetTemplate, openArgs: List<String>
        ): Boolean {
            return sender.hasPermission(Permission.DYN_TEMPLATE.text + argument.name)
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.templateNotAllowed
        }
    }
    private val materialMustExist = object : Validator<String> {
        override suspend fun validate(
            sender: CommandSender, prevArgs: List<Any>, argument: String, openArgs: List<String>
        ): Boolean {
            try {
                itemService.toItemStack(Item(argument)) // Test if material is valid.
                return true
            } catch (e: Exception) {
                return false
            }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return String.format(PetBlocksLanguage.petSkinTypeNotFound, openArgs[0])
        }
    }

    private val petNameMustBeLimited = object : Validator<String> {
        private val regex = configurationService.findValue<String>(regexPath)
        private val blackList = (configurationService.findValue<List<String>>(blackListPath)).map { e -> e.lowercase() }
        private val minLength = configurationService.findValue<Int>(minLengthPath)
        private val maxLength = configurationService.findValue<Int>(maxLengthPath)
        override suspend fun validate(
            sender: CommandSender, prevArgs: List<Any>, argument: String, openArgs: List<String>
        ): Boolean {
            if (argument.length < minLength || argument.length > maxLength) {
                return false
            }

            if (!regex.toRegex().matches(argument)) {
                return false
            }

            val lowerDisplayName = argument.lowercase()

            for (blackWord in blackList) {
                if (lowerDisplayName.contains(blackWord)) {
                    return false
                }
            }

            return true
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return PetBlocksLanguage.petCharacterNotAllowed
        }
    }

    private suspend fun petMustExist(player: Player, name: String): Pet {
        val pets = petService.getPetsFromPlayer(player)
        val pet = pets.firstOrNull { e -> e.name.equals(name, true) }

        if (pet == null) {
            throw ValidationException(PetBlocksLanguage.petNotFoundMessage.format(name))
        }

        return pet
    }

    init {
        val commandBuilder = CommandBuilder(plugin, coroutineExecutor, "petblocks", chatMessageService) {
            usage(PetBlocksLanguage.commandUsage)
            description(PetBlocksLanguage.commandDescription)
            aliases(plugin.config.getStringList("commands.petblocks.aliases"))
            permission(Permission.COMMAND)
            permissionMessage(PetBlocksLanguage.noPermissionCommand)
            subCommand("create") {
                permission(Permission.CREATE)
                toolTip { PetBlocksLanguage.createCommandHint }
                builder().argument("name").validator(petNameMustBeLimited).tabs { listOf("<name>") }
                    .argument("template").validator(templateMustExist).validator(templateMustHavePermission)
                    .tabs(templateTabs).executePlayer(senderHasToBePlayer) { player, name, petTemplate ->
                        createPet(player, player, name, petTemplate)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, petTemplate, player ->
                        createPet(commandSender, player, name, petTemplate)
                    }
            }
            subCommand("delete") {
                permission(Permission.DELETE)
                toolTip { PetBlocksLanguage.deleteCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    deletePet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        deletePet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("list") {
                permission(Permission.LIST)
                toolTip { PetBlocksLanguage.listCommandHint }
                builder().executePlayer(senderHasToBePlayer) { player ->
                    listPet(player, player)
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { _, player ->
                        listPet(player, player)
                    }
            }
            subCommand("call") {
                permission(Permission.CALL)
                toolTip { PetBlocksLanguage.callCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    callPet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        callPet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("lookat") {
                permission(Permission.LOOKAT)
                toolTip { PetBlocksLanguage.lookAtCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("x").validator(mustBeDouble)
                    .tabs { listOf("<x>") }.argument("y").validator(mustBeDouble).tabs { listOf("<y>") }.argument("z")
                    .validator(mustBeDouble).tabs { listOf("<z>") }
                    .executePlayer(senderHasToBePlayer) { player, name, x, y, z ->
                        lookAtLocation(player, petMustExist(player, name), Location(player.world, x, y, z))
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, x, y, z, player ->
                        lookAtLocation(commandSender, petMustExist(player, name), Location(player.world, x, y, z))
                    }
            }
            subCommand("lookatOwner") {
                permission(Permission.LOOKATOWNER)
                toolTip { PetBlocksLanguage.lookAtOwnerCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    lookAtLocation(player, petMustExist(player, name), player.location.add(0.0, 1.0, 0.0))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        lookAtLocation(commandSender, petMustExist(player, name), player.location.add(0.0, 1.0, 0.0))
                    }
            }.helpCommand()
            subCommand("moveto") {
                permission(Permission.MOVETO)
                toolTip { PetBlocksLanguage.moveToCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("x").validator(mustBeDouble)
                    .tabs { listOf("<x>") }.argument("y").validator(mustBeDouble).tabs { listOf("<y>") }.argument("z")
                    .validator(mustBeDouble).tabs { listOf("<z>") }.argument("speed").validator(mustBeDouble)
                    .tabs { listOf("<speed>") }.executePlayer(senderHasToBePlayer) { player, name, x, y, z, speed ->
                        walkToLocation(player, petMustExist(player, name), Location(player.world, x, y, z), speed)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, x, y, z, speed, player ->
                        walkToLocation(
                            commandSender, petMustExist(player, name), Location(player.world, x, y, z), speed
                        )
                    }
            }
            subCommand("movetoowner") {
                permission(Permission.MOVETOOWNER)
                toolTip { PetBlocksLanguage.moveToOwnerCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("speed").validator(mustBeDouble)
                    .tabs { listOf("<speed>") }.executePlayer(senderHasToBePlayer) { player, name, speed ->
                        walkToLocation(
                            player,
                            petMustExist(player, name),
                            player.location.toVector3d().addRelativeFront(3.0).toLocation(),
                            speed
                        )
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, speed, player ->
                        walkToLocation(
                            commandSender,
                            petMustExist(player, name),
                            player.location.toVector3d().addRelativeFront(3.0).toLocation(),
                            speed
                        )
                    }
            }
            subCommand("hat") {
                permission(Permission.HAT)
                toolTip { PetBlocksLanguage.hatCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    hat(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        hat(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("ride") {
                permission(Permission.RIDE)
                toolTip { PetBlocksLanguage.rideCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    ridePet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        ridePet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("unmount") {
                permission(Permission.UNMOUNT)
                toolTip { PetBlocksLanguage.unmountCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    unmount(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        unmount(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("teleport") {
                permission(Permission.TELEPORT)
                toolTip { PetBlocksLanguage.teleportCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("world").validator(worldMustExist)
                    .tabs(worldTabs).argument("x").validator(mustBeDouble).tabs { listOf("<x>") }.argument("y")
                    .validator(mustBeDouble).tabs { listOf("<y>") }.argument("z").validator(mustBeDouble)
                    .tabs { listOf("<z>") }.argument("yaw").validator(mustBeDouble).tabs { listOf("<yaw>") }
                    .argument("pitch").validator(mustBeDouble).tabs { listOf("<pitch>") }
                    .executePlayer(senderHasToBePlayer) { player, name, world, x, y, z, yaw, pitch ->
                        teleportPet(
                            player, petMustExist(player, name), Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
                        )
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, world, x, y, z, yaw, pitch, player ->
                        teleportPet(
                            commandSender,
                            petMustExist(player, name),
                            Location(world, x, y, z, yaw.toFloat(), pitch.toFloat())
                        )
                    }
            }
            subCommand("velocity") {
                permission(Permission.VELOCITY)
                toolTip { PetBlocksLanguage.velocityCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("x").validator(mustBeDouble)
                    .tabs { listOf("<x>") }.argument("y").validator(mustBeDouble).tabs { listOf("<y>") }.argument("z")
                    .validator(mustBeDouble).tabs { listOf("<z>") }
                    .executePlayer(senderHasToBePlayer) { player, name, x, y, z ->
                        setVelocityToPet(
                            player, petMustExist(player, name), Vector(x, y, z)
                        )
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, x, y, z, player ->
                        setVelocityToPet(
                            commandSender, petMustExist(player, name), Vector(x, y, z)
                        )
                    }
            }
            subCommand("skintype") {
                permission(Permission.SKIN)
                toolTip { PetBlocksLanguage.skinTypeCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("material").validator(materialMustExist)
                    .tabs(materialTabs).executePlayer(senderHasToBePlayer) { player, name, material ->
                        setSkinType(player, petMustExist(player, name), material, 0)
                    }
                    .argument("durability").validator(mustBeInt)
                    .tabs(materialTabs).executePlayer(senderHasToBePlayer) { player, name, material, durability ->
                        setSkinType(player, petMustExist(player, name), material, durability)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, material, durability, player ->
                        setSkinType(commandSender, petMustExist(player, name), material, durability)
                    }
            }
            subCommand("skinnbt") {
                permission(Permission.SKIN)
                toolTip { PetBlocksLanguage.skinNbtCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("nbt").tabs { listOf("<nbt>") }
                    .executePlayer(senderHasToBePlayer) { player, name, nbt ->
                        setSkinNbt(player, petMustExist(player, name), nbt)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, nbt, player ->
                        setSkinNbt(commandSender, petMustExist(player, name), nbt)
                    }
            }
            subCommand("skincomponent") {
                permission(Permission.SKIN)
                toolTip { PetBlocksLanguage.skinComponentCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("datacomponent")
                    .tabs { listOf("<datacomponent>") }.executePlayer(senderHasToBePlayer) { player, name, component ->
                        setSkinDataComponent(player, petMustExist(player, name), component)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, component, player ->
                        setSkinDataComponent(commandSender, petMustExist(player, name), component)
                    }
            }
            subCommand("skinbase64") {
                permission(Permission.SKIN)
                toolTip { PetBlocksLanguage.skinBase64CommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("skin").tabs { listOf("<skin>") }
                    .executePlayer(senderHasToBePlayer) { player, name, skin ->
                        setSkinBase64(player, petMustExist(player, name), skin)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, skin, player ->
                        setSkinBase64(commandSender, petMustExist(player, name), skin)
                    }
            }
            subCommand("skinheaddatabase") {
                permission(Permission.SKIN_HEADDATABASE)
                toolTip { PetBlocksLanguage.skinHeadDatabaseCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("hdbId").tabs { listOf("<hdbId>") }
                    .executePlayer(senderHasToBePlayer) { player, name, hdbId ->
                        setSkinHeadDatabase(player, petMustExist(player, name), hdbId)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, hdbId, player ->
                        setSkinHeadDatabase(commandSender, petMustExist(player, name), hdbId)
                    }
            }
            subCommand("rename") {
                permission(Permission.RENAME)
                toolTip { PetBlocksLanguage.renameCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("displayName").validator(petNameMustBeLimited)
                    .tabs { listOf("<displayName>") }.executePlayer(senderHasToBePlayer) { player, name, displayName ->
                        setDisplayName(player, petMustExist(player, name), displayName)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, displayName, player ->
                        setDisplayName(commandSender, petMustExist(player, name), displayName)
                    }
            }
            subCommand("visibility") {
                permission(Permission.VISIBILITY)
                toolTip { PetBlocksLanguage.visibilityCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("type").validator(visibilityMustExist)
                    .tabs(visibilityTabs).executePlayer(senderHasToBePlayer) { player, name, visibilityType ->
                        setVisibility(player, petMustExist(player, name), visibilityType)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, visibilityType, player ->
                        setVisibility(commandSender, petMustExist(player, name), visibilityType)
                    }
            }
            subCommand("loop") {
                permission(Permission.LOOP)
                toolTip { PetBlocksLanguage.loopCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("loop").tabs { listOf("<loop>") }
                    .executePlayer(senderHasToBePlayer) { player, name, loop ->
                        setPetLoop(player, petMustExist(player, name), loop)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, loop, player ->
                        setPetLoop(commandSender, petMustExist(player, name), loop)
                    }
            }
            subCommand("template") {
                permission(Permission.TEMPLATE)
                toolTip { PetBlocksLanguage.templateCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("template").validator(templateMustExist)
                    .tabs(templateTabs).executePlayer(senderHasToBePlayer) { player, name, template ->
                        setPetTemplate(player, petMustExist(player, name), template)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, template, player ->
                        setPetTemplate(commandSender, petMustExist(player, name), template)
                    }
            }
            subCommand("spawn") {
                permission(Permission.SPAWN)
                toolTip { PetBlocksLanguage.spawnCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    spawnPet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        spawnPet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("despawn") {
                permission(Permission.DESPAWN)
                toolTip { PetBlocksLanguage.deleteCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    deSpawnPet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        deSpawnPet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("toggle") {
                permission(Permission.TOGGLE)
                toolTip { PetBlocksLanguage.toggleCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    togglePet(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        togglePet(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("select") {
                permission(Permission.SELECT)
                toolTip { PetBlocksLanguage.selectCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    selectPet(player, player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        selectPet(commandSender, player, petMustExist(player, name))
                    }
            }
            subCommand("openheaddatabase") {
                permission(Permission.OPEN_HEADDATABSE)
                toolTip { PetBlocksLanguage.openHeadDatabaseCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    openHeadDatabase(player, player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        openHeadDatabase(commandSender, player, petMustExist(player, name))
                    }
            }
            subCommand("breakblock") {
                permission(Permission.BREAK_BLOCK)
                toolTip { PetBlocksLanguage.breakBlockCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("timeToBreak").validator(mustBeInt)
                    .tabs { listOf("<ticks>") }.argument("dropType").tabs { DropType.values().map { e -> e.name } }
                    .executePlayer(senderHasToBePlayer) { player, name, timeToBreak, dropType ->
                        breakBlock(player, petMustExist(player, name), dropType, timeToBreak)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, timeToBreak, dropType, player ->
                        breakBlock(commandSender, petMustExist(player, name), dropType, timeToBreak)
                    }
            }
            subCommand("cancel") {
                permission(Permission.CANCEL)
                toolTip { PetBlocksLanguage.cancelCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    cancel(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        cancel(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("moveforward") {
                permission(Permission.MOVEREL)
                toolTip { PetBlocksLanguage.moveForwardCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("speed").validator(mustBeDouble)
                    .executePlayer(senderHasToBePlayer) { player, name, speed ->
                        moveForward(player, petMustExist(player, name), speed)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, speed, player ->
                        moveForward(commandSender, petMustExist(player, name), speed)
                    }
            }
            subCommand("rotaterel") {
                permission(Permission.ROTATEREL)
                toolTip { PetBlocksLanguage.rotateRelCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("type").validator(rotationTypeMustExist)
                    .tabs(rotationTypeTabs).argument("angle").validator(mustBeDouble)
                    .executePlayer(senderHasToBePlayer) { player, name, rotationType, angle ->
                        rotateRel(player, petMustExist(player, name), rotationType, angle.toFloat())
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, rotationType, angle, player ->
                        rotateRel(commandSender, petMustExist(player, name), rotationType, angle.toFloat())
                    }
            }
            subCommand("entitytype") {
                permission(Permission.ENTITYTYPE)
                toolTip { PetBlocksLanguage.entityTypeCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("type")
                    .tabs { EntityType.values().map { e -> "minecraft:${e.name.lowercase()}" } }
                    .executePlayer(senderHasToBePlayer) { player, name, type ->
                        setEntityType(player, petMustExist(player, name), type)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, type, player ->
                        setEntityType(commandSender, petMustExist(player, name), type)
                    }
            }
            subCommand("entityvisible") {
                permission(Permission.ENTITYVISIBILITY)
                toolTip { PetBlocksLanguage.entityVisibleCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("flag").validator(mustBeBoolean)
                    .tabs(booleanTabs).executePlayer(senderHasToBePlayer) { player, name, flag ->
                        setEntityVisible(player, petMustExist(player, name), flag)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, flag, player ->
                        setEntityVisible(commandSender, petMustExist(player, name), flag)
                    }
            }
            subCommand("groundoffset") {
                permission(Permission.GROUNDOFFSET)
                toolTip { PetBlocksLanguage.groundOffSetCommandHint }
                builder().argument("name").tabs(petNamesTabs).argument("offset").validator(mustBeDouble)
                    .tabs { listOf("<offset>") }.executePlayer(senderHasToBePlayer) { player, name, offset ->
                        setGroundOffset(player, petMustExist(player, name), offset)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, offset, player ->
                        setGroundOffset(commandSender, petMustExist(player, name), offset)
                    }
            }
            subCommand("snap") {
                permission(Permission.SNAP)
                toolTip { PetBlocksLanguage.snapCommandHint }
                builder().argument("name").tabs(petNamesTabs).executePlayer(senderHasToBePlayer) { player, name ->
                    snap(player, petMustExist(player, name))
                }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, player ->
                        snap(commandSender, petMustExist(player, name))
                    }
            }
            subCommand("variable") {
                permission(Permission.VARIABLE)
                toolTip { PetBlocksLanguage.variableCommandHint }
                builder().argument("name").tabs(petNamesTabs)
                    .argument("key").argument("value").executePlayer(senderHasToBePlayer) { player, name, key, value ->
                        setMemoryVariable(player, petMustExist(player, name), key, value)
                    }
                    .argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, key, value, player ->
                        setMemoryVariable(commandSender, petMustExist(player, name), key, value)
                    }
            }
            subCommand("reload") {
                permission(Permission.RELOAD)
                toolTip { PetBlocksLanguage.reloadCommandHint }
                builder().execute { sender ->
                    templateRepository.clearCache()
                    plugin.saveDefaultConfig()
                    plugin.reloadConfig()
                    configurationService.reload()
                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "petblocksgui reload")
                    sender.sendMessage(PetBlocksLanguage.reloadMessage)
                }
            }
        }
        commandBuilder.build()
    }

    private suspend fun createPet(
        sender: CommandSender, player: Player, petName: String, petTemplate: PetTemplate
    ) {
        val pets = petService.getPetsFromPlayer(player)
        val pet = pets.firstOrNull { e -> e.name.equals(petName, true) }

        if (pet != null) {
            sender.sendMessage(String.format(PetBlocksLanguage.petNameExistsMessage, petName))
            return
        }

        val petAmountPermission = "${Permission.DYN_AMOUNT.text}${pets.size + 1}"

        if (!player.hasPermission(petAmountPermission)) {
            sender.sendMessage(String.format(PetBlocksLanguage.petAmountNotAllowed, (pets.size + 1).toString()))
            return
        }

        if (!PetBlocksDependencyInjectionModule.areLegacyVersionsIncluded && pets.isNotEmpty()) {
            sender.sendMessage(PetBlocksLanguage.premiumMultiplePets)
            return
        }

        petService.createPet(
            player,
            player.location.toVector3d().addRelativeFront(3.0).toLocation(),
            petTemplate.name,
            petName,
        )
        sender.sendMessage(String.format(PetBlocksLanguage.petCreatedMessage, petName))
    }

    private suspend fun deletePet(sender: CommandSender, pet: Pet) {
        petService.deletePet(pet)
        sender.sendMessage(String.format(PetBlocksLanguage.petDeletedMessage, pet.name))
    }

    private suspend fun listPet(sender: CommandSender, player: Player) {
        val pets = petService.getPetsFromPlayer(player)
        val petString = pets.joinToString(", ") { e -> e.name }
        sender.sendMessage(String.format(PetBlocksLanguage.petListMessage, pets.size, petString))
    }

    private fun callPet(sender: CommandSender, pet: Pet) {
        pet.call()
        sender.sendMessage(String.format(PetBlocksLanguage.petCalledMessage, pet.name))
    }

    private fun lookAtLocation(sender: CommandSender, pet: Pet, location: Location) {
        location.y -= pet.groundOffset
        pet.lookAt(location)
        sender.sendMessage(String.format(PetBlocksLanguage.petLookAtMessage))
    }

    private fun walkToLocation(
        sender: CommandSender, pet: Pet, location: Location, speed: Double
    ) {
        pet.moveTo(location, speed)
        sender.sendMessage(String.format(PetBlocksLanguage.petWalkToLocationMessage))
    }

    private fun hat(sender: CommandSender, pet: Pet) {
        pet.hat()
        val loop = "hat"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendPluginMessage(String.format(PetBlocksLanguage.petHatMessage, pet.name))
    }

    private fun unmount(sender: CommandSender, pet: Pet) {
        pet.unmount()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petUnmountMessage, pet.name))
    }

    private fun teleportPet(sender: CommandSender, pet: Pet, location: Location) {
        pet.location = location
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petTeleportedMessage, pet.name))
    }

    private fun setVelocityToPet(
        sender: CommandSender, pet: Pet, vector: Vector
    ) {
        pet.velocity = vector
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petVelocityAppliedMessage, pet.name))
    }

    private fun setSkinType(sender: CommandSender, pet: Pet, material: String, durability: Int) {
        val item = pet.headItem
        item.typeName = material
        item.durability = durability.toString()
        pet.headItem = item
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinTypeChangedMessage, pet.name))
    }

    private fun setSkinNbt(sender: CommandSender, pet: Pet, nbt: String) {
        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.nbt = nbt
            itemService.toItemStack(testItem)// Test if nbt is valid.

            val item = pet.headItem
            item.nbt = nbt
            pet.headItem = item
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, pet.name))
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseNbtMessage, nbt))
        }
    }

    private fun setSkinDataComponent(
        sender: CommandSender, pet: Pet, dataComponent: String
    ) {
        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.component = dataComponent
            itemService.toItemStack(testItem)// Test if dataComponent is valid.

            val item = pet.headItem
            item.component = dataComponent
            pet.headItem = item
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, pet.name))
        } catch (e: Exception) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.cannotParseDataComponentMessage, dataComponent))
        }
    }

    private fun setSkinBase64(
        sender: CommandSender, pet: Pet, base64EncodedSkinUrl: String
    ) {
        val headItem = pet.headItem
        headItem.typeName = "minecraft:player_head,397"
        headItem.skinBase64 = base64EncodedSkinUrl
        headItem.durability = 3.toString()
        pet.headItem = headItem
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSkinNbtChanged, pet.name))
    }

    private fun setSkinHeadDatabase(sender: CommandSender, pet: Pet, hdbId: String) {
        try {
            val itemStack = dependencyHeadDatabaseService!!.getItemStackFromId(hdbId)!!
            val item = itemService.toItem(itemStack)
            setSkinBase64(sender, pet, item.skinBase64!!)
        } catch (e: Exception) {
            sender.sendPluginMessage(PetBlocksLanguage.headDatabasePluginNotLoaded)
            return
        }
    }

    private fun setDisplayName(sender: CommandSender, pet: Pet, displayName: String) {
        pet.displayName = displayName.replace("_", " ")
        sender.sendPluginMessage(
            String.format(
                PetBlocksLanguage.petNameChangeMessage, pet.name, pet.displayName.translateChatColors()
            )
        )
    }

    private fun ridePet(sender: CommandSender, pet: Pet) {
        pet.ride()

        val loop = "ride"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendPluginMessage(String.format(PetBlocksLanguage.petRideMessage, pet.name))
    }

    private fun setVisibility(
        sender: CommandSender, pet: Pet, visibility: PetVisibility
    ) {
        pet.visibility = visibility
        sender.sendPluginMessage(String.format(PetBlocksLanguage.visibilityChangedMessage, pet.name, visibility))
    }

    private fun setPetLoop(sender: CommandSender, pet: Pet, loop: String) {
        if (!pet.template.loops.containsKey(loop)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.petLoopNotFound, loop))
            return
        }

        pet.loop = loop
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petLoopChangedMessage, pet.name, loop))
    }

    private fun setPetTemplate(sender: CommandSender, pet: Pet, template: PetTemplate) {
        if (!sender.hasPermission(Permission.DYN_TEMPLATE.text + template.name)) {
            sender.sendPluginMessage(String.format(PetBlocksLanguage.templateNotAllowed, template.name))
            return
        }

        pet.template = template
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petTemplateChangeMessage, pet.name, template.name))
    }

    private fun spawnPet(sender: CommandSender, pet: Pet) {
        pet.spawn()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSpawnedMessage, pet.name))
    }

    private fun togglePet(sender: CommandSender, pet: Pet) {
        if (pet.isSpawned) {
            deSpawnPet(sender, pet)
        } else {
            spawnPet(sender, pet)
        }
    }

    private fun deSpawnPet(sender: CommandSender, pet: Pet) {
        pet.remove()
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petDespawnedMessage, pet.name))
    }

    private suspend fun selectPet(sender: CommandSender, player: Player, pet: Pet) {
        val playerInformation = petMetaRepository.getByPlayer(player) ?: return
        playerInformation.selectedPet = pet.name
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petSelectedMessage, pet.name))
    }


    private fun openHeadDatabase(sender: CommandSender, player: Player, pet: Pet) {
        try {
            dependencyHeadDatabaseService!!.registerPlayerForNextClick(player, pet.name)
            val configValue = "headDatabaseCommand"
            var command = if (configurationService.containsValue(configValue)) {
                configurationService.findValue("headDatabaseCommand")
            } else {
                // TODO: Remove it in 2025. Compatibility to 9.0.3
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


    private fun breakBlock(sender: CommandSender, pet: Pet, dropTypes: String, timeToBreak: Int) {
        val block = pet.getBlockInFrontOf() ?: return
        val playerInteractEvent = BlockBreakEvent(block, pet.player)
        Bukkit.getPluginManager().callEvent(playerInteractEvent)

        if (playerInteractEvent.isCancelled) {
            return
        }

        val actualDropTypes = try {
            dropTypes.split(",").map { e -> DropType.values().first { t -> t.name.equals(e, true) } }
        } catch (e: Exception) {
            sender.sendMessage(PetBlocksLanguage.dropTypeNotFound.format(DropType.values().joinToString(",")))
            return
        }

        pet.breakBlock(timeToBreak, actualDropTypes)
    }

    private fun cancel(sender: CommandSender, pet: Pet) {
        pet.cancelAction()
        sender.sendPluginMessage(PetBlocksLanguage.cancelMessage)
    }

    private fun snap(sender: CommandSender, pet: Pet) {
        pet.snap()
        sender.sendPluginMessage(PetBlocksLanguage.snapMessage)
    }


    private fun moveForward(sender: CommandSender, pet: Pet, speed: Double) {
        pet.moveForward(speed)
        sender.sendPluginMessage(String.format(PetBlocksLanguage.petMoveForwardMessage))
    }

    private fun rotateRel(sender: CommandSender, pet: Pet, petRotationType: PetRotationType, angle: Float) {
        val location = pet.location

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

    private fun setEntityType(sender: CommandSender, pet: Pet, entityType: String) {
        pet.entityType = entityType
        if (entityType.lowercase().contains("armor_stand")) {
            pet.isEntityVisible = false
        } else {
            pet.isEntityVisible = true
        }
        sender.sendPluginMessage(String.format(PetBlocksLanguage.entityTypeChangeMessage, entityType))
    }

    private fun setEntityVisible(sender: CommandSender, pet: Pet, flag: Boolean) {
        pet.isEntityVisible = flag
        sender.sendPluginMessage(String.format(PetBlocksLanguage.entityVisibilityChangedMessage, flag))
    }

    private fun setGroundOffset(sender: CommandSender, pet: Pet, offset: Double) {
        pet.groundOffset = offset
        sender.sendPluginMessage(String.format(PetBlocksLanguage.groundOffsetChangedMessage, offset))
    }

    private fun setMemoryVariable(sender: CommandSender, pet: Pet, key: String, value: String) {
        pet.javaScriptMemory[key] = value
        sender.sendPluginMessage(String.format(PetBlocksLanguage.variableChangedMessage, key, value))
    }

    private fun CommandSender.sendPluginMessage(message: String) {
        if (message.isBlank()) {
            return
        }

        this.sendMessage(message)
    }

    private fun CommandBuilder.permission(permission: Permission) {
        this.permission(permission.text)
    }
}
