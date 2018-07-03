Scripting
=========

PetBlock contains 2 GUI engines at the moment. The old one holding the default action and the new one
parsing the config.yml dynamically.
This means you can customize your GUI even more than before.

Description
~~~~~~~~~~~

Before you get started consider the following:

* You can add or remove items to the **gui.items** section and they get rendered in the GUI, however they simply do not do anything at all.
* You can bind an action to these items by using the new **script** tag.
* The action specified in the script tag tries to get executed when a player clicks on this item in the GUI.
* The script language contains currently only a limited amount of actions which are specified below.

Actions
~~~~~~~

1. This action allows to open a page collection of items. It acts the same as clicking on the costume categories.

* *path:* Path to the skins in the config.yml
* *permission:* Base permission to the items which automatically resolves into <permission>.all and <permission>.<position>

**action**:
::
    script: 'binding collection <path> <permission>'

The following sample would open the collection of wardrobe.ordinary items.

**sample**:
::
   script: 'binding collection wardrobe.ordinary petblocks.wardrobe'

2. This action allows to scroll the current page collection to the left right with the given amount of slots.

* *positive-or-negative-slots-amount:* Amount of slots the collection should scroll to the left or right.

**action**:
::
    script: 'scrolling collection <positive-or-negative-slots-amount>'

The following sample would scroll the item collection 45 slots to the left.

**sample**:
::
   script: 'scrolling collection -45'

Example: Adding a new costume category
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

In this section we try to add a new skin category to our wardrobe using scripting.
We want to add a new category exclusive for pokemon related skins.

1. Add a new item for our category and give it a unique name like pokemon-category. We also want to make
sure it gets rendered on the WARDROBE page.

**Important:** Choose a position where no other item is already located and give the item a unique name.

**config.yml**:
::
 gui:
  items:
    pokemon-category:
      enabled: true
      position: 26
      page: 'WARDROBE'
      id: 397
      damage: 3
      skin: 'textures.minecraft.net/texture/e3a88146def8154a3a913a5b2ab72b625bb20c74c5461f00a29b3c0ae732ec58'
      name: '&e&lPokemon Skins'
      unbreakable: false
      lore:
        - '&7Use pokemon skins as costume.'


Type **/petblockreload** and check if the item is already appearing in your config.yml.
It will not do anything when you click it but it should be visible.

.. image:: ../_static/images/script_1.jpg

2. Create a new skin collection in your config.yml. You can put it anywhere but for keeping it clean put it below the wardrobe
tag.

**config.yml**:
::
 wardrobe:
   pokemon-collection:
    1:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/e3a88146def8154a3a913a5b2ab72b625bb20c74c5461f00a29b3c0ae732ec58'
     name: 'Flareon'
     unbreakable: false
     lore:
       - 'none'
    2:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/c5ea93557401e05432aebf876f91250154e7a784287b1616a4e72977c570ffa0'
     name: 'Jolteon'
     unbreakable: false
     lore:
       - 'none'
    3:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/ca88655b16a8331a8d130c172243ce18ef7d28573ccd3c9faee464be6fdaf346'
     name: 'Espeon'
     unbreakable: false
     lore:
       - 'none'

**Important:** You can always type **/petblockreload** and take a look into your console log to see if the config.yml can still be parsed.
If the script is correctly added but it gets not executed try restarting the server.

3. Add the 'script' tag to the category item with the correct action, path and permission.

**config.yml**:
::
 gui:
  items:
    pokemon-category:
      enabled: true
      position: 26
      page: 'WARDROBE'
      id: 397
      damage: 3
      skin: 'textures.minecraft.net/texture/e3a88146def8154a3a913a5b2ab72b625bb20c74c5461f00a29b3c0ae732ec58'
      name: '&e&lPokemon Skins'
      unbreakable: false
      script: 'binding collection wardrobe.pokemon-collection petblocks.selection.pokemoncostumes'
      lore:
        - '&7Use pokemon skins as costume.'


4. Finished, open ingame your GUI and try if its's working.


.. image:: ../_static/images/script_2.jpg


5. [Optionally] A public server has to manage permissions, so let us see if our permission **petblocks.selection.pokemoncostumes** works.


a) Change the item lore to display the permission ingame.

**config.yml**:
::
 wardrobe:
   pokemon-collection:
    1:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/e3a88146def8154a3a913a5b2ab72b625bb20c74c5461f00a29b3c0ae732ec58'
     name: 'Flareon'
     unbreakable: false
     lore:
       - '&7Permission: <permission>'
    2:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/c5ea93557401e05432aebf876f91250154e7a784287b1616a4e72977c570ffa0'
     name: 'Jolteon'
     unbreakable: false
     lore:
       - '&7Permission: <permission>'
    3:
     id: 397
     damage: 3
     skin: 'textures.minecraft.net/texture/ca88655b16a8331a8d130c172243ce18ef7d28573ccd3c9faee464be6fdaf346'
     name: 'Espeon'
     unbreakable: false
     lore:
       - '&7Permission: <permission>'


The permission should work correctly. Sometimes the lore is cached and a server restart is necessary!

.. image:: ../_static/images/script_3.jpg


b) Now let us give our players permission to use Flareon by adding the permission **petblocks.selection.pokemoncostumes.1** via any permission plugin.

Alternatively, you can use the permission **petblocks.selection.pokemoncostumes.all** to allow access to all costumes in this category.

.. image:: ../_static/images/script_4.jpg
