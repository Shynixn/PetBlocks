package com.github.shynixn.petblocks.impl.commandexecutor

import com.github.shynixn.mcutils.common.*
import com.github.shynixn.mcutils.common.chat.ChatMessageService
import com.github.shynixn.mcutils.common.chat.ClickEvent
import com.github.shynixn.mcutils.common.chat.ClickEventType
import com.github.shynixn.mcutils.common.chat.TextComponent
import com.github.shynixn.mcutils.common.command.CommandBuilder
import com.github.shynixn.mcutils.common.command.ValidationException
import com.github.shynixn.mcutils.common.command.Validator
import com.github.shynixn.mcutils.common.item.Item
import com.github.shynixn.mcutils.common.item.ItemService
import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.reloadTranslation
import com.github.shynixn.mcutils.common.placeholder.PlaceHolderService
import com.github.shynixn.mcutils.common.repository.CacheRepository
import com.github.shynixn.mcutils.database.api.CachePlayerRepository
import com.github.shynixn.petblocks.PetBlocksDependencyInjectionModule
import com.github.shynixn.petblocks.contract.DependencyHeadDatabaseService
import com.github.shynixn.petblocks.contract.Pet
import com.github.shynixn.petblocks.contract.PetBlocksLanguage
import com.github.shynixn.petblocks.contract.PetService
import com.github.shynixn.petblocks.entity.PetTemplate
import com.github.shynixn.petblocks.entity.PlayerInformation
import com.github.shynixn.petblocks.enumeration.DropType
import com.github.shynixn.petblocks.enumeration.Permission
import com.github.shynixn.petblocks.enumeration.PetRotationType
import com.github.shynixn.petblocks.enumeration.PetVisibility
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.command.CommandSender
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.util.Vector
import java.util.*

