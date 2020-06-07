Icon and Skins
==============

Items in the GUI get rendered by the specified icon and pets by the skin properties.

Taking a look at the GUI icon
~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

Take a look at the **icon** tag. This tag contains all available options to describe the item in the GUI.

**config.yml**
::
    gui:
      main:
        wardrobe:
          row: 3
          col: 2
          script: 'open-page gui.wardrobe'
          icon:
            id: 397
            damage: 3
            skin: 'textures.minecraft.net/texture/55507d6517eff952dd38fa8bc551dd6d6a7a5e4ea134519b44650ac1ffa59c3'
            name: '&6&lPet Customization <permission>'
            lore:
            - '&7Change the appearance of your pet. <permission>'


* Id 397 is the id of a skull
* Damage 3 is the damage for a player skull
* Skin is the skin for the player skull
* Name is the displayname of the item.
* Lore can accept multiple lines for the lore of the item.

.. note::
 The **skin** tag in the config.yml accepts the following player head formats:

    * Name of the player
      ::
        Shynixn
    * The texture url with http prefix
      ::
        http://textures.minecraft.net/texture/9e134e6dd838ae71abdbf1350f367d51d4239bf046fd6e525165979fcf22e812
    * The texture url without http prefix
      ::
        textures.minecraft.net/texture/9e134e6dd838ae71abdbf1350f367d51d4239bf046fd6e525165979fcf22e812
    * The base64 encoded texture url
      ::
        eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWUxMzRlNmRkODM4YWU3MWFiZGJmMTM1MGYzNjdkNTFkNDIzOWJmMDQ2ZmQ2ZTUyNTE2NTk3OWZjZjIyZTgxMiJ9fX0=


.. note::
 Each skin you upload to your **personal minecraft account** generates a new texture url which you can use. Old texture urls are **not overwritten**
 ,so you can upload as many skins as you like to the minecraft database. In order to get the url of your current player skin, you can use the
 **Custom heads generator** from  `Minecraft-Heads.com <https://minecraft-heads.com/custom/heads-generator>`__

Changing the wardrobe skin
~~~~~~~~~~~~~~~~~~~~~~~~~~

Let's try to change the skin of the wardrobe to barrel.

1. Let's go to https://minecraft-heads.com/custom-heads/decoration/26322-barrel-open. If you want to know more about the connection of PetBlocks to minecraft-heads.com please take a look at `this page. <minecraftheads.html>`__

2. Scroll down to the bottom of this page.

3. Copy the following value.

.. image:: ../_static/images/minecraftheadscomvalue.png

If the site is not available, here is the encoded texture url:

**url**
::
 eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM0OGY3M2U5ZDIyMjQ4ZDg5YjJlOWYyNjE1Zjk4MGNjNjA4MjdlZDNiNmQzOTVlNTNiNTdhODJkNGVhNWZlIn19fQ==

4. Put it into the config.yml.

**config.yml**
::
    gui:
      main:
        wardrobe:
          row: 3
          col: 2
          script: 'open-page gui.wardrobe'
          icon:
            id: 397
            damage: 3
            skin: 'eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmM0OGY3M2U5ZDIyMjQ4ZDg5YjJlOWYyNjE1Zjk4MGNjNjA4MjdlZDNiNmQzOTVlNTNiNTdhODJkNGVhNWZlIn19fQ=='
            name: '&6&lPet Customization <permission>'
            lore:
            - '&7Change the appearance of your pet. <permission>'

5. Execute the **/petblockreload** command and view it ingame.

.. image:: ../_static/images/gui-barrel.png