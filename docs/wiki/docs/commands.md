# Commands

PetBlocks is a command line based pet plugin. This means, you can fully control the pets using commands, which may be executed by **players**, **console**,  **other plugins**, **command blocks**.

e.g. Letting the pet named ``pet`` of player ``Steve98`` move to a certain location from the console. (The pet has to be in a nearby location)

```
/petblocks moveto pet <x> <y> <z> <speed> [player]
```

```
petblocks moveto pet 250 5 300 0.2 Steve98
```

!!! note "Removing the pet under certain conditions"
    You can always take a way the ``petblocks.pet.spawn`` permission to remove the pet from players, if they enter certain areas (e.g. joining minigames, entering regions, etc.)

## Command /petblock

**Required Permission:**
``
petblocks.command
``

### /petblock

Opens the main GUI of petblocks. You can configure this command in the ``gui/petblocks_main_menu.yml`` file.

## Command /petblocks

**Required Permission:**
``
petblocks.command
``

### /petblocks create 

```
/petblocks create <name> <template> [player]
```

Creates a new pet for the player with the given pet template.

* Name: Identifier of a pet
* Template: Identifier of a template 
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks delete

```
/petblocks delete <name> [player]
```

Deletes the pet of the player.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks list

```
/petblocks list [player]
```

Lists all pets of a player.

* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks call

```
/petblocks call <name> [player]
```

Spawns and teleports the pet in front of the owner.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks lookat

```
/petblocks lookat <name> <x> <y> <z> [player]
```

Makes the pet look at the given location.

* Name: Identifier of a pet
* X: X Coordinate
* Y: Y Coordinate
* Z: Z Coordinate
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks lookatowner

```
/petblocks lookatowner [player]
```

Makes the pet look at the owner.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks moveto

```
/petblocks moveto <name> <x> <y> <z> <speed> [player]
```

Makes the pet walk to a given location.

* Name: Identifier of a pet
* X: X Coordinate
* Y: Y Coordinate
* Z: Z Coordinate
* Speed: Walking speed of the pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks movetoowner

```
/petblocks movetoowner <name> <speed> [player]
```

Makes the pet walk to the owner.

* Name: Identifier of a pet
* Speed: Walking speed of the pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks hat

```
/petblocks hat <name> [player]
```

Makes the owner wear the pet.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks ride

```
/petblocks ride <name> [player]
```

Makes the owner ride the pet.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks unmount

```
/petblocks unmount <name> [player]
```

Makes the owner unmount (stop riding/hat) the pet.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks teleport

```
/petblocks teleport <name> <world> <x> <y> <z> <yaw> <pitch> [player]
```

Teleports the pet to the given location.

* Name: Identifier of a pet
* World: Target world
* X: X Coordinate
* Y: Y Coordinate
* Z: Z Coordinate
* Yaw: Horizontal Axe
* Pitch: Vertical Axe
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks velocity

```
/petblocks velocity <name> <x> <y> <z> [player]
```

Launches the pet into the given direction.

* Name: Identifier of a pet
* X: X Vector
* Y: Y Vector
* Z: Z Vector
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks velocityrel

```
/petblocks velocityrel <name> <mx> <my> <mz> [oy] [player]
```

Launches the pet into the current looking direction with the given multipliers. 

* Name: Identifier of a pet
* MX: Multiplier in X direction. Try to set it to 1 first.
* MY: Multiplier in Y direction. Try to set it to 1 first.
* MZ: Multiplier in Z direction. Try to set it to 1 first.
* OY: Optional overwrite for the base y value. If you set this to 0.5 or higher, the pet always gets launched upwards.
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.


### /petblocks skintype

```
/petblocks skintype <name> <material> [durability] [player]"
```

Changes the material used as a head. 

* Name: Identifier of a pet
* Material: Minecraft material name e.g. STONE
* Durability of the type. Only used in old minecraft versions < 1.16.5. Set it to 0 in modern minecraft versions.
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks skinnbt

```
/petblocks skinnbt <name> <nbt> [player]
```

Changes the NBT tags of the head item. 
> Works in Minecraft versions below 1.20.5. Use the ``/petblocks skincomponent`` command for Minecraft >= 1.20.5.

* Name: Identifier of a pet
* NBT: Standard Minecraft NBT format. e.g. {Unbreakable:1}
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks skincomponent

