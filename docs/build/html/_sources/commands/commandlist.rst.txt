Commandslist
============

.. note:: You can find the permissions for the commands at the `permission list <../gettingstarted/permissions.html#permissionlist>`__.

.. toctree::
  commandlist

User Commands
"""""""""""""

/petblock
~~~~~~~~~

This user command opens the PetBlocks GUI.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

/petblock [gui-path]
~~~~~~~~~

This user command opens the PetBlocks GUI at the given gui path.
Opening gui paths directly requires the corresponding permission to this path.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

**Sample list:**

Opens the gui storage page (Requires the permission **petblocks.command.use.gui.storage**)
::
  /petblock gui.storage

Opens the gui main page (Requires the permission **petblocks.command.use.gui.main**)
::
  /petblocks gui.main

/petblock call
~~~~~~~~~~~~~~

This user command teleports the user's own pet to the user. Also spawns the pet if it has not spawned yet.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

/petblock toggle
~~~~~~~~~~~~~~~~

This user command spawns or despawns the user's pet.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

/petblock rename <name>
~~~~~~~~~~~~~~~~~~~~~~~

This user command allows setting the name of the user's own pet.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

**Sample list:**

Set your pet name to the name MyPet.
::
  /petblock rename MyPet

Set your pet name to the name My Super Cool Pet.
::
  /petblock rename My Super Cool Pet

Set your pet name to the name My Cool Pet with colors.
::
  /petblock rename &eMy &aCool &cPet

/petblock skin <skin>
~~~~~~~~~~~~~~~~~~~~~

This user command allows setting the skin of the user's own pet to any player or url.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

**Sample list:**

Set your pet skin to the player skin Shynixn:
::
  /petblock skin Shynixn

Set your pet skin to the skin Black Cat by a base64 encoded url
::
  /petblock skin eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWU0ZmNiOWNjYWU0ZjA2MzljNjg5ZTY1NjViNTIwNWQ3YWJhY2EyMTYwY2ZmNmIwYTVlOTA2ZDhiMTQ4MTkzMyJ9fX0=

Set your pet skin to the skin Black Cat by a full texture url
::
  /petblock skin http://textures.minecraft.net/texture/9e4fcb9ccae4f0639c689e6565b5205d7abaca2160cff6b0a5e906d8b1481933

Set your pet skin to the skin Black Cat by a shortened texture url
::
  /petblock skin textures.minecraft.net/texture/9e4fcb9ccae4f0639c689e6565b5205d7abaca2160cff6b0a5e906d8b1481933

Admin Commands
""""""""""""""

.. note:: All admin commands can be executed via the server console.

/petblocks [page]
~~~~~~~~~~~~~~~~~

This admin command opens the command list for administrators.

The optional [page] parameter specifies the target command list page.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

Opens page 1
::
  /petblocks

Opens page 2
::
  /petblocks 2

/petblocks enable [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~

Respawns the pet of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Enable your pet
::
  /petblocks enable

Enable the pet of player Mario
::
  /petblocks enable Mario

/petblocks disable [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~~

Removes the pet of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Disable your pet
::
  /petblocks disable

Disable the pet of player Mario
::
  /petblocks disable Mario

/petblocks toggle [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~

Enables or disables the pet of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Toggles your pet
::
  /petblocks toggle

Toggles the pet of player Mario
::
  /petblocks disable Mario

/petblocks ai <path> [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Changes the pet ai from the given player to the specified ais at the given config.yml path.

The <path> parameter specifies the section in your config.yml where the item is located. Please
take a look at the example below.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

.. warning:: The same ais can be added multiple times per default.

**Sample list:**

Applies the pet ai from the default pet configuration in the config.yml.
::
  /petblocks ai pet

Apply a custom ai to a pet.

1. Include the follow ai section anywhere in your config.yml.

**config.yml**
::
    mycustomais:
     fastfollowai:
       add-ai:
        1:
          type: 'follow-owner'
          min-distance: 3.0
          max-distance: 10.0
          speed: 5.0
       remove-ai:
        1:
          type: 'follow-owner'

.. note:: Execute the **/petblockreload** command after changing the config.yml.

2. Execute the follow command
::
  /petblocks ai mycustomais.fastfollowai

/petblocks skin <path> [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Sets the pet skin from the given player to the specified skin at the given config.yml path.

The <path> parameter specifies the section in your config.yml where the item is located. Please
take a look at the example below.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Applies the pet skin from the default pet configuration in the config.yml.
::
  /petblocks skin pet.skin

Apply a custom skin to a pet.

1. Include the follow skin section anywhere in your config.yml.

**config.yml**
::
  mycustomskins:
   marioskin:
    id: 397
    damage: 3
    skin: 'http://textures.minecraft.net/texture/a0c2549a893726988f3428bef799875ba871688ae64eb0cfdc43f7d6e24c6c'

.. note:: Execute the **/petblockreload** command after changing the config.yml.

2. Execute the follow command
::
  /petblocks skin mycustomskins.marioskin

/petblocks rename [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~

Renames the pet of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Renames your pet to Beast
::
  /petblocks rename Beast

Renames your pet to My awesome Pet
::
  /petblocks rename &aMy awesome Pet

/petblocks togglesound [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Toggles the pet sounds of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Toggles the sound
::
  /petblocks togglesound

Toggles the sound of player Shynixn
::
  /petblocks togglesound Shynixn

/petblocks toggleparticle [player]
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Toggles the pet particles of the given player.

The optional [player] parameter specifies the target player otherwise the player executing the command gets used.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Toggles the particle
::
  /petblocks toggleparticle

Toggles the particle of player Shynixn
::
  /petblocks toggleparticle  Shynixn

/petblocks reset [player]
~~~~~~~~~~~~~~~~~~~~~~~~~

Resets the pet settings of the given player to the default pet settings in the config.yml.

* Players (require permissions) ✔
* Server console ✔
* Command blocks ✔

**Sample list:**

Resets the pet
::
  /petblocks reset

Resets the pet of player Shynixn
::
  /petblocks reset Shynixn

/petblocks debug [player]
~~~~~~~~~~~~~~~~~~~~~~~~~

Displays the debug menu of the pet of the specified player to the player executing this command.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

**Sample list:**

Displays the debug menu of your own pet
::
  /petblocks debug

Displays the debug menu of Shynixn to yourself
::
  /petblocks debug Shynixn

/petblocks killnext
~~~~~~~~~~~~~~~~~~~

Kills the nearest entity. Does not kill other players.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

**Sample list:**

Kills the nearest entity in the current chunk
::
  /petblocks killnext