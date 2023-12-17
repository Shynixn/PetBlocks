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

* Name: Identifier of a pet
* NBT: Standard Minecraft NBT format. e.g. {Unbreakable:1}
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







