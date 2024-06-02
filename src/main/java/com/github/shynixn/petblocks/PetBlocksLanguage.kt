package com.github.shynixn.petblocks

object PetBlocksLanguage {
  /** Changes if the body of an entity is visible. For armorstands this is false, for most of the other entities this should be true. **/
  var entityVisibleCommandHint : String = "Changes if the body of an entity is visible. For armorstands this is false, for most of the other entities this should be true."

  /** Teleports the pet to the given location. **/
  var teleportCommandHint : String = "Teleports the pet to the given location."

  /** [&9PetBlocks&f] Pet with name %1$1s has been spawned. **/
  var petSpawnedMessage : String = "[&9PetBlocks&f] Pet with name %1$1s has been spawned."

  /** [&9PetBlocks&f] &cYou do not have permission to have %1$1s pet(s). **/
  var petAmountNotAllowed : String = "[&9PetBlocks&f] &cYou do not have permission to have %1$1s pet(s)."

  /** Makes the pet walk to the owner. **/
  var moveToOwnerCommandHint : String = "Makes the pet walk to the owner."

  /** [&9PetBlocks&f] &cLoop %1$1s not found. **/
  var petLoopNotFound : String = "[&9PetBlocks&f] &cLoop %1$1s not found."

  /** [&9PetBlocks&f] Selected pet %1$1s. **/
  var petSelectedMessage : String = "[&9PetBlocks&f] Selected pet %1$1s."

  /** Changes the head material to player_head and sets the texture url from a skin loaded by the HeadDatabase plugin. **/
  var skinHeadDatabaseCommandHint : String = "Changes the head material to player_head and sets the texture url from a skin loaded by the HeadDatabase plugin."

  /** [&9PetBlocks&f] &cPetBlocks-Premium is required to spawn multiple pets per player. **/
  var premiumMultiplePets : String = "[&9PetBlocks&f] &cPetBlocks-Premium is required to spawn multiple pets per player."

  /** [&9PetBlocks&f] &cPlayer %1$1s not found. **/
  var playerNotFoundMessage : String = "[&9PetBlocks&f] &cPlayer %1$1s not found."

  /** Makes the pet look at the owner. **/
  var lookAtOwnerCommandHint : String = "Makes the pet look at the owner."

  /** [&9PetBlocks&f] Use /petblocks help to see more info about the plugin. **/
  var commandUsage : String = "[&9PetBlocks&f] Use /petblocks help to see more info about the plugin."

  /** Changes the Data Component tags of the head item. Data Components replace NBT tags since the release of Minecraft 1.20.5. **/
  var skinComponentCommandHint : String = "Changes the Data Component tags of the head item. Data Components replace NBT tags since the release of Minecraft 1.20.5."

  /** [&9PetBlocks&f] &cCannot parse number %1$1s. **/
  var cannotParseNumberMessage : String = "[&9PetBlocks&f] &cCannot parse number %1$1s."

  /** Toggles the pet spawn state. **/
  var toggleCommandHint : String = "Toggles the pet spawn state."

  /** [&9PetBlocks&f] Pet has been moved. **/
  var petVelocityAppliedMessage : String = "[&9PetBlocks&f] Pet has been moved."

  /** [&9PetBlocks&f] Started riding pet %1$1s. **/
  var petRideMessage : String = "[&9PetBlocks&f] Started riding pet %1$1s."

  /** [&9PetBlocks&f] Started hat pet %1$1s. **/
  var petHatMessage : String = "[&9PetBlocks&f] Started hat pet %1$1s."

  /** Makes the pet walk to a given location. **/
  var moveToCommandHint : String = "Makes the pet walk to a given location."

  /** Changes which loop from the template is being executed by the pet. An example loop is idle. **/
  var loopCommandHint : String = "Changes which loop from the template is being executed by the pet. An example loop is idle."

  /** [&9PetBlocks&f] Pet is looking at a location. **/
  var petLookAtMessage : String = "[&9PetBlocks&f] Pet is looking at a location."

  /** [&9PetBlocks&f] &cPet with name %1$1s already exists. **/
  var petNameExistsMessage : String = "[&9PetBlocks&f] &cPet with name %1$1s already exists."

  /** [&9PetBlocks&f] &cThe groundOffset value %1$1s cannot be parsed. **/
  var groundOffsetCannotBeParsed : String = "[&9PetBlocks&f] &cThe groundOffset value %1$1s cannot be parsed."