```
/petblocks skincomponent <name> <datacomponent> [player]
```

Changes the Data Component tags of the head item. Data Components replace NBT tags since the release of Minecraft 1.20.5.

> See [https://minecraft.wiki/w/Data_component_format](https://minecraft.wiki/w/Data_component_format) for details.
Use the ``/petblocks skinnbt`` command for Minecraft server below 1.20.5.

* Name: Identifier of a pet
* DataComponent: Data components in JSON format. e.g. {"minecraft:unbreakable":true}
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks skinbase64

```
/petblocks skinbase64 <name> <skin> [player]
```

Changes the head material to player_head and sets the base64 encoded texture url. 

* Name: Identifier of a pet
* Skin: Bas64 Encoded Texture value e.g. ``eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYThhZDZmYzEyNzA4NDQ2ZWI2NmM3NTY5ZTI4ZDVlMjUyMWQyMTQ2MzQ4YjQ4YmY2YzQ3ZjU0OGQ5ZWVjNDYwYSJ9fX0=`` on https://minecraft-heads.com/custom-heads/humanoid/67436-beegsmol-mumei 
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks skinheaddatabase

```
/petblocks skinheaddatabase <name> <hdbId> [player]
```

Changes the head material to player_head and sets the texture url from a skin loaded by the HeadDatabase plugin.

* Name: Identifier of a pet
* HdbId: Internal id the HeadDatabase plugin uses.
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks rename

```
/petblocks rename <name> <displayname> [player]
```

Changes the display name of the pet. Does not accept spaces. Underlines '_' are automatically replaced by spaces.
Allows values should be set in the ``config.yml``.

* Name: Identifier of a pet
* DisplayName: Name hovering above the pet, which supports ChatColors.
* Player: Optional player_name/player_UUID *p*arameter targeting a player from the console or command block.

### /petblocks visibility

```
/petblocks visibility <name> <type> [player]
```

Changes who can see the pet. 

* Name: Identifier of a pet
* Types: Possibles values: ``ALL``, ``OWNER``, ``NOBODY``
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks loop

```
/petblocks loop <name> <loop> [player]
```

Changes which loop from the template is being executed by the pet. Example loops are ``idle``.

* Name: Identifier of a pet
* Loop: Identifier of a loop in the template of the pet.
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks template

```
/petblocks template <name> <template> [player]
```

Changes the template of a pet without recreating the pet.

* Name: Identifier of a pet
* Template: Template identifier e.g. classic
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks spawn

```
/petblocks spawn <name> [player]
```

Spawns the pet if it has not already spawned.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks despawn

```
/petblocks despawn <name> [player]
```

Despawns the pet if it has not already despawned.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks toggle

```
/petblocks toggle <name> [player]
```

Toggles the pet spawn state.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks select

```
/petblocks select <name> [player]
```

Makes the owner select one of his pets as primary pet. This is only helpful if a single player has got multiple pets in PetBlocks-Premium.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks openheaddatabase

```
/petblocks openheaddatabase <name> [player]
```

Opens the headDatabase inventory with a special hook, which applies the next item you select in the headdatabse gui to the pet.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.


### /petblocks breakblock

```
/petblocks breakblock <name> <timeToBreak> <dropType> [player]
```

Breaks the block the pet is looking at. There is a placeholder, which contains the name of the block type.
This command only works on blocks, if the player executing the command has got the permission to break this specific block. 
The command is cancel able using the cancel command.
Breaking a block is automatically cancelled on certain actions. e.g. a pet looks at a player, a pet starts moving

* Name: Identifier of a pet
* TimeToBreak: Time in ticks (20 ticks = 1 second) to break a block
* DropType: Describes what happens to the dropped item (VANISH, DROP, SEND_TO_OWNER_INVENTORY). Multiple drop types can be specified by comma separation. e.g. SEND_TO_OWNER_INVENTORY,DROP tries to send it to the owner inventory first, if full, it drops it
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks cancel

```
/petblocks cancel <name> [player]
```

Cancels any long running actions like breaking a block.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks snap

```
/petblocks snap <name> [player]
```

Rotates the pet to the exact line of the nearest x or z axe.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks moveforward

```
/petblocks moveforward <name> <speed> [player]
```

Lets the pet move forward in its current direction. Executing the snap command before executing this is helpful to
move in a straight direction. If the pet reaches a cliff (1 block difference), moving forward stops.

* Name: Identifier of a pet
* Speed: Speed parameter, should be comma values e.g. 0.3
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks rotaterel

```
/petblocks rotaterel <name> <direction> <angle> [player]
```

Rotates the pet relative to its current rotation.

* Name: Identifier of a pet
* Direction: LEFT, RIGHT, UP, DOWN
* Angle: Angle in degrees e.g. 45, 90
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks entitytype

```
/petblocks entitytype <name> <entityType> [player]
```

Changes the entity type of the pet. The default type is minecraft:armor_stand

* Name: Identifier of a pet
* EntityType: An entitytype in the minecraft format. e.g. minecraft:bee, minecraft:dolphin, minecraft:armor_stand. For minecraft versions below 1.11, you need to use the [entity number](https://github.com/CircuitLord/Minecraft-1.9-MCP/blob/bc89baf1fd0b5d422478619e7aba01c0b23bd405/temp/src/minecraft/net/minecraft/entity/EntityList.java#L232) instead. 
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks entityvisible

```
/petblocks entityvisible <name> <true/false> [player]
```

Changes if the body of an entity is visible. For armorstands this is false, for most of the other entities this should be true.

* Name: Identifier of a pet
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks groundoffset

```
/petblocks groundoffset <name> <offset> [player]
```

Changes the offset of the body of the entity to the ground. Useful when configuring different entity types.

* Name: Identifier of a pet
* Offset: A numeric comma value. e.g. 0.3, -0.3, 1.0
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks ridingspeed

```
/petblocks ridingspeed <name> <speed> [player]
```

Changes the speed while riding a pet.

* Name: Identifier of a pet
* Offset: A numeric comma value. e.g. 0.3, 0.5, 1.5
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks variable

```
/petblocks variable <name> <key> <value> [player]
```

Sets the value of the variable with the given key. This is useful store arbitrary data into the pet.
e.g. health, selected particle, selected sound, etc.

* Name: Identifier of a pet
* Key: A key to store and retrieve a value
* Value: Arbitrary data to store.
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.


## Command /petblocksgui

**Required Permission:**
``
petblocks.command
``

### /petblocksgui open

```
/petblocksgui open <name> [arguments.../player]
```

Starts a new GUI session and opens the GUI with the given name for the executing player. If any other GUIs were open when executing this command, they get discarded from the navigation history.

* Name: Identifier of a GUI menu
* Argument/Player: Optional arguments to provide the GUI with additional arguments and optionally a player to open the GUI for.

Samples:

* Opens the inventory for the executing player.

```
/petblocksgui open simple_sample_menu
```

* Opens the inventory for the player named "Pikachu".
* The slash separates arguments with the player name (in this case there are 0 arguments)

```
/petblocksgui open simple_sample_menu / Pikachu
```

* Opens the inventory with additional arguments which can be accessed via the placeholders. ``%petblocks_gui_param1%`` is now ``123456``.

```
/petblocksgui open simple_sample_menu 123456
```

* Opens the inventory with additional arguments which can be accessed via the placeholders for the player named "Pikachu". ``%petblocks_gui_param1%`` is now ``123456`` ``%petblocks_gui_param2%`` is now ``abcde``.
* The slash separates arguments with the player name

```
/petblocksgui open simple_sample_menu 123456 abcde / Pikachu
```

### /petblocksgui next

```
/petblocksgui next <name> [arguments.../player]
```

Reuses the existing GUI session (or starts a new one if it is not available) and opens the GUI with the given name for the executing player. If any other GUIs were open when executing this command, they get put into the navigation history. Executing ``/petblocksgui back`` reopens the previous GUI.

* Name: Identifier of a GUI menu
* Argument/Player: Optional arguments to provide the GUI with additional arguments. See the open command for samples.

### /petblocksgui back

```
/petblocksgui back [player]
```

Checks if the current GUI session contains a previously opened GUI. If that is the case, the previous GUI is opened and the current GUI is discarded. If not GUI is found, the current GUI is simply closed.

* Player: Optional player argument to execute the action for another player.

### /petblocksgui close

```
/petblocksgui close [player]
```

Closes the current GUI and clears the GUI session and navigation history.

* Player: Optional player argument to execute the action for another player.
