# Giving Pets to Players

Once you have setup the permission, join your server. The PetBlocks default configuration creates a pet
with template ``classic`` for you and makes it spawn in front of you. If you cannot see the pet, try executing ``/petblocks call pet`` or review your permissions again.


**Make sure your players have this permission to see their pet:**
```
petblocks.pet.spawn
```

The next step is to decide, how you want your players to receive a pet. There are multiple options below.

## Receiving a pet on first join

This is enabled per default and can be changed in the ``config.yml`` under ``pet/receivePetsOnJoin``. This creates a new database entry for each joining player, regardless if he has the spawn permission or not.
The player requires the minimum ``petblocks.pet.spawn`` to see their pet.

If you want to receive no pets on join and unlock pets later own (e.g. for shops or VIP perks), change this to: 

```
pet:
  receivePetsOnJoin: []
```

If you want to receive multiple pets (PetBlocks-Premium only) on first join. Append other pets below it. They have to have different names.

```
pet:
  receivePetsOnJoin:
    - name: "pet1"
      template: "classic"
    - name: "pet2"
      template: "classic"      
```

In order to configure, if the pet should automatically spawn in front of the player on creation, open the template 
``plugins/PetBlocks/pets/pet_classic.yml``. Set ``pet/spawned`` to ``true`` or ``false``.


## Receiving pets using a shop

There are many ways how you can handle it. These are just examples below:

!!! note "Building GUIS with DeluxeMenu"
    PetBlocks distributes premade DeluxeMenu based inventories, which may help you design your own inventories. See ``plugins/PetBlocks/thirdparty/DeluxeMenu``.
    You can try them by copying the contents of the ``plugins/PetBlocks/thirdparty/DeluxeMenu`` folder to the ``plugins/DeluxeMenu`` folder. Execute /petblocksmenu
    with the permission ``petblocks.deluxemenu``.
      

#### Option 1 - Just selling skins for your pet (easy)

1. Build a shop GUI using an external GUI plugin. (e.g. DeluxeMenu).
2. Keep the receive pet on join settings, the player should receive a pet everytime. 
3. Invent a new permission for each pet you would like to sell like ``petblocks.pettype.<yourpettype>`` e.g. ``petblocks.pettype.pikachu``.
4. Once a player buys the item in your shop, give them your newly invented permission e.g. ``petblocks.pettype.pikachu``
5. Show an item in your GUI to change the skin to pikachu only, if the player has got the newly invented permission e.g. ``petblocks.pettype.pikachu``
6. Execute the command with server level permission:


```
/petblocks skinbase64 pet eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdlYmNlZjQ2ODNjZGI3MTYzZTk2OWU0ZTIyNjlmMzY3M2E1ZDVlNmI3OGUwNmZhZWU0NWJjZjdjNDljMzk3In19fQ== %petblocks_owner_name%
```

DeluxeMenu e.g.:
```
[console] petblocks skinbase64 %petblocks_pet_name_selected% eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTdlYmNlZjQ2ODNjZGI3MTYzZTk2OWU0ZTIyNjlmMzY3M2E1ZDVlNmI3OGUwNmZhZWU0NWJjZjdjNDljMzk3In19fQ== %petblocks_owner_name%
```

#### Option 2 - State handling of pets (hard)

1. Build a shop GUI using an external GUI plugin. (e.g. DeluxeMenu).
2. Disable receive pet on join like shown above
3. Invent a new permission for each pet you would like to sell like ``petblocks.pettype.<yourpettype>`` e.g. ``petblocks.pettype.pikachu``. 
4. Once a player buys the item in your shop, give them your new invented permission e.g. ``petblocks.pettype.pikachu``
5. Build a pet management GUI using an external GUI plugin (e.g. DeluxeMenu).
6. Create a new item which is only visible if the player has obtained the e.g. ``petblocks.pettype.pikachu`` permission.
7. When the player clicks on that item, execute the following command with server level permission:

```
/petblocks create <petName> <templateName> %petblocks_owner_name%
```

DeluxeMenu e.g.:

```
[console] petblocks create pet classic %petblocks_owner_name%
```

Executing the create command multiple times is fine, it does not do anything if the pet already exists.

#### Option 3 - Free creation/deletion of pets for players (easy)

1. Give your players the permission to the following

```
petblocks.pet.create
petblocks.pet.delete
petblocks.pet.amount.1
```

2. Disable receive pet on join like shown above
3. Build a shop GUI using an external GUI plugin. (e.g. DeluxeMenu).
4. Once a player buys the item in your shop, give them the permission to the petblocks template e.g. ``petblocks.pet.template.classic``.
5. Let the player freely execute ``/petblocks create <petName> classic`` and  ``/petblocks delete <petName>`` . You can also setup this in a GUI.





