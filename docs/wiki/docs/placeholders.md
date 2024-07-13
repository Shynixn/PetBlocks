# PlaceHolders

The following placeholders are available in PetBlocks and can also be used via PlaceHolderApi.

!!! note "PlaceHolder Api"
    As PetBlocks supports multiple pets per player, you need to select the pet in your placeholders. You can do this by appending the pet slot ``_1`` ``_2`` etc. or use the ``/petblocks select <name>`` command to set a pet as selected which can be retrieved using ``_selected``.
    This results into placeholders such as e.g. ``%petblocks_pet_displayName_1%`` or ``%petblocks_pet_displayName_selected%``. This is only relevant in external plugins. For actions in PetBlocks, you can directly use the placeholders below.


| Player PlaceHolders            | Description                                          |   
|--------------------------------|------------------------------------------------------|
| %petblocks_owner_name%         | Player name of the owner of a pet.                   |   
| %petblocks_owner_displayName%  | Player displayName of the owner of a pet.            |
| %petblocks_owner_locationWorld% | Name of the world the owner of a pet is inside.      |   
| %petblocks_owner_locationX%    | X coordinate of the owner of a pet.                  |   
| %petblocks_owner_locationY%    | Y coordinate of the owner of a pet.                  |   
| %petblocks_owner_locationZ%    | Z coordinate of the owner of a pet.                  |   
| %petblocks_owner_locationYaw%  | Yaw rotation of the owner of a pet.                  |   
| %petblocks_owner_locationPitch% | Yaw rotation of the owner of a pet.                  |   
| %petblocks_owner_itemMainHand_type% | Name of the item type in the owner's main hand       |   
| %petblocks_owner_isFlying%     | True if the owner is currently flying, false if not. |   

| Pet PlaceHolders                | Description                                                                                        |   
|---------------------------------|----------------------------------------------------------------------------------------------------|
| %petblocks_pet_name%            | Id of a pet                                                                                        |   
| %petblocks_pet_displayName%     | Displayname of a pet                                                                               |
| %petblocks_pet_distanceToOwner% | Distance from the pet to the owner. If the owner is in another world, this value becomes very high. |   
| %petblocks_pet_isSpawned%       | True if the pet is spawned, false if not.                                                          |   
| %petblocks_pet_template%        | Name of the template a pet uses.                                                                   |   
| %petblocks_pet_visibility%      | Visibility Type a pet uses.                                                                        |   
| %petblocks_pet_isMounted%       | True if the owner rides the pet or uses it as a hat.                                               |   
| %petblocks_pet_loop%            | Name of the loop a pet currently executes.                                                         |   
| %petblocks_pet_locationWorld%   | Name of the world the pet is inside.                                                               |   
| %petblocks_pet_locationX%       | X coordinate of the pet.                                                                           |   
| %petblocks_pet_locationY%       | Y coordinate of the pet.                                                                           |   
| %petblocks_pet_locationZ%       | Z coordinate of the pet.                                                                           |   
| %petblocks_pet_locationYaw%     | Yaw rotation of the pet.                                                                           |   
| %petblocks_pet_locationPitch%   | Yaw rotation of the pet.                                                                           |   
| %petblocks_pet_itemType%        | The head item type of the pet.                                                                     |   
| %petblocks_pet_itemNbt%         | The head item NBT of the pet. < 1.20.5                                                             |   
| %petblocks_pet_itemComponent%         | The head item data component of the pet. >= 1.20.5                                                 |
| %petblocks_pet_itemHeadBase64%         | The head item Base64 Skin Url of the pet.                                                          |
| %petblocks_pet_isBreakingBlock%         | True if the pet is currently breaking a block, false if not.                                       |
| %petblocks_pet_blockInFrontType%         | Name of the block type the pet is looking at                                                       |


| Event Player PlaceHolders       | Description                                                |   
|---------------------------------|------------------------------------------------------------|
| %petblocks_eventPlayer_name%          | Player name of the event trigger player.                   |   
| %petblocks_eventPlayer_displayName%   | Player displayName of the event trigger player.            |
| %petblocks_eventPlayer_locationWorld% | Name of the world event trigger player of a pet is inside. |   
| %petblocks_eventPlayer_locationX%     | X coordinate of the event trigger player.                  |   
| %petblocks_eventPlayer_locationY%     | Y coordinate of the event trigger player.                  |   
| %petblocks_eventPlayer_locationZ%     | Z coordinate of the event trigger player.                  |   
| %petblocks_eventPlayer_locationYaw%   | Yaw rotation of the event trigger player.                  |   
| %petblocks_eventPlayer_locationPitch% | Yaw rotation of the event trigger player.                  |   
| %petblocks_eventPlayer_itemMainHand_type% | Name of the item type in the players's main hand           |   
| %petblocks_eventPlayer_isFlying%     | True if the player is currently flying, false if not.      |   

| GUI PlaceHolders                   | Description                                                |   
|-------------------------------------------|------------------------------------------------------------|
| %petblocks_player_name%        | Player name of the player clicking in the current inventory.          |   
| %petblocks_player_displayName% | Player displayName of the player session in the current inventory.    |
| %petblocks_gui_name%           | Name of the current inventory.                                        |   
| %petblocks_gui_backName%       | Name of the previous inventory (if you have multiple sub pages)       |   
| %petblocks_gui_param1%         | A GUI can have up to 9 parameters. This placeholders access number 1. |   
| %petblocks_gui_param2%         | A GUI can have up to 9 parameters. This placeholders access number 2. |   
| %petblocks_gui_param3%         | A GUI can have up to 9 parameters. This placeholders access number 3. |   
| %petblocks_gui_param4%         | A GUI can have up to 9 parameters. This placeholders access number 4. |   
| %petblocks_gui_param5%         | A GUI can have up to 9 parameters. This placeholders access number 5. |   
| %petblocks_gui_param6%         | A GUI can have up to 9 parameters. This placeholders access number 6. |   
| %petblocks_gui_param7%         | A GUI can have up to 9 parameters. This placeholders access number 7. |   
| %petblocks_gui_param8%         | A GUI can have up to 9 parameters. This placeholders access number 8. |   
| %petblocks_gui_param9%         | A GUI can have up to 9 parameters. This placeholders access number 9. |   

