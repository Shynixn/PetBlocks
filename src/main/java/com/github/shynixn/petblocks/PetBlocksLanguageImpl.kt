package com.github.shynixn.petblocks

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.petblocks.contract.PetBlocksLanguage

class PetBlocksLanguageImpl : PetBlocksLanguage {
 override val names: List<String>
  get() = listOf("en_us","es_es")
 override var petNameChangeMessage = LanguageItem("[&9PetBlocks&f] The name of the pet has been changed to %1$1s.")

 override var templateNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cTemplate %1$1s not found.")

 override var petNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cPet not found.")

 override var petNameExistsMessage = LanguageItem("[&9PetBlocks&f] &cPet already exists.")

 override var petCreatedMessage = LanguageItem("[&9PetBlocks&f] Pet has been created.")

 override var petDeletedMessage = LanguageItem("[&9PetBlocks&f] Pet has been deleted.")

 override var playerNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cPlayer %1$1s not found.")

 override var petCalledMessage = LanguageItem("[&9PetBlocks&f] Pet has been called.")

 override var petSpawnedMessage = LanguageItem("[&9PetBlocks&f] Pet has been spawned.")

 override var petDespawnedMessage = LanguageItem("[&9PetBlocks&f] Pet has been removed.")

 override var visibilityTypeNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cOnly the visibility types %1$1s are supported.")

 override var visibilityChangedMessage = LanguageItem("[&9PetBlocks&f] The visibility of the pet has been changed to %1$1s.")

 override var petSkinTypeChangedMessage = LanguageItem("[&9PetBlocks&f] The skinType of the pet has been changed.")

 override var petSkinNbtChanged = LanguageItem("[&9PetBlocks&f] The skin of the pet has been changed.")

 override var petSkinTypeNotFound = LanguageItem("[&9PetBlocks&f] &cMaterial %1$1s not found.")

 override var petListMessage = LanguageItem("[&9PetBlocks&f] Count: %1$1d Names: %2$1s")

 override var petRideMessage = LanguageItem("[&9PetBlocks&f] Started riding pet.")

 override var petUnmountMessage = LanguageItem("[&9PetBlocks&f] Stopped mounting pet.")

 override var petHatMessage = LanguageItem("[&9PetBlocks&f] Started hat pet.")

 override var worldNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cWorld %1$1s not found.")

 override var petLookAtMessage = LanguageItem("[&9PetBlocks&f] Pet is looking at a location.")

 override var petWalkToLocationMessage = LanguageItem("[&9PetBlocks&f] Pet is walking to location.")

 override var cannotParseNumberMessage = LanguageItem("[&9PetBlocks&f] &cCannot parse number %1$1s.")

 override var cannotParseNbtMessage = LanguageItem("[&9PetBlocks&f] &cCannot parse nbt %1$1s.")

 override var placeHolderPetNotFound = LanguageItem("No pet.")

 override var petTeleportedMessage = LanguageItem("[&9PetBlocks&f] Pet has been teleported.")

 override var petVelocityAppliedMessage = LanguageItem("[&9PetBlocks&f] Pet has been moved.")

 override var petCharacterNotAllowed = LanguageItem("[&9PetBlocks&f] &cThis pet name is not allowed.")

 override var reloadMessage = LanguageItem("[&9PetBlocks&f] PetBlocks has been reloaded.")

 override var petLoopNotFound = LanguageItem("[&9PetBlocks&f] &cLoop %1$1s not found.")

 override var premiumMultiplePets = LanguageItem("[&9PetBlocks&f] &cPetBlocks-Premium is required to spawn multiple pets per player.")

 override var petLoopChangedMessage = LanguageItem("[&9PetBlocks&f] Changed pet to loop %2$1s.")

 override var petTemplateChangeMessage = LanguageItem("[&9PetBlocks&f] Changed pet to template %2$1s.")

 override var petAmountNotAllowed = LanguageItem("[&9PetBlocks&f] &cYou do not have permission to have %1$1s pet(s).")

 override var petSelectedMessage = LanguageItem("")

 override var templateNotAllowed = LanguageItem("[&9PetBlocks&f] &cYou do not have permission for template %1$1s.")

 override var speedCannotBeParsed = LanguageItem("[&9PetBlocks&f] &cThe speed value %1$1s cannot be parsed.")

