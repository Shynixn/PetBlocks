Commandslist
============

.. note:: You can find the permissions for the commands at the `permission list <../gettingstarted/permissions.html#permissionlist>`__.

/petblock
~~~~~~~~~~

This user command opens the PetBlocks GUI.

* Players (require permissions) ✔
* Server console ✘
* Command blocks ✘

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