All tags
========

Each gui item has got the following sub tags in the **config.yml** where some are required.

Tags with additional information below are marked bold.


List of root gui item tags
~~~~~~~~~~~~~~~~~~~~~~~~~~

================================  ========= ======================================================== =======================================================================
Tag                               Required  Description                                              Samples
================================  ========= ======================================================== =======================================================================
row                               yes       Row grid index starting from 1 top to down               row: 1, row: 3
col                               yes       Column grid index starting from 1 left to right          col: 1, col: 3
**icon**                          yes       Holds the displaying item information                    icon:
hidden                            no        Hides the gui item per default                           hidden: false (default), hidden: true
fixed                             no        Fixed the gui item at the same slot on scrolling         fixed: false (default), fixed: true
permission                        no        Custom required permission to execute this item          permission: 'petblocks.wardrobe.open', permission: 'strange.perm'
**script**                        no        Execute one of the actions defined below                 script: 'call-pet', 'open-page gui.colored-block-skins'
**hidden-on**                     no        Hides the gui item on certain states                     hidden-on
**blocked-on**                    no        Blocks the gui item on certain states                    blocked-on
**set-skin**                      no        Skin which gets applied to your pet on click             set-skin
**add-ai**                        no        Ais which get applied to your pet on click               add-ai
**remove-ai**                     no        Ais which get removed from your pet on click             remove-ai
**replace-ai**                    no        Ais which get replaced from your pet on click            replace-ai
================================  ========= ======================================================== =======================================================================

List of icon tags
~~~~~~~~~~~~~~~~~

**config.yml**
::
      icon:
        id: 397
        damage: 3
        skin: 'http://textures.minecraft.net/texture/f45c9acea8da71b4f252cd4deb5943f49e7dbc0764274b25a6a6f5875baea3'
        name: '&a&lPet enabled'
        lore:
        - '&7Click to disable the pet.'

================================  ========= ======================================================== =======================================================================
Tag                               Required  Description                                              Samples
================================  ========= ======================================================== =======================================================================
id                                yes       Numeric id of the minecraft item                         id: 1, id: 397
name                              yes       Unique name on the current gui page                      '&a&lPet enabled'
damage                            no        Damage or DataValue to describe the minecraft item       damage: 0, damage:1
skin                              no        `Skin of a player head <skins.html>`_                    'Shynixn', 'textures.minecraft.net/texture/ad5fcd31287d63e7826ea760a7ed154f685dfdc7f3465732a96e619b2e1347'
lore                              no        List of lore text.                                       lore:
unbreakable                       no        Unbreakable item tag.                                    unbreakable: false (default), unbreakable: true
**script**                        no        Special script tags only available for icons             script:
================================  ========= ======================================================== =======================================================================

The icon script tag
~~~~~~~~~~~~~~~~~~~

These are scripts exclusive for icons.

**config.yml**
::
   script: 'copy-pet-skin'

================================ ========================================================
Tag                              Description
================================ ========================================================
copy-pet-skin                    Copies the current pet skin
hide-left-scroll                 Hides the gui item if the gui scroll has reached the left border
hide-right-scroll                Hides the gui item if the gui scroll has reached the right border
================================ ========================================================

The script tag
~~~~~~~~~~~~~~

These are scripts for the gui items.


**config.yml**
::
   script: 'call-pet'

================================ ========================================================
Tag                              Description
================================ ========================================================
call-pet                         Calls the pet to the player.
disable-pet                      Disables the pet of the player
close-gui                        Goes back to the previous gui page or closes the gui
open-page <name>                 Opens the specified gui page. See wardrobe item
scroll <x> 0                     Scrolls the gui page to the x axe
print-suggest-heads-message      Sends the suggest head message to the player
print-custom-skin-message        Sends the custom skin message to the player
print-rename-message             Sends the rename message to the player
connect-head-database            Connects the player to the HeadDatabase plugin if available
launch-cannon                    Launches the pet like a cannon
enable-sound                     Enables the pet sounds
disable-sound                    Disables the pet sounds
enable-particles                 Enables the pet particles
disable-particles                Disables the pet particles
================================ ========================================================

The hidden-on tag and blocked-on tag
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

The difference between the hidden-on and blocked-on tag is that gui items get hidden entirely when using hidden-on.
Only one condition has to match.

**config.yml**
::
   hidden-on:
      - 'pet-disabled'
      - 'sound-disabled'
      - 'float-in-water'

================================ ========================================================
Tag                              Description
================================ ========================================================
pet-enabled                      Hides or blocks when the pet is enabled
pet-disabled                     Hides or blocks when the pet is disabled
sound-enabled                    Hides or blocks when the pet sound is enabled
sound-disabled                   Hides or blocks when the pet sound is disabled
particle-enabled                 Hides or blocks when the pet particle is enabled
particle-disabled                Hides or blocks when the pet particle is disabled
no-permission                    Hides or blocks when the player has not got the permission specified in the permission tag
<aitype>                         Hides or blocks when the pet has got the specified ai type name
================================ ========================================================

The set-skin tag
~~~~~~~~~~~~~~~~

If the set-skin tag is set to any gui item, then the pet will receive this skin when the player clicks on it in the gui.

**config.yml**
::
    set-skin:
        id: 397
        damage: 3
        skin: 'http://textures.minecraft.net/texture/456eec1c2169c8c60a7ae436abcd2dc5417d56f8adef84f11343dc1188fe138'

================================  ========= ======================================================== =======================================================================
Tag                               Required  Description                                              Samples
================================  ========= ======================================================== =======================================================================
id                                yes       Numeric id of the minecraft item                         id: 1, id: 397
damage                            no        Damage or DataValue to describe the minecraft item       damage: 0, damage:1
skin                              no        `Skin of a player head <skins.html>`_                    'Shynixn', 'textures.minecraft.net/texture/ad5fcd31287d63e7826ea760a7ed154f685dfdc7f3465732a96e619b2e1347'
unbreakable                       no        Unbreakable item tag.                                    unbreakable: false (default), unbreakable: true
================================  ========= ======================================================== =======================================================================

The add-ai, remove-ai, replace-ai tag
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

If the add-ai tag is set to any gui item, then the pet will receive this ai when the player clicks on it in the gui.

If the remove-ai tag is set to any gui item, then the pet will lose this ai when the player clicks on it in the gui.

If the replace-ai tag is set to any gui item, then the pet will replace any ai with the same type with the ai specified in the gui.

.. note:: For best practises, prefer using the **replace-ai** as endless stacking of ais is easier to avoid.

**config.yml**
::
    add-ai:
        1:
          type: 'follow-owner'
          min-distance: 3.0
          max-distance: 50.0
          speed: 1.5

**config.yml**
::
    remove-ai:
        1:
          type: 'follow-owner'
          min-distance: 3.0
          max-distance: 50.0
          speed: 1.5

**config.yml**
::
    replace-ai:
        1:
          type: 'wearing'
        2:
          type: 'feeding'
          item-id: 391
          item-damage: 0
          click-particle:
            name: 'heart'
            speed: 0.1
            amount: 20
            offx: 1.0
            offy: 1.0
            offz: 1.0
          click-sound:
            name: 'EAT'
            volume: 5.0
            pitch: 1.0

The syntax is simple and multiple ais can be applied by using the incrementing number.
Each available ai is explained in the ai section of customizing.