  /** [&9PetBlocks&f] Pet is walking forward. **/
  var petMoveForwardMessage : String = "[&9PetBlocks&f] Pet is walking forward."

  /** Launches the pet into the given direction. **/
  var velocityCommandHint : String = "Launches the pet into the given direction."

  /** [&9PetBlocks&f] The pet entity visibility has been changed to %1$1s. **/
  var entityVisibilityChangedMessage : String = "[&9PetBlocks&f] The pet entity visibility has been changed to %1$1s."

  /** Changes the entity type of the pet. The default type is minecraft:armor_stand **/
  var entityTypeCommandHint : String = "Changes the entity type of the pet. The default type is minecraft:armor_stand"

  /** No pet. **/
  var placeHolderPetNotFound : String = "No pet."

  /** Makes the pet look at the given location. **/
  var lookAtCommandHint : String = "Makes the pet look at the given location."

  /** Opens the headDatabase inventory with a special hook, which applies the next item you select in the headdatabse gui to the pet. **/
  var openHeadDatabaseCommandHint : String = "Opens the headDatabase inventory with a special hook, which applies the next item you select in the headdatabse gui to the pet."

  /** [&9PetBlocks&f] &cOnly the visibility types %1$1s are supported. **/
  var visibilityTypeNotFoundMessage : String = "[&9PetBlocks&f] &cOnly the visibility types %1$1s are supported."

  /** [&9PetBlocks&f] Pet with name %1$1s has been created. **/
  var petCreatedMessage : String = "[&9PetBlocks&f] Pet with name %1$1s has been created."

  /** [&9PetBlocks&f] Pet has been teleported. **/
  var petTeleportedMessage : String = "[&9PetBlocks&f] Pet has been teleported."

  /** [&9PetBlocks&f] &cThis pet name is not allowed. **/
  var petCharacterNotAllowed : String = "[&9PetBlocks&f] &cThis pet name is not allowed."

  /** Makes the owner select one of his pets as primary pet. This is only helpful if a single player has got multiple pets in PetBlocks-Premium. **/
  var selectCommandHint : String = "Makes the owner select one of his pets as primary pet. This is only helpful if a single player has got multiple pets in PetBlocks-Premium."

  /** [&9PetBlocks&f] Changed pet %1$1s to template %2$1s. **/
  var petTemplateChangeMessage : String = "[&9PetBlocks&f] Changed pet %1$1s to template %2$1s."

  /** [&9PetBlocks&f] &cTemplate %1$1s not found. **/
  var templateNotFoundMessage : String = "[&9PetBlocks&f] &cTemplate %1$1s not found."

  /** Changes the head material to player_head and sets the base64 encoded texture url. **/
  var skinBase64CommandHint : String = "Changes the head material to player_head and sets the base64 encoded texture url."

  /** Lets the pet move forward in its current direction. Executing the snap command before executing this is helpful to move in a straight direction. If the pet reaches a cliff (1 block difference), moving forward stops. **/
  var moveForwardCommandHint : String = "Lets the pet move forward in its current direction. Executing the snap command before executing this is helpful to move in a straight direction. If the pet reaches a cliff (1 block difference), moving forward stops."

  /** [&9PetBlocks&f] The pet has been snapped to a coordinate axe. **/
  var snapMessage : String = "[&9PetBlocks&f] The pet has been snapped to a coordinate axe."

  /** [&9PetBlocks&f] &cThe speed value %1$1s cannot be parsed. **/
  var speedCannotBeParsed : String = "[&9PetBlocks&f] &cThe speed value %1$1s cannot be parsed."

  /** [&9PetBlocks&f] All commands for the PetBlocks plugin. **/
  var commandDescription : String = "[&9PetBlocks&f] All commands for the PetBlocks plugin."

  /** Despawns the pet if it has not already despawned. **/
  var deSpawnCommandHint : String = "Despawns the pet if it has not already despawned."

  /** Changes the NBT tags of the head item. Works in Minecraft versions below 1.20.5. Use the /petblocks skincomponent command for Minecraft >= 1.20.5. **/
  var skinNbtCommandHint : String = "Changes the NBT tags of the head item. Works in Minecraft versions below 1.20.5. Use the /petblocks skincomponent command for Minecraft >= 1.20.5."