 override var headDatabasePluginNotLoaded = LanguageItem("[&9PetBlocks&f] &cThe plugin HeadDatabase is not loaded.")

 override var errorLoadingTemplatesMessage = LanguageItem("[&9PetBlocks&f] &cCannot parse template file. See console log for details.")

 override var dropTypeNotFound = LanguageItem("[&9PetBlocks&f] &cOnly the drop types %1$1s are supported.")

 override var cancelMessage = LanguageItem("[&9PetBlocks&f] Cancelled action.")

 override var snapMessage = LanguageItem("[&9PetBlocks&f] The pet has been snapped to a coordinate axe.")

 override var petMoveForwardMessage = LanguageItem("[&9PetBlocks&f] Pet is walking forward.")

 override var petRotationTypeNotFound = LanguageItem("[&9PetBlocks&f] &cOnly the direction types %1$1s are supported.")

 override var rotationRelMessage = LanguageItem("[&9PetBlocks&f] The pet has been rotated.")

 override var entityTypeChangeMessage = LanguageItem("[&9PetBlocks&f] The pet entity type has been changed to %1$1s.")

 override var entityVisibilityChangedMessage = LanguageItem("[&9PetBlocks&f] The pet entity visibility has been changed to %1$1s.")

 override var groundOffsetChangedMessage = LanguageItem("[&9PetBlocks&f] The groundOffset of the pet has been changed.")

 override var groundOffsetCannotBeParsed = LanguageItem("[&9PetBlocks&f] &cThe groundOffset value %1$1s cannot be parsed.")

 override var cannotParseDataComponentMessage = LanguageItem("[&9PetBlocks&f] &cCannot parse data component %1$1s.")

 override var maxLength20Characters = LanguageItem("[&9PetBlocks&f] The text length has to be less than 20 characters.")

 override var noPermissionCommand = LanguageItem("[&9PetBlocks&f] &cYou do not have permission to execute this command.")

 override var commandUsage = LanguageItem("[&9PetBlocks&f] Use /petblocks help to see more info about the plugin.")

 override var commandDescription = LanguageItem("[&9PetBlocks&f] All commands for the PetBlocks plugin.")

 override var commandSenderHasToBePlayer = LanguageItem("[&9PetBlocks&f] The command sender has to be a player if you do not specify the optional player argument.")

 override var cannotParseBoolean = LanguageItem("[&9PetBlocks&f] &cCannot parse boolean %1$1s.")

 override var createCommandHint = LanguageItem("Creates a new pet for the player with the given pet template.")

 override var deleteCommandHint = LanguageItem("Deletes the pet of the player.")

 override var listCommandHint = LanguageItem("Lists all pets of a player.")

 override var callCommandHint = LanguageItem("Spawns and teleports the pet in front of the owner.")

 override var lookAtCommandHint = LanguageItem("Makes the pet look at the given location.")

 override var lookAtOwnerCommandHint = LanguageItem("Makes the pet look at the owner.")

 override var moveToCommandHint = LanguageItem("Makes the pet walk to a given location.")

 override var moveToOwnerCommandHint = LanguageItem("Makes the pet walk to the owner.")

 override var hatCommandHint = LanguageItem("Makes the owner wear the pet.")

 override var rideCommandHint = LanguageItem("Makes the owner ride the pet.")

 override var unmountCommandHint = LanguageItem("Makes the owner unmount (stop riding/hat) the pet.")

 override var teleportCommandHint = LanguageItem("Teleports the pet to the given location.")

 override var velocityCommandHint = LanguageItem("Launches the pet into the given direction.")

 override var skinTypeCommandHint = LanguageItem("Changes the material used as a head.")

 override var skinNbtCommandHint = LanguageItem("Changes the NBT tags of the head item. Works in Minecraft versions below 1.20.5. Use the /petblocks skincomponent command for Minecraft >= 1.20.5.")

 override var skinComponentCommandHint = LanguageItem("Changes the Data Component tags of the head item. Data Components replace NBT tags since the release of Minecraft 1.20.5.")

 override var skinBase64CommandHint = LanguageItem("Changes the head material to player_head and sets the base64 encoded texture url.")

