Taking a look at the skin
=========================

Skins are easy to understand as you can actually see a difference immediately ingame.
Make sure you have understood the `introduction <petblocks.html>`_ to this topic.

The skins of a pet currently consist of 4 properties where only 1 is required.

================================  ========= =======================================================================
Skin                              Required  Samples
================================  ========= =======================================================================
id                                yes       1, 2, 397
damage                            no        0, 1, 2
skin                              no        Notch, eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc4NzhiYmYxYjA5ZjdmMjFhZjBiNDA2ZWY3MzEyZWUyMjViOGNjMTAyY2QwOWVlZmYyNDAyNDkzYzUwMzQ0MiJ9fX0=,  http://textures.minecraft.net/texture/f31f9ccc6b3e32ecf13b8a11ac29cd33d18c95fc73db8a66c5d657ccb8be70
unbreakable                       no        yes, no
================================  ========= =======================================================================

The pet you can see on your screen uses the skin configured at the default pet specified in the config.yml.

**config.yml**
::
    ############################

    # Default Pet settings

    # The pets of this plugin are very, very customizeable.
    # Please take a look into the official documentation mentioned at the top of this file
    # in order to configure your pet correctly.

    # These are the settings players are starting with when they get their first pet. However,
    # you can add all of these settings to the GUI in order to change them per skin or particle or something else.

    # You can find a list of all available ais on the documentation.

    ############################

    pet:
      enabled: false
      name: "<player>'s Pet"
      sound-enabled: true
      particle-enabled: true
      skin:
        id: 2


Let's change this skin to a tired Pufferfish which I have got from the bottom of `this page. <https://minecraft-heads.com/custom-heads/animals/26341-pufferfish-tired/>`__

**value**
::
 eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc4NzhiYmYxYjA5ZjdmMjFhZjBiNDA2ZWY3MzEyZWUyMjViOGNjMTAyY2QwOWVlZmYyNDAyNDkzYzUwMzQ0MiJ9fX0=


**config.yml**
::
    ############################

    # Default Pet settings

    # The pets of this plugin are very, very customizeable.
    # Please take a look into the official documentation mentioned at the top of this file
    # in order to configure your pet correctly.

    # These are the settings players are starting with when they get their first pet. However,
    # you can add all of these settings to the GUI in order to change them per skin or particle or something else.

    # You can find a list of all available ais on the documentation.

    ############################

    pet:
      enabled: false
      name: "<player>'s Pet"
      sound-enabled: true
      particle-enabled: true
      skin:
        id: 397
        damage: 3
        skin: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzc4NzhiYmYxYjA5ZjdmMjFhZjBiNDA2ZWY3MzEyZWUyMjViOGNjMTAyY2QwOWVlZmYyNDAyNDkzYzUwMzQ0MiJ9fX0='

Unfortunately, you have already got your pet and it **does not get overwritten** when the config.yml changes. All of your
**pet properties** are stored in the **PetBlocks.db** file. This is not a problem at all as we can reset the pet of a player easily.

1. Reload the config changes into the plugin via **/petblockreload**
2. Reset your pet to the default pet via **/petblocks reset**

Now the pet should look like this if everything worked fine. You can even see the changes in the debug menu.

.. image:: ../_static/images/pet-changed-puff.png

.. note::
 Player heads always have the **id: 397**, **damage: 3** and a **skin url**, **skin name** or **base64 skin url**.

.. note::
 Decide on the **default pet skin** for your server and configure it in the config.yml. It will be later discussed in the Customizing section
 how skins can be changed via the gui or commands.