  /** [&9PetBlocks&f] The skin of pet %1$1s has been changed. **/
  var petSkinNbtChanged : String = "[&9PetBlocks&f] The skin of pet %1$1s has been changed."

  /** [&9PetBlocks&f] The pet has been rotated. **/
  var rotationRelMessage : String = "[&9PetBlocks&f] The pet has been rotated."

  /** [&9PetBlocks&f] The pet entity type has been changed to %1$1s. **/
  var entityTypeChangeMessage : String = "[&9PetBlocks&f] The pet entity type has been changed to %1$1s."

  /** [&9PetBlocks&f] The text length has to be less than 20 characters. **/
  var maxLength20Characters : String = "[&9PetBlocks&f] The text length has to be less than 20 characters."

  /** Changes who can see the pet. **/
  var visibilityCommandHint : String = "Changes who can see the pet."

  /** [&9PetBlocks&f] &cThe plugin HeadDatabase is not loaded. **/
  var headDatabasePluginNotLoaded : String = "[&9PetBlocks&f] &cThe plugin HeadDatabase is not loaded."

  /** [&9PetBlocks&f] The skinType of pet %1$1s has been changed. **/
  var petSkinTypeChangedMessage : String = "[&9PetBlocks&f] The skinType of pet %1$1s has been changed."

  /** [&9PetBlocks&f] &cYou do not have permission for template %1$1s. **/
  var templateNotAllowed : String = "[&9PetBlocks&f] &cYou do not have permission for template %1$1s."

  /** [&9PetBlocks&f] Count: %1$1d Names: %2$1s **/
  var petListMessage : String = "[&9PetBlocks&f] Count: %1$1d Names: %2$1s"

  /** [&9PetBlocks&f] The name of pet %1$1s has been changed to %2$1s. **/
  var petNameChangeMessage : String = "[&9PetBlocks&f] The name of pet %1$1s has been changed to %2$1s."

  /** [&9PetBlocks&f] &cWorld %1$1s not found. **/
  var worldNotFoundMessage : String = "[&9PetBlocks&f] &cWorld %1$1s not found."

  /** Rotates the pet to the exact line of the nearest x or z axe. **/
  var snapCommandHint : String = "Rotates the pet to the exact line of the nearest x or z axe."

  /** [&9PetBlocks&f] &cOnly the direction types %1$1s are supported. **/
  var petRotationTypeNotFound : String = "[&9PetBlocks&f] &cOnly the direction types %1$1s are supported."

  /** [&9PetBlocks&f] Pet with name %1$1s has been removed. **/
  var petDespawnedMessage : String = "[&9PetBlocks&f] Pet with name %1$1s has been removed."

  /** Spawns and teleports the pet in front of the owner. **/
  var callCommandHint : String = "Spawns and teleports the pet in front of the owner."

  /** [&9PetBlocks&f] &cYou do not have permission to execute this command. **/
  var noPermissionCommand : String = "[&9PetBlocks&f] &cYou do not have permission to execute this command."

  /** [&9PetBlocks&f] &cCannot parse boolean %1$1s. **/
  var cannotParseBoolean : String = "[&9PetBlocks&f] &cCannot parse boolean %1$1s."

  /** Creates a new pet for the player with the given pet template. **/
  var createCommandHint : String = "Creates a new pet for the player with the given pet template."

  /** Rotates the pet relative to its current rotation. **/
  var rotateRelCommandHint : String = "Rotates the pet relative to its current rotation."

  /** Changes the template of a pet without recreating the pet. **/
  var templateCommandHint : String = "Changes the template of a pet without recreating the pet."

  /** Lists all pets of a player. **/
  var listCommandHint : String = "Lists all pets of a player."

  /** [&9PetBlocks&f] &cPet with name %1$1s not found. **/
  var petNotFoundMessage : String = "[&9PetBlocks&f] &cPet with name %1$1s not found."

  /** [&9PetBlocks&f] Changed pet %1$1s to loop %2$1s. **/
  var petLoopChangedMessage : String = "[&9PetBlocks&f] Changed pet %1$1s to loop %2$1s."

  /** [&9PetBlocks&f] Pet with name %1$1s has been deleted. **/
  var petDeletedMessage : String = "[&9PetBlocks&f] Pet with name %1$1s has been deleted."

  /** Makes the owner wear the pet. **/
  var hatCommandHint : String = "Makes the owner wear the pet."