 override var skinHeadDatabaseCommandHint = LanguageItem("Changes the head material to player_head and sets the texture url from a skin loaded by the HeadDatabase plugin.")

 override var renameCommandHint = LanguageItem("Changes the display name of the pet. Does not accept spaces. Underlines '_' are automatically replaced by spaces.")

 override var visibilityCommandHint = LanguageItem("Changes who can see the pet.")

 override var loopCommandHint = LanguageItem("Changes which loop from the template is being executed by the pet. An example loop is idle.")

 override var templateCommandHint = LanguageItem("Changes the template of a pet without recreating the pet.")

 override var spawnCommandHint = LanguageItem("Spawns the pet if it has not already spawned.")

 override var deSpawnCommandHint = LanguageItem("Despawns the pet if it has not already despawned.")

 override var toggleCommandHint = LanguageItem("Toggles the pet spawn state.")

 override var selectCommandHint = LanguageItem("Makes the owner select one of his pets as primary pet. This is only helpful if a single player has got multiple pets in PetBlocks-Premium.")

 override var openHeadDatabaseCommandHint = LanguageItem("Opens the headDatabase inventory with a special hook, which applies the next item you select in the headdatabase GUI to the pet.")

 override var breakBlockCommandHint = LanguageItem("Breaks the block the pet is looking at. There is a placeholder, which contains the name of the block type. Breaking a block is automatically cancelled on certain actions. e.g., a pet looks at a player, a pet starts moving.")

 override var cancelCommandHint = LanguageItem("Cancels any long-running actions like breaking a block.")

 override var snapCommandHint = LanguageItem("Rotates the pet to the exact line of the nearest x or z axe.")

 override var moveForwardCommandHint = LanguageItem("Lets the pet move forward in its current direction. Executing the snap command before executing this is helpful to move in a straight direction. If the pet reaches a cliff (1 block difference), moving forward stops.")

 override var rotateRelCommandHint = LanguageItem("Rotates the pet relative to its current rotation.")

 override var entityTypeCommandHint = LanguageItem("Changes the entity type of the pet. The default type is minecraft:armor_stand.")

 override var entityVisibleCommandHint = LanguageItem("Changes if the body of an entity is visible. For armor stands this is false, for most of the other entities this should be true.")

 override var groundOffSetCommandHint = LanguageItem("Changes the offset of the body of the entity to the ground. Useful when configuring different entity types.")

 override var manipulateOtherMessage = LanguageItem("[&9PetBlocks&f] &cYou do not have permission to edit the pets of other players.")

 override var reloadCommandHint = LanguageItem("Reloads all pets, menus and configuration.")

 override var closeCommandHint = LanguageItem("Closes the GUI menu.")

 override var backCommandHint = LanguageItem("Goes back one GUI page.")

 override var openCommandHint = LanguageItem("Opens the GUI menu with the given name.")

 override var nextCommandHint = LanguageItem("Opens the next GUI page.")

 override var messageCommandHint = LanguageItem("Sends a chat message.")

 override var guiMenuNotFoundMessage = LanguageItem("[&9PetBlocks&f] &cMenu %1$1s not found.")

 override var guiMenuNoPermissionMessage = LanguageItem("[&9PetBlocks&f] &cYou do not have permission for menu %1$1s.")

 override var variableCommandHint = LanguageItem("Sets the value of the variable with the given key. This is useful to store arbitrary data into the pet e.g., health.")

 override var variableChangedMessage = LanguageItem("[&9PetBlocks&f] The pet memory variable %1$1s has been set to %2$1s.")

 override var velocityRelCommandHint = LanguageItem("Launches the pet into the current looking direction with the given multipliers.")

 override var ridingSpeedChangedMessage = LanguageItem("[&9PetBlocks&f] The riding-speed of the pet has been changed.")

 override var ridingSpeedCommandHint = LanguageItem("Changes the speed while riding a pet.")

 override var cannotParseItemStackError = LanguageItem("[&9ShyGUI&f] &cCannot parse ItemStack at row %1$1s and col %2$1s! Check your GUI menu configuration by reviewing the full item parsing error in the console log.")

 override var rowColOutOfRangeError = LanguageItem("[&9ShyGUI&f] &cThe specified row %1$1s and col %2$1s are out of range of the GUI! Check your GUI menu configuration.")
}
