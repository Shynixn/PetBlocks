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
      

## Command /petblocks

PetBlocks does only have 1 command, which is restricted using permissions.

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

Deletes the pet for the player.

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
* World: Target world
* X: X Vector
* Y: Y Vector
* Z: Z Vector
* Player: Optional player_name/player_UUID parameter targeting a player from the console or command block.

### /petblocks skintype

```
/petblocks skintype <name> <material> [player]"
```

Changes the material used as a head. 

* Name: Identifier of a pet
* Material: Minecraft material name e.g. STONE
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
* Types: Possibles values: ``ALL``, ``OWNER``
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
