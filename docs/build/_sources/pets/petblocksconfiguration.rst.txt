Taking a look at the user settings
==================================

The smallest part of PetBlocks are the user settings.
Make sure you have understood the `introduction <petblocks.html>`_ to this topic.

These are some of the available user settings in PetBlocks. All user settings
are listed up in the Customizing section.

**config.yml**
::
    pet:
      enabled: false
      name: "<player>'s Pet"
      sound-enabled: true
      particle-enabled: true

User settings are just values which are too basic to be ais and should be able to be changed by the user.
The **enabled:** tag for example, states if the pet of a player is enabled or not.

Let's change the enabled tag in the **config.yml**. This means if a player joins for the first time, the pet
is already enabled and will spawn in front of him.

**config.yml**
::
    pet:
      enabled: true
      name: "<player>'s Pet"
      sound-enabled: true
      particle-enabled: true

Unfortunately, you have already got your pet and it **does not get overwritten** when the config.yml changes. All of your
**pet properties** are stored in the **PetBlocks.db** file. This is not a problem at all as we can reset the pet of a player easily.

1. Reload the config changes into the plugin via **/petblockreload**
2. Reset your pet to the default pet via **/petblocks reset**
3. Rejoin your server

You can see that your pet instantly spawns instead of having to enable it first after resetting.

.. image:: ../_static/images/pet-debug-walking.png