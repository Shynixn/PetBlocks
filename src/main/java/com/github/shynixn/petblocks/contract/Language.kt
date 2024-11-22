package com.github.shynixn.petblocks.contract

import com.github.shynixn.mcutils.common.language.LanguageItem
import com.github.shynixn.mcutils.common.language.LanguageProvider

interface Language : LanguageProvider {
  var petNameChangeMessage: LanguageItem

  var templateNotFoundMessage: LanguageItem

  var petNotFoundMessage: LanguageItem

  var petNameExistsMessage: LanguageItem

  var petCreatedMessage: LanguageItem

  var petDeletedMessage: LanguageItem

  var playerNotFoundMessage: LanguageItem

  var petCalledMessage: LanguageItem

  var petSpawnedMessage: LanguageItem

  var petDespawnedMessage: LanguageItem

  var visibilityTypeNotFoundMessage: LanguageItem

  var visibilityChangedMessage: LanguageItem

  var petSkinTypeChangedMessage: LanguageItem

  var petSkinNbtChanged: LanguageItem

  var petSkinTypeNotFound: LanguageItem

  var petListMessage: LanguageItem

  var petRideMessage: LanguageItem

  var petUnmountMessage: LanguageItem

  var petHatMessage: LanguageItem

  var worldNotFoundMessage: LanguageItem

  var petLookAtMessage: LanguageItem

  var petWalkToLocationMessage: LanguageItem

  var cannotParseNumberMessage: LanguageItem

  var cannotParseNbtMessage: LanguageItem

  var placeHolderPetNotFound: LanguageItem

  var petTeleportedMessage: LanguageItem

  var petVelocityAppliedMessage: LanguageItem

  var petCharacterNotAllowed: LanguageItem

  var reloadMessage: LanguageItem

  var petLoopNotFound: LanguageItem

  var premiumMultiplePets: LanguageItem

  var petLoopChangedMessage: LanguageItem

  var petTemplateChangeMessage: LanguageItem

  var petAmountNotAllowed: LanguageItem

  var petSelectedMessage: LanguageItem

  var templateNotAllowed: LanguageItem

  var speedCannotBeParsed: LanguageItem

  var headDatabasePluginNotLoaded: LanguageItem

  var errorLoadingTemplatesMessage: LanguageItem

  var dropTypeNotFound: LanguageItem

  var cancelMessage: LanguageItem

  var snapMessage: LanguageItem

  var petMoveForwardMessage: LanguageItem

  var petRotationTypeNotFound: LanguageItem

  var rotationRelMessage: LanguageItem

  var entityTypeChangeMessage: LanguageItem

  var entityVisibilityChangedMessage: LanguageItem

  var groundOffsetChangedMessage: LanguageItem

  var groundOffsetCannotBeParsed: LanguageItem

  var cannotParseDataComponentMessage: LanguageItem

  var maxLength20Characters: LanguageItem

  var noPermissionCommand: LanguageItem

  var commandUsage: LanguageItem

  var commandDescription: LanguageItem

  var commandSenderHasToBePlayer: LanguageItem

  var cannotParseBoolean: LanguageItem

  var createCommandHint: LanguageItem

  var deleteCommandHint: LanguageItem

  var listCommandHint: LanguageItem

  var callCommandHint: LanguageItem

  var lookAtCommandHint: LanguageItem

  var lookAtOwnerCommandHint: LanguageItem

  var moveToCommandHint: LanguageItem

  var moveToOwnerCommandHint: LanguageItem

  var hatCommandHint: LanguageItem

  var rideCommandHint: LanguageItem

  var unmountCommandHint: LanguageItem

  var teleportCommandHint: LanguageItem

  var velocityCommandHint: LanguageItem

  var skinTypeCommandHint: LanguageItem

  var skinNbtCommandHint: LanguageItem

  var skinComponentCommandHint: LanguageItem

  var skinBase64CommandHint: LanguageItem

  var skinHeadDatabaseCommandHint: LanguageItem

  var renameCommandHint: LanguageItem

  var visibilityCommandHint: LanguageItem

  var loopCommandHint: LanguageItem

  var templateCommandHint: LanguageItem

  var spawnCommandHint: LanguageItem

  var deSpawnCommandHint: LanguageItem

  var toggleCommandHint: LanguageItem

  var selectCommandHint: LanguageItem

  var openHeadDatabaseCommandHint: LanguageItem

  var breakBlockCommandHint: LanguageItem

  var cancelCommandHint: LanguageItem

  var snapCommandHint: LanguageItem

  var moveForwardCommandHint: LanguageItem

  var rotateRelCommandHint: LanguageItem

  var entityTypeCommandHint: LanguageItem

  var entityVisibleCommandHint: LanguageItem

  var groundOffSetCommandHint: LanguageItem

  var manipulateOtherMessage: LanguageItem

  var reloadCommandHint: LanguageItem

  var closeCommandHint: LanguageItem

  var backCommandHint: LanguageItem

  var openCommandHint: LanguageItem

  var nextCommandHint: LanguageItem

  var messageCommandHint: LanguageItem

  var guiMenuNotFoundMessage: LanguageItem

  var guiMenuNoPermissionMessage: LanguageItem

  var variableCommandHint: LanguageItem

  var variableChangedMessage: LanguageItem

  var velocityRelCommandHint: LanguageItem
}