  /** [&9PetBlocks&f] The visibility of pet %1$1s has been changed. **/
  var visibilityChangedMessage : String = "[&9PetBlocks&f] The visibility of pet %1$1s has been changed."

  /** [&9PetBlocks&f] Pet with name %1$1s has been called. **/
  var petCalledMessage : String = "[&9PetBlocks&f] Pet with name %1$1s has been called."

  /** Spawns the pet if it has not already spawned. **/
  var spawnCommandHint : String = "Spawns the pet if it has not already spawned."

  /** [&9PetBlocks&f] &cCannot parse template file. See console log for details. **/
  var errorLoadingTemplatesMessage : String = "[&9PetBlocks&f] &cCannot parse template file. See console log for details."

  /** [&9PetBlocks&f] Cancelled action. **/
  var cancelMessage : String = "[&9PetBlocks&f] Cancelled action."

  /** [&9PetBlocks&f] &cYou do not have permission to edit the pets of other players. **/
  var manipulateOtherMessage : String = "[&9PetBlocks&f] &cYou do not have permission to edit the pets of other players."

  /** [&9PetBlocks&f] PetBlocks has been reloaded. **/
  var reloadMessage : String = "[&9PetBlocks&f] PetBlocks has been reloaded."

  /** Makes the owner ride the pet. **/
  var rideCommandHint : String = "Makes the owner ride the pet."

  /** Cancels any long running actions like breaking a block. **/
  var cancelCommandHint : String = "Cancels any long running actions like breaking a block."

  /** [&9PetBlocks&f] Stopped mounting pet %1$1s. **/
  var petUnmountMessage : String = "[&9PetBlocks&f] Stopped mounting pet %1$1s."

  /** Makes the owner unmount (stop riding/hat) the pet. **/
  var unmountCommandHint : String = "Makes the owner unmount (stop riding/hat) the pet."

  /** [&9PetBlocks&f] &cMaterial %1$1s not found. **/
  var petSkinTypeNotFound : String = "[&9PetBlocks&f] &cMaterial %1$1s not found."

  /** [&9PetBlocks&f] &cCannot parse nbt %1$1s. **/
  var cannotParseNbtMessage : String = "[&9PetBlocks&f] &cCannot parse nbt %1$1s."

  /** Breaks the block the pet is looking at. There is a placeholder, which contains the name of the block type.Breaking a block is automatically cancelled on certain actions. e.g. a pet looks at a player, a pet starts moving **/
  var breakBlockCommandHint : String = "Breaks the block the pet is looking at. There is a placeholder, which contains the name of the block type.Breaking a block is automatically cancelled on certain actions. e.g. a pet looks at a player, a pet starts moving"

  /** Changes the offset of the body of the entity to the ground. Useful when configuring different entity types. **/
  var groundOffSetCommandHint : String = "Changes the offset of the body of the entity to the ground. Useful when configuring different entity types."

  /** [&9PetBlocks&f] The groundOffset of the pet has been changed. **/
  var groundOffsetChangedMessage : String = "[&9PetBlocks&f] The groundOffset of the pet has been changed."

  /** [&9PetBlocks&f] The command sender has to be a player if you do not specify the optional player argument. **/
  var commandSenderHasToBePlayer : String = "[&9PetBlocks&f] The command sender has to be a player if you do not specify the optional player argument."

  /** [&9PetBlocks&f] &cCannot parse data component %1$1s. **/
  var cannotParseDataComponentMessage : String = "[&9PetBlocks&f] &cCannot parse data component %1$1s."

  /** [&9PetBlocks&f] Pet is walking to location. **/
  var petWalkToLocationMessage : String = "[&9PetBlocks&f] Pet is walking to location."

  /** [&9PetBlocks&f] &cOnly the drop types %1$1s are supported. **/
  var dropTypeNotFound : String = "[&9PetBlocks&f] &cOnly the drop types %1$1s are supported."

  /** Changes the display name of the pet. Does not accept spaces. Underlines '_' are automatically replaced by spaces. **/
  var renameCommandHint : String = "Changes the display name of the pet. Does not accept spaces. Underlines '_' are automatically replaced by spaces."

  /** Deletes the pet of the player. **/
  var deleteCommandHint : String = "Deletes the pet of the player."

  /** Changes the material used as a head. **/
  var skinTypeCommandHint : String = "Changes the material used as a head."
}
