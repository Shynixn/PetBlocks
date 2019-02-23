Taking a look at the ais
========================

Ais are the most complex and powerful parts of PetBlocks to understand.
Make sure you have understood the `introduction <petblocks.html>`_ to this topic.

There are many different ais in PetBlocks and they are listed up in the Customizing section. This page however
analyzes a single ai and how it effects a pet.

.. note::
 Ais of a pet in PetBlocks handle **all interactions** of the pet with the environment and the player.

Each ai has got a unique identifier which is for example **type: 'hopping'**.

Let's go through some examples before we do a change to the config.yml.

.. note::
 When a pet has got the ai **type: 'hopping'**, the pet will be hopping around. If it does not have this
 ai or any other movement ai, the pet will simply be **standing still** at its spawnpoint.

.. note::
 When a pet has got the ai **type: 'float-in-water'**, the pet will be floating in water. If it does not have
 this ai, the pet will simply **sink to the ground** in water.

Some ais (not all) can be **added more than once** to a pet which allows customizing the pet even more.

.. note::
 When a pet has got the ai **type: 'ambient-sound'** with 'CHICKEN_IDLE' sound and another ai **type: 'ambient-sound'**
 with 'PIG_IDLE' the ai will play both sounds.

Let's take a look at the first default ai tag in PetBlocks.

**config.yml**
::
   pet:
      enabled: false
      name: "<player>'s Pet"
      add-ai:
        1:
          type: 'hopping'
          climbing-height: 1.0
          speed: 1.0
          offset-y: -1.0
          sound:
            name: 'CHICKEN_WALK'
            volume: 1.0
            itch: 1.0
          particle:
            name: 'reddust'
            speed: 0.01
            amount: 20
            offx: 0
            offy: 0
            offz: 255

The first thing you can notice that the config tag is called **add-ai**. This means the ais are applied to a pet when this configuration section
gets triggered. There are also the tags **remove-ai** and **replace-ai** which are used in the Customizing Section, they do not make much sense by
configuring the default pet.

Let's change the ai type from hopping to walking. Luckily, these ais have the same properties so we do not have to change
anything else.

.. note::
 It is a rare case that different ai types share the same properties.

**config.yml**
::
   pet:
      enabled: false
      name: "<player>'s Pet"
      add-ai:
        1:
          type: 'walking'
          climbing-height: 1.0
          speed: 1.0
          offset-y: -1.0
          sound:
            name: 'CHICKEN_WALK'
            volume: 1.0
            itch: 1.0
          particle:
            name: 'reddust'
            speed: 0.01
            amount: 20
            offx: 0
            offy: 0
            offz: 255



Unfortunately, you have already got your pet and it **does not get overwritten** when the config.yml changes. All of your
**pet properties** are stored in the **PetBlocks.db** file. This is not a problem at all as we can reset the pet of a player easily.

1. Reload the config changes into the plugin via **/petblockreload**
2. Reset your pet to the default pet via **/petblocks reset**

.. note::
 It is highly recommend to use the **/petblocks debug** command to view the ais of your pet.

Now the pet should be walking after you instead of hopping.

.. image:: ../_static/images/pet-debug-walking.png