class PetBlocksCommandExecutor(
    private val petService: PetService,
    private val templateRepository: CacheRepository<PetTemplate>,
    private val plugin: CoroutinePlugin,
    private val configurationService: ConfigurationService,
    private val chatMessageService: ChatMessageService,
    private val petMetaRepository: CachePlayerRepository<PlayerInformation>,
    private val itemService: ItemService,
    private val language: PetBlocksLanguage,
    private val placeHolderService: PlaceHolderService
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
    private val manipulateOtherPermission: () -> String = {
        Permission.MANIPULATE_OTHER.text
    }
    private val manipulateOtherPermissionMessage: () -> String = {
        language.manipulateOtherMessage.text
    }
    private val onlinePlayerTabs: ((CommandSender) -> List<String>) = {
        Bukkit.getOnlinePlayers().map { e -> e.name }
    }
    private val worldTabs: ((CommandSender) -> List<String>) = {
        Bukkit.getWorlds().map { e -> e.name }
    }
    private val templateTabs: ((CommandSender) -> List<String>) = {
        val cache = templateRepository.getCache()
        if (cache != null) {
            cache.map { e -> e.name }
        } else {
            emptyList<String>()
        }
    }
    private val booleanTabs: ((CommandSender) -> List<String>) = {
        listOf(true, false).map { e -> e.toString() }
    }
    private val petNamesTabs: ((CommandSender) -> List<String>) = { sender ->
        if (sender is Player) {
            val cachedPets = petService.getCache()[sender]
            cachedPets?.map { e -> e.name } ?: emptyList()
        } else {
            emptyList()
        }
    }
    private val materialTabs: ((CommandSender) -> List<String>) = { _ ->
        Material.values().map { e -> "minecraft:${e.name}" }
    }
    private val visibilityTabs: ((CommandSender) -> List<String>) = { _ ->
        PetVisibility.values().map { e -> e.name }
    }
    private val rotationTypeTabs: ((CommandSender) -> List<String>) = { _ ->
        PetRotationType.values().map { e -> e.name }
    }

    private val senderHasToBePlayer: () -> String = {
        language.commandSenderHasToBePlayer.text
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
            return language.worldNotFoundMessage.text
        }
    }

    private val mustBeDouble = object : Validator<Double> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Double? {
            return openArgs[0].toDoubleOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.cannotParseNumberMessage.text, null, mapOf("0" to openArgs[0])
            )
        }
    }

    private val mustBeInt = object : Validator<Int> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Int? {
            return openArgs[0].toIntOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.cannotParseNumberMessage.text, null, mapOf("0" to openArgs[0])
            )
        }
    }

    private val mustBeBoolean = object : Validator<Boolean> {
        override suspend fun transform(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): Boolean? {
            return openArgs[0].toBooleanStrictOrNull()
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.cannotParseBoolean.text, null, mapOf("0" to openArgs[0])
            )
        }
    }

    private val visibilityMustExist = object : Validator<PetVisibility> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetVisibility? {
            return PetVisibility.values().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.visibilityTypeNotFoundMessage.text,
                null,
                mapOf("0" to PetVisibility.values().joinToString(",") { e -> e.name.lowercase() })
            )
        }
    }

    private val rotationTypeMustExist = object : Validator<PetRotationType> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetRotationType? {
            return PetRotationType.values().firstOrNull { e -> e.name.equals(openArgs[0], true) }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.petRotationTypeNotFound.text,
                null,
                mapOf("0" to PetRotationType.values().joinToString(",") { e -> e.name.lowercase() })
            )
        }
    }

    private val templateMustExist = object : Validator<PetTemplate> {
        override suspend fun transform(
            sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>
        ): PetTemplate? {
            return templateRepository.getAll().firstOrNull { e -> e.name == openArgs[0] }
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return placeHolderService.resolvePlaceHolder(
                language.templateNotFoundMessage.text, null, mapOf("0" to openArgs[0])
            )
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
            return placeHolderService.resolvePlaceHolder(
                language.playerNotFoundMessage.text, null, mapOf("0" to openArgs[0])
            )
        }
    }
    private val templateMustHavePermission = object : Validator<PetTemplate> {
        override suspend fun validate(
            sender: CommandSender, prevArgs: List<Any>, argument: PetTemplate, openArgs: List<String>
        ): Boolean {
            return sender.hasPermission(Permission.DYN_TEMPLATE.text + argument.name)
        }

        override suspend fun message(sender: CommandSender, prevArgs: List<Any>, openArgs: List<String>): String {
            return language.templateNotAllowed.text
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
            return placeHolderService.resolvePlaceHolder(
                language.petSkinTypeNotFound.text, null, mapOf("0" to openArgs[0])
            )
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
            return language.petCharacterNotAllowed.text
        }
    }

    private suspend fun petMustExist(player: Player, name: String): Pet {
        val pets = petService.getPetsFromPlayer(player)
        val pet = pets.firstOrNull { e -> e.name.equals(name, true) }

        if (pet == null) {
            val message =
                placeHolderService.resolvePlaceHolder(language.petNotFoundMessage.text, null, mapOf("0" to name))
            throw ValidationException(message)
        }

        return pet
    }

    init {
        val commandBuilder = CommandBuilder(plugin, "petblocks", chatMessageService) {
            usage(language.commandUsage.text)
            description(language.commandDescription.text)
            aliases(plugin.config.getStringList("commands.petblocks.aliases"))
            permission(Permission.COMMAND)
            permissionMessage(language.noPermissionCommand.text)
            subCommand("create") {
                permission(Permission.CREATE)
                toolTip { language.createCommandHint.text }
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
                toolTip { language.deleteCommandHint.text }
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
                toolTip { language.listCommandHint.text }
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
                toolTip { language.callCommandHint.text }
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
                toolTip { language.lookAtCommandHint.text }
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
                toolTip { language.lookAtOwnerCommandHint.text }
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
                toolTip { language.moveToCommandHint.text }
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
                toolTip { language.moveToOwnerCommandHint.text }
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
                toolTip { language.hatCommandHint.text }
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
                toolTip { language.rideCommandHint.text }
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
                toolTip { language.unmountCommandHint.text }
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
                toolTip { language.teleportCommandHint.text }
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
                toolTip { language.velocityCommandHint.text }
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
            subCommand("velocityrel") {
                permission(Permission.VELOCITYREL)
                toolTip { language.velocityRelCommandHint.text }
                builder().argument("name").tabs(petNamesTabs).argument("mx").validator(mustBeDouble)
                    .tabs { listOf("<mx>") }.argument("my").validator(mustBeDouble).tabs { listOf("<my>") }
                    .argument("mz").validator(mustBeDouble).tabs { listOf("<mz>") }
                    .executePlayer(senderHasToBePlayer) { player, name, x, y, z ->
                        setRelativeVelocityToPet(
                            player, petMustExist(player, name), Vector(x, y, z), "-"
                        )
                    }.argument("oy").tabs { listOf("<oy>", "-") }
                    .executePlayer(senderHasToBePlayer) { player, name, x, y, z, oy ->
                        setRelativeVelocityToPet(
                            player, petMustExist(player, name), Vector(x, y, z), oy
                        )
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, x, y, z, oy, player ->
                        setRelativeVelocityToPet(
                            commandSender, petMustExist(player, name), Vector(x, y, z), oy
                        )
                    }
            }
            subCommand("skintype") {
                permission(Permission.SKIN)
                toolTip { language.skinTypeCommandHint.text }
                builder().argument("name").tabs(petNamesTabs).argument("material").validator(materialMustExist)
                    .tabs(materialTabs).executePlayer(senderHasToBePlayer) { player, name, material ->
                        setSkinType(player, petMustExist(player, name), material, 0)
                    }.argument("durability").validator(mustBeInt).tabs(materialTabs)
                    .executePlayer(senderHasToBePlayer) { player, name, material, durability ->
                        setSkinType(player, petMustExist(player, name), material, durability)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, material, durability, player ->
                        setSkinType(commandSender, petMustExist(player, name), material, durability)
                    }
            }
            subCommand("skinnbt") {
                permission(Permission.SKIN)
                toolTip { language.skinNbtCommandHint.text }
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
                toolTip { language.skinComponentCommandHint.text }
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
                toolTip { language.skinBase64CommandHint.text }
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
                toolTip { language.skinHeadDatabaseCommandHint.text }
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
                toolTip { language.renameCommandHint.text }
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
                toolTip { language.visibilityCommandHint.text }
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
                toolTip { language.loopCommandHint.text }
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
                toolTip { language.templateCommandHint.text }
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
                toolTip { language.spawnCommandHint.text }
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
                toolTip { language.deleteCommandHint.text }
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
                toolTip { language.toggleCommandHint.text }
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
                toolTip { language.selectCommandHint.text }
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
                toolTip { language.openHeadDatabaseCommandHint.text }
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
                toolTip { language.breakBlockCommandHint.text }
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
                toolTip { language.cancelCommandHint.text }
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
                toolTip { language.moveForwardCommandHint.text }
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
                toolTip { language.rotateRelCommandHint.text }
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
                toolTip { language.entityTypeCommandHint.text }
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
                toolTip { language.entityVisibleCommandHint.text }
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
                toolTip { language.groundOffSetCommandHint.text }
                builder().argument("name").tabs(petNamesTabs).argument("offset").validator(mustBeDouble)
                    .tabs { listOf("<offset>") }.executePlayer(senderHasToBePlayer) { player, name, offset ->
                        setGroundOffset(player, petMustExist(player, name), offset)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, offset, player ->
                        setGroundOffset(commandSender, petMustExist(player, name), offset)
                    }
            }
            subCommand("ridingspeed") {
                permission(Permission.RIDINGSPEED)
                toolTip { language.ridingSpeedCommandHint.text }
                builder().argument("name").tabs(petNamesTabs).argument("speed").validator(mustBeDouble)
                    .tabs { listOf("<speed>") }.executePlayer(senderHasToBePlayer) { player, name, speed ->
                        setRidingSpeed(player, petMustExist(player, name), speed)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, speed, player ->
                        setRidingSpeed(commandSender, petMustExist(player, name), speed)
                    }
            }
            subCommand("snap") {
                permission(Permission.SNAP)
                toolTip { language.snapCommandHint.text }
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
                toolTip { language.variableCommandHint.text }
                builder().argument("name").tabs(petNamesTabs).argument("key").tabs { listOf("<key>") }.argument("value")
                    .tabs { listOf("<value>") }.executePlayer(senderHasToBePlayer) { player, name, key, value ->
                        setMemoryVariable(player, petMustExist(player, name), key, value)
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { commandSender, name, key, value, player ->
                        setMemoryVariable(commandSender, petMustExist(player, name), key, value)
                    }
            }
            subCommand("suggestrename") {
                permission(Permission.VARIABLE)
                builder().argument("name").tabs(petNamesTabs)
                    .tabs { listOf("<value>") }
                    .executePlayer(senderHasToBePlayer) { player, name ->
                        sendRenameMessage(player, petMustExist(player, name))
                    }.argument("player").validator(playerMustExist).tabs(onlinePlayerTabs)
                    .permission(manipulateOtherPermission).permissionMessage(manipulateOtherPermissionMessage)
                    .execute { _, name, player ->
                        sendRenameMessage(player, petMustExist(player, name))
                    }
            }
            subCommand("reload") {
                permission(Permission.RELOAD)
                toolTip { language.reloadCommandHint.text }
                builder().execute { sender ->
                    templateRepository.clearCache()
                    plugin.saveDefaultConfig()
                    plugin.reloadConfig()
                    plugin.reloadTranslation(language)
                    val templates = templateRepository.getAll()

                    // Updates the templates.
                    for (player in petService.getCache().keys) {
                        val pets = petService.getCache()[player]

                        if (pets != null) {
                            for (pet in pets) {
                                val newTemplate = templates.firstOrNull { e -> e.name == pet.template.name }
                                if (newTemplate != null) {
                                    pet.template = newTemplate
                                }
                            }
                        }
                    }

                    Bukkit.getServer().dispatchCommand(Bukkit.getConsoleSender(), "petblocksgui reload")
                    sender.sendLanguageMessage(language.reloadMessage)
                }
            }
        }
        commandBuilder.build()
    }

    private fun sendRenameMessage(player: Player, pet: Pet) {
        chatMessageService.sendChatMessage(player, TextComponent().also {
            it.components = mutableListOf(TextComponent().also {
                it.text = language.suggestRenamePrefix.text
            }, TextComponent().also {
                it.text = language.suggestRenameClickable.text
                it.clickEvent = ClickEvent(ClickEventType.SUGGEST_COMMAND, "/petblocks rename ${pet.name} ")
            }, TextComponent().also {
                it.text = language.suggestRenameSuffix.text
            })
        })
    }

    private fun setRidingSpeed(sender: CommandSender, pet: Pet, speed: Double) {
        pet.ridingSpeed = speed
        sender.sendLanguageMessage(language.ridingSpeedChangedMessage, speed.toString())
    }

    private suspend fun createPet(
        sender: CommandSender, player: Player, petName: String, petTemplate: PetTemplate
    ) {
        val pets = petService.getPetsFromPlayer(player)
        val pet = pets.firstOrNull { e -> e.name.equals(petName, true) }

        if (pet != null) {
            sender.sendLanguageMessage(language.petNameExistsMessage, petName)
            return
        }

        val petAmountPermission = "${Permission.DYN_AMOUNT.text}${pets.size + 1}"

        if (!player.hasPermission(petAmountPermission)) {
            sender.sendLanguageMessage(language.petAmountNotAllowed, (pets.size + 1).toString())
            return
        }

        if (!PetBlocksDependencyInjectionModule.areLegacyVersionsIncluded && pets.isNotEmpty()) {
            sender.sendLanguageMessage(language.premiumMultiplePets)
            return
        }

        petService.createPet(
            player,
            player.location.toVector3d().addRelativeFront(3.0).toLocation(),
            petTemplate.name,
            petName,
        )

        sender.sendLanguageMessage(language.petCreatedMessage, petName)
    }

    private suspend fun deletePet(sender: CommandSender, pet: Pet) {
        petService.deletePet(pet)
        sender.sendLanguageMessage(language.petDeletedMessage, pet.name)
    }

    private suspend fun listPet(sender: CommandSender, player: Player) {
        val pets = petService.getPetsFromPlayer(player)
        val petString = pets.joinToString(", ") { e -> e.name }
        sender.sendLanguageMessage(language.petListMessage, pets.size.toString(), petString)
    }

    private fun callPet(sender: CommandSender, pet: Pet) {
        pet.call()
        sender.sendLanguageMessage(language.petCalledMessage, pet.name)
    }

    private fun lookAtLocation(sender: CommandSender, pet: Pet, location: Location) {
        location.y -= pet.groundOffset
        pet.lookAt(location)
        sender.sendLanguageMessage(language.petLookAtMessage, pet.name)
    }

    private fun walkToLocation(
        sender: CommandSender, pet: Pet, location: Location, speed: Double
    ) {
        pet.moveTo(location, speed)
        sender.sendLanguageMessage(language.petWalkToLocationMessage, pet.name)
    }

    private fun hat(sender: CommandSender, pet: Pet) {
        pet.hat()
        val loop = "hat"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendLanguageMessage(language.petHatMessage, pet.name)
    }

    private fun unmount(sender: CommandSender, pet: Pet) {
        pet.unmount()
        sender.sendLanguageMessage(language.petUnmountMessage, pet.name)
    }

    private fun teleportPet(sender: CommandSender, pet: Pet, location: Location) {
        pet.location = location
        sender.sendLanguageMessage(language.petTeleportedMessage, pet.name)
    }

    private fun setVelocityToPet(
        sender: CommandSender, pet: Pet, vector: Vector
    ) {
        pet.velocity = vector
        sender.sendLanguageMessage(language.petVelocityAppliedMessage, pet.name)
    }

    private fun setRelativeVelocityToPet(
        sender: CommandSender, pet: Pet, vector: Vector, overrideY: String
    ) {
        val normalized = pet.location.direction.normalize()

        if (overrideY.toDoubleOrNull() != null) {
            normalized.y = overrideY.toDouble()
        }

        normalized.x *= vector.x
        normalized.y *= vector.y
        normalized.z *= vector.z
        pet.velocity = normalized
        sender.sendLanguageMessage(language.petVelocityAppliedMessage, pet.name)
    }

    private fun setSkinType(sender: CommandSender, pet: Pet, material: String, durability: Int) {
        val item = Item() // Has to be a fresh item.
        item.typeName = material
        item.durability = durability.toString()
        pet.headItem = item
        sender.sendLanguageMessage(language.petSkinTypeChangedMessage, pet.name)
    }

    private fun setSkinNbt(sender: CommandSender, pet: Pet, nbt: String) {
        try {
            val testItem = Item(pet.headItem.typeName)
            testItem.nbt = nbt
            itemService.toItemStack(testItem)// Test if nbt is valid.

            val item = pet.headItem
            item.nbt = nbt
            pet.headItem = item
            sender.sendLanguageMessage(language.petSkinNbtChanged, pet.name)
        } catch (e: Exception) {
            sender.sendLanguageMessage(language.cannotParseNbtMessage, pet.name)
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
            sender.sendLanguageMessage(language.petSkinNbtChanged, pet.name)
        } catch (e: Exception) {
            sender.sendLanguageMessage(language.cannotParseDataComponentMessage, pet.name)
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
        sender.sendLanguageMessage(language.petSkinNbtChanged, pet.name)
    }

    private fun setSkinHeadDatabase(sender: CommandSender, pet: Pet, hdbId: String) {
        try {
            val itemStack = dependencyHeadDatabaseService!!.getItemStackFromId(hdbId)!!
            val item = itemService.toItem(itemStack)
            setSkinBase64(sender, pet, item.skinBase64!!)
        } catch (e: Exception) {
            sender.sendLanguageMessage(language.headDatabasePluginNotLoaded, pet.name)
            return
        }
    }

    private fun setDisplayName(sender: CommandSender, pet: Pet, displayName: String) {
        pet.displayName = displayName.replace("_", " ")
        sender.sendLanguageMessage(language.petNameChangeMessage, pet.displayName.translateChatColors())
    }

    private fun ridePet(sender: CommandSender, pet: Pet) {
        pet.ride()

        val loop = "ride"

        if (pet.template.loops.containsKey(loop)) {
            pet.loop = loop
        }

        sender.sendLanguageMessage(language.petRideMessage, pet.name)
    }

    private fun setVisibility(
        sender: CommandSender, pet: Pet, visibility: PetVisibility
    ) {
        pet.visibility = visibility
        sender.sendLanguageMessage(language.visibilityChangedMessage, pet.name)
    }

    private fun setPetLoop(sender: CommandSender, pet: Pet, loop: String) {
        if (!pet.template.loops.containsKey(loop)) {
            sender.sendLanguageMessage(language.petLoopNotFound, loop)
            return
        }

        pet.loop = loop
        sender.sendLanguageMessage(language.petLoopChangedMessage, pet.name, loop)
    }

    private fun setPetTemplate(sender: CommandSender, pet: Pet, template: PetTemplate) {
        if (!sender.hasPermission(Permission.DYN_TEMPLATE.text + template.name)) {
            sender.sendLanguageMessage(language.templateNotAllowed, template.name)
            return
        }

        pet.template = template
        sender.sendLanguageMessage(language.petTemplateChangeMessage, pet.name, template.name)
    }

    private fun spawnPet(sender: CommandSender, pet: Pet) {
        pet.spawn()
        sender.sendLanguageMessage(language.petSpawnedMessage, pet.name)
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
        sender.sendLanguageMessage(language.petDespawnedMessage, pet.name)
    }

    private suspend fun selectPet(sender: CommandSender, player: Player, pet: Pet) {
        val playerInformation = petMetaRepository.getByPlayer(player) ?: return
        playerInformation.selectedPet = pet.name
        sender.sendLanguageMessage(language.petSelectedMessage, pet.name)
    }

    private fun openHeadDatabase(sender: CommandSender, player: Player, pet: Pet) {
        try {
            dependencyHeadDatabaseService!!.registerPlayerForNextClick(player, pet.name)
            var command = configurationService.findValue<String>("headDatabaseCommand")

            command = if (command.startsWith("/")) {
                command.substring(1)
            } else {
                command
            }

            Bukkit.getServer().dispatchCommand(player, command)
        } catch (e: Exception) {
            sender.sendLanguageMessage(language.headDatabasePluginNotLoaded)
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
            sender.sendLanguageMessage(language.dropTypeNotFound, DropType.values().joinToString(","))
            return
        }

        pet.breakBlock(timeToBreak, actualDropTypes)
    }

    private fun cancel(sender: CommandSender, pet: Pet) {
        pet.cancelAction()
        sender.sendLanguageMessage(language.cancelMessage, pet.name)
    }

    private fun snap(sender: CommandSender, pet: Pet) {
        pet.snap()
        sender.sendLanguageMessage(language.snapMessage, pet.name)
    }


    private fun moveForward(sender: CommandSender, pet: Pet, speed: Double) {
        pet.moveForward(speed)
        sender.sendLanguageMessage(language.petMoveForwardMessage, pet.name)
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
        sender.sendLanguageMessage(language.rotationRelMessage, pet.name)
    }

    private fun setEntityType(sender: CommandSender, pet: Pet, entityType: String) {
        pet.entityType = entityType
        pet.isEntityVisible = !entityType.lowercase().contains("armor_stand")
        sender.sendLanguageMessage(language.entityTypeChangeMessage, entityType)
    }

    private fun setEntityVisible(sender: CommandSender, pet: Pet, flag: Boolean) {
        pet.isEntityVisible = flag
        sender.sendLanguageMessage(language.entityVisibilityChangedMessage, flag.toString())
    }

    private fun setGroundOffset(sender: CommandSender, pet: Pet, offset: Double) {
        pet.groundOffset = offset
        sender.sendLanguageMessage(language.groundOffsetChangedMessage, offset.toString())
    }

    private fun setMemoryVariable(sender: CommandSender, pet: Pet, key: String, value: String) {
        pet.memory[key] = value
        sender.sendLanguageMessage(language.variableChangedMessage, value)
    }

    private fun CommandBuilder.permission(permission: Permission) {
        this.permission(permission.text)
    }

    private fun CommandSender.sendLanguageMessage(languageItem: LanguageItem, vararg args: String) {
        val sender = this
        chatMessageService.sendLanguageMessage(sender, languageItem, *args)
    }